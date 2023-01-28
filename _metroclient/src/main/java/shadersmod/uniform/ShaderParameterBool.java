package shadersmod.uniform;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.optifine.entity.model.anim.ExpressionType;
import net.optifine.entity.model.anim.IExpressionBool;

public enum ShaderParameterBool implements IExpressionBool {
      IS_ALIVE("is_alive"),
      IS_BURNING("is_burning"),
      IS_CHILD("is_child"),
      IS_GLOWING("is_glowing"),
      IS_HURT("is_hurt"),
      IS_IN_LAVA("is_in_lava"),
      IS_IN_WATER("is_in_water"),
      IS_INVISIBLE("is_invisible"),
      IS_ON_GROUND("is_on_ground"),
      IS_RIDDEN("is_ridden"),
      IS_RIDING("is_riding"),
      IS_SNEAKING("is_sneaking"),
      IS_SPRINTING("is_sprinting"),
      IS_WET("is_wet");

      private String name;
      private RenderManager renderManager;
      private static final ShaderParameterBool[] VALUES = values();

      private ShaderParameterBool(String name) {
            this.name = name;
            this.renderManager = RenderManager.instance;
      }

      public String getName() {
            return this.name;
      }

      public ExpressionType getExpressionType() {
            return ExpressionType.BOOL;
      }

      public boolean eval() {
            Entity entityGeneral = Minecraft.getMinecraft().renderViewEntity;
            if (entityGeneral instanceof EntityLivingBase) {
                  EntityLivingBase entity = (EntityLivingBase)entityGeneral;
                  switch(this) {
                  case IS_ALIVE:
                        return entity.isEntityAlive();
                  case IS_BURNING:
                        return entity.isBurning();
                  case IS_CHILD:
                        return entity.isChild();
                  case IS_GLOWING:
                        return false;
                  case IS_HURT:
                        return entity.hurtTime > 0;
                  case IS_IN_LAVA:
                        return entity.handleLavaMovement();
                  case IS_IN_WATER:
                        return entity.isInWater();
                  case IS_INVISIBLE:
                        return entity.isInvisible();
                  case IS_ON_GROUND:
                        return entity.onGround;
                  case IS_RIDDEN:
                        return entity.riddenByEntity != null;
                  case IS_RIDING:
                        return entity.isRiding();
                  case IS_SNEAKING:
                        return entity.isSneaking();
                  case IS_SPRINTING:
                        return entity.isSprinting();
                  case IS_WET:
                        return entity.isWet();
                  }
            }

            return false;
      }

      public static ShaderParameterBool parse(String str) {
            if (str == null) {
                  return null;
            } else {
                  for(int i = 0; i < VALUES.length; ++i) {
                        ShaderParameterBool type = VALUES[i];
                        if (type.getName().equals(str)) {
                              return type;
                        }
                  }

                  return null;
            }
      }
}
