package net.minecraft.src;

import java.util.HashMap;
import java.util.Map;

public class TimedEvent {
      private static Map mapEventTimes = new HashMap();

      public static boolean isActive(String name, long timeIntervalMs) {
            synchronized(mapEventTimes) {
                  long timeNowMs = System.currentTimeMillis();
                  Long timeLastMsObj = (Long)mapEventTimes.get(name);
                  if (timeLastMsObj == null) {
                        timeLastMsObj = new Long(timeNowMs);
                        mapEventTimes.put(name, timeLastMsObj);
                  }

                  long timeLastMs = timeLastMsObj;
                  if (timeNowMs < timeLastMs + timeIntervalMs) {
                        return false;
                  } else {
                        mapEventTimes.put(name, new Long(timeNowMs));
                        return true;
                  }
            }
      }
}
