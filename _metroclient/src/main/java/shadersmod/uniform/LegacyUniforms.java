package shadersmod.uniform;

import java.util.HashMap;
import java.util.Map;

public class LegacyUniforms {
      private static Map map = new HashMap();
      private static Map mapKeysX = new HashMap();
      private static Map mapKeysY = new HashMap();
      private static Map mapKeysZ = new HashMap();
      private static Map mapKeysR = new HashMap();
      private static Map mapKeysG = new HashMap();
      private static Map mapKeysB = new HashMap();

      public static Number getNumber(String name) {
            return (Number)map.get(name);
      }

      public static void setFloat(String name, float val) {
            map.put(name, val);
      }

      public static void setInt(String name, int val) {
            map.put(name, val);
      }

      public static void setIntXy(String name, int x, int y) {
            setInt(getCompoundKey(name, "x", mapKeysX), x);
            setInt(getCompoundKey(name, "y", mapKeysY), y);
      }

      public static void setFloatXyz(String name, float x, float y, float z) {
            setFloat(getCompoundKey(name, "x", mapKeysX), x);
            setFloat(getCompoundKey(name, "y", mapKeysY), y);
            setFloat(getCompoundKey(name, "z", mapKeysZ), z);
      }

      public static void setFloatRgb(String name, float x, float y, float z) {
            setFloat(getCompoundKey(name, "r", mapKeysR), x);
            setFloat(getCompoundKey(name, "g", mapKeysG), y);
            setFloat(getCompoundKey(name, "b", mapKeysB), z);
      }

      private static String getCompoundKey(String name, String suffix, Map mapKeys) {
            String key = (String)mapKeys.get(name);
            if (key != null) {
                  return key;
            } else {
                  key = name + "." + suffix;
                  mapKeys.put(name, key);
                  return key;
            }
      }

      public static void reset() {
            map.clear();
      }
}
