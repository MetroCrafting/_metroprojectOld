package net.minecraft.src;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;

public class GuiOtherSettingsOF extends GuiScreen implements GuiYesNoCallback {
      private GuiScreen prevScreen;
      protected String title;
      private GameSettings settings;
      private static GameSettings.Options[] enumOptions;
      private TooltipManager tooltipManager = new TooltipManager(this, new TooltipProviderOptions());

      public GuiOtherSettingsOF(GuiScreen guiscreen, GameSettings gamesettings) {
            this.prevScreen = guiscreen;
            this.settings = gamesettings;
      }

      public void initGui() {
            this.title = I18n.format("of.options.otherTitle");
            super.buttonList.clear();

            for(int i = 0; i < enumOptions.length; ++i) {
                  GameSettings.Options enumoptions = enumOptions[i];
                  int x = super.width / 2 - 155 + i % 2 * 160;
                  int y = super.height / 6 + 21 * (i / 2) - 12;
                  if (!enumoptions.getEnumFloat()) {
                        super.buttonList.add(new GuiOptionButtonOF(enumoptions.returnEnumOrdinal(), x, y, enumoptions, this.settings.getKeyBinding(enumoptions)));
                  } else {
                        super.buttonList.add(new GuiOptionSliderOF(enumoptions.returnEnumOrdinal(), x, y, enumoptions));
                  }
            }

            super.buttonList.add(new GuiButton(210, super.width / 2 - 100, super.height / 6 + 168 + 11 - 44, I18n.format("of.options.other.reset")));
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

                  if (guibutton.id == 210) {
                        super.mc.gameSettings.saveOptions();
                        GuiYesNo guiyesno = new GuiYesNo(this, I18n.format("of.message.other.reset"), "", 9999);
                        super.mc.displayGuiScreen(guiyesno);
                  }

            }
      }

      public void confirmClicked(boolean flag, int i) {
            if (flag) {
                  super.mc.gameSettings.resetSettings();
            }

            super.mc.displayGuiScreen(this);
      }

      public void drawScreen(int x, int y, float f) {
            this.drawDefaultBackground();
            this.drawCenteredString(super.fontRendererObj, this.title, super.width / 2, 15, 16777215);
            super.drawScreen(x, y, f);
            this.tooltipManager.drawTooltips(x, y, super.buttonList);
      }

      static {
            enumOptions = new GameSettings.Options[]{GameSettings.Options.LAGOMETER, GameSettings.Options.PROFILER, GameSettings.Options.SHOW_FPS, GameSettings.Options.AUTOSAVE_TICKS, GameSettings.Options.WEATHER, GameSettings.Options.TIME, GameSettings.Options.USE_FULLSCREEN, GameSettings.Options.FULLSCREEN_MODE, GameSettings.Options.ANAGLYPH};
      }
}
