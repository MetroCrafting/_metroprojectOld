package com.creativemd.littletiles.common.structure;

import net.minecraft.nbt.NBTTagCompound;

import com.creativemd.creativecore.common.gui.SubGui;

public class LittleLadder extends LittleStructure {

    @Override
    protected void loadFromNBTExtra(NBTTagCompound nbt) {

    }

    @Override
    protected void writeToNBTExtra(NBTTagCompound nbt) {

    }

    @Override
    public void createControls(SubGui gui, LittleStructure structure) {

    }

    @Override
    public LittleStructure parseStructure(SubGui gui) {
        return new LittleLadder();
    }

    @Override
    public boolean isLadder() {
        return true;
    }

}
