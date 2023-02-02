package com.creativemd.littletiles.common.items;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.creativemd.creativecore.common.container.SubContainer;
import com.creativemd.creativecore.common.gui.IGuiCreator;
import com.creativemd.creativecore.common.gui.SubGui;
import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.creativecore.core.CreativeCore;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.gui.SubContainerHammer;
import com.creativemd.littletiles.common.gui.SubGuiHammer;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemHammer extends Item implements IGuiCreator {

    public ItemHammer() {
        setCreativeTab(CreativeTabs.tabTools);
        setMaxStackSize(1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        list.add("used for hammering normal");
        list.add("blocks into small pieces");
        list.add("shift+rightclick will harvest and");
        list.add("drop all tiles inside one block");
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote && !player.isSneaking()) {
            player.openGui(CreativeCore.instance, 1, world, (int) player.posX, (int) player.posY, (int) player.posZ);
            return stack;
        }
        return stack;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
            float hitX, float hitY, float hitZ) {
        if (!world.isRemote && player.isSneaking()) {
            TileEntity tileEntity = world.getTileEntity(x, y, z);
            if (tileEntity instanceof TileEntityLittleTiles) {
                if (((TileEntityLittleTiles) tileEntity).getTiles().size() <= 1) {
                    LittleTiles.blockTile.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
                } else {
                    ItemStack drop = new ItemStack(LittleTiles.multiTiles);
                    ItemRecipe.saveTiles(world, ((TileEntityLittleTiles) tileEntity).getTiles(), drop);
                    WorldUtils.dropItem(world, drop, x, y, z);
                }
                world.setBlockToAir(x, y, z);
                return true;
            }
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected String getIconString() {
        return LittleTiles.modid + ":LTHammer";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SubGui getGui(EntityPlayer player, ItemStack stack, World world, int x, int y, int z) {
        return new SubGuiHammer();
    }

    @Override
    public SubContainer getContainer(EntityPlayer player, ItemStack stack, World world, int x, int y, int z) {
        return new SubContainerHammer(player);
    }

}
