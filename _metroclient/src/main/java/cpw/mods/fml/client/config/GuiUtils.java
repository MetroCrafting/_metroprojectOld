/*
 * Forge Mod Loader
 * Copyright (c) 2012-2014 cpw.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Contributors (this class):
 *     bspkrs - implementation
 */

package cpw.mods.fml.client.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

/**
 * This class provides several methods and constants used by the Config GUI classes.
 * 
 * @author bspkrs
 */
public class GuiUtils 
{
    public static final String UNDO_CHAR  = "\u21B6";
    public static final String RESET_CHAR = "\u2604";
    public static final String VALID      = "\u2714";
    public static final String INVALID    = "\u2715";
    
    private static int[] colorCodes = new int[] { 0, 170, 43520, 43690, 11141120, 11141290, 16755200, 11184810, 5592405, 5592575, 5635925, 5636095, 16733525, 16733695, 16777045, 16777215,
        0, 42, 10752, 10794, 2752512, 2752554, 2763264, 2763306, 1381653, 1381695, 1392405, 1392447, 4134165, 4134207, 4144917, 4144959 };

    public static int getColorCode(char c, boolean isLighter)
    {
        return colorCodes[isLighter ? "0123456789abcdef".indexOf(c) : "0123456789abcdef".indexOf(c) + 16];
    }
    /**
     * Draws a textured box of any size (smallest size is borderSize * 2 square) based on a fixed size textured box with continuous borders
     * and filler. It is assumed that the desired texture ResourceLocation object has been bound using
     * Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocation).
     * 
     * @param x x axis offset
     * @param y y axis offset
     * @param u bound resource location image x offset
     * @param v bound resource location image y offset
     * @param width the desired box width
     * @param height the desired box height
     * @param textureWidth the width of the box texture in the resource location image
     * @param textureHeight the height of the box texture in the resource location image
     * @param borderSize the size of the box's borders
     * @param zLevel the zLevel to draw at
     */
    public static void drawContinuousTexturedBox(int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight,
            int borderSize, float zLevel)
    {
        drawContinuousTexturedBox(x, y, u, v, width, height, textureWidth, textureHeight, borderSize, borderSize, borderSize, borderSize, zLevel);
    }
    
    /**
     * Draws a textured box of any size (smallest size is borderSize * 2 square) based on a fixed size textured box with continuous borders
     * and filler. The provided ResourceLocation object will be bound using
     * Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocation).
     * 
     * @param res the ResourceLocation object that contains the desired image
     * @param x x axis offset
     * @param y y axis offset
     * @param u bound resource location image x offset
     * @param v bound resource location image y offset
     * @param width the desired box width
     * @param height the desired box height
     * @param textureWidth the width of the box texture in the resource location image
     * @param textureHeight the height of the box texture in the resource location image
     * @param borderSize the size of the box's borders
     * @param zLevel the zLevel to draw at
     */
    public static void drawContinuousTexturedBox(ResourceLocation res, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight,
            int borderSize, float zLevel)
    {
        drawContinuousTexturedBox(res, x, y, u, v, width, height, textureWidth, textureHeight, borderSize, borderSize, borderSize, borderSize, zLevel);
    }
    
    /**
     * Draws a textured box of any size (smallest size is borderSize * 2 square) based on a fixed size textured box with continuous borders
     * and filler. The provided ResourceLocation object will be bound using
     * Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocation).
     * 
     * @param res the ResourceLocation object that contains the desired image
     * @param xPosition x axis offset
     * @param yPosition y axis offset
     * @param u bound resource location image x offset
     * @param v bound resource location image y offset
     * @param width the desired box width
     * @param height the desired box height
     * @param textureWidth the width of the box texture in the resource location image
     * @param textureHeight the height of the box texture in the resource location image
     * @param topBorder the size of the box's top border
     * @param bottomBorder the size of the box's bottom border
     * @param leftBorder the size of the box's left border
     * @param rightBorder the size of the box's right border
     * @param zLevel the zLevel to draw at
     */
    public static void drawContinuousTexturedBox(ResourceLocation res, float xPosition, float yPosition, int u, int v, float width, float height, int textureWidth, int textureHeight,
            int topBorder, int bottomBorder, int leftBorder, int rightBorder, float zLevel)
    {
        Minecraft.getMinecraft().getTextureManager().bindTexture(res);
        drawContinuousTexturedBox(xPosition, yPosition, u, v, width, height, textureWidth, textureHeight, topBorder, bottomBorder, leftBorder, rightBorder, zLevel);
    }
    
    /**
     * Draws a textured box of any size (smallest size is borderSize * 2 square) based on a fixed size textured box with continuous borders
     * and filler. It is assumed that the desired texture ResourceLocation object has been bound using
     * Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocation).
     * 
     * @param xPosition x axis offset
     * @param yPosition y axis offset
     * @param u bound resource location image x offset
     * @param v bound resource location image y offset
     * @param width the desired box width
     * @param height the desired box height
     * @param textureWidth the width of the box texture in the resource location image
     * @param textureHeight the height of the box texture in the resource location image
     * @param topBorder the size of the box's top border
     * @param bottomBorder the size of the box's bottom border
     * @param leftBorder the size of the box's left border
     * @param rightBorder the size of the box's right border
     * @param zLevel the zLevel to draw at
     */
    public static void drawContinuousTexturedBox(float xPosition, float yPosition, int u, int v, float width, float height, int textureWidth, int textureHeight,
            int topBorder, int bottomBorder, int leftBorder, int rightBorder, float zLevel)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        
        int fillerWidth = textureWidth - leftBorder - rightBorder;
        int fillerHeight = textureHeight - topBorder - bottomBorder;
        float canvasWidth = width - leftBorder - rightBorder;
        float canvasHeight = height - topBorder - bottomBorder;
        float xPasses = canvasWidth / fillerWidth;
        float remainderWidth = canvasWidth % fillerWidth;
        float yPasses = canvasHeight / fillerHeight;
        float remainderHeight = canvasHeight % fillerHeight;
        
        // Draw Border
        // Top Left
        drawTexturedModalRect(xPosition, yPosition, u, v, leftBorder, topBorder, zLevel);
        // Top Right
        drawTexturedModalRect(xPosition + leftBorder + canvasWidth, yPosition, u + leftBorder + fillerWidth, v, rightBorder, topBorder, zLevel);
        // Bottom Left
        drawTexturedModalRect(xPosition, yPosition + topBorder + canvasHeight, u, v + topBorder + fillerHeight, leftBorder, bottomBorder, zLevel);
        // Bottom Right
        drawTexturedModalRect(xPosition + leftBorder + canvasWidth, yPosition + topBorder + canvasHeight, u + leftBorder + fillerWidth, v + topBorder + fillerHeight, rightBorder, bottomBorder, zLevel);
        
        for (int i = 0; i < xPasses + (remainderWidth > 0 ? 1 : 0); i++)
        {
            // Top Border
            drawTexturedModalRect(xPosition + leftBorder + (i * fillerWidth), yPosition, u + leftBorder, v, (i == xPasses ? remainderWidth : fillerWidth), topBorder, zLevel);
            // Bottom Border
            drawTexturedModalRect(xPosition + leftBorder + (i * fillerWidth), yPosition + topBorder + canvasHeight, u + leftBorder, v + topBorder + fillerHeight, (i == xPasses ? remainderWidth : fillerWidth), bottomBorder, zLevel);
            
            // Throw in some filler for good measure
            for (int j = 0; j < yPasses + (remainderHeight > 0 ? 1 : 0); j++)
                drawTexturedModalRect(xPosition + leftBorder + (i * fillerWidth), yPosition + topBorder + (j * fillerHeight), u + leftBorder, v + topBorder, (i == xPasses ? remainderWidth : fillerWidth), (j == yPasses ? remainderHeight : fillerHeight), zLevel);
        }
        
        // Side Borders
        for (int j = 0; j < yPasses + (remainderHeight > 0 ? 1 : 0); j++)
        {
            // Left Border
            drawTexturedModalRect(xPosition, yPosition + topBorder + (j * fillerHeight), u, v + topBorder, leftBorder, (j == yPasses ? remainderHeight : fillerHeight), zLevel);
            // Right Border
            drawTexturedModalRect(xPosition + leftBorder + canvasWidth, yPosition + topBorder + (j * fillerHeight), u + leftBorder + fillerWidth, v + topBorder, rightBorder, (j == yPasses ? remainderHeight : fillerHeight), zLevel);
        }
    }
    
    public static void drawTexturedModalRect(float xPosition, float yPosition, int u, int v, float f, float g, float zLevel)
    {
        float var7 = 0.00390625F;
        float var8 = 0.00390625F;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV((xPosition + 0), (yPosition + g), zLevel, ((u + 0) * var7), ((v + g) * var8));
        tessellator.addVertexWithUV((xPosition + f), (yPosition + g), zLevel, ((u + f) * var7), ((v + g) * var8));
        tessellator.addVertexWithUV((xPosition + f), (yPosition + 0), zLevel, ((u + f) * var7), ((v + 0) * var8));
        tessellator.addVertexWithUV((xPosition + 0), (yPosition + 0), zLevel, ((u + 0) * var7), ((v + 0) * var8));
        tessellator.draw();
    }

}