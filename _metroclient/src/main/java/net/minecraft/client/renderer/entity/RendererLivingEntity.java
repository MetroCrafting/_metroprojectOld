package net.minecraft.client.renderer.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.src.Config;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import shadersmod.client.Shaders;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public abstract class RendererLivingEntity extends Render
{
    private static final Logger logger = LogManager.getLogger();
    private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");
    protected ModelBase mainModel;
    /** The model to be used during the render passes. */
    protected ModelBase renderPassModel;
    private static final String __OBFID = "CL_00001012";

    public static float NAME_TAG_RANGE = 64.0f;
    public static float NAME_TAG_RANGE_SNEAK = 32.0f;

    public RendererLivingEntity(ModelBase p_i1261_1_, float p_i1261_2_)
    {
        this.mainModel = p_i1261_1_;
        this.shadowSize = p_i1261_2_;
    }

    /**
     * Sets the model to be used in the current render pass (the first render pass is done after the primary model is
     * rendered) Args: model
     */
    public void setRenderPassModel(ModelBase p_77042_1_)
    {
        this.renderPassModel = p_77042_1_;
    }

    /**
     * Returns a rotation angle that is inbetween two other rotation angles. par1 and par2 are the angles between which
     * to interpolate, par3 is probably a float between 0.0 and 1.0 that tells us where "between" the two angles we are.
     * Example: par1 = 30, par2 = 50, par3 = 0.5, then return = 40
     */
    private float interpolateRotation(float p_77034_1_, float p_77034_2_, float p_77034_3_)
    {
        float f3;

        for (f3 = p_77034_2_ - p_77034_1_; f3 < -180.0F; f3 += 360.0F)
        {
            ;
        }

        while (f3 >= 180.0F)
        {
            f3 -= 360.0F;
        }

        return p_77034_1_ + p_77034_3_ * f3;
    }

    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity) and this method has signature public void func_76986_a(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doesn't do that.
     */
    public void doRender(EntityLivingBase par1Entity, double par2, double par4, double par6, float par8, float par9) {
        if (MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Pre(par1Entity, this, par2, par6, par6))) 
        {
            return;
        }
              boolean isShaders = Config.isShaders();
              float var10;
              float var26;
              float var14;
              float var15;
              float var16;
              if (isShaders && Shaders.useEntityColor) {
                    if (par1Entity.hurtTime <= 0 && par1Entity.deathTime <= 0) {
                          var10 = par1Entity.getBrightness(par9);
                          int col = this.getColorMultiplier(par1Entity, var10, par9);
                          boolean flash = (col >> 24 & 255) > 0;
                          if (flash) {
                                var26 = (float)(col >> 24 & 255) / 255.0F;
                                var14 = (float)(col >> 16 & 255) / 255.0F;
                                var15 = (float)(col >> 8 & 255) / 255.0F;
                                var16 = (float)(col & 255) / 255.0F;
                                Shaders.setEntityColor(var14, var15, var16, 1.0F - var26);
                          }
                    } else {
                          Shaders.setEntityColor(1.0F, 0.0F, 0.0F, 0.3F);
                    }
              }

              GL11.glPushMatrix();
              GL11.glDisable(2884);
              this.mainModel.onGround = this.renderSwingProgress(par1Entity, par9);
              if (this.renderPassModel != null) {
                    this.renderPassModel.onGround = this.mainModel.onGround;
              }

              this.mainModel.isRiding = par1Entity.isRiding();
              if (this.renderPassModel != null) {
                    this.renderPassModel.isRiding = this.mainModel.isRiding;
              }

              this.mainModel.isChild = par1Entity.isChild();
              if (this.renderPassModel != null) {
                    this.renderPassModel.isChild = this.mainModel.isChild;
              }

              try {
                    var10 = this.interpolateRotation(par1Entity.prevRenderYawOffset, par1Entity.renderYawOffset, par9);
                    float var11 = this.interpolateRotation(par1Entity.prevRotationYawHead, par1Entity.rotationYawHead, par9);
                    float var13;
                    if (par1Entity.isRiding() && par1Entity.ridingEntity instanceof EntityLivingBase) {
                          EntityLivingBase var12 = (EntityLivingBase)par1Entity.ridingEntity;
                          var10 = this.interpolateRotation(var12.prevRenderYawOffset, var12.renderYawOffset, par9);
                          var13 = MathHelper.wrapAngleTo180_float(var11 - var10);
                          if (var13 < -85.0F) {
                                var13 = -85.0F;
                          }

                          if (var13 >= 85.0F) {
                                var13 = 85.0F;
                          }

                          var10 = var11 - var13;
                          if (var13 * var13 > 2500.0F) {
                                var10 += var13 * 0.2F;
                          }
                    }

                    var26 = par1Entity.prevRotationPitch + (par1Entity.rotationPitch - par1Entity.prevRotationPitch) * par9;
                    this.renderLivingAt(par1Entity, par2, par4, par6);
                    var13 = this.handleRotationFloat(par1Entity, par9);
                    this.rotateCorpse(par1Entity, var13, var10, par9);
                    var14 = 0.0625F;
                    GL11.glEnable(32826);
                    GL11.glScalef(-1.0F, -1.0F, 1.0F);
                    this.preRenderCallback(par1Entity, par9);
                    GL11.glTranslatef(0.0F, -24.0F * var14 - 0.0078125F, 0.0F);
                    var15 = par1Entity.prevLimbSwingAmount + (par1Entity.limbSwingAmount - par1Entity.prevLimbSwingAmount) * par9;
                    var16 = par1Entity.limbSwing - par1Entity.limbSwingAmount * (1.0F - par9);
                    if (par1Entity.isChild()) {
                          var16 *= 3.0F;
                    }

                    if (var15 > 1.0F) {
                          var15 = 1.0F;
                    }

                    GL11.glEnable(3008);
                    this.mainModel.setLivingAnimations(par1Entity, var16, var15, par9);
                    this.renderModel(par1Entity, var16, var15, var13, var11 - var10, var26, var14);

                    int var18;
                    float var19;
                    float var20;
                    float var22;
                    int var28;
                    for(int var17 = 0; var17 < 4; ++var17) {
                          var18 = this.shouldRenderPass(par1Entity, var17, par9);
                          if (var18 > 0) {
                                this.renderPassModel.setLivingAnimations(par1Entity, var16, var15, par9);
                                this.renderPassModel.render(par1Entity, var16, var15, var13, var11 - var10, var26, var14);
                                if ((var18 & 240) == 16) {
                                      this.func_82408_c(par1Entity, var17, par9);
                                      this.renderPassModel.render(par1Entity, var16, var15, var13, var11 - var10, var26, var14);
                                }

                                if ((var18 & 15) == 15) {
                                      var19 = (float)par1Entity.ticksExisted + par9;
                                      this.bindTexture(RES_ITEM_GLINT);
                                      GL11.glEnable(3042);
                                      var20 = 0.5F;
                                      GL11.glColor4f(var20, var20, var20, 1.0F);
                                      GL11.glDepthFunc(514);
                                      GL11.glDepthMask(false);

                                      for(var28 = 0; var28 < 2; ++var28) {
                                            GL11.glDisable(2896);
                                            var22 = 0.76F;
                                            GL11.glColor4f(0.5F * var22, 0.25F * var22, 0.8F * var22, 1.0F);
                                            GL11.glBlendFunc(768, 1);
                                            GL11.glMatrixMode(5890);
                                            GL11.glLoadIdentity();
                                            float var23 = var19 * (0.001F + (float)var28 * 0.003F) * 20.0F;
                                            float var24 = 0.33333334F;
                                            GL11.glScalef(var24, var24, var24);
                                            GL11.glRotatef(30.0F - (float)var28 * 60.0F, 0.0F, 0.0F, 1.0F);
                                            GL11.glTranslatef(0.0F, var23, 0.0F);
                                            GL11.glMatrixMode(5888);
                                            this.renderPassModel.render(par1Entity, var16, var15, var13, var11 - var10, var26, var14);
                                      }

                                      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                                      GL11.glMatrixMode(5890);
                                      GL11.glDepthMask(true);
                                      GL11.glLoadIdentity();
                                      GL11.glMatrixMode(5888);
                                      GL11.glEnable(2896);
                                      GL11.glDisable(3042);
                                      GL11.glDepthFunc(515);
                                }

                                GL11.glDisable(3042);
                                GL11.glEnable(3008);
                          }
                    }

                    GL11.glDepthMask(true);
                    if (isShaders && Shaders.useEntityColor) {
                          Shaders.setEntityColor(0.0F, 0.0F, 0.0F, 0.0F);
                    }

                    this.renderEquippedItems(par1Entity, par9);
                    if (!isShaders || !Shaders.useEntityColor) {
                          float var27 = par1Entity.getBrightness(par9);
                          var18 = this.getColorMultiplier(par1Entity, var27, par9);
                          OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
                          GL11.glDisable(3553);
                          OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
                          if (isShaders) {
                                Shaders.disableLightmap();
                          }

                          if ((var18 >> 24 & 255) > 0 || par1Entity.hurtTime > 0 || par1Entity.deathTime > 0) {
                                GL11.glDisable(3553);
                                GL11.glDisable(3008);
                                GL11.glEnable(3042);
                                GL11.glBlendFunc(770, 771);
                                GL11.glDepthFunc(514);
                                if (isShaders) {
                                      Shaders.beginLivingDamage();
                                }

                                if (par1Entity.hurtTime > 0 || par1Entity.deathTime > 0) {
                                      GL11.glColor4f(var27, 0.0F, 0.0F, 0.4F);
                                      this.mainModel.render(par1Entity, var16, var15, var13, var11 - var10, var26, var14);

                                      for(var28 = 0; var28 < 4; ++var28) {
                                            if (this.inheritRenderPass(par1Entity, var28, par9) >= 0) {
                                                  GL11.glColor4f(var27, 0.0F, 0.0F, 0.4F);
                                                  this.renderPassModel.render(par1Entity, var16, var15, var13, var11 - var10, var26, var14);
                                            }
                                      }
                                }

                                if ((var18 >> 24 & 255) > 0) {
                                      var19 = (float)(var18 >> 16 & 255) / 255.0F;
                                      var20 = (float)(var18 >> 8 & 255) / 255.0F;
                                      float var29 = (float)(var18 & 255) / 255.0F;
                                      var22 = (float)(var18 >> 24 & 255) / 255.0F;
                                      GL11.glColor4f(var19, var20, var29, var22);
                                      this.mainModel.render(par1Entity, var16, var15, var13, var11 - var10, var26, var14);

                                      for(int var30 = 0; var30 < 4; ++var30) {
                                            if (this.inheritRenderPass(par1Entity, var30, par9) >= 0) {
                                                  GL11.glColor4f(var19, var20, var29, var22);
                                                  this.renderPassModel.render(par1Entity, var16, var15, var13, var11 - var10, var26, var14);
                                            }
                                      }
                                }

                                GL11.glDepthFunc(515);
                                if (isShaders) {
                                      Shaders.endLivingDamage();
                                }

                                GL11.glDisable(3042);
                                GL11.glEnable(3008);
                                GL11.glEnable(3553);
                          }
                    }

                    GL11.glDisable(32826);
              } catch (Exception exception) {
                    logger.error("Couldn't render entity", exception);
              }

              OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
              GL11.glEnable(3553);
              OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
              if (isShaders) {
                    Shaders.enableLightmap();
              }

              GL11.glEnable(2884);
              GL11.glPopMatrix();
              this.passSpecialRender(par1Entity, par2, par4, par6);
              MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Post(par1Entity, this, par2, par4, par6));
        }
  

    /**
     * Renders the model in RenderLiving
     */
    protected void renderModel(EntityLivingBase p_77036_1_, float p_77036_2_, float p_77036_3_, float p_77036_4_, float p_77036_5_, float p_77036_6_, float p_77036_7_)
    {
        this.bindEntityTexture(p_77036_1_);

        if (!p_77036_1_.isInvisible())
        {
            this.mainModel.render(p_77036_1_, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, p_77036_7_);
        }
        else if (!p_77036_1_.isInvisibleToPlayer(Minecraft.getMinecraft().thePlayer))
        {
            GL11.glPushMatrix();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.15F);
            GL11.glDepthMask(false);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.003921569F);
            this.mainModel.render(p_77036_1_, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, p_77036_7_);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
            GL11.glPopMatrix();
            GL11.glDepthMask(true);
        }
        else
        {
            this.mainModel.setRotationAngles(p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, p_77036_7_, p_77036_1_);
        }
    }

    /**
     * Sets a simple glTranslate on a LivingEntity.
     */
    protected void renderLivingAt(EntityLivingBase p_77039_1_, double p_77039_2_, double p_77039_4_, double p_77039_6_)
    {
        GL11.glTranslatef((float)p_77039_2_, (float)p_77039_4_, (float)p_77039_6_);
    }

    protected void rotateCorpse(EntityLivingBase p_77043_1_, float p_77043_2_, float p_77043_3_, float p_77043_4_)
    {
        GL11.glRotatef(180.0F - p_77043_3_, 0.0F, 1.0F, 0.0F);

        if (p_77043_1_.deathTime > 0)
        {
            float f3 = ((float)p_77043_1_.deathTime + p_77043_4_ - 1.0F) / 20.0F * 1.6F;
            f3 = MathHelper.sqrt_float(f3);

            if (f3 > 1.0F)
            {
                f3 = 1.0F;
            }

            GL11.glRotatef(f3 * this.getDeathMaxRotation(p_77043_1_), 0.0F, 0.0F, 1.0F);
        }
        else
        {
            String s = EnumChatFormatting.getTextWithoutFormattingCodes(p_77043_1_.getCommandSenderName());

            if ((s.equals("Dinnerbone") || s.equals("Grumm")) && (!(p_77043_1_ instanceof EntityPlayer) || !((EntityPlayer)p_77043_1_).getHideCape()))
            {
                GL11.glTranslatef(0.0F, p_77043_1_.height + 0.1F, 0.0F);
                GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
            }
        }
    }

    protected float renderSwingProgress(EntityLivingBase p_77040_1_, float p_77040_2_)
    {
        return p_77040_1_.getSwingProgress(p_77040_2_);
    }

    /**
     * Defines what float the third param in setRotationAngles of ModelBase is
     */
    protected float handleRotationFloat(EntityLivingBase p_77044_1_, float p_77044_2_)
    {
        return (float)p_77044_1_.ticksExisted + p_77044_2_;
    }

    protected void renderEquippedItems(EntityLivingBase p_77029_1_, float p_77029_2_) {}

    /**
     * renders arrows the Entity has been attacked with, attached to it
     */
    protected void renderArrowsStuckInEntity(EntityLivingBase p_85093_1_, float p_85093_2_)
    {
        int i = p_85093_1_.getArrowCountInEntity();

        if (i > 0)
        {
            EntityArrow entityarrow = new EntityArrow(p_85093_1_.worldObj, p_85093_1_.posX, p_85093_1_.posY, p_85093_1_.posZ);
            Random random = new Random((long)p_85093_1_.getEntityId());
            RenderHelper.disableStandardItemLighting();

            for (int j = 0; j < i; ++j)
            {
                GL11.glPushMatrix();
                ModelRenderer modelrenderer = this.mainModel.getRandomModelBox(random);
                ModelBox modelbox = (ModelBox)modelrenderer.cubeList.get(random.nextInt(modelrenderer.cubeList.size()));
                modelrenderer.postRender(0.0625F);
                float f1 = random.nextFloat();
                float f2 = random.nextFloat();
                float f3 = random.nextFloat();
                float f4 = (modelbox.posX1 + (modelbox.posX2 - modelbox.posX1) * f1) / 16.0F;
                float f5 = (modelbox.posY1 + (modelbox.posY2 - modelbox.posY1) * f2) / 16.0F;
                float f6 = (modelbox.posZ1 + (modelbox.posZ2 - modelbox.posZ1) * f3) / 16.0F;
                GL11.glTranslatef(f4, f5, f6);
                f1 = f1 * 2.0F - 1.0F;
                f2 = f2 * 2.0F - 1.0F;
                f3 = f3 * 2.0F - 1.0F;
                f1 *= -1.0F;
                f2 *= -1.0F;
                f3 *= -1.0F;
                float f7 = MathHelper.sqrt_float(f1 * f1 + f3 * f3);
                entityarrow.prevRotationYaw = entityarrow.rotationYaw = (float)(Math.atan2((double)f1, (double)f3) * 180.0D / Math.PI);
                entityarrow.prevRotationPitch = entityarrow.rotationPitch = (float)(Math.atan2((double)f2, (double)f7) * 180.0D / Math.PI);
                double d0 = 0.0D;
                double d1 = 0.0D;
                double d2 = 0.0D;
                float f8 = 0.0F;
                this.renderManager.renderEntityWithPosYaw(entityarrow, d0, d1, d2, f8, p_85093_2_);
                GL11.glPopMatrix();
            }

            RenderHelper.enableStandardItemLighting();
        }
    }

    protected int inheritRenderPass(EntityLivingBase p_77035_1_, int p_77035_2_, float p_77035_3_)
    {
        return this.shouldRenderPass(p_77035_1_, p_77035_2_, p_77035_3_);
    }

    /**
     * Queries whether should render the specified pass or not.
     */
    protected int shouldRenderPass(EntityLivingBase p_77032_1_, int p_77032_2_, float p_77032_3_)
    {
        return -1;
    }

    protected void func_82408_c(EntityLivingBase p_82408_1_, int p_82408_2_, float p_82408_3_) {}

    protected float getDeathMaxRotation(EntityLivingBase p_77037_1_)
    {
        return 90.0F;
    }

    /**
     * Returns an ARGB int color back. Args: entityLiving, lightBrightness, partialTickTime
     */
    protected int getColorMultiplier(EntityLivingBase p_77030_1_, float p_77030_2_, float p_77030_3_)
    {
        return 0;
    }

    /**
     * Allows the render to do any OpenGL state modifications necessary before the model is rendered. Args:
     * entityLiving, partialTickTime
     */
    protected void preRenderCallback(EntityLivingBase p_77041_1_, float p_77041_2_) {}

    /**
     * Passes the specialRender and renders it
     */
      protected void passSpecialRender(EntityLivingBase par1EntityLivingBase, double par2, double par4, double par6) {
        if (MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Specials.Pre(par1EntityLivingBase, this, par2, par4, par6))) {
        	return;
        }
                  GL11.glAlphaFunc(516, 0.1F);
                  if (this.func_110813_b(par1EntityLivingBase)) {
                        float var8 = 1.6F;
                        float var9 = 0.016666668F * var8;
                        double var10 = par1EntityLivingBase.getDistanceSqToEntity(super.renderManager.livingPlayer);
                        float var12 = par1EntityLivingBase.isSneaking() ? NAME_TAG_RANGE_SNEAK : NAME_TAG_RANGE;
                        if (var10 < (double)(var12 * var12)) {
                              String var13 = par1EntityLivingBase.func_145748_c_().getFormattedText();
                              if (par1EntityLivingBase.isSneaking()) {
                                    FontRenderer var14 = this.getFontRendererFromRenderManager();
                                    GL11.glPushMatrix();
                                    GL11.glTranslatef((float)par2 + 0.0F, (float)par4 + par1EntityLivingBase.height + 0.5F, (float)par6);
                                    GL11.glNormal3f(0.0F, 1.0F, 0.0F);
                                    GL11.glRotatef(-super.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
                                    GL11.glRotatef(super.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
                                    GL11.glScalef(-var9, -var9, var9);
                                    GL11.glDisable(2896);
                                    GL11.glTranslatef(0.0F, 0.25F / var9, 0.0F);
                                    GL11.glDepthMask(false);
                                    GL11.glEnable(3042);
                                    OpenGlHelper.glBlendFunc(770, 771, 1, 0);
                                    Tessellator var15 = Tessellator.instance;
                                    GL11.glDisable(3553);
                                    var15.startDrawingQuads();
                                    int var16 = var14.getStringWidth(var13) / 2;
                                    var15.setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.25F);
                                    var15.addVertex((double)(-var16 - 1), -1.0D, 0.0D);
                                    var15.addVertex((double)(-var16 - 1), 8.0D, 0.0D);
                                    var15.addVertex((double)(var16 + 1), 8.0D, 0.0D);
                                    var15.addVertex((double)(var16 + 1), -1.0D, 0.0D);
                                    var15.draw();
                                    GL11.glEnable(3553);
                                    GL11.glDepthMask(true);
                                    var14.drawString(var13, -var14.getStringWidth(var13) / 2, 0, 553648127);
                                    GL11.glEnable(2896);
                                    GL11.glDisable(3042);
                                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                                    GL11.glPopMatrix();
                              } else {
                                    this.func_96449_a(par1EntityLivingBase, par2, par4, par6, var13, var9, var10);
                              }
                        }
                  }
                  MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Specials.Post(par1EntityLivingBase, this, par2, par4, par6));
            }
		

    protected boolean func_110813_b(EntityLivingBase p_110813_1_)
    {
        return Minecraft.isGuiEnabled() && p_110813_1_ != this.renderManager.livingPlayer && !p_110813_1_.isInvisibleToPlayer(Minecraft.getMinecraft().thePlayer) && p_110813_1_.riddenByEntity == null;
    }

    protected void func_96449_a(EntityLivingBase p_96449_1_, double p_96449_2_, double p_96449_4_, double p_96449_6_, String p_96449_8_, float p_96449_9_, double p_96449_10_)
    {
        if (p_96449_1_.isPlayerSleeping())
        {
            this.func_147906_a(p_96449_1_, p_96449_8_, p_96449_2_, p_96449_4_ - 1.5D, p_96449_6_, 64);
        }
        else
        {
            this.func_147906_a(p_96449_1_, p_96449_8_, p_96449_2_, p_96449_4_, p_96449_6_, 64);
        }
    }

    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity) and this method has signature public void func_76986_a(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doesn't do that.
     */
    public void doRender(Entity p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_)
    {
        this.doRender((EntityLivingBase)p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
    }
    
    public ModelBase getMainModel() {
        return this.mainModel;
  }
}