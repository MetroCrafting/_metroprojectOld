package com.creativemd.littletiles.common.gui.controls;

import java.util.ArrayList;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector4d;

import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.creativemd.creativecore.client.block.BlockRenderHelper;
import com.creativemd.creativecore.client.rendering.RenderHelper2D;
import com.creativemd.creativecore.client.rendering.RenderHelper3D;
import com.creativemd.creativecore.common.gui.controls.GuiControl;
import com.creativemd.creativecore.common.gui.premade.SubGuiControl;
import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.creativecore.common.utils.RotationUtils.Axis;
import com.creativemd.littletiles.client.render.ITilesRenderer;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;

public class GuiTileViewer extends GuiControl {

    public ItemStack stack;

    public float scale = 1;
    public float offsetX = 0;
    public float offsetY = 0;

    public ForgeDirection viewDirection = ForgeDirection.EAST;

    public boolean visibleAxis = false;

    public Axis normalAxis = null;
    public Axis axisDirection = Axis.Yaxis;
    public int axisX = 0;
    public int axisY = 0;
    public int axisZ = 0;

    public boolean grabbed = false;

    public GuiTileViewer(String name, int x, int y, int width, int height, ItemStack stack) {
        super(name, x, y, width, height);
        this.stack = stack;
        updateNormalAxis();
    }

    public void updateNormalAxis() {
        ArrayList<CubeObject> cubes = ((ITilesRenderer) stack.getItem()).getRenderingCubes(stack);
        double minX = Integer.MAX_VALUE;
        double minY = Integer.MAX_VALUE;
        double minZ = Integer.MAX_VALUE;
        double maxX = Integer.MIN_VALUE;
        double maxY = Integer.MIN_VALUE;
        double maxZ = Integer.MIN_VALUE;

        for (CubeObject cube : cubes) {
            minX = Math.min(minX, cube.minX);
            minY = Math.min(minY, cube.minY);
            minZ = Math.min(minZ, cube.minZ);
            maxX = Math.max(maxX, cube.maxX);
            maxY = Math.max(maxY, cube.maxY);
            maxZ = Math.max(maxZ, cube.maxZ);
        }

        double sizeX = maxX - minX;
        double sizeY = maxY - minZ;
        double sizeZ = maxZ - minZ;

        switch (axisDirection) {
            case Xaxis:
                if (sizeY >= sizeZ) normalAxis = Axis.Zaxis;
                else normalAxis = Axis.Yaxis;
                break;
            case Yaxis:
                if (sizeX >= sizeZ) normalAxis = Axis.Zaxis;
                else normalAxis = Axis.Xaxis;
                break;
            case Zaxis:
                if (sizeX >= sizeY) normalAxis = Axis.Yaxis;
                else normalAxis = Axis.Xaxis;
                break;
            default:
                break;
        }
    }

    public void changeNormalAxis() {
        switch (axisDirection) {
            case Xaxis:
                if (normalAxis == Axis.Zaxis) normalAxis = Axis.Yaxis;
                else normalAxis = Axis.Zaxis;
                break;
            case Yaxis:
                if (normalAxis == Axis.Zaxis) normalAxis = Axis.Xaxis;
                else normalAxis = Axis.Zaxis;
                break;
            case Zaxis:
                if (normalAxis == Axis.Yaxis) normalAxis = Axis.Xaxis;
                else normalAxis = Axis.Yaxis;
                break;
            default:
                break;
        }
    }

    @Override
    public void drawControl(FontRenderer renderer) {
        Vector4d black = new Vector4d(0, 0, 0, 255);
        RenderHelper2D.drawGradientRect(0, 0, this.width, this.height, black, black);

        Vector4d color = new Vector4d(255, 255, 255, 255); // new Vector4d(140, 140, 140, 255);
        RenderHelper2D.drawGradientRect(1, 1, this.width - 1, this.height - 1, color, color);

        ScaledResolution scaledresolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int i = scaledresolution.getScaledWidth();
        int j = scaledresolution.getScaledHeight();
        int movex = i / 2 - parent.width / 2 + (posX) + 1;
        int movey = j / 2 - parent.height / 2 + (parent.height - (height + posY)) + 1;
        if (parent instanceof SubGuiControl) {
            movey = j / 2 - ((SubGuiControl) parent).parent.parent.height / 2
                    + (((SubGuiControl) parent).parent.parent.height - (height + posY))
                    + 1;
            // Vector2d offset = ((SubGuiControl) parent).parent.getCenterOffset();
            // movex -= offset.x;
            // movey += offset.y;
        }
        int scale = scaledresolution.getScaleFactor();
        movex *= scale;
        movey *= scale;

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(movex, movey, (this.width - 2) * scale, (this.height - 2) * scale);
        GL11.glPushMatrix();

        // Vec3 offset = Vec3.createVectorHelper(p_72443_0_, p_72443_2_, p_72443_4_);
        GL11.glTranslated((float) (this.width / 2) - offsetX, (float) (this.height / 2) - offsetY, 0);
        GL11.glScaled(4, 4, 4);
        GL11.glScaled(this.scale, this.scale, this.scale);
        GL11.glTranslated(offsetX * 2, offsetY * 2, 0);

        mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        // Block block = Block.getBlockFromItem(stack.getItem());
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
        GL11.glEnable(GL11.GL_BLEND);
        // OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        /*
         * if (block.getRenderBlockPass() != 0) { GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F); GL11.glEnable(GL11.GL_BLEND);
         * OpenGlHelper.glBlendFunc(770, 771, 1, 0); } else {
         */
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.5F);
        GL11.glDisable(GL11.GL_BLEND);

        // }

        if (viewDirection == ForgeDirection.UP /*
                                                * || viewDirection == ForgeDirection.DOWN // why is there is duplicate
                                                * condition here ?
                                                */) {
            GL11.glRotated(-90, 1, 0, 0);
        } else if (viewDirection == ForgeDirection.DOWN) {
            GL11.glRotated(90, 1, 0, 0);
        } else {
            RenderHelper3D.applyDirection(viewDirection);
        }

        GL11.glPushMatrix();
        GL11.glTranslatef((float) (-2), (float) (+3), -3.0F);
        GL11.glScalef(10.0F, 10.0F, 10.0F);
        GL11.glTranslatef(1.0F, 0.5F, 1.0F);
        GL11.glScalef(1.0F, 1.0F, -1.0F);
        GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
        // GL11.glRotatef(210.0F, 1.0F, 0.0F, 0.0F);
        /*
         * GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F); GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
         */
        GL11.glEnable(GL11.GL_LIGHTING);
        // RenderBlocks.getInstance().useInventoryTint = true;
        // RenderBlocks.getInstance().renderBlockAsItem(block, k, 1.0F);
        ArrayList<CubeObject> cubes = ((ITilesRenderer) stack.getItem()).getRenderingCubes(stack);

        if (visibleAxis) {
            double min = -10d / scale;
            double max = -min;
            CubeObject cube = new LittleTileBox(axisX, axisY, axisZ, axisX + 1, axisY + 1, axisZ + 1).getCube();
            cube.block = Blocks.wool;
            cube.meta = 0;
            switch (normalAxis) {
                case Xaxis:
                    // cube.minZ = min;
                    // cube.maxZ = max;
                    cube.minX = min;
                    cube.maxX = max;
                    break;
                case Yaxis:
                    cube.minY = min;
                    cube.maxY = max;
                    break;
                case Zaxis:
                    // cube.minX = min;
                    // cube.maxX = max;
                    cube.minZ = min;
                    cube.maxZ = max;
                    break;
                default:
                    break;
            }
            cubes.add(cube);
            cube = new LittleTileBox(axisX, axisY, axisZ, axisX + 1, axisY + 1, axisZ + 1).getCube();
            cube.block = Blocks.wool;
            cube.meta = 5;

            switch (axisDirection) {
                case Xaxis:
                    // cube.minZ = min;
                    // cube.maxZ = max;
                    cube.minX = min;
                    cube.maxX = max;
                    break;
                case Yaxis:
                    cube.minY = min;
                    cube.maxY = max;
                    break;
                case Zaxis:
                    // cube.minX = min;
                    // cube.maxX = max;
                    cube.minZ = min;
                    cube.maxZ = max;
                    break;
                default:
                    break;
            }
            cubes.add(cube);
        }

        /*
         * for (int k = 0; k < cubes.size(); k++) { RotationUtils.applyCubeRotation(cubes.get(k), viewDirection); }
         */
        BlockRenderHelper.renderInventoryCubes(
                RenderHelper3D.renderBlocks,
                cubes,
                Block.getBlockFromItem(stack.getItem()),
                stack.getItemDamage());

        // RenderBlocks.getInstance().useInventoryTint = true;
        GL11.glDisable(GL11.GL_LIGHTING);
        // if (block.getRenderBlockPass() == 0)
        // {
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
        // }

        GL11.glPopMatrix();
        // RenderHelper2D.renderItem(stack, 0, 0);

        GL11.glPopMatrix();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Override
    public boolean mouseScrolled(int posX, int posY, int scrolled) {
        if (scrolled > 0) scale *= scrolled * 1.5;
        else if (scrolled < 0) scale /= scrolled * -1.5;
        return true;
    }

    @Override
    public boolean mousePressed(int posX, int posY, int button) {
        grabbed = true;
        lastPosition = Vec3.createVectorHelper(posX, posY, 0);
        return true;
    }

    public Vec3 lastPosition;

    @Override
    public void mouseMove(int posX, int posY, int button) {
        Vector2d mouse = parent.getMousePos();
        if (grabbed) {
            Vec3 currentPosition = Vec3.createVectorHelper(posX, posY, 0);
            if (lastPosition != null) {
                Vec3 move = lastPosition.subtract(currentPosition);
                double percent = 0.3;
                offsetX += 1 / scale * move.xCoord * percent;
                offsetY += 1 / scale * move.yCoord * percent;
            }
            lastPosition = currentPosition;
        }
    }

    @Override
    public void mouseReleased(int posX, int posY, int button) {
        if (this.grabbed) {
            lastPosition = null;
            grabbed = false;
        }
    }

    @Override
    public boolean onKeyPressed(char character, int key) {
        if (key == Keyboard.KEY_ADD) {
            scale *= 2;
            return true;
        }
        if (key == Keyboard.KEY_SUBTRACT) {
            scale /= 2;
            return true;
        }
        int ammount = 5;
        if (key == Keyboard.KEY_UP) {
            offsetY += ammount;
            return true;
        }
        if (key == Keyboard.KEY_DOWN) {
            offsetY -= ammount;
            return true;
        }
        if (key == Keyboard.KEY_RIGHT) {
            offsetX -= ammount;
            return true;
        }
        if (key == Keyboard.KEY_LEFT) {
            offsetX += ammount;
            return true;
        }

        return false;
    }

    public void updateViewDirection() {
        switch (axisDirection) {
            case Xaxis:
                viewDirection = Axis.Zaxis.getDirection();
                break;
            case Yaxis:
                viewDirection = Axis.Yaxis.getDirection();
                break;
            case Zaxis:
                viewDirection = Axis.Xaxis.getDirection();
                break;
            default:
                break;
        }
    }

}
