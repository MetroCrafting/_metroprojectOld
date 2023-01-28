package shadersmod.uniform;

import net.optifine.entity.model.anim.ExpressionType;
import net.optifine.entity.model.anim.IExpression;
import net.optifine.entity.model.anim.IExpressionBool;
import net.optifine.entity.model.anim.IExpressionFloat;

public enum UniformType {
      BOOL,
      INT,
      FLOAT;

      public ShaderUniformBase makeShaderUniform(String name) {
            switch(this) {
            case BOOL:
                  return new ShaderUniformInt(name);
            case INT:
                  return new ShaderUniformInt(name);
            case FLOAT:
                  return new ShaderUniformFloat(name);
            default:
                  throw new RuntimeException("Unknown uniform type: " + this);
            }
      }

      public void updateUniform(IExpression expression, ShaderUniformBase uniform) {
            switch(this) {
            case BOOL:
                  this.updateUniformBool((IExpressionBool)expression, (ShaderUniformInt)uniform);
                  return;
            case INT:
                  this.updateUniformInt((IExpressionFloat)expression, (ShaderUniformInt)uniform);
                  return;
            case FLOAT:
                  this.updateUniformFloat((IExpressionFloat)expression, (ShaderUniformFloat)uniform);
                  return;
            default:
                  throw new RuntimeException("Unknown uniform type: " + this);
            }
      }

      private void updateUniformBool(IExpressionBool expression, ShaderUniformInt uniform) {
            boolean val = expression.eval();
            int valInt = val ? 1 : 0;
            uniform.setValue(valInt);
      }

      private void updateUniformInt(IExpressionFloat expression, ShaderUniformInt uniform) {
            int val = (int)expression.eval();
            uniform.setValue(val);
      }

      private void updateUniformFloat(IExpressionFloat expression, ShaderUniformFloat uniform) {
            float val = expression.eval();
            uniform.setValue(val);
      }

      public boolean matchesExpressionType(ExpressionType expressionType) {
            switch(this) {
            case BOOL:
                  return expressionType == ExpressionType.BOOL;
            case INT:
                  return expressionType == ExpressionType.FLOAT;
            case FLOAT:
                  return expressionType == ExpressionType.FLOAT;
            default:
                  throw new RuntimeException("Unknown uniform type: " + this);
            }
      }

      public static UniformType parse(String type) {
            UniformType[] values = values();

            for(int i = 0; i < values.length; ++i) {
                  UniformType uniformType = values[i];
                  if (uniformType.name().toLowerCase().equals(type)) {
                        return uniformType;
                  }
            }

            return null;
      }
}
