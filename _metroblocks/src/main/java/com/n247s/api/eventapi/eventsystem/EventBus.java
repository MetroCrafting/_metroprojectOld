package com.n247s.api.eventapi.eventsystem;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.Logger;

import com.n247s.api.eventapi.EventApi;

public class EventBus {

    private static final Logger log = EventApi.logger;
    protected HashMap<Class<? extends EventType>, ? super CallHandler> EventList = new HashMap<Class<? extends EventType>, CallHandler>();

    /**
     * Be very careful when creating your own EventBus! Only create one when you really need to, since this is the most
     * sensitive part of the CustomEventSystem, messing up the system is rather easy achievable.
     */
    public EventBus() {}

    /**
     * @param eventType
     * @return - true if EventType is Canceled.
     */
    public boolean raiseEvent(EventType eventType) {
        try {
            Class eventClass = eventType.getClass();
            if (!this.EventList.containsKey(eventClass))
                this.EventList.put(eventClass, new EventApiCallHandler(eventClass));
            return ((CallHandler) this.EventList.get(eventClass)).CallInstances(eventType);
        } catch (Exception e) {
            log.catching(e);
        }
        return false;
    }

    /**
     * used to bind a custom CallHandler to a EventType.
     * 
     * @param eventType
     * @param callHandler
     */
    public void bindCallHandler(CallHandler callHandler) {
        if (this.EventList.containsKey(callHandler.eventType)) this.EventList.remove(callHandler.eventType);
        this.EventList.put(callHandler.eventType, callHandler);
    }

    /**
     * With this method you can add an EventListner(Class object/Class instance) which CustomEventSubscribed methods
     * should be Called on eventRaise.
     * 
     * @param listener a Class object or instance that contains CustomEventSubscribe methods.
     * @throws IllegalArgumentException - If a CustomEventSubscribed Method contains more than one parameter, or if the
     *                                  parameter of that method is not an instance of EventType.class.
     */
    public void RegisterEventListener(Object listener) {
        Class clazz;
        if (!(listener instanceof Class)) clazz = listener.getClass();
        else clazz = (Class) listener;

        Method[] methods = clazz.getMethods();

        for (int i = 0; i < methods.length; i++) {
            Method currentMethod = methods[i];
            if (currentMethod.isAnnotationPresent(CustomEventSubscribe.class)) {
                if (currentMethod.getParameterTypes().length > 1) log.catching(
                        new IllegalArgumentException(
                                "An CustomEventSubScribed Method Can't have more than one Parameter!"));
                if (!(EventType.class.isAssignableFrom(currentMethod.getParameterTypes()[0])))
                    log.catching(new IllegalArgumentException("The Parameter isn't an EventType!"));

                CustomEventSubscribe annotation = currentMethod.getAnnotation(CustomEventSubscribe.class);
                Class<? extends EventType> methodEventType = (Class<? extends EventType>) currentMethod
                        .getParameterTypes()[0];

                getCallHandlerFromEventType(methodEventType)
                        .RegisterEventListener(annotation.eventPriority(), listener, currentMethod);
            }
        }
    }

    public void RegisterEventListners(List<Object> listenersList) {
        for (int i = 0; i < listenersList.size(); i++) {
            RegisterEventListener(listenersList.get(i));
        }
    }

    /**
     * With this method you can remove an EventListner(Class object/Class instance).
     * 
     * @param Listener
     * @throws NullPointerException - if the EventListner isn't registered.
     */
    public void removeEventListener(Object Listener) {
        Class clazz;
        if (!(Listener instanceof Class)) clazz = Listener.getClass();
        else clazz = (Class) Listener;

        Method[] methodArray = clazz.getMethods();

        for (int i = 0; i < methodArray.length; i++) {
            Method currentMethod = methodArray[i];

            if (currentMethod.isAnnotationPresent(CustomEventSubscribe.class)) {
                if (currentMethod.getParameterTypes().length > 1) log.catching(
                        new IllegalArgumentException(
                                "An CustomEventSubScribed Method Can't have more than one Parameter!"));
                if (Listener instanceof Class && !Modifier.isStatic(currentMethod.getModifiers())) log.catching(
                        new IllegalArgumentException(
                                "An CustomEventSubScribed Method Can't be non-static if you register an Class Object!"));
                if (!(EventType.class.isAssignableFrom(currentMethod.getParameterTypes()[0]))) log.catching(
                        new IllegalArgumentException(
                                "The Parameter of a CustomEventSubscribed method isn't an EventType!"));

                CustomEventSubscribe annotation = currentMethod.getAnnotation(CustomEventSubscribe.class);
                Class<? extends EventType> methodEventType = (Class<? extends EventType>) currentMethod
                        .getParameterTypes()[0];

                getCallHandlerFromEventType(methodEventType).removeListener(annotation.eventPriority(), Listener);
            }
        }
    }

    public void removeEventListeners(List<Object> listenersList) {
        for (int i = 0; i < listenersList.size(); i++) {
            removeEventListener(listenersList.get(i));
        }
    }

    /**
     * @param eventTypeClass
     * @return The instance of the CallHandler of this specific EventType. Note that if no CallHanlder is assigned, a
     *         default instance is returned!
     */
    public CallHandler getCallHandlerFromEventType(Class<? extends EventType> eventTypeClass) {
        if (!this.EventList.containsKey(eventTypeClass))
            this.EventList.put(eventTypeClass, new EventApiCallHandler(eventTypeClass));
        return (CallHandler) this.EventList.get(eventTypeClass);
    }
}
