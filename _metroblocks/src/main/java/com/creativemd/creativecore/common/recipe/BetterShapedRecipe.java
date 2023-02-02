package com.creativemd.creativecore.common.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

import com.creativemd.creativecore.common.utils.stack.StackInfo;

public class BetterShapedRecipe implements IRecipe, IRecipeInfo {

    public StackInfo[] info;
    public ItemStack output;
    public int width;
    public int height;

    public BetterShapedRecipe(int width, StackInfo[] info, ItemStack output) {
        this.info = info;
        this.output = output;
        this.width = width;
        this.height = this.info.length / this.width;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World world) {
        for (int x = 0; x <= 3 - width; x++) {
            for (int y = 0; y <= 3 - height; ++y) {
                if (checkMatch(inv, x, y, false)) {
                    return true;
                }

                if (checkMatch(inv, x, y, true)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean checkMatch(InventoryCrafting inv, int startX, int startY, boolean mirror) {
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                int subX = x - startX;
                int subY = y - startY;
                StackInfo target = null;

                if (subX >= 0 && subY >= 0 && subX < width && subY < height) {
                    if (mirror) {
                        target = info[width - subX - 1 + subY * width];
                    } else {
                        target = info[subX + subY * width];
                    }
                }

                ItemStack slot = inv.getStackInRowAndColumn(x, y);

                if (target == null && slot != null) return false;
                if (target != null && slot == null) return false;
                if (target != null && slot != null) if (!target.isInstance(slot)) return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventory) {
        return output.copy();
    }

    @Override
    public int getRecipeSize() {
        return this.width * this.height;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return output.copy();
    }

    @Override
    public ItemStack[] getInput() {
        ItemStack[] stacks = new ItemStack[info.length];
        for (int i = 0; i < stacks.length; i++) {
            if (info[i] != null) stacks[i] = info[i].getItemStack();
        }
        return stacks;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

}
