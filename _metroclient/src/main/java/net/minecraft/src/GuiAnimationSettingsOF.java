package net.minecraft.src;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;

public class GuiAnimationSettingsOF extends GuiScreen {
      private GuiScreen prevScreen;
      protected String title;
      private GameSettings settings;
      private static GameSettings.Options[] enumOptions;

      public GuiAnimationSettingsOF(GuiScreen guiscreen, GameSettings gamesettings) {
            this.prevScreen = guiscreen;
            this.settings = gamesettings;
      }

      public void initGui() {
            this.title = I18n.format("of.options.animationsTitle");
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

            super.buttonList.add(new GuiButton(210, super.width / 2 - 155, super.height / 6 + 168 + 11, 70, 20, Lang.get("of.options.animation.allOn")));
            super.buttonList.add(new GuiButton(211, super.width / 2 - 155 + 80, super.height / 6 + 168 + 11, 70, 20, Lang.get("of.options.animation.allOff")));
            super.buttonList.add(new GuiOptionButton(200, super.width / 2 + 5, super.height / 6 + 168 + 11, I18n.format("gui.done")));
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
                        super.mc.gameSettings.setAllAnimations(true);
                  }

                  if (guibutton.id == 211) {
                        super.mc.gameSettings.setAllAnimations(false);
                  }

                  ScaledResolution sr = new ScaledResolution(super.mc, super.mc.displayWidth, super.mc.displayHeight);
                  this.setWorldAndResolution(super.mc, sr.getScaledWidth(), sr.getScaledHeight());
            }
      }

      public void drawScreen(int x, int y, float f) {
            this.drawDefaultBackground();
            this.drawCenteredString(super.fontRendererObj, this.title, super.width / 2, 15, 16777215);
            super.drawScreen(x, y, f);
      }

      static {
            enumOptions = new GameSettings.Options[]{GameSettings.Options.ANIMATED_WATER, GameSettings.Options.ANIMATED_LAVA, GameSettings.Options.ANIMATED_FIRE, GameSettings.Options.ANIMATED_PORTAL, GameSettings.Options.ANIMATED_REDSTONE, GameSettings.Options.ANIMATED_EXPLOSION, GameSettings.Options.ANIMATED_FLAME, GameSettings.Options.ANIMATED_SMOKE, GameSettings.Options.VOID_PARTICLES, GameSettings.Options.WATER_PARTICLES, GameSettings.Options.RAIN_SPLASH, GameSettings.Options.PORTAL_PARTICLES, GameSettings.Options.POTION_PARTICLES, GameSettings.Options.DRIPPING_WATER_LAVA, GameSettings.Options.ANIMATED_TERRAIN, GameSettings.Options.ANIMATED_ITEMS, GameSettings.Options.ANIMATED_TEXTURES, GameSettings.Options.PARTICLES};
      }
}
