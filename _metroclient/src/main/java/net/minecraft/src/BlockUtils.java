package net.minecraft.src;

import net.minecraft.block.Block;

public class BlockUtils
{

    public static void setLightOpacity(Block block, int opacity)
    {
        block.setLightOpacity(opacity);
    }
}