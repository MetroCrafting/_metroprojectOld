package net.minecraft.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class Gui
{
    public static final ResourceLocation optionsBackground = new ResourceLocation("textures/gui/options_background.png");
    public static final ResourceLocation statIcons = new ResourceLocation("textures/gui/container/stats_icons.png");
    public static final ResourceLocation icons = new ResourceLocation("textures/gui/icons.png");
    protected float zLevel;
    //private static final String __OBFID = "CL_00000662";

    protected void drawHorizontalLine(int p_73730_1_, int p_73730_2_, int p_73730_3_, int p_73730_4_)
    {
        if (p_73730_2_ < p_73730_1_)
        {
            int i1 = p_73730_1_;
            p_73730_1_ = p_73730_2_;
            p_73730_2_ = i1;
        }

        drawRect(p_73730_1_, p_73730_3_, p_73730_2_ + 1, p_73730_3_ + 1, p_73730_4_);
    }

    protected void drawVerticalLine(int p_73728_1_, int p_73728_2_, int p_73728_3_, int p_73728_4_)
    {
        if (p_73728_3_ < p_73728_2_)
        {
            int i1 = p_73728_2_;
            p_73728_2_ = p_73728_3_;
            p_73728_3_ = i1;
        }

        drawRect(p_73728_1_, p_73728_2_ + 1, p_73728_1_ + 1, p_73728_3_, p_73728_4_);
    }

    /**
     * Draws a solid color rectangle with the specified coordinates and color. Args: x1, y1, x2, y2, color
     */
    public static void drawRect(float x1, float y1, float x2, float y2, int color)
    {
        float j1;

        if (x1 < x2)
        {
            j1 = x1;
            x1 = x2;
            x2 = j1;
        }

        if (y1 < y2)
        {
            j1 = y1;
            y1 = y2;
            y2 = j1;
        }

        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.instance;
//        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColor4f(f, f1, f2, f3);
        tessellator.startDrawingQuads();
        tessellator.addVertex(x1, y2, 0.0D);
        tessellator.addVertex(x2, y2, 0.0D);
        tessellator.addVertex(x2, (double)y1, 0.0D);
        tessellator.addVertex(x1, y1, 0.0D);
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
//        GL11.glDisable(GL11.GL_BLEND);
    }

    /**
     * Draws a rectangle with a vertical gradient between the specified colors.
     */
    public void drawGradientRect(int p_73733_1_, int p_73733_2_, int p_73733_3_, int p_73733_4_, int p_73733_5_, int p_73733_6_)
    {
        float f = (float)(p_73733_5_ >> 24 & 255) / 255.0F;
        float f1 = (float)(p_73733_5_ >> 16 & 255) / 255.0F;
        float f2 = (float)(p_73733_5_ >> 8 & 255) / 255.0F;
        float f3 = (float)(p_73733_5_ & 255) / 255.0F;
        float f4 = (float)(p_73733_6_ >> 24 & 255) / 255.0F;
        float f5 = (float)(p_73733_6_ >> 16 & 255) / 255.0F;
        float f6 = (float)(p_73733_6_ >> 8 & 255) / 255.0F;
        float f7 = (float)(p_73733_6_ & 255) / 255.0F;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(f1, f2, f3, f);
        tessellator.addVertex(p_73733_3_, p_73733_2_, this.zLevel);
        tessellator.addVertex(p_73733_1_, p_73733_2_, this.zLevel);
        tessellator.setColorRGBA_F(f5, f6, f7, f4);
        tessellator.addVertex(p_73733_1_, p_73733_4_, this.zLevel);
        tessellator.addVertex(p_73733_3_, p_73733_4_, this.zLevel);
        tessellator.draw();
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    /**
     * Renders the specified text to the screen, center-aligned.
     */
    public void drawCenteredString(FontRenderer font, String string, float x, float y, int color)
    {
        font.drawStringWithShadow(string, x - font.getStringWidth(string) / 2f, y, color);
    }

    /**
     * Renders the specified text to the screen.
     */
    public void drawString(FontRenderer font, String string, float x, float y, int color)
    {
        font.drawStringWithShadow(string, x, y, color);
    }

    /**
     * Draws a textured rectangle at the stored z-value. Args: x, y, u, v, width, height
     */
    public void drawTexturedModalRect(float x, float y, float u, float v, float width, float height)
    {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x + 0, y + height, this.zLevel, (u + 0) * f, (v + height) * f1);
        tessellator.addVertexWithUV(x + width, y + height, this.zLevel, (u + width) * f, (v + height) * f1);
        tessellator.addVertexWithUV(x + width, y + 0, this.zLevel, (u + width) * f, (v + 0) * f1);
        tessellator.addVertexWithUV(x + 0, y + 0, this.zLevel, (u + 0) * f, (v + 0) * f1);
        tessellator.draw();
    }

    public void drawTexturedModelRectFromIcon(int p_94065_1_, int p_94065_2_, IIcon p_94065_3_, int p_94065_4_, int p_94065_5_)
    {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(p_94065_1_, p_94065_2_ + p_94065_5_, this.zLevel, p_94065_3_.getMinU(), p_94065_3_.getMaxV());
        tessellator.addVertexWithUV(p_94065_1_ + p_94065_4_, p_94065_2_ + p_94065_5_, this.zLevel, p_94065_3_.getMaxU(), p_94065_3_.getMaxV());
        tessellator.addVertexWithUV(p_94065_1_ + p_94065_4_, p_94065_2_, this.zLevel, p_94065_3_.getMaxU(), p_94065_3_.getMinV());
        tessellator.addVertexWithUV(p_94065_1_, p_94065_2_, this.zLevel, p_94065_3_.getMinU(), p_94065_3_.getMinV());
        tessellator.draw();
    }

    public static void func_146110_a(int p_146110_0_, int p_146110_1_, float p_146110_2_, float p_146110_3_, int p_146110_4_, int p_146110_5_, float p_146110_6_, float p_146110_7_)
    {
        float f4 = 1.0F / p_146110_6_;
        float f5 = 1.0F / p_146110_7_;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV((double)p_146110_0_, (double)(p_146110_1_ + p_146110_5_), 0.0D, (double)(p_146110_2_ * f4), (double)((p_146110_3_ + (float)p_146110_5_) * f5));
        tessellator.addVertexWithUV((double)(p_146110_0_ + p_146110_4_), (double)(p_146110_1_ + p_146110_5_), 0.0D, (double)((p_146110_2_ + (float)p_146110_4_) * f4), (double)((p_146110_3_ + (float)p_146110_5_) * f5));
        tessellator.addVertexWithUV((double)(p_146110_0_ + p_146110_4_), (double)p_146110_1_, 0.0D, (double)((p_146110_2_ + (float)p_146110_4_) * f4), (double)(p_146110_3_ * f5));
        tessellator.addVertexWithUV((double)p_146110_0_, (double)p_146110_1_, 0.0D, (double)(p_146110_2_ * f4), (double)(p_146110_3_ * f5));
        tessellator.draw();
    }

    public static void func_152125_a(int p_152125_0_, int p_152125_1_, float p_152125_2_, float p_152125_3_, int p_152125_4_, int p_152125_5_, int p_152125_6_, int p_152125_7_, float p_152125_8_, float p_152125_9_)
    {
        float f4 = 1.0F / p_152125_8_;
        float f5 = 1.0F / p_152125_9_;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV((double)p_152125_0_, (double)(p_152125_1_ + p_152125_7_), 0.0D, (double)(p_152125_2_ * f4), (double)((p_152125_3_ + (float)p_152125_5_) * f5));
        tessellator.addVertexWithUV((double)(p_152125_0_ + p_152125_6_), (double)(p_152125_1_ + p_152125_7_), 0.0D, (double)((p_152125_2_ + (float)p_152125_4_) * f4), (double)((p_152125_3_ + (float)p_152125_5_) * f5));
        tessellator.addVertexWithUV((double)(p_152125_0_ + p_152125_6_), (double)p_152125_1_, 0.0D, (double)((p_152125_2_ + (float)p_152125_4_) * f4), (double)(p_152125_3_ * f5));
        tessellator.addVertexWithUV((double)p_152125_0_, (double)p_152125_1_, 0.0D, (double)(p_152125_2_ * f4), (double)(p_152125_3_ * f5));
        tessellator.draw();
    }
}