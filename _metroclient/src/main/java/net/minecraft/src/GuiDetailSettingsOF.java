package net.minecraft.src;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;

public class GuiDetailSettingsOF extends GuiScreen {
      private GuiScreen prevScreen;
      protected String title;
      private GameSettings settings;
      private static GameSettings.Options[] enumOptions;
      private TooltipManager tooltipManager = new TooltipManager(this, new TooltipProviderOptions());

      public GuiDetailSettingsOF(GuiScreen guiscreen, GameSettings gamesettings) {
            this.prevScreen = guiscreen;
            this.settings = gamesettings;
      }

      public void initGui() {
            this.title = I18n.format("of.options.detailsTitle");
            super.buttonList.clear();

            for(int i = 0; i < enumOptions.length; ++i) {
                  GameSettings.Options opt = enumOptions[i];
                  int x = super.width / 2 - 155 + i % 2 * 160;
                  int y = super.height / 6 + 21 * (i / 2) - 12;
                  if (!opt.getEnumFloat()) {
                        super.buttonList.add(new GuiOptionButtonOF(opt.returnEnumOrdinal(), x, y, opt, this.settings.getKeyBinding(opt)));
                  } else {
                        super.buttonList.add(new GuiOptionSliderOF(opt.returnEnumOrdinal(), x, y, opt));
                  }
            }

            super.buttonList.add(new GuiButton(200, super.width / 2 - 100, super.height / 6 + 168 + 11, I18n.format("gui.done")));
      }

      protected void actionPerformed(GuiButton guibutton) {
            if (guibutton.enabled) {
                  if (guibutton.id < 200 && guibutton instanceof GuiOptionButton) {
                        this.settings.setOptionValue(((GuiOptionButton)guibutton).returnEnumOptions(), 1);
                        guibutton.displayString = this.settings.getKeyBinding(GameSettings.Options.getEnumOptions(guibutton.id));
                  }

                  if (guibutton.id == 200) {
                        super.mc.gameSettings.saveOptions();
                        super.mc.displayGuiScreen(this.prevScreen);
                  }

            }
      }

      public void drawScreen(int x, int y, float f) {
            this.drawDefaultBackground();
            this.drawCenteredString(super.fontRendererObj, this.title, super.width / 2, 15, 16777215);
            super.drawScreen(x, y, f);
            this.tooltipManager.drawTooltips(x, y, super.buttonList);
      }

      static {
            enumOptions = new GameSettings.Options[]{GameSettings.Options.CLOUDS, GameSettings.Options.CLOUD_HEIGHT, GameSettings.Options.TREES, GameSettings.Options.GRASS, GameSettings.Options.WATER, GameSettings.Options.RAIN, GameSettings.Options.SKY, GameSettings.Options.STARS, GameSettings.Options.SUN_MOON, GameSettings.Options.SHOW_CAPES, GameSettings.Options.FOG_FANCY, GameSettings.Options.FOG_START, GameSettings.Options.DEPTH_FOG, GameSettings.Options.HELD_ITEM_TOOLTIPS, GameSettings.Options.TRANSLUCENT_BLOCKS, GameSettings.Options.DROPPED_ITEMS, GameSettings.Options.VIGNETTE};
      }
}
