package net.minecraft.client.renderer.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.src.Config;
import shadersmod.client.Shaders;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public abstract class RenderLiving extends RendererLivingEntity
{
    private static final String __OBFID = "CL_00001015";

    public RenderLiving(ModelBase p_i1262_1_, float p_i1262_2_)
    {
        super(p_i1262_1_, p_i1262_2_);
    }

    protected boolean func_110813_b(EntityLiving p_110813_1_)
    {
        return super.func_110813_b(p_110813_1_) && (p_110813_1_.getAlwaysRenderNameTagForRender() || p_110813_1_.hasCustomNameTag() && p_110813_1_ == this.renderManager.field_147941_i);
    }

    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity) and this method has signature public void func_76986_a(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doesn't do that.
     */
    public void doRender(EntityLiving p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_)
    {
        super.doRender((EntityLivingBase)p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
        this.func_110827_b(p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
    }

    private double func_110828_a(double p_110828_1_, double p_110828_3_, double p_110828_5_)
    {
        return p_110828_1_ + (p_110828_3_ - p_110828_1_) * p_110828_5_;
    }

   protected void func_110827_b(EntityLiving par1EntityLiving, double par2, double par4, double par6, float par8, float par9) {
            if (!Config.isShaders() || !Shaders.isShadowPass) {
                  Entity var10 = par1EntityLiving.getLeashedToEntity();
                  if (var10 != null) {
                        par4 -= (1.6D - (double)par1EntityLiving.height) * 0.5D;
                        Tessellator var11 = Tessellator.instance;
                        double var12 = this.func_110828_a((double)var10.prevRotationYaw, (double)var10.rotationYaw, (double)(par9 * 0.5F)) * 0.01745329238474369D;
                        double var14 = this.func_110828_a((double)var10.prevRotationPitch, (double)var10.rotationPitch, (double)(par9 * 0.5F)) * 0.01745329238474369D;
                        double var16 = Math.cos(var12);
                        double var18 = Math.sin(var12);
                        double var20 = Math.sin(var14);
                        if (var10 instanceof EntityHanging) {
                              var16 = 0.0D;
                              var18 = 0.0D;
                              var20 = -1.0D;
                        }

                        double var22 = Math.cos(var14);
                        double var24 = this.func_110828_a(var10.prevPosX, var10.posX, (double)par9) - var16 * 0.7D - var18 * 0.5D * var22;
                        double var26 = this.func_110828_a(var10.prevPosY + (double)var10.getEyeHeight() * 0.7D, var10.posY + (double)var10.getEyeHeight() * 0.7D, (double)par9) - var20 * 0.5D - 0.25D;
                        double var28 = this.func_110828_a(var10.prevPosZ, var10.posZ, (double)par9) - var18 * 0.7D + var16 * 0.5D * var22;
                        double var30 = this.func_110828_a((double)par1EntityLiving.prevRenderYawOffset, (double)par1EntityLiving.renderYawOffset, (double)par9) * 0.01745329238474369D + 1.5707963267948966D;
                        var16 = Math.cos(var30) * (double)par1EntityLiving.width * 0.4D;
                        var18 = Math.sin(var30) * (double)par1EntityLiving.width * 0.4D;
                        double var32 = this.func_110828_a(par1EntityLiving.prevPosX, par1EntityLiving.posX, (double)par9) + var16;
                        double var34 = this.func_110828_a(par1EntityLiving.prevPosY, par1EntityLiving.posY, (double)par9);
                        double var36 = this.func_110828_a(par1EntityLiving.prevPosZ, par1EntityLiving.posZ, (double)par9) + var18;
                        par2 += var16;
                        par6 += var18;
                        double var38 = (double)((float)(var24 - var32));
                        double var40 = (double)((float)(var26 - var34));
                        double var42 = (double)((float)(var28 - var36));
                        GL11.glDisable(3553);
                        GL11.glDisable(2896);
                        GL11.glDisable(2884);
                        if (Config.isShaders()) {
                              Shaders.beginLeash();
                        }

                        boolean var44 = true;
                        double var45 = 0.025D;
                        var11.startDrawing(5);

                        int var47;
                        float var48;
                        for(var47 = 0; var47 <= 24; ++var47) {
                              if (var47 % 2 == 0) {
                                    var11.setColorRGBA_F(0.5F, 0.4F, 0.3F, 1.0F);
                              } else {
                                    var11.setColorRGBA_F(0.35F, 0.28F, 0.21000001F, 1.0F);
                              }

                              var48 = (float)var47 / 24.0F;
                              var11.addVertex(par2 + var38 * (double)var48 + 0.0D, par4 + var40 * (double)(var48 * var48 + var48) * 0.5D + (double)((24.0F - (float)var47) / 18.0F + 0.125F), par6 + var42 * (double)var48);
                              var11.addVertex(par2 + var38 * (double)var48 + 0.025D, par4 + var40 * (double)(var48 * var48 + var48) * 0.5D + (double)((24.0F - (float)var47) / 18.0F + 0.125F) + 0.025D, par6 + var42 * (double)var48);
                        }

                        var11.draw();
                        var11.startDrawing(5);

                        for(var47 = 0; var47 <= 24; ++var47) {
                              if (var47 % 2 == 0) {
                                    var11.setColorRGBA_F(0.5F, 0.4F, 0.3F, 1.0F);
                              } else {
                                    var11.setColorRGBA_F(0.35F, 0.28F, 0.21000001F, 1.0F);
                              }

                              var48 = (float)var47 / 24.0F;
                              var11.addVertex(par2 + var38 * (double)var48 + 0.0D, par4 + var40 * (double)(var48 * var48 + var48) * 0.5D + (double)((24.0F - (float)var47) / 18.0F + 0.125F) + 0.025D, par6 + var42 * (double)var48);
                              var11.addVertex(par2 + var38 * (double)var48 + 0.025D, par4 + var40 * (double)(var48 * var48 + var48) * 0.5D + (double)((24.0F - (float)var47) / 18.0F + 0.125F), par6 + var42 * (double)var48 + 0.025D);
                        }

                        var11.draw();
                        if (Config.isShaders()) {
                              Shaders.endLeash();
                        }

                        GL11.glEnable(2896);
                        GL11.glEnable(3553);
                        GL11.glEnable(2884);
                  }

            }
      }

    protected boolean func_110813_b(EntityLivingBase p_110813_1_)
    {
        return this.func_110813_b((EntityLiving)p_110813_1_);
    }

    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity) and this method has signature public void func_76986_a(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doesn't do that.
     */
    public void doRender(EntityLivingBase p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_)
    {
        this.doRender((EntityLiving)p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
    }

    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity) and this method has signature public void func_76986_a(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doesn't do that.
     */
    public void doRender(Entity p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_)
    {
        this.doRender((EntityLiving)p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
    }
}