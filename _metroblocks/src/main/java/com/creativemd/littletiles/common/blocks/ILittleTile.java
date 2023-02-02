package com.creativemd.littletiles.common.blocks;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.utils.LittleTilePreview;

public interface ILittleTile {

    ArrayList<LittleTilePreview> getLittlePreview(ItemStack stack);

    void rotateLittlePreview(ItemStack stack, ForgeDirection direction);

    void flipLittlePreview(ItemStack stack, ForgeDirection direction);

    LittleStructure getLittleStructure(ItemStack stack);

    // public ArrayList<LittleTile> getLittleTile(ItemStack stack);
}
