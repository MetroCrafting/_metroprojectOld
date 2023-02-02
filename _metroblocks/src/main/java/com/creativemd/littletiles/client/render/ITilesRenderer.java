package com.creativemd.littletiles.client.render;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;

import com.creativemd.creativecore.common.utils.CubeObject;

public interface ITilesRenderer { // is client-side effective only!

    ArrayList<CubeObject> getRenderingCubes(ItemStack stack);

    boolean hasBackground(ItemStack stack);

}
