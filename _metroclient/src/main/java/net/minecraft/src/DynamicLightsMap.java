package net.minecraft.src;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicLightsMap {
      private Map map = new HashMap();
      private List list = new ArrayList();
      private boolean dirty = false;

      public DynamicLight put(int id, DynamicLight dynamicLight) {
            DynamicLight old = (DynamicLight)this.map.put(id, dynamicLight);
            this.setDirty();
            return old;
      }

      public DynamicLight get(int id) {
            return (DynamicLight)this.map.get(id);
      }

      public int size() {
            return this.map.size();
      }

      public DynamicLight remove(int id) {
            DynamicLight old = (DynamicLight)this.map.remove(id);
            if (old != null) {
                  this.setDirty();
            }

            return old;
      }

      public void clear() {
            this.map.clear();
            this.setDirty();
      }

      private void setDirty() {
            this.dirty = true;
      }

      public List valueList() {
            if (this.dirty) {
                  this.list.clear();
                  this.list.addAll(this.map.values());
                  this.dirty = false;
            }

            return this.list;
      }
}
