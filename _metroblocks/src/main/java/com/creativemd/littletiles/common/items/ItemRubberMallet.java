package com.creativemd.littletiles.common.items;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.gui.SubContainerHammer;
import com.creativemd.littletiles.common.packet.LittleBlockPacket;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTileBlock;
import com.creativemd.littletiles.common.utils.LittleTileBlockColored;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.utils.TileList;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemRubberMallet extends Item {

    public ItemRubberMallet() {
        setCreativeTab(CreativeTabs.tabTools);
        hasSubtypes = true;
        setMaxStackSize(1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        list.add("splits all tiles into");
        list.add("smallest pieces possible");
        list.add("limit: " + LittleTiles.maxNewTiles);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
            float hitX, float hitY, float hitZ) {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof TileEntityLittleTiles) {
            if (!world.isRemote) {
                if (player.isSneaking()) {
                    TileList<LittleTile> newTiles = TileEntityLittleTiles.createTileList();
                    TileEntityLittleTiles te = (TileEntityLittleTiles) tileEntity;
                    TileList<LittleTile> tiles = te.getTiles();
                    for (LittleTile oldTile : tiles) {
                        if ((oldTile.getClass() == LittleTileBlock.class || oldTile instanceof LittleTileBlockColored)
                                && oldTile.structure == null) {
                            for (int j = 0; j < oldTile.boundingBoxes.size(); j++) {
                                LittleTileBox box = oldTile.boundingBoxes.get(j);
                                for (int littleX = box.minX; littleX < box.maxX; littleX++) {
                                    for (int littleY = box.minY; littleY < box.maxY; littleY++) {
                                        for (int littleZ = box.minZ; littleZ < box.maxZ; littleZ++) {
                                            LittleTile tile = oldTile.copy();
                                            tile.boundingBoxes.clear();
                                            tile.boundingBoxes.add(
                                                    new LittleTileBox(
                                                            littleX,
                                                            littleY,
                                                            littleZ,
                                                            littleX + 1,
                                                            littleY + 1,
                                                            littleZ + 1));
                                            tile.updateCorner();
                                            tile.te = te;
                                            newTiles.add(tile);
                                        }
                                    }
                                }
                            }

                        } else newTiles.add(oldTile);
                    }
                    if (LittleTiles.maxNewTiles >= newTiles.size() - te.getTiles().size()) {
                        te.setTiles(newTiles);
                        te.update();
                    } else {
                        player.addChatComponentMessage(
                                new ChatComponentText("Too much new tiles! Limit=" + LittleTiles.maxNewTiles));
                    }
                }
            } else {
                PacketHandler.sendPacketToServer(new LittleBlockPacket(x, y, z, player, 4, new NBTTagCompound()));
            }
            return true;
        } else {
            Block block = world.getBlock(x, y, z);

            if (tileEntity == null && SubContainerHammer.isBlockValid(block)) {
                if (!world.isRemote) {
                    if (LittleTiles.maxNewTiles < 4096) {
                        player.addChatComponentMessage(
                                new ChatComponentText("Too much new tiles! Limit=" + LittleTiles.maxNewTiles));
                        return true;
                    }
                    int meta = world.getBlockMetadata(x, y, z);
                    TileEntityLittleTiles te = new TileEntityLittleTiles();
                    for (int littleX = LittleTile.minPos; littleX < LittleTile.maxPos; littleX++) {
                        for (int littleY = LittleTile.minPos; littleY < LittleTile.maxPos; littleY++) {
                            for (int littleZ = LittleTile.minPos; littleZ < LittleTile.maxPos; littleZ++) {
                                LittleTileBlock tile = new LittleTileBlock(block, meta);
                                tile.boundingBoxes.add(
                                        new LittleTileBox(
                                                littleX,
                                                littleY,
                                                littleZ,
                                                littleX + 1,
                                                littleY + 1,
                                                littleZ + 1));
                                tile.updateCorner();
                                tile.te = te;
                                te.getTiles().add(tile);
                            }
                        }
                    }
                    if (LittleTiles.maxNewTiles >= te.getTiles().size()) {
                        world.setBlock(x, y, z, LittleTiles.blockTile);
                        world.setTileEntity(x, y, z, te);
                    } else {
                        player.addChatComponentMessage(
                                new ChatComponentText("Too much new tiles! Limit=" + LittleTiles.maxNewTiles));
                    }

                }
                return true;
            }
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected String getIconString() {
        return LittleTiles.modid + ":LTRubberMallet";
    }
}
