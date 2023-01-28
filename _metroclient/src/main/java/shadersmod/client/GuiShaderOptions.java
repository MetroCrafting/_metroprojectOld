package shadersmod.client;

import java.util.Iterator;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.src.Config;
import net.minecraft.src.GuiScreenOF;
import net.minecraft.src.Lang;
import net.minecraft.src.TooltipManager;
import net.minecraft.src.TooltipProviderShaderOptions;
import net.minecraft.util.MathHelper;

public class GuiShaderOptions extends GuiScreenOF {
      private GuiScreen prevScreen;
      protected String title;
      private GameSettings settings;
      private TooltipManager tooltipManager;
      private String screenName;
      private String screenText;
      private boolean changed;
      public static final String OPTION_PROFILE = "<profile>";
      public static final String OPTION_EMPTY = "<empty>";
      public static final String OPTION_REST = "*";
      private FontRenderer fontRendererObj;

      public GuiShaderOptions(GuiScreen guiscreen, GameSettings gamesettings) {
            this.tooltipManager = new TooltipManager(this, new TooltipProviderShaderOptions());
            this.screenName = null;
            this.screenText = null;
            this.changed = false;
            this.title = "Shader Options";
            this.prevScreen = guiscreen;
            this.settings = gamesettings;
      }

      public GuiShaderOptions(GuiScreen guiscreen, GameSettings gamesettings, String screenName) {
            this(guiscreen, gamesettings);
            this.screenName = screenName;
            if (screenName != null) {
                  this.screenText = Shaders.translate("screen." + screenName, screenName);
            }

      }

      public void initGui() {
            this.fontRendererObj = super.fontRendererObj;
            this.title = I18n.format("of.options.shaderOptionsTitle");
            int baseId = 100;
            int baseY = 30;
            int stepY = 20;
            int btnWidth = 120;
            int btnHeight = 20;
            int columns = Shaders.getShaderPackColumns(this.screenName, 2);
            ShaderOption[] ops = Shaders.getShaderPackOptions(this.screenName);
            if (ops != null) {
                  int colsMin = MathHelper.ceiling_double_int((double)ops.length / 9.0D);
                  if (columns < colsMin) {
                        columns = colsMin;
                  }

                  for(int i = 0; i < ops.length; ++i) {
                        ShaderOption so = ops[i];
                        if (so != null && so.isVisible()) {
                              int col = i % columns;
                              int row = i / columns;
                              int colWidth = Math.min(super.width / columns, 200);
                              int baseX = (super.width - colWidth * columns) / 2;
                              int x = col * colWidth + 5 + baseX;
                              int y = baseY + row * stepY;
                              int w = colWidth - 10;
                              String text = getButtonText(so, w);
                              GuiButtonShaderOption btn;
                              if (Shaders.isShaderPackOptionSlider(so.getName())) {
                                    btn = new GuiSliderShaderOption(baseId + i, x, y, w, btnHeight, so, text);
                              } else {
                                    btn = new GuiButtonShaderOption(baseId + i, x, y, w, btnHeight, so, text);
                              }

                              ((GuiButton)btn).enabled = so.isEnabled();
                              super.buttonList.add(btn);
                        }
                  }
            }

            super.buttonList.add(new GuiButton(201, super.width / 2 - btnWidth - 20, super.height / 6 + 168 + 11, btnWidth, btnHeight, I18n.format("controls.reset")));
            super.buttonList.add(new GuiButton(200, super.width / 2 + 20, super.height / 6 + 168 + 11, btnWidth, btnHeight, I18n.format("gui.done")));
      }

      public static String getButtonText(ShaderOption so, float f) {
            String labelName = so.getNameText();
            if (so instanceof ShaderOptionScreen) {
                  ShaderOptionScreen soScr = (ShaderOptionScreen)so;
                  return labelName + "...";
            } else {
                  FontRenderer fr = Config.getMinecraft().fontRenderer;

                  for(int lenSuffix = fr.getStringWidth(": " + Lang.getOff()) + 5; fr.getStringWidth(labelName) + lenSuffix >= f && labelName.length() > 0; labelName = labelName.substring(0, labelName.length() - 1)) {
                  }

                  String col = so.isChanged() ? so.getValueColor(so.getValue()) : "";
                  String labelValue = so.getValueText(so.getValue());
                  return labelName + ": " + col + labelValue;
            }
      }

      protected void actionPerformed(GuiButton guibutton) {
            if (guibutton.enabled) {
                  if (guibutton.id < 200 && guibutton instanceof GuiButtonShaderOption) {
                        GuiButtonShaderOption btnSo = (GuiButtonShaderOption)guibutton;
                        ShaderOption so = btnSo.getShaderOption();
                        if (so instanceof ShaderOptionScreen) {
                              String screenName = so.getName();
                              GuiShaderOptions scr = new GuiShaderOptions(this, this.settings, screenName);
                              super.mc.displayGuiScreen(scr);
                              return;
                        }

                        if (isShiftKeyDown()) {
                              so.resetValue();
                        } else {
                              so.nextValue();
                        }

                        this.updateAllButtons();
                        this.changed = true;
                  }

                  if (guibutton.id == 201) {
                        ShaderOption[] opts = Shaders.getChangedOptions(Shaders.getShaderPackOptions());

                        for(int i = 0; i < opts.length; ++i) {
                              ShaderOption opt = opts[i];
                              opt.resetValue();
                              this.changed = true;
                        }

                        this.updateAllButtons();
                  }

                  if (guibutton.id == 200) {
                        if (this.changed) {
                              Shaders.saveShaderPackOptions();
                              this.changed = false;
                              Shaders.uninit();
                        }

                        super.mc.displayGuiScreen(this.prevScreen);
                  }

            }
      }

      protected void actionPerformedRightClick(GuiButton btn) {
            if (btn instanceof GuiButtonShaderOption) {
                  GuiButtonShaderOption btnSo = (GuiButtonShaderOption)btn;
                  ShaderOption so = btnSo.getShaderOption();
                  if (isShiftKeyDown()) {
                        so.resetValue();
                  } else {
                        so.prevValue();
                  }

                  this.updateAllButtons();
                  this.changed = true;
            }

      }

      public void onGuiClosed() {
            super.onGuiClosed();
            if (this.changed) {
                  Shaders.saveShaderPackOptions();
                  this.changed = false;
                  Shaders.uninit();
            }

      }

      private void updateAllButtons() {
            Iterator it = super.buttonList.iterator();

            while(it.hasNext()) {
                  GuiButton btn = (GuiButton)it.next();
                  if (btn instanceof GuiButtonShaderOption) {
                        GuiButtonShaderOption gbso = (GuiButtonShaderOption)btn;
                        ShaderOption opt = gbso.getShaderOption();
                        if (opt instanceof ShaderOptionProfile) {
                              ShaderOptionProfile optProf = (ShaderOptionProfile)opt;
                              optProf.updateProfile();
                        }

                        gbso.displayString = getButtonText(opt, gbso.getButtonWidth());
                        gbso.valueChanged();
                  }
            }

      }

      public void drawScreen(int x, int y, float f) {
            this.drawDefaultBackground();
            if (this.screenText != null) {
                  this.drawCenteredString(this.fontRendererObj, this.screenText, super.width / 2, 15, 16777215);
            } else {
                  this.drawCenteredString(this.fontRendererObj, this.title, super.width / 2, 15, 16777215);
            }

            super.drawScreen(x, y, f);
            this.tooltipManager.drawTooltips(x, y, super.buttonList);
      }
}
