package com.creativemd.littletiles.common.structure;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;

import com.creativemd.creativecore.common.utils.RotationUtils.Axis;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTilePreview;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;
import com.creativemd.littletiles.utils.PreviewTile;

public class PreviewTileAxis extends PreviewTile {

    public static Vec3 red = Vec3.createVectorHelper(1, 0, 0);
    public Axis axis;

    public PreviewTileAxis(LittleTileBox box, LittleTilePreview preview, Axis axis) {
        super(box, preview);
        this.axis = axis;
    }

    @Override
    public Vec3 getPreviewColor() {
        return red;
    }

    @Override
    public boolean needsCollisionTest() {
        return false;
    }

    @Override
    public PreviewTile copy() {
        return new PreviewTileAxis(box.copy(), null, axis);
    }

    @Override
    public LittleTileBox getPreviewBox() {
        LittleTileBox preview = box.copy();
        int max = 40 * 16;
        int min = -max;
        switch (axis) {
            case Xaxis:
                preview.minX = min;
                preview.maxX = max;
                break;
            case Yaxis:
                preview.minY = min;
                preview.maxY = max;
                break;
            case Zaxis:
                preview.minZ = min;
                preview.maxZ = max;
                break;
            default:
                break;
        }
        return preview;
    }

    @Override
    public LittleTile placeTile(EntityPlayer player, ItemStack stack, TileEntityLittleTiles teLT,
            LittleStructure structure, ArrayList<LittleTile> unplaceableTiles) {
        if (structure instanceof LittleDoor) {
            LittleDoor door = (LittleDoor) structure;
            door.axisVec = box.getMinVec(); // Check if this would be perfect
            door.axisVec.addVec(new LittleTileVec(teLT.xCoord * 16, teLT.yCoord * 16, teLT.zCoord * 16));
            door.axisVec.subVec(door.mainTile.cornerVec);
        }
        return null;
    }

    /*
     * @Override public boolean split(HashMapList<ChunkCoordinates, PreviewTile> tiles, int x, int y, int z) {
     * tiles.add(new ChunkCoordinates(x, y, z), this); return true; }
     */

}
