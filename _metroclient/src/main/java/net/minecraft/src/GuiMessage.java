package net.minecraft.src;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

public class GuiMessage extends GuiScreen {
      private GuiScreen parentScreen;
      private String messageLine1;
      private String messageLine2;
      private final List listLines2 = Lists.newArrayList();
      protected String confirmButtonText;
      private int ticksUntilEnable;

      public GuiMessage(GuiScreen parentScreen, String line1, String line2) {
            this.parentScreen = parentScreen;
            this.messageLine1 = line1;
            this.messageLine2 = line2;
            this.confirmButtonText = I18n.format("gui.done");
      }

      public void initGui() {
            super.buttonList.add(new GuiOptionButton(0, super.width / 2 - 74, super.height / 6 + 96, this.confirmButtonText));
            this.listLines2.clear();
            this.listLines2.addAll(super.fontRendererObj.listFormattedStringToWidth(this.messageLine2, super.width - 50));
      }

      protected void actionPerformed(GuiButton button) {
            Config.getMinecraft().displayGuiScreen(this.parentScreen);
      }

      public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            this.drawDefaultBackground();
            this.drawCenteredString(super.fontRendererObj, this.messageLine1, super.width / 2, 70, 16777215);
            int var4 = 90;

            for(Iterator var5 = this.listLines2.iterator(); var5.hasNext(); var4 += super.fontRendererObj.FONT_HEIGHT) {
                  String var6 = (String)var5.next();
                  this.drawCenteredString(super.fontRendererObj, var6, super.width / 2, var4, 16777215);
            }

            super.drawScreen(mouseX, mouseY, partialTicks);
      }

      public void setButtonDelay(int ticksUntilEnable) {
            this.ticksUntilEnable = ticksUntilEnable;

            GuiButton var3;
            for(Iterator var2 = super.buttonList.iterator(); var2.hasNext(); var3.enabled = false) {
                  var3 = (GuiButton)var2.next();
            }

      }

      public void updateScreen() {
            super.updateScreen();
            GuiButton var2;
            if (--this.ticksUntilEnable == 0) {
                  for(Iterator var1 = super.buttonList.iterator(); var1.hasNext(); var2.enabled = true) {
                        var2 = (GuiButton)var1.next();
                  }
            }

      }
}
