package shadersmod.client;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.src.Config;
import net.minecraft.src.Lang;
import net.minecraft.src.TooltipManager;
import net.minecraft.src.TooltipProviderEnumShaderOptions;
import org.lwjgl.Sys;

public class GuiShaders extends GuiScreen {
      protected GuiScreen parentGui;
      protected String screenTitle = "Shaders";
      private TooltipManager tooltipManager = new TooltipManager(this, new TooltipProviderEnumShaderOptions());
      private int updateTimer = -1;
      private GuiSlotShaders shaderList;
      private boolean saved = false;
      private FontRenderer fontRendererObj;
      private static float[] QUALITY_MULTIPLIERS = new float[]{0.5F, 0.70710677F, 1.0F, 1.4142135F, 2.0F};
      private static String[] QUALITY_MULTIPLIER_NAMES = new String[]{"0.5x", "0.7x", "1x", "1.5x", "2x"};
      private static float[] HAND_DEPTH_VALUES = new float[]{0.0625F, 0.125F, 0.25F};
      private static String[] HAND_DEPTH_NAMES = new String[]{"0.5x", "1x", "2x"};
      public static final int EnumOS_UNKNOWN = 0;
      public static final int EnumOS_WINDOWS = 1;
      public static final int EnumOS_OSX = 2;
      public static final int EnumOS_SOLARIS = 3;
      public static final int EnumOS_LINUX = 4;

      public GuiShaders(GuiScreen par1GuiScreen, GameSettings par2GameSettings) {
            this.parentGui = par1GuiScreen;
      }

      public void initGui() {
            this.fontRendererObj = super.fontRendererObj;
            this.screenTitle = I18n.format("of.options.shadersTitle");
            if (Shaders.shadersConfig == null) {
                  Shaders.loadConfig();
            }

            int btnWidth = 120;
            int btnHeight = 20;
            int btnX = super.width - btnWidth - 10;
            int baseY = 30;
            int stepY = 20;
            int shaderListWidth = super.width - btnWidth - 20;
            this.shaderList = new GuiSlotShaders(this, shaderListWidth, super.height, baseY, super.height - 50, 16);
            this.shaderList.registerScrollButtons(7, 8);
            super.buttonList.add(new GuiButtonEnumShaderOption(EnumShaderOption.ANTIALIASING, btnX, 0 * stepY + baseY, btnWidth, btnHeight));
            super.buttonList.add(new GuiButtonEnumShaderOption(EnumShaderOption.NORMAL_MAP, btnX, 1 * stepY + baseY, btnWidth, btnHeight));
            super.buttonList.add(new GuiButtonEnumShaderOption(EnumShaderOption.SPECULAR_MAP, btnX, 2 * stepY + baseY, btnWidth, btnHeight));
            super.buttonList.add(new GuiButtonEnumShaderOption(EnumShaderOption.RENDER_RES_MUL, btnX, 3 * stepY + baseY, btnWidth, btnHeight));
            super.buttonList.add(new GuiButtonEnumShaderOption(EnumShaderOption.SHADOW_RES_MUL, btnX, 4 * stepY + baseY, btnWidth, btnHeight));
            super.buttonList.add(new GuiButtonEnumShaderOption(EnumShaderOption.HAND_DEPTH_MUL, btnX, 5 * stepY + baseY, btnWidth, btnHeight));
            super.buttonList.add(new GuiButtonEnumShaderOption(EnumShaderOption.OLD_HAND_LIGHT, btnX, 6 * stepY + baseY, btnWidth, btnHeight));
            super.buttonList.add(new GuiButtonEnumShaderOption(EnumShaderOption.OLD_LIGHTING, btnX, 7 * stepY + baseY, btnWidth, btnHeight));
            int btnFolderWidth = Math.min(150, shaderListWidth / 2 - 10);
            super.buttonList.add(new GuiButton(201, shaderListWidth / 4 - btnFolderWidth / 2, super.height - 25, btnFolderWidth, btnHeight, Lang.get("of.options.shaders.shadersFolder")));
            super.buttonList.add(new GuiButton(202, shaderListWidth / 4 * 3 - btnFolderWidth / 2, super.height - 25, btnFolderWidth, btnHeight, I18n.format("gui.done")));
            super.buttonList.add(new GuiButton(203, btnX, super.height - 25, btnWidth, btnHeight, Lang.get("of.options.shaders.shaderOptions")));
            this.updateButtons();
      }

      public void updateButtons() {
            boolean shaderActive = Config.isShaders();
            Iterator it = super.buttonList.iterator();

            while(it.hasNext()) {
                  GuiButton button = (GuiButton)it.next();
                  if (button.id != 201 && button.id != 202 && button.id != EnumShaderOption.ANTIALIASING.ordinal()) {
                        button.enabled = shaderActive;
                  }
            }

      }

      public void handleMouseInput() {
            super.handleMouseInput();
      }

      protected void actionPerformed(GuiButton button) {
            if (button.enabled) {
                  if (button instanceof GuiButtonEnumShaderOption) {
                        GuiButtonEnumShaderOption gbeso = (GuiButtonEnumShaderOption)button;
                        String[] names;
                        int index;
                        float val;
                        float[] values;
                        switch(gbeso.getEnumShaderOption()) {
                        case ANTIALIASING:
                              Shaders.nextAntialiasingLevel();
                              Shaders.uninit();
                              break;
                        case NORMAL_MAP:
                              Shaders.configNormalMap = !Shaders.configNormalMap;
                              Shaders.uninit();
                              super.mc.scheduleResourcesRefresh();
                              break;
                        case SPECULAR_MAP:
                              Shaders.configSpecularMap = !Shaders.configSpecularMap;
                              Shaders.uninit();
                              super.mc.scheduleResourcesRefresh();
                              break;
                        case RENDER_RES_MUL:
                              val = Shaders.configRenderResMul;
                              values = QUALITY_MULTIPLIERS;
                              names = QUALITY_MULTIPLIER_NAMES;
                              index = getValueIndex(val, values);
                              if (isShiftKeyDown()) {
                                    --index;
                                    if (index < 0) {
                                          index = values.length - 1;
                                    }
                              } else {
                                    ++index;
                                    if (index >= values.length) {
                                          index = 0;
                                    }
                              }

                              Shaders.configRenderResMul = values[index];
                              Shaders.uninit();
                              Shaders.scheduleResize();
                              break;
                        case SHADOW_RES_MUL:
                              val = Shaders.configShadowResMul;
                              values = QUALITY_MULTIPLIERS;
                              names = QUALITY_MULTIPLIER_NAMES;
                              index = getValueIndex(val, values);
                              if (isShiftKeyDown()) {
                                    --index;
                                    if (index < 0) {
                                          index = values.length - 1;
                                    }
                              } else {
                                    ++index;
                                    if (index >= values.length) {
                                          index = 0;
                                    }
                              }

                              Shaders.configShadowResMul = values[index];
                              Shaders.uninit();
                              Shaders.scheduleResizeShadow();
                              break;
                        case HAND_DEPTH_MUL:
                              val = Shaders.configHandDepthMul;
                              values = HAND_DEPTH_VALUES;
                              names = HAND_DEPTH_NAMES;
                              index = getValueIndex(val, values);
                              if (isShiftKeyDown()) {
                                    --index;
                                    if (index < 0) {
                                          index = values.length - 1;
                                    }
                              } else {
                                    ++index;
                                    if (index >= values.length) {
                                          index = 0;
                                    }
                              }

                              Shaders.configHandDepthMul = values[index];
                              Shaders.uninit();
                              break;
                        case OLD_HAND_LIGHT:
                              Shaders.configOldHandLight.nextValue();
                              Shaders.uninit();
                              break;
                        case OLD_LIGHTING:
                              Shaders.configOldLighting.nextValue();
                              Shaders.updateBlockLightLevel();
                              Shaders.uninit();
                              super.mc.scheduleResourcesRefresh();
                              break;
                        case TWEAK_BLOCK_DAMAGE:
                              Shaders.configTweakBlockDamage = !Shaders.configTweakBlockDamage;
                              break;
                        case CLOUD_SHADOW:
                              Shaders.configCloudShadow = !Shaders.configCloudShadow;
                              break;
                        case TEX_MIN_FIL_B:
                              Shaders.configTexMinFilB = (Shaders.configTexMinFilB + 1) % 3;
                              Shaders.configTexMinFilN = Shaders.configTexMinFilS = Shaders.configTexMinFilB;
                              button.displayString = "Tex Min: " + Shaders.texMinFilDesc[Shaders.configTexMinFilB];
                              ShadersTex.updateTextureMinMagFilter();
                              break;
                        case TEX_MAG_FIL_N:
                              Shaders.configTexMagFilN = (Shaders.configTexMagFilN + 1) % 2;
                              button.displayString = "Tex_n Mag: " + Shaders.texMagFilDesc[Shaders.configTexMagFilN];
                              ShadersTex.updateTextureMinMagFilter();
                              break;
                        case TEX_MAG_FIL_S:
                              Shaders.configTexMagFilS = (Shaders.configTexMagFilS + 1) % 2;
                              button.displayString = "Tex_s Mag: " + Shaders.texMagFilDesc[Shaders.configTexMagFilS];
                              ShadersTex.updateTextureMinMagFilter();
                              break;
                        case SHADOW_CLIP_FRUSTRUM:
                              Shaders.configShadowClipFrustrum = !Shaders.configShadowClipFrustrum;
                              button.displayString = "ShadowClipFrustrum: " + toStringOnOff(Shaders.configShadowClipFrustrum);
                              ShadersTex.updateTextureMinMagFilter();
                        }

                        gbeso.updateButtonText();
                  } else {
                        switch(button.id) {
                        case 201:
                              switch(getOSType()) {
                              case 1:
                                    String var2 = String.format("cmd.exe /C start \"Open file\" \"%s\"", Shaders.shaderpacksdir.getAbsolutePath());

                                    try {
                                          Runtime.getRuntime().exec(var2);
                                          return;
                                    } catch (IOException var8) {
                                          var8.printStackTrace();
                                          break;
                                    }
                              case 2:
                                    try {
                                          Runtime.getRuntime().exec(new String[]{"/usr/bin/open", Shaders.shaderpacksdir.getAbsolutePath()});
                                          return;
                                    } catch (IOException var9) {
                                          var9.printStackTrace();
                                    }
                              }

                              boolean var8 = false;

                              try {
                                    Class var3 = Class.forName("java.awt.Desktop");
                                    Object var4 = var3.getMethod("getDesktop").invoke((Object)null);
                                    var3.getMethod("browse", URI.class).invoke(var4, (new File(super.mc.mcDataDir, Shaders.shaderpacksdirname)).toURI());
                              } catch (Throwable var7) {
                                    var7.printStackTrace();
                                    var8 = true;
                              }

                              if (var8) {
                                    Config.dbg("Opening via system class!");
                                    Sys.openURL("file://" + Shaders.shaderpacksdir.getAbsolutePath());
                              }
                              break;
                        case 202:
                              new File(Shaders.shadersdir, "current.cfg");
                              Shaders.storeConfig();
                              this.saved = true;
                              super.mc.displayGuiScreen(this.parentGui);
                              break;
                        case 203:
                              GuiShaderOptions gui = new GuiShaderOptions(this, Config.getGameSettings());
                              Config.getMinecraft().displayGuiScreen(gui);
                              break;
                        default:
                              this.shaderList.actionPerformed(button);
                        }

                  }
            }
      }

      public void onGuiClosed() {
            super.onGuiClosed();
            if (!this.saved) {
                  Shaders.storeConfig();
            }

      }

      public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            this.drawDefaultBackground();
            this.shaderList.drawScreen(mouseX, mouseY, partialTicks);
            if (this.updateTimer <= 0) {
                  this.shaderList.updateList();
                  this.updateTimer += 20;
            }

            this.drawCenteredString(this.fontRendererObj, this.screenTitle + " ", super.width / 2, 15, 16777215);
            String info = "OpenGL: " + Shaders.glVersionString + ", " + Shaders.glVendorString + ", " + Shaders.glRendererString;
            int infoWidth = this.fontRendererObj.getStringWidth(info);
            if (infoWidth < super.width - 5) {
                  this.drawCenteredString(this.fontRendererObj, info, super.width / 2, super.height - 40, 8421504);
            } else {
                  this.drawString(this.fontRendererObj, info, 5, super.height - 40, 8421504);
            }

            super.drawScreen(mouseX, mouseY, partialTicks);
            this.tooltipManager.drawTooltips(mouseX, mouseY, super.buttonList);
      }

      public void updateScreen() {
            super.updateScreen();
            --this.updateTimer;
      }

      public Minecraft getMc() {
            return super.mc;
      }

      public void drawCenteredString(String text, int x, int y, int color) {
            this.drawCenteredString(this.fontRendererObj, text, x, y, color);
      }

      public static String toStringOnOff(boolean value) {
            String on = Lang.getOn();
            String off = Lang.getOff();
            return value ? on : off;
      }

      public static String toStringAa(int value) {
            if (value == 2) {
                  return "FXAA 2x";
            } else {
                  return value == 4 ? "FXAA 4x" : Lang.getOff();
            }
      }

      public static String toStringValue(float val, float[] values, String[] names) {
            int index = getValueIndex(val, values);
            return names[index];
      }

      public static int getValueIndex(float val, float[] values) {
            for(int i = 0; i < values.length; ++i) {
                  float value = values[i];
                  if (value >= val) {
                        return i;
                  }
            }

            return values.length - 1;
      }

      public static String toStringQuality(float val) {
            return toStringValue(val, QUALITY_MULTIPLIERS, QUALITY_MULTIPLIER_NAMES);
      }

      public static String toStringHandDepth(float val) {
            return toStringValue(val, HAND_DEPTH_VALUES, HAND_DEPTH_NAMES);
      }

      public static int getOSType() {
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("win")) {
                  return 1;
            } else if (osName.contains("mac")) {
                  return 2;
            } else if (osName.contains("solaris")) {
                  return 3;
            } else if (osName.contains("sunos")) {
                  return 3;
            } else if (osName.contains("linux")) {
                  return 4;
            } else {
                  return osName.contains("unix") ? 4 : 0;
            }
      }
}
