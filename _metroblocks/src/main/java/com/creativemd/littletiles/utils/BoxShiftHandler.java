package com.creativemd.littletiles.utils;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import com.creativemd.creativecore.client.rendering.RenderHelper3D;
import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileSize;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;

public abstract class BoxShiftHandler extends ShiftHandler {

    public ArrayList<LittleTileBox> boxes = new ArrayList<>();

    public void init(World world, int x, int y, int z) {
        boxes = getBoxes(world, x, y, z);
    }

    public abstract ArrayList<LittleTileBox> getBoxes(World world, int x, int y, int z);

    @Override
    public void handleRendering(Minecraft mc, double x, double y, double z) {
        for (LittleTileBox box : boxes) {
            if (!box.isValidBox()) continue;
            GL11.glPushMatrix();
            CubeObject cube = box.getCube();
            LittleTileSize size = box.getSize();
            double cubeX = x + cube.minX + size.getPosX() / 2D;
            double cubeY = y + cube.minY + size.getPosY() / 2D;
            double cubeZ = z + cube.minZ + size.getPosZ() / 2D;
            RenderHelper3D.renderBlock(
                    cubeX,
                    cubeY,
                    cubeZ,
                    size.getPosX(),
                    size.getPosY(),
                    size.getPosZ(),
                    0,
                    0,
                    0,
                    1,
                    1,
                    0.5,
                    (Math.sin(System.nanoTime() / 200000000D) + 1.5) * 0.2D);
            GL11.glPopMatrix();
        }

    }

    @Override
    public double getDistance(LittleTileVec suggestedPos) {
        double distance = 2;
        for (LittleTileBox box : boxes) distance = Math.min(distance, box.distanceTo(suggestedPos));
        return 0;
    }

    @Override
    protected LittleTileBox getNewPos(World world, int x, int y, int z, LittleTileBox suggested) {
        /*
         * double distance = 2; LittleTileBox nearestBox = null; for (int i = 0; i < boxes.size(); i++) { distance =
         * Math.min(distance, boxes.get(i).distanceTo(suggested)); nearestBox = boxes.get(i); } if(nearestBox != null) {
         * LittleTileVec min = nearestBox.getNearstedPointTo(suggested); LittleTileVec max = min.copy(); max.addVec(new
         * LittleTileVec(1, 1, 1)); return new LittleTileBox(min, max); //LittleTileVec max }
         */
        return null;
    }

}
