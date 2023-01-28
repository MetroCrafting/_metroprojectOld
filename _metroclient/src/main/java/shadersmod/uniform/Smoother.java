package shadersmod.uniform;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.src.SmoothFloat;

public class Smoother {
      private static Map mapSmoothValues = new HashMap();

      public static float getSmoothValue(int id, float value, float timeFadeUpSec, float timeFadeDownSec) {
            synchronized(mapSmoothValues) {
                  Integer key = id;
                  SmoothFloat sf = (SmoothFloat)mapSmoothValues.get(key);
                  if (sf == null) {
                        sf = new SmoothFloat(value, timeFadeUpSec, timeFadeDownSec);
                        mapSmoothValues.put(key, sf);
                  }

                  float valueSmooth = sf.getSmoothValue(value);
                  return valueSmooth;
            }
      }

      public static void reset() {
            synchronized(mapSmoothValues) {
                  mapSmoothValues.clear();
            }
      }
}
