package net.minecraft.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.src.Config;
import net.minecraft.src.GuiAnimationSettingsOF;
import net.minecraft.src.GuiDetailSettingsOF;
import net.minecraft.src.GuiOptionButtonOF;
import net.minecraft.src.GuiOptionSliderOF;
import net.minecraft.src.GuiOtherSettingsOF;
import net.minecraft.src.GuiPerformanceSettingsOF;
import net.minecraft.src.GuiQualitySettingsOF;
import net.minecraft.src.GuiScreenOF;
import net.minecraft.src.Lang;
import net.minecraft.src.TooltipManager;
import net.minecraft.src.TooltipProviderOptions;
import shadersmod.client.GuiShaders;

@SideOnly(Side.CLIENT)
public class GuiVideoSettings extends GuiScreenOF
{
    private GuiScreen parentGuiScreen;
    protected String screenTitle = "Video Settings";
    private GameSettings guiGameSettings;
    private GuiListExtended optionsRowList;
    /** An array of all of GameSettings.Options's video options. */
    private static final GameSettings.Options[] videoOptions;
    private static final String __OBFID = "CL_00000718";
    private TooltipManager tooltipManager = new TooltipManager(this, new TooltipProviderOptions());
    private FontRenderer fontRendererObj;

    public GuiVideoSettings(GuiScreen p_i1062_1_, GameSettings p_i1062_2_)
    {
        this.parentGuiScreen = p_i1062_1_;
        this.guiGameSettings = p_i1062_2_;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui() {
            this.fontRendererObj = super.fontRendererObj;
            this.screenTitle = I18n.format("options.videoTitle");
            super.buttonList.clear();

            int i;
            for(i = 0; i < videoOptions.length; ++i) {
                  GameSettings.Options opt = videoOptions[i];
                  if (opt != null) {
                        int x = super.width / 2 - 155 + i % 2 * 160;
                        int y = super.height / 6 + 21 * (i / 2) - 12;
                        if (opt.getEnumFloat()) {
                              super.buttonList.add(new GuiOptionSliderOF(opt.returnEnumOrdinal(), x, y, opt));
                        } else {
                              super.buttonList.add(new GuiOptionButtonOF(opt.returnEnumOrdinal(), x, y, opt, this.guiGameSettings.getKeyBinding(opt)));
                        }
                  }
            }

            i = super.height / 6 + 21 * (videoOptions.length / 2) - 12;
            int x = super.width / 2 - 155 + 0;
            super.buttonList.add(new GuiOptionButton(231, x, i, Lang.get("of.options.shaders")));
            x = super.width / 2 - 155 + 160;
            super.buttonList.add(new GuiOptionButton(202, x, i, Lang.get("of.options.quality")));
            i += 21;
            x = super.width / 2 - 155 + 0;
            super.buttonList.add(new GuiOptionButton(201, x, i, Lang.get("of.options.details")));
            x = super.width / 2 - 155 + 160;
            super.buttonList.add(new GuiOptionButton(212, x, i, Lang.get("of.options.performance")));
            i += 21;
            x = super.width / 2 - 155 + 0;
            super.buttonList.add(new GuiOptionButton(211, x, i, Lang.get("of.options.animations")));
            x = super.width / 2 - 155 + 160;
            super.buttonList.add(new GuiOptionButton(222, x, i, Lang.get("of.options.other")));
            i += 21;
            super.buttonList.add(new GuiButton(200, super.width / 2 - 100, super.height / 6 + 168 + 11, I18n.format("gui.done")));
      }

    protected void actionPerformed(GuiButton button) {
            this.actionPerformed(button, 1);
      }
      
    protected void actionPerformedRightClick(GuiButton button) {
          if (button.id == GameSettings.Options.GUI_SCALE.ordinal()) {
                this.actionPerformed(button, -1);
          }

    }

    private void actionPerformed(GuiButton button, int val) {
          if (button.enabled) {
                int guiScale = this.guiGameSettings.guiScale;
                if (button.id < 200 && button instanceof GuiOptionButton) {
                      this.guiGameSettings.setOptionValue(((GuiOptionButton)button).returnEnumOptions(), val);
                      button.displayString = this.guiGameSettings.getKeyBinding(GameSettings.Options.getEnumOptions(button.id));
                }

                if (button.id == 200) {
                      super.mc.gameSettings.saveOptions();
                      super.mc.displayGuiScreen(this.parentGuiScreen);
                }

                if (this.guiGameSettings.guiScale != guiScale) {
                      ScaledResolution var3 = new ScaledResolution(super.mc, super.mc.displayWidth, super.mc.displayHeight);
                      int var4 = var3.getScaledWidth();
                      int var5 = var3.getScaledHeight();
                      this.setWorldAndResolution(super.mc, var4, var5);
                }

                if (button.id == 201) {
                      super.mc.gameSettings.saveOptions();
                      GuiDetailSettingsOF scr = new GuiDetailSettingsOF(this, this.guiGameSettings);
                      super.mc.displayGuiScreen(scr);
                }

                if (button.id == 202) {
                      super.mc.gameSettings.saveOptions();
                      GuiQualitySettingsOF scr = new GuiQualitySettingsOF(this, this.guiGameSettings);
                      super.mc.displayGuiScreen(scr);
                }

                if (button.id == 211) {
                      super.mc.gameSettings.saveOptions();
                      GuiAnimationSettingsOF scr = new GuiAnimationSettingsOF(this, this.guiGameSettings);
                      super.mc.displayGuiScreen(scr);
                }

                if (button.id == 212) {
                      super.mc.gameSettings.saveOptions();
                      GuiPerformanceSettingsOF scr = new GuiPerformanceSettingsOF(this, this.guiGameSettings);
                      super.mc.displayGuiScreen(scr);
                }

                if (button.id == 222) {
                      super.mc.gameSettings.saveOptions();
                      GuiOtherSettingsOF scr = new GuiOtherSettingsOF(this, this.guiGameSettings);
                      super.mc.displayGuiScreen(scr);
                }

                if (button.id == 231) {
                      if (Config.isAntialiasing() || Config.isAntialiasingConfigured()) {
                            Config.showGuiMessage(Lang.get("of.message.shaders.aa1"), Lang.get("of.message.shaders.aa2"));
                            return;
                      }

                      if (Config.isAnisotropicFiltering()) {
                            Config.showGuiMessage(Lang.get("of.message.shaders.af1"), Lang.get("of.message.shaders.af2"));
                            return;
                      }

                      if (Config.isFastRender()) {
                            Config.showGuiMessage(Lang.get("of.message.shaders.fr1"), Lang.get("of.message.shaders.fr2"));
                            return;
                      }

                      if (Config.getGameSettings().anaglyph) {
                            Config.showGuiMessage(Lang.get("of.message.shaders.an1"), Lang.get("of.message.shaders.an2"));
                            return;
                      }

                      super.mc.gameSettings.saveOptions();
                      GuiShaders scr = new GuiShaders(this, this.guiGameSettings);
                      super.mc.displayGuiScreen(scr);
                }

          }
    }

      
    /**
     * Called when the mouse is clicked.
     */
//    public void mouseClicked(int p_73864_1_, int p_73864_2_, int p_73864_3_)
//    {
//        int l = this.guiGameSettings.guiScale;
//        super.mouseClicked(p_73864_1_, p_73864_2_, p_73864_3_);
//        this.optionsRowList.func_148179_a(p_73864_1_, p_73864_2_, p_73864_3_);
//
//        if (this.guiGameSettings.guiScale != l)
 //       {
 //           ScaledResolution scaledresolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
//            int i1 = scaledresolution.getScaledWidth();
//            int j1 = scaledresolution.getScaledHeight();
//            this.setWorldAndResolution(this.mc, i1, j1);
//        }
//    }

    /**
     * Called when the mouse is moved or a mouse button is released.  Signature: (mouseX, mouseY, which) which==-1 is
     * mouseMove, which==0 or which==1 is mouseUp
     */
//    protected void mouseMovedOrUp(int p_146286_1_, int p_146286_2_, int p_146286_3_)
//    {
//        int l = this.guiGameSettings.guiScale;
//        super.mouseMovedOrUp(p_146286_1_, p_146286_2_, p_146286_3_);
//        this.optionsRowList.func_148181_b(p_146286_1_, p_146286_2_, p_146286_3_);
//
//        if (this.guiGameSettings.guiScale != l)
//        {
//            ScaledResolution scaledresolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
//            int i1 = scaledresolution.getScaledWidth();
//            int j1 = scaledresolution.getScaledHeight();
//            this.setWorldAndResolution(this.mc, i1, j1);
//        }
//    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int x, int y, float z) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.screenTitle, super.width / 2, 15, 16777215);
        String ver = Config.getVersion();
        String ed = "HD_U";
        if (ed.equals("HD")) {
              ver = "OptiFine HD E7";
        }

        if (ed.equals("HD_U")) {
              ver = "OptiFine HD E7 Ultra";
        }

        if (ed.equals("L")) {
              ver = "OptiFine E7 Light";
        }

        this.drawString(this.fontRendererObj, ver, 2, super.height - 10, 8421504);
        String verMc = "Minecraft 1.7.10";
        int lenMc = this.fontRendererObj.getStringWidth(verMc);
        this.drawString(this.fontRendererObj, verMc, super.width - lenMc - 2, super.height - 10, 8421504);
        super.drawScreen(x, y, z);
        this.tooltipManager.drawTooltips(x, y, super.buttonList);
    }
    
    public static float getButtonWidth(GuiButton btn) {
        return btn.width;
  }

  public static float getButtonHeight(GuiButton btn) {
        return btn.height;
  }

  public static void drawGradientRect(GuiScreen guiScreen, int left, int top, int right, int bottom, int startColor, int endColor) {
        guiScreen.drawGradientRect(left, top, right, bottom, startColor, endColor);
  }

  /* 
   * Панели из оптифайна/майнкрафта
   * */
  
  static {
        videoOptions = new GameSettings.Options[] {
        		GameSettings.Options.GRAPHICS,
        		GameSettings.Options.RENDER_DISTANCE,
        		GameSettings.Options.AMBIENT_OCCLUSION,
        		GameSettings.Options.FRAMERATE_LIMIT,
        		GameSettings.Options.AO_LEVEL,
        		GameSettings.Options.VIEW_BOBBING,
        		GameSettings.Options.GUI_SCALE,
        		GameSettings.Options.ADVANCED_OPENGL,
        		GameSettings.Options.GAMMA,
        		GameSettings.Options.CHUNK_LOADING,
        		GameSettings.Options.DYNAMIC_LIGHTS,
        		GameSettings.Options.DYNAMIC_FOV 
        		};
  		}
}