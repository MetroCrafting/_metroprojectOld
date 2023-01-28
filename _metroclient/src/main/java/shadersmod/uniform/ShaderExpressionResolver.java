package shadersmod.uniform;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.biome.BiomeGenBase;
import net.optifine.entity.model.anim.ConstantFloat;
import net.optifine.entity.model.anim.IExpression;
import net.optifine.entity.model.anim.IExpressionResolver;
import shadersmod.common.SMCLog;

public class ShaderExpressionResolver implements IExpressionResolver {
      private Map mapExpressions = new HashMap();

      public ShaderExpressionResolver(Map map) {
            this.registerExpressions();
            Set keys = map.keySet();
            Iterator it = keys.iterator();

            while(it.hasNext()) {
                  String name = (String)it.next();
                  IExpression expr = (IExpression)map.get(name);
                  this.registerExpression(name, expr);
            }

      }

      private void registerExpressions() {
            ShaderParameterFloat[] spfs = ShaderParameterFloat.values();

            for(int i = 0; i < spfs.length; ++i) {
                  ShaderParameterFloat spf = spfs[i];
                  this.mapExpressions.put(spf.getName(), spf);
            }

            ShaderParameterBool[] spbs = ShaderParameterBool.values();

            for(int i = 0; i < spbs.length; ++i) {
                  ShaderParameterBool spb = spbs[i];
                  this.mapExpressions.put(spb.getName(), spb);
            }

            BiomeGenBase[] biomeList = BiomeGenBase.getBiomeGenArray();

            for(int i = 0; i < biomeList.length; ++i) {
                  BiomeGenBase biome = biomeList[i];
                  if (biome != null) {
                        String name = biome.biomeName.trim();
                        name = "BIOME_" + name.toUpperCase().replace(' ', '_');
                        int id = biome.biomeID;
                        IExpression expr = new ConstantFloat((float)id);
                        this.registerExpression(name, expr);
                  }
            }

      }

      public boolean registerExpression(String name, IExpression expr) {
            if (this.mapExpressions.containsKey(name)) {
                  SMCLog.warning("Expression already defined: " + name);
                  return false;
            } else {
                  this.mapExpressions.put(name, expr);
                  return true;
            }
      }

      public IExpression getExpression(String name) {
            return (IExpression)this.mapExpressions.get(name);
      }

      public boolean hasExpression(String name) {
            return this.mapExpressions.containsKey(name);
      }
}
