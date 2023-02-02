package com.creativemd.creativecore.common.gui.controls;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.IIcon;

import com.creativemd.creativecore.client.rendering.RenderHelper2D;

public abstract class GuiImageButton extends GuiButton {

    public GuiImageButton(String caption, int x, int y, int width, int height, int id) {
        super(caption, x, y, width, height, id);
    }

    public abstract IIcon getIcon();

    @Override
    public void drawControl(FontRenderer renderer) {
        super.drawControl(renderer);
        IIcon icon = getIcon();
        RenderHelper2D.renderIcon(icon, width / 2 - 8, height / 2 - 8, 1, true, rotation, 16, 16);
    }
}
