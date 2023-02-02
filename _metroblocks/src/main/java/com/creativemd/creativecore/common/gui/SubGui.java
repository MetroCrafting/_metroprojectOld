package com.creativemd.creativecore.common.gui;

import java.util.ArrayList;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector4d;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import com.creativemd.creativecore.client.rendering.RenderHelper2D;
import com.creativemd.creativecore.common.container.SubContainer;
import com.creativemd.creativecore.common.event.CreativeCoreEventBus;
import com.creativemd.creativecore.common.gui.controls.GuiControl;
import com.creativemd.creativecore.common.gui.event.ControlClickEvent;
import com.creativemd.creativecore.common.gui.event.GuiControlEvent;
import com.creativemd.creativecore.common.gui.event.GuiToolTipEvent;
import com.creativemd.creativecore.common.gui.premade.SubGuiDialog;
import com.creativemd.creativecore.common.packet.GuiControlPacket;
import com.creativemd.creativecore.common.packet.GuiLayerPacket;
import com.creativemd.creativecore.common.packet.PacketHandler;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class SubGui {

    public static Minecraft mc = Minecraft.getMinecraft();

    public static RenderItem itemRender;

    private CreativeCoreEventBus eventBus;

    public GuiContainerSub gui;
    public SubContainer container;

    public int width;
    public int height;

    public SubGui() {
        this(176, 166);
    }

    public SubGui(int width, int height) {
        this.width = width;
        this.height = height;
        eventBus = new CreativeCoreEventBus();
        eventBus.RegisterEventListener(this);
    }

    // ================LAYERS================

    public boolean isTopLayer() {
        if (gui == null) return false;
        return gui.getTopLayer() == this;
    }

    public int getLayerID() {
        return gui.layers.indexOf(this);
    }

    public void openNewLayer(NBTTagCompound nbt) {
        openNewLayer(nbt, false);
    }

    public void openNewLayer(NBTTagCompound nbt, boolean isPacket) {
        gui.addLayer(createLayer(mc.theWorld, mc.thePlayer, nbt));
        if (!isPacket) PacketHandler.sendPacketToServer(new GuiLayerPacket(nbt, getLayerID(), false));
    }

    public void closeLayer(NBTTagCompound nbt) {
        closeLayer(nbt, false);
    }

    public void closeLayer(NBTTagCompound nbt, boolean isPacket) {
        onGuiClose();
        if (!isPacket) PacketHandler.sendPacketToServer(new GuiLayerPacket(nbt, getLayerID(), true));
        gui.removeLayer(this);
        if (gui.hasTopLayer()) gui.getTopLayer().onLayerClosed(this, nbt);
    }

    public void onLayerClosed(SubGui gui, NBTTagCompound nbt) {
        if (nbt.getBoolean("dialog")) {
            String[] buttons = new String[nbt.getInteger("count")];
            for (int i = 0; i < buttons.length; i++) {
                buttons[i] = nbt.getString("b" + i);
            }
            onDialogClosed(nbt.getString("text"), buttons, nbt.getString("clicked"));
        }
    }

    public void closeGui() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setBoolean("exit", true);
        closeLayer(nbt);
        if (gui.layers.size() == 0) mc.thePlayer.closeScreen();
    }

    public SubGui createLayer(World world, EntityPlayer player, NBTTagCompound nbt) {
        SubGui layer = createLayerFromPacket(world, player, nbt);
        layer.container = container.createLayerFromPacket(world, player, nbt);
        layer.gui = gui;
        layer.initGui();
        return layer;
    }

    public SubGui createLayerFromPacket(World world, EntityPlayer player, NBTTagCompound nbt) {
        if (nbt.getBoolean("dialog")) {
            String[] buttons = new String[nbt.getInteger("count")];
            for (int i = 0; i < buttons.length; i++) {
                buttons[i] = nbt.getString("b" + i);
            }
            return new SubGuiDialog(nbt.getString("text"), buttons);
        }
        return null;
    }

    // ================DIALOGS================

    public void openYesNoDialog(String text) {
        openButtonDialogDialog(text, "Yes", "No");
    }

    public void openButtonDialogDialog(String text, String... buttons) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setBoolean("dialog", true);
        nbt.setString("text", text);
        nbt.setInteger("count", buttons.length);
        for (int i = 0; i < buttons.length; i++) {
            nbt.setString("b" + i, buttons[i]);
        }
        openNewLayer(nbt);
    }

    public void openSaveDialog(String text) {
        openButtonDialogDialog(text, "Yes", "No", "Cancel");
    }

    /*
     * public void openSelectItemInfo() { WIP }
     */

    public void onDialogClosed(String text, String[] buttons, String clicked) {

    }

    // ================CONTROLS================

    public ArrayList<GuiControl> controls = new ArrayList<GuiControl>();

    public void initGui() {
        createControls();
        for (int i = 0; i < controls.size(); i++) {
            controls.get(i).parent = this;
            controls.get(i).init();
        }

        if (container != null) {
            for (int i = 0; i < container.controls.size(); i++) {
                container.controls.get(i).init();
                controls.add(container.controls.get(i).guiControl);
            }
        }
        refreshControls();
    }

    public void removeControl(GuiControl control) {
        controls.remove(control);
    }

    public void refreshControls() {
        for (int i = 0; i < controls.size(); i++) {
            controls.get(i).parent = this;
            controls.get(i).resetID();
            controls.get(i).setID(i);
        }
    }

    public GuiControl getControl(String name) {
        for (int i = 0; i < controls.size(); i++) {
            if (controls.get(i).name.equalsIgnoreCase(name)) return controls.get(i);
        }
        return null;
    }

    public boolean hasControl(String name) {
        for (int i = 0; i < controls.size(); i++) {
            if (controls.get(i).name.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    public void removeControls(String... except) {
        int i = 0;
        while (i < controls.size()) {
            boolean isException = false;
            for (int j = 0; j < except.length; j++) if (controls.get(i).is(except[j])) {
                isException = true;
                break;
            }
            if (!isException) controls.remove(i);
            else i++;
        }
    }

    public void removeControls() {
        controls.clear();
    }

    public abstract void createControls();

    // ================CUSTOM EVENTS================

    public void onGuiClose() {
        for (int i = 0; i < controls.size(); i++) {
            controls.get(i).onGuiClose();
        }
        eventBus.removeAllEventListeners();
    }

    // ================EVENTS================

    public boolean raiseEvent(GuiControlEvent event) {
        return !eventBus.raiseEvent(event);
    }

    public void addListener(Object listener) {
        eventBus.RegisterEventListener(listener);
    }

    public void removeListener(Object listener) {
        eventBus.removeEventListener(listener);
    }

    // ================SORTING================

    public void moveControlBehind(GuiControl control, GuiControl controlInBack) {
        if (controls.contains(controlInBack) && controls.remove(control)
                && controls.indexOf(controlInBack) + 1 < controls.size())
            controls.add(controls.indexOf(controlInBack) + 1, control);
        else moveControlToBottom(control);
        refreshControls();
    }

    public void moveControlAbove(GuiControl control, GuiControl controlInFront) {
        if (controls.contains(controlInFront) && controls.remove(control))
            controls.add(controls.indexOf(controlInFront), control);
        refreshControls();
    }

    public void moveControlToTop(GuiControl control) {
        if (controls.remove(control)) controls.add(1, control);
        refreshControls();
    }

    public void moveControlToBottom(GuiControl control) {
        if (controls.remove(control)) controls.add(control);
        refreshControls();
    }

    // ================NETWORK================

    public void readFromOpeningNBT(NBTTagCompound nbt) {}

    public void readFromNBT(NBTTagCompound nbt) {}

    public void sendPacketToServer(int controlID, NBTTagCompound nbt) {
        PacketHandler.sendPacketToServer(new GuiControlPacket(getLayerID(), controlID, nbt));
    }

    // ================MOUSE/KEYBOARD================

    public void mouseMove(int posX, int posY, int button) {
        Vector2d mouse = getMousePos();
        for (int i = 0; i < controls.size(); i++) {
            Vector2d pos = controls.get(i).getValidPos((int) mouse.x, (int) mouse.y);
            if (controls.get(i).isInteractable()) controls.get(i).mouseMove((int) pos.x, (int) pos.y, button);
        }
    }

    public void mouseReleased(int posX, int posY, int button) {
        Vector2d mouse = getMousePos();
        for (int i = 0; i < controls.size(); i++) {
            Vector2d pos = controls.get(i).getValidPos((int) mouse.x, (int) mouse.y);
            if (controls.get(i).isInteractable()) controls.get(i).mouseReleased((int) pos.x, (int) pos.y, button);
        }
    }

    public void mousePressed(int posX, int posY, int button) {
        Vector2d mouse = getMousePos();
        for (int i = 0; i < controls.size(); i++) {
            GuiControl control = controls.get(i);
            Vector2d pos = control.getValidPos((int) mouse.x, (int) mouse.y);
            if (control.isInteractable()) {
                if (control.isMouseOver((int) pos.x, (int) pos.y)
                        && control.mousePressed((int) pos.x, (int) pos.y, button)) {
                    control.raiseEvent(new ControlClickEvent(control, (int) pos.x, (int) pos.y));
                    return;
                } else {
                    control.onLoseFocus();
                }
            }
        }
    }

    public void mouseDragged(int posX, int posY, int button, long time) {
        Vector2d mouse = getMousePos();
        for (int i = 0; i < controls.size(); i++) {
            Vector2d pos = controls.get(i).getValidPos((int) mouse.x, (int) mouse.y);
            if (controls.get(i).isInteractable() && controls.get(i).isMouseOver((int) pos.x, (int) pos.y)
                    && controls.get(i).mouseDragged((int) pos.x, (int) pos.y, button, time)) {
                return;
            }
        }
    }

    public boolean mouseScrolled(int posX, int posY, int scrolled) {
        Vector2d mouse = getMousePos();
        for (int i = 0; i < controls.size(); i++) {
            Vector2d pos = controls.get(i).getValidPos((int) mouse.x, (int) mouse.y);
            if (controls.get(i).isInteractable() && controls.get(i).isMouseOver((int) pos.x, (int) pos.y)
                    && controls.get(i).mouseScrolled((int) pos.x, (int) pos.y, scrolled > 0 ? 1 : -1))
                return true;
        }
        return false;
    }

    public boolean keyTyped(char character, int key) {
        for (int i = 0; i < controls.size(); i++) {
            if (controls.get(i).isInteractable() && controls.get(i).onKeyPressed(character, key)) return true;
        }
        if (key == 1 || key == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
            closeGui();
            return true;
        }
        return false;
    }

    // ================RENDER================

    public Vector2d getMousePos() {
        return GuiControl.getMousePos(width, height);
    }

    public abstract void drawOverlay(FontRenderer fontRenderer);

    public void renderControls(FontRenderer fontRenderer) {
        for (int i = controls.size() - 1; i >= 0; i--) {
            GuiControl control = controls.get(i);
            if (control.visible && control.posY + control.height >= 0 && control.posY <= height)
                control.renderControl(fontRenderer, 0);
        }
    }

    public void renderTooltip(FontRenderer fontRenderer) {
        Vector2d mouse = getMousePos();
        for (int i = 0; i < controls.size(); i++) {
            Vector2d pos = controls.get(i).getValidPos((int) mouse.x, (int) mouse.y);
            if (controls.get(i).visible && controls.get(i).isMouseOver((int) pos.x, (int) pos.y)) {
                ArrayList<String> tooltip = controls.get(i).getTooltip();
                if (raiseEvent(new GuiToolTipEvent(tooltip, controls.get(i))))
                    RenderHelper2D.drawHoveringText(tooltip, (int) mouse.x, (int) mouse.y, fontRenderer, width, height);
                return;
            }
        }
    }

    public void onTick() {}

    public void drawForeground(FontRenderer fontRenderer) {
        onTick();

        renderControls(fontRenderer);

        this.drawOverlay(fontRenderer);

        if (isTopLayer()) renderTooltip(fontRenderer);
    }

    public void drawBackground() {
        int k = (this.width - this.width) / 2;
        int l = (this.height - this.height) / 2;

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(GuiContainerSub.background);
        final int frameSX = 176;
        final int frameSY = 166;
        this.drawTexturedModalRect(k, l, 0, 0, 6, 6);
        this.drawTexturedModalRect(k + this.width - 6, l, frameSX - 6, 0, 6, 6);
        this.drawTexturedModalRect(k, l + this.height - 6, 0, frameSY - 6, 6, 6);
        this.drawTexturedModalRect(k + this.width - 6, l + this.height - 6, frameSX - 6, frameSY - 6, 6, 6);

        float sizeX = (frameSX - 12);
        float amountX = (float) (width - 12) / sizeX;

        int i = 0;
        while (amountX > 0) {
            float percent = 1;
            if (amountX < 1) percent = amountX;
            this.drawTexturedModalRect(k + 6 + (int) (i * sizeX), l, 6, 0, (int) Math.ceil(sizeX * percent), 6);
            this.drawTexturedModalRect(
                    k + 6 + (int) (i * sizeX),
                    l + this.height - 6,
                    6,
                    frameSY - 6,
                    (int) Math.ceil(sizeX * percent),
                    6);
            amountX--;
            i++;
        }

        float sizeY = (frameSY - 12);
        float amountY = (float) (height - 12) / sizeY;
        i = 0;
        while (amountY > 0) {
            float percent = 1;
            if (amountY < 1) percent = amountY;
            this.drawTexturedModalRect(k, l + 6 + (int) (i * sizeY), 0, 6, 6, (int) Math.ceil(sizeY * percent));
            this.drawTexturedModalRect(
                    k + this.width - 6,
                    l + 6 + (int) (i * sizeY),
                    frameSX - 6,
                    6,
                    6,
                    (int) Math.ceil(sizeY * percent));
            amountY--;
            i++;
        }

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glColorMask(true, true, true, false);
        Vector4d color = new Vector4d(198, 198, 198, 255);
        RenderHelper2D.drawGradientRect(k + 6, l + 6, k + this.width - 6, l + this.height - 6, color, color);
        GL11.glColorMask(true, true, true, true);
    }

    public static void drawTexturedModalRect(int x, int y, int u, int v, int width, int height) {
        int zLevel = 0;
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(
                (double) (x + 0),
                (double) (y + height),
                (double) zLevel,
                (double) ((float) (u + 0) * f),
                (double) ((float) (v + height) * f1));
        tessellator.addVertexWithUV(
                (double) (x + width),
                (double) (y + height),
                (double) zLevel,
                (double) ((float) (u + width) * f),
                (double) ((float) (v + height) * f1));
        tessellator.addVertexWithUV(
                (double) (x + width),
                (double) (y + 0),
                (double) zLevel,
                (double) ((float) (u + width) * f),
                (double) ((float) (v + 0) * f1));
        tessellator.addVertexWithUV(
                (double) (x + 0),
                (double) (y + 0),
                (double) zLevel,
                (double) ((float) (u + 0) * f),
                (double) ((float) (v + 0) * f1));
        tessellator.draw();
    }
}
