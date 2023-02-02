package net.minecraft.client.settings;

import com.google.common.collect.Maps;
import com.google.gson.Gson;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.stream.TwitchStream;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer.EnumChatVisibility;
import net.minecraft.network.play.client.C15PacketClientSettings;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.src.ClearWater;
import net.minecraft.src.Config;
import net.minecraft.src.CustomColorizer;
import net.minecraft.src.CustomSky;
import net.minecraft.src.DynamicLights;
import net.minecraft.src.IWrUpdater;
import net.minecraft.src.Lang;
import net.minecraft.src.NaturalTextures;
import net.minecraft.src.RandomMobs;
import net.minecraft.src.TextureUtils;
import net.minecraft.src.WrUpdaterSmooth;
import net.minecraft.src.WrUpdaterThreaded;
import net.minecraft.src.WrUpdates;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import shadersmod.client.Shaders;

@SideOnly(Side.CLIENT)
public class GameSettings
{
    private static final Logger logger = LogManager.getLogger();
    private static final Gson gson = new Gson();
    private static final ParameterizedType typeListString = new ParameterizedType()
    {
        private static final String __OBFID = "CL_00000651";
        public Type[] getActualTypeArguments()
        {
            return new Type[] {String.class};
        }
        public Type getRawType()
        {
            return List.class;
        }
        public Type getOwnerType()
        {
            return null;
        }
    };
    /** GUI scale values */
    private static final String[] GUISCALES = new String[] {"options.guiScale.auto", "options.guiScale.small", "options.guiScale.normal", "options.guiScale.large"};
    private static final String[] PARTICLES = new String[] {"options.particles.all", "options.particles.decreased", "options.particles.minimal"};
    private static final String[] AMBIENT_OCCLUSIONS = new String[] {"options.ao.off", "options.ao.min", "options.ao.max"};
    private static final String[] field_152391_aS = new String[] {"options.stream.compression.low", "options.stream.compression.medium", "options.stream.compression.high"};
    private static final String[] field_152392_aT = new String[] {"options.stream.chat.enabled.streaming", "options.stream.chat.enabled.always", "options.stream.chat.enabled.never"};
    private static final String[] field_152393_aU = new String[] {"options.stream.chat.userFilter.all", "options.stream.chat.userFilter.subs", "options.stream.chat.userFilter.mods"};
    private static final String[] field_152394_aV = new String[] {"options.stream.mic_toggle.mute", "options.stream.mic_toggle.talk"};
    public float mouseSensitivity = 0.5F;
    public boolean invertMouse;
    public int renderDistanceChunks = -1;
    public boolean viewBobbing = true;
    public boolean anaglyph;
    /** Advanced OpenGL */
    public boolean advancedOpengl;
    public boolean fboEnable = true;
    public int limitFramerate = 120;
    public boolean fancyGraphics = true;
    /** Smooth Lighting */
    public int ambientOcclusion = 2;
    
    public int ofFogType = 1;
    public float ofFogStart = 0.8F;
    public int ofMipmapType = 0;
    public boolean ofLoadFar = false;
    public int ofPreloadedChunks = 0;
    public boolean ofOcclusionFancy = false;
    public boolean ofSmoothFps = false;
    public boolean ofSmoothWorld = Config.isSingleProcessor();
    public boolean ofLazyChunkLoading = Config.isSingleProcessor();
    public float ofAoLevel = 1.0F;
    public int ofAaLevel = 0;
    public int ofClouds = 0;
    public float ofCloudsHeight = 0.0F;
    public int ofTrees = 0;
    public int ofGrass = 0;
    public int ofRain = 0;
    public int ofWater = 0;
    public int ofDroppedItems = 0;
    public int ofBetterGrass = 3;
    public int ofAutoSaveTicks = 4000;
    public boolean ofLagometer = false;
    public boolean ofProfiler = false;
    public boolean ofShowFps = false;
    public boolean ofWeather = true;
    public boolean ofSky = true;
    public boolean ofStars = true;
    public boolean ofSunMoon = true;
    public int ofVignette = 0;
    public int ofChunkUpdates = 1;
    public int ofChunkLoading = 0;
    public boolean ofChunkUpdatesDynamic = false;
    public int ofTime = 0;
    public boolean ofClearWater = false;
    public boolean ofDepthFog = true;
    public boolean ofBetterSnow = false;
    public String ofFullscreenMode = "Default";
    public boolean ofSwampColors = true;
    public boolean ofRandomMobs = true;
    public boolean ofSmoothBiomes = true;
    public boolean ofCustomFonts = true;
    public boolean ofCustomColors = true;
    public boolean ofCustomSky = true;
    public boolean ofShowCapes = true;
    public int ofConnectedTextures = 2;
    public boolean ofNaturalTextures = false;
    public boolean ofFastMath = false;
    public boolean ofFastRender = false;
    public int ofTranslucentBlocks = 2;
    public boolean ofDynamicFov = true;
    public int ofDynamicLights = 3;
    public int ofAnimatedWater = 0;
    public int ofAnimatedLava = 0;
    public boolean ofAnimatedFire = true;
    public boolean ofAnimatedPortal = true;
    public boolean ofAnimatedRedstone = true;
    public boolean ofAnimatedExplosion = true;
    public boolean ofAnimatedFlame = true;
    public boolean ofAnimatedSmoke = true;
    public boolean ofVoidParticles = true;
    public boolean ofWaterParticles = true;
    public boolean ofRainSplash = true;
    public boolean ofPortalParticles = true;
    public boolean ofPotionParticles = true;
    public boolean ofDrippingWaterLava = true;
    public boolean ofAnimatedTerrain = true;
    public boolean ofAnimatedItems = true;
    public boolean ofAnimatedTextures = true;
    public static final int DEFAULT = 0;
    public static final int FAST = 1;
    public static final int FANCY = 2;
    public static final int OFF = 3;
    public static final int ANIM_ON = 0;
    public static final int ANIM_GENERATED = 1;
    public static final int ANIM_OFF = 2;
    public static final int CL_DEFAULT = 0;
    public static final int CL_SMOOTH = 1;
    public static final int CL_THREADED = 2;
    public static final String DEFAULT_STR = "Default";
    private static final int[] OF_DYNAMIC_LIGHTS = new int[]{3, 1, 2};
    private static final String[] KEYS_DYNAMIC_LIGHTS = new String[]{"options.off", "options.graphics.fast", "options.graphics.fancy"};
    public KeyBinding ofKeyBindZoom;
    private File optionsFileOF;
    
    /** Clouds flag */
    public boolean clouds = true;
    public List resourcePacks = new ArrayList();
    public EntityPlayer.EnumChatVisibility chatVisibility;
    public boolean chatColours;
    public boolean chatLinks;
    public boolean chatLinksPrompt;
    public float chatOpacity;
    public boolean snooperEnabled;
    public boolean fullScreen;
    public boolean enableVsync;
    public boolean hideServerAddress;
    /** Whether to show advanced information on item tooltips, toggled by F3+H */
    public boolean advancedItemTooltips;
    /** Whether to pause when the game loses focus, toggled by F3+P */
    public boolean pauseOnLostFocus;
    /** Whether to show your cape */
    public boolean showCape;
    public boolean touchscreen;
    public int overrideWidth;
    public int overrideHeight;
    public boolean heldItemTooltips;
    public float chatScale;
    public float chatWidth;
    public float chatHeightUnfocused;
    public float chatHeightFocused;
    public boolean showInventoryAchievementHint;
    public int mipmapLevels;
    public int anisotropicFiltering;
    private Map mapSoundLevels;
    public float field_152400_J;
    public float field_152401_K;
    public float field_152402_L;
    public float field_152403_M;
    public float field_152404_N;
    public int field_152405_O;
    public boolean field_152406_P;
    public String field_152407_Q;
    public int field_152408_R;
    public int field_152409_S;
    public int field_152410_T;
    public KeyBinding keyBindForward;
    public KeyBinding keyBindLeft;
    public KeyBinding keyBindBack;
    public KeyBinding keyBindRight;
    public KeyBinding keyBindJump;
    public KeyBinding keyBindSneak;
    public KeyBinding keyBindInventory;
    public KeyBinding keyBindUseItem;
    public KeyBinding keyBindDrop;
    public KeyBinding keyBindAttack;
    public KeyBinding keyBindPickBlock;
    public KeyBinding keyBindSprint;
    public KeyBinding keyBindChat;
    public KeyBinding keyBindPlayerList;
    public KeyBinding keyBindCommand;
    public KeyBinding keyBindScreenshot;
    public KeyBinding keyBindTogglePerspective;
    public KeyBinding keyBindSmoothCamera;
    public KeyBinding field_152395_am;
    public KeyBinding field_152396_an;
    public KeyBinding field_152397_ao;
    public KeyBinding field_152398_ap;
    public KeyBinding field_152399_aq;
    public KeyBinding[] keyBindsHotbar;
    public KeyBinding[] keyBindings;
    protected Minecraft mc;
    private File optionsFile;
    public EnumDifficulty difficulty;
    public boolean hideGUI;
    public int thirdPersonView;
    /** true if debug info should be displayed instead of version */
    public boolean showDebugInfo;
    public boolean showDebugProfilerChart;
    /** The lastServer string. */
    public String lastServer;
    /** No clipping for singleplayer */
    public boolean noclip;
    /** Smooth Camera Toggle */
    public boolean smoothCamera;
    public boolean debugCamEnable;
    /** No clipping movement rate */
    public float noclipRate;
    /** Change rate for debug camera */
    public float debugCamRate;
    public float fovSetting;
    public float gammaSetting;
    public float saturation;
    /** GUI scale */
    public int guiScale;
    /** Determines amount of particles. 0 = All, 1 = Decreased, 2 = Minimal */
    public int particleSetting;
    /** Game settings language */
    public String language;
    public boolean forceUnicodeFont;
    private static final String __OBFID = "CL_00000650";

    public GameSettings(Minecraft par1Minecraft, File par2File) {
            this.chatVisibility = EnumChatVisibility.FULL;
            this.chatColours = true;
            this.chatLinks = true;
            this.chatLinksPrompt = true;
            this.chatOpacity = 1.0F;
            this.snooperEnabled = true;
            this.enableVsync = true;
            this.pauseOnLostFocus = true;
            this.showCape = true;
            this.heldItemTooltips = true;
            this.chatScale = 1.0F;
            this.chatWidth = 1.0F;
            this.chatHeightUnfocused = 0.44366196F;
            this.chatHeightFocused = 1.0F;
            this.showInventoryAchievementHint = true;
            this.mipmapLevels = 4;
            this.anisotropicFiltering = 1;
            this.mapSoundLevels = Maps.newEnumMap(SoundCategory.class);
            this.field_152400_J = 0.5F;
            this.field_152401_K = 1.0F;
            this.field_152402_L = 1.0F;
            this.field_152403_M = 0.5412844F;
            this.field_152404_N = 0.31690142F;
            this.field_152405_O = 1;
            this.field_152406_P = true;
            this.field_152407_Q = "";
            this.field_152408_R = 0;
            this.field_152409_S = 0;
            this.field_152410_T = 0;
            this.keyBindForward = new KeyBinding("key.forward", 17, "key.categories.movement");
            this.keyBindLeft = new KeyBinding("key.left", 30, "key.categories.movement");
            this.keyBindBack = new KeyBinding("key.back", 31, "key.categories.movement");
            this.keyBindRight = new KeyBinding("key.right", 32, "key.categories.movement");
            this.keyBindJump = new KeyBinding("key.jump", 57, "key.categories.movement");
            this.keyBindSneak = new KeyBinding("key.sneak", 42, "key.categories.movement");
            this.keyBindInventory = new KeyBinding("key.inventory", 18, "key.categories.inventory");
            this.keyBindUseItem = new KeyBinding("key.use", -99, "key.categories.gameplay");
            this.keyBindDrop = new KeyBinding("key.drop", 16, "key.categories.gameplay");
            this.keyBindAttack = new KeyBinding("key.attack", -100, "key.categories.gameplay");
            this.keyBindPickBlock = new KeyBinding("key.pickItem", -98, "key.categories.gameplay");
            this.keyBindSprint = new KeyBinding("key.sprint", 29, "key.categories.gameplay");
            this.keyBindChat = new KeyBinding("key.chat", 20, "key.categories.multiplayer");
            this.keyBindPlayerList = new KeyBinding("key.playerlist", 15, "key.categories.multiplayer");
            this.keyBindCommand = new KeyBinding("key.command", 53, "key.categories.multiplayer");
            this.keyBindScreenshot = new KeyBinding("key.screenshot", 60, "key.categories.misc");
            this.keyBindTogglePerspective = new KeyBinding("key.togglePerspective", 63, "key.categories.misc");
            this.keyBindSmoothCamera = new KeyBinding("key.smoothCamera", 0, "key.categories.misc");
            this.field_152395_am = new KeyBinding("key.fullscreen", 87, "key.categories.misc");
            this.field_152396_an = new KeyBinding("key.streamStartStop", 64, "key.categories.stream");
            this.field_152397_ao = new KeyBinding("key.streamPauseUnpause", 65, "key.categories.stream");
            this.field_152398_ap = new KeyBinding("key.streamCommercial", 0, "key.categories.stream");
            this.field_152399_aq = new KeyBinding("key.streamToggleMic", 0, "key.categories.stream");
            this.keyBindsHotbar = new KeyBinding[]{new KeyBinding("key.hotbar.1", 2, "key.categories.inventory"), new KeyBinding("key.hotbar.2", 3, "key.categories.inventory"), new KeyBinding("key.hotbar.3", 4, "key.categories.inventory"), new KeyBinding("key.hotbar.4", 5, "key.categories.inventory"), new KeyBinding("key.hotbar.5", 6, "key.categories.inventory"), new KeyBinding("key.hotbar.6", 7, "key.categories.inventory"), new KeyBinding("key.hotbar.7", 8, "key.categories.inventory"), new KeyBinding("key.hotbar.8", 9, "key.categories.inventory"), new KeyBinding("key.hotbar.9", 10, "key.categories.inventory")};
            this.keyBindings = (KeyBinding[])((KeyBinding[])ArrayUtils.addAll(new KeyBinding[]{this.keyBindAttack, this.keyBindUseItem, this.keyBindForward, this.keyBindLeft, this.keyBindBack, this.keyBindRight, this.keyBindJump, this.keyBindSneak, this.keyBindDrop, this.keyBindInventory, this.keyBindChat, this.keyBindPlayerList, this.keyBindPickBlock, this.keyBindCommand, this.keyBindScreenshot, this.keyBindTogglePerspective, this.keyBindSmoothCamera, this.keyBindSprint, this.field_152396_an, this.field_152397_ao, this.field_152398_ap, this.field_152399_aq, this.field_152395_am}, this.keyBindsHotbar));
            this.difficulty = EnumDifficulty.NORMAL;
            this.lastServer = "";
            this.noclipRate = 1.0F;
            this.debugCamRate = 1.0F;
            this.fovSetting = 70.0F;
            this.language = "ru_RU";
            this.forceUnicodeFont = false;
            this.mc = par1Minecraft;
            this.optionsFile = new File(par2File, "options.txt");
            this.optionsFileOF = new File(par2File, "optionsof.txt");
            this.limitFramerate = (int)GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
            this.ofKeyBindZoom = new KeyBinding("of.key.zoom", 29, "key.categories.misc");
            this.keyBindings = (KeyBinding[])((KeyBinding[])ArrayUtils.add(this.keyBindings, this.ofKeyBindZoom));
            GameSettings.Options.RENDER_DISTANCE.setValueMax(32.0F);
            this.renderDistanceChunks = par1Minecraft.isJava64bit() ? 12 : 8;
            this.loadOptions();
            Config.initGameSettings(this);
      }

    public GameSettings() {
            this.chatVisibility = EnumChatVisibility.FULL;
            this.chatColours = true;
            this.chatLinks = true;
            this.chatLinksPrompt = true;
            this.chatOpacity = 1.0F;
            this.snooperEnabled = true;
            this.enableVsync = true;
            this.pauseOnLostFocus = true;
            this.showCape = true;
            this.heldItemTooltips = true;
            this.chatScale = 1.0F;
            this.chatWidth = 1.0F;
            this.chatHeightUnfocused = 0.44366196F;
            this.chatHeightFocused = 1.0F;
            this.showInventoryAchievementHint = true;
            this.mipmapLevels = 4;
            this.anisotropicFiltering = 1;
            this.mapSoundLevels = Maps.newEnumMap(SoundCategory.class);
            this.field_152400_J = 0.5F;
            this.field_152401_K = 1.0F;
            this.field_152402_L = 1.0F;
            this.field_152403_M = 0.5412844F;
            this.field_152404_N = 0.31690142F;
            this.field_152405_O = 1;
            this.field_152406_P = true;
            this.field_152407_Q = "";
            this.field_152408_R = 0;
            this.field_152409_S = 0;
            this.field_152410_T = 0;
            this.keyBindForward = new KeyBinding("key.forward", 17, "key.categories.movement");
            this.keyBindLeft = new KeyBinding("key.left", 30, "key.categories.movement");
            this.keyBindBack = new KeyBinding("key.back", 31, "key.categories.movement");
            this.keyBindRight = new KeyBinding("key.right", 32, "key.categories.movement");
            this.keyBindJump = new KeyBinding("key.jump", 57, "key.categories.movement");
            this.keyBindSneak = new KeyBinding("key.sneak", 42, "key.categories.movement");
            this.keyBindInventory = new KeyBinding("key.inventory", 18, "key.categories.inventory");
            this.keyBindUseItem = new KeyBinding("key.use", -99, "key.categories.gameplay");
            this.keyBindDrop = new KeyBinding("key.drop", 16, "key.categories.gameplay");
            this.keyBindAttack = new KeyBinding("key.attack", -100, "key.categories.gameplay");
            this.keyBindPickBlock = new KeyBinding("key.pickItem", -98, "key.categories.gameplay");
            this.keyBindSprint = new KeyBinding("key.sprint", 29, "key.categories.gameplay");
            this.keyBindChat = new KeyBinding("key.chat", 20, "key.categories.multiplayer");
            this.keyBindPlayerList = new KeyBinding("key.playerlist", 15, "key.categories.multiplayer");
            this.keyBindCommand = new KeyBinding("key.command", 53, "key.categories.multiplayer");
            this.keyBindScreenshot = new KeyBinding("key.screenshot", 60, "key.categories.misc");
            this.keyBindTogglePerspective = new KeyBinding("key.togglePerspective", 63, "key.categories.misc");
            this.keyBindSmoothCamera = new KeyBinding("key.smoothCamera", 0, "key.categories.misc");
            this.field_152395_am = new KeyBinding("key.fullscreen", 87, "key.categories.misc");
            this.field_152396_an = new KeyBinding("key.streamStartStop", 64, "key.categories.stream");
            this.field_152397_ao = new KeyBinding("key.streamPauseUnpause", 65, "key.categories.stream");
            this.field_152398_ap = new KeyBinding("key.streamCommercial", 0, "key.categories.stream");
            this.field_152399_aq = new KeyBinding("key.streamToggleMic", 0, "key.categories.stream");
            this.keyBindsHotbar = new KeyBinding[]{new KeyBinding("key.hotbar.1", 2, "key.categories.inventory"), new KeyBinding("key.hotbar.2", 3, "key.categories.inventory"), new KeyBinding("key.hotbar.3", 4, "key.categories.inventory"), new KeyBinding("key.hotbar.4", 5, "key.categories.inventory"), new KeyBinding("key.hotbar.5", 6, "key.categories.inventory"), new KeyBinding("key.hotbar.6", 7, "key.categories.inventory"), new KeyBinding("key.hotbar.7", 8, "key.categories.inventory"), new KeyBinding("key.hotbar.8", 9, "key.categories.inventory"), new KeyBinding("key.hotbar.9", 10, "key.categories.inventory")};
            this.keyBindings = (KeyBinding[])((KeyBinding[])ArrayUtils.addAll(new KeyBinding[]{this.keyBindAttack, this.keyBindUseItem, this.keyBindForward, this.keyBindLeft, this.keyBindBack, this.keyBindRight, this.keyBindJump, this.keyBindSneak, this.keyBindDrop, this.keyBindInventory, this.keyBindChat, this.keyBindPlayerList, this.keyBindPickBlock, this.keyBindCommand, this.keyBindScreenshot, this.keyBindTogglePerspective, this.keyBindSmoothCamera, this.keyBindSprint, this.field_152396_an, this.field_152397_ao, this.field_152398_ap, this.field_152399_aq, this.field_152395_am}, this.keyBindsHotbar));
            this.difficulty = EnumDifficulty.NORMAL;
            this.lastServer = "";
            this.noclipRate = 1.0F;
            this.debugCamRate = 1.0F;
            this.fovSetting = 70.0F;
            this.language = "ru_RU";
            this.forceUnicodeFont = false;
            this.limitFramerate = (int)GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
            this.ofKeyBindZoom = new KeyBinding("of.key.zoom", 29, "key.categories.misc");
            this.keyBindings = (KeyBinding[])((KeyBinding[])ArrayUtils.add(this.keyBindings, this.ofKeyBindZoom));
      }

    /**
     * Represents a key or mouse button as a string. Args: key
     */
    public static String getKeyDisplayString(int p_74298_0_)
    {
        return p_74298_0_ < 0 ? I18n.format("key.mouseButton", new Object[] {Integer.valueOf(p_74298_0_ + 101)}): Keyboard.getKeyName(p_74298_0_);
    }

    /**
     * Returns whether the specified key binding is currently being pressed.
     */
    public static boolean isKeyDown(KeyBinding p_100015_0_)
    {
        return p_100015_0_.getKeyCode() == 0 ? false : (p_100015_0_.getKeyCode() < 0 ? Mouse.isButtonDown(p_100015_0_.getKeyCode() + 100) : Keyboard.isKeyDown(p_100015_0_.getKeyCode()));
    }

    /**
     * Sets a key binding and then saves all settings.
     */
    public void setOptionKeyBinding(KeyBinding p_151440_1_, int p_151440_2_)
    {
        p_151440_1_.setKeyCode(p_151440_2_);
        this.saveOptions();
    }

    /**
     * If the specified option is controlled by a slider (float value), this will set the float value.
     */
      public void setOptionFloatValue(GameSettings.Options par1EnumOptions, float par2) {
            if (par1EnumOptions == GameSettings.Options.CLOUD_HEIGHT) {
                  this.ofCloudsHeight = par2;
            }

            if (par1EnumOptions == GameSettings.Options.AO_LEVEL) {
                  this.ofAoLevel = par2;
                  this.mc.renderGlobal.loadRenderers();
            }

            int var3;
            if (par1EnumOptions == GameSettings.Options.MIPMAP_TYPE) {
                  var3 = (int)par2;
                  this.ofMipmapType = Config.limit(var3, 0, 3);
                  TextureUtils.refreshBlockTextures();
            }

            if (par1EnumOptions == GameSettings.Options.SENSITIVITY) {
                  this.mouseSensitivity = par2;
            }

            if (par1EnumOptions == GameSettings.Options.FOV) {
                  this.fovSetting = par2;
            }

            if (par1EnumOptions == GameSettings.Options.GAMMA) {
                  this.gammaSetting = par2;
            }

            if (par1EnumOptions == GameSettings.Options.FRAMERATE_LIMIT) {
                  this.limitFramerate = (int)par2;
                  this.enableVsync = false;
                  if (this.limitFramerate <= 0) {
                        this.limitFramerate = (int)GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
                        this.enableVsync = true;
                  }

                  this.updateVSync();
            }

            if (par1EnumOptions == GameSettings.Options.CHAT_OPACITY) {
                  this.chatOpacity = par2;
                  this.mc.ingameGUI.getChatGUI().refreshChat();
            }

            if (par1EnumOptions == GameSettings.Options.CHAT_HEIGHT_FOCUSED) {
                  this.chatHeightFocused = par2;
                  this.mc.ingameGUI.getChatGUI().refreshChat();
            }

            if (par1EnumOptions == GameSettings.Options.CHAT_HEIGHT_UNFOCUSED) {
                  this.chatHeightUnfocused = par2;
                  this.mc.ingameGUI.getChatGUI().refreshChat();
            }

            if (par1EnumOptions == GameSettings.Options.CHAT_WIDTH) {
                  this.chatWidth = par2;
                  this.mc.ingameGUI.getChatGUI().refreshChat();
            }

            if (par1EnumOptions == GameSettings.Options.CHAT_SCALE) {
                  this.chatScale = par2;
                  this.mc.ingameGUI.getChatGUI().refreshChat();
            }

            if (par1EnumOptions == GameSettings.Options.ANISOTROPIC_FILTERING) {
                  var3 = this.anisotropicFiltering;
                  if (var3 > 1 && Config.isShaders()) {
                        Config.showGuiMessage(Lang.get("of.message.af.shaders1"), Lang.get("of.message.af.shaders2"));
                        return;
                  }

                  this.anisotropicFiltering = (int)par2;
                  if ((float)var3 != par2) {
                        this.mc.getTextureMapBlocks().setAnisotropicFiltering(this.anisotropicFiltering);
                        this.mc.scheduleResourcesRefresh();
                  }
            }

            if (par1EnumOptions == GameSettings.Options.MIPMAP_LEVELS) {
                  var3 = this.mipmapLevels;
                  this.mipmapLevels = (int)par2;
                  if ((float)var3 != par2) {
                        this.mc.getTextureMapBlocks().setMipmapLevels(this.mipmapLevels);
                        this.mc.scheduleResourcesRefresh();
                  }
            }

            if (par1EnumOptions == GameSettings.Options.RENDER_DISTANCE) {
                  this.renderDistanceChunks = (int)par2;
            }

            if (par1EnumOptions == GameSettings.Options.STREAM_BYTES_PER_PIXEL) {
                  this.field_152400_J = par2;
            }

            if (par1EnumOptions == GameSettings.Options.STREAM_VOLUME_MIC) {
                  this.field_152401_K = par2;
                  this.mc.func_152346_Z().func_152915_s();
            }

            if (par1EnumOptions == GameSettings.Options.STREAM_VOLUME_SYSTEM) {
                  this.field_152402_L = par2;
                  this.mc.func_152346_Z().func_152915_s();
            }

            if (par1EnumOptions == GameSettings.Options.STREAM_KBPS) {
                  this.field_152403_M = par2;
            }

            if (par1EnumOptions == GameSettings.Options.STREAM_FPS) {
                  this.field_152404_N = par2;
            }

      }

    /**
     * For non-float options. Toggles the option on/off, or cycles through the list i.e. render distances.
     */
    public void setOptionValue(GameSettings.Options par1EnumOptions, int par2) {
            if (par1EnumOptions == GameSettings.Options.FOG_FANCY) {
                  switch(this.ofFogType) {
                  case 1:
                        this.ofFogType = 2;
                        if (!Config.isFancyFogAvailable()) {
                              this.ofFogType = 3;
                        }
                        break;
                  case 2:
                        this.ofFogType = 3;
                        break;
                  case 3:
                        this.ofFogType = 1;
                        break;
                  default:
                        this.ofFogType = 1;
                  }
            }

            if (par1EnumOptions == GameSettings.Options.FOG_START) {
                  this.ofFogStart += 0.2F;
                  if (this.ofFogStart > 0.81F) {
                        this.ofFogStart = 0.2F;
                  }
            }

            if (par1EnumOptions == GameSettings.Options.LOAD_FAR) {
                  this.ofLoadFar = !this.ofLoadFar;
                  this.mc.renderGlobal.loadRenderers();
            }

            if (par1EnumOptions == GameSettings.Options.PRELOADED_CHUNKS) {
                  this.ofPreloadedChunks += 2;
                  if (this.ofPreloadedChunks > 8) {
                        this.ofPreloadedChunks = 0;
                  }

                  this.mc.renderGlobal.loadRenderers();
            }

            if (par1EnumOptions == GameSettings.Options.SMOOTH_FPS) {
                  this.ofSmoothFps = !this.ofSmoothFps;
            }

            if (par1EnumOptions == GameSettings.Options.SMOOTH_WORLD) {
                  this.ofSmoothWorld = !this.ofSmoothWorld;
                  Config.updateAvailableProcessors();
                  if (!Config.isSingleProcessor()) {
                        this.ofSmoothWorld = false;
                  }

                  Config.updateThreadPriorities();
            }

            if (par1EnumOptions == GameSettings.Options.CLOUDS) {
                  ++this.ofClouds;
                  if (this.ofClouds > 3) {
                        this.ofClouds = 0;
                  }
            }

            if (par1EnumOptions == GameSettings.Options.TREES) {
                  ++this.ofTrees;
                  if (this.ofTrees > 2) {
                        this.ofTrees = 0;
                  }

                  this.mc.renderGlobal.loadRenderers();
            }

            if (par1EnumOptions == GameSettings.Options.GRASS) {
                  ++this.ofGrass;
                  if (this.ofGrass > 2) {
                        this.ofGrass = 0;
                  }

                  RenderBlocks.fancyGrass = Config.isGrassFancy();
                  this.mc.renderGlobal.loadRenderers();
            }

            if (par1EnumOptions == GameSettings.Options.DROPPED_ITEMS) {
                  ++this.ofDroppedItems;
                  if (this.ofDroppedItems > 2) {
                        this.ofDroppedItems = 0;
                  }
            }

            if (par1EnumOptions == GameSettings.Options.RAIN) {
                  ++this.ofRain;
                  if (this.ofRain > 3) {
                        this.ofRain = 0;
                  }
            }

            if (par1EnumOptions == GameSettings.Options.WATER) {
                  ++this.ofWater;
                  if (this.ofWater > 2) {
                        this.ofWater = 0;
                  }
            }

            if (par1EnumOptions == GameSettings.Options.ANIMATED_WATER) {
                  ++this.ofAnimatedWater;
                  if (this.ofAnimatedWater == 1) {
                        ++this.ofAnimatedWater;
                  }

                  if (this.ofAnimatedWater > 2) {
                        this.ofAnimatedWater = 0;
                  }
            }

            if (par1EnumOptions == GameSettings.Options.ANIMATED_LAVA) {
                  ++this.ofAnimatedLava;
                  if (this.ofAnimatedLava == 1) {
                        ++this.ofAnimatedLava;
                  }

                  if (this.ofAnimatedLava > 2) {
                        this.ofAnimatedLava = 0;
                  }
            }

            if (par1EnumOptions == GameSettings.Options.ANIMATED_FIRE) {
                  this.ofAnimatedFire = !this.ofAnimatedFire;
            }

            if (par1EnumOptions == GameSettings.Options.ANIMATED_PORTAL) {
                  this.ofAnimatedPortal = !this.ofAnimatedPortal;
            }

            if (par1EnumOptions == GameSettings.Options.ANIMATED_REDSTONE) {
                  this.ofAnimatedRedstone = !this.ofAnimatedRedstone;
            }

            if (par1EnumOptions == GameSettings.Options.ANIMATED_EXPLOSION) {
                  this.ofAnimatedExplosion = !this.ofAnimatedExplosion;
            }

            if (par1EnumOptions == GameSettings.Options.ANIMATED_FLAME) {
                  this.ofAnimatedFlame = !this.ofAnimatedFlame;
            }

            if (par1EnumOptions == GameSettings.Options.ANIMATED_SMOKE) {
                  this.ofAnimatedSmoke = !this.ofAnimatedSmoke;
            }

            if (par1EnumOptions == GameSettings.Options.VOID_PARTICLES) {
                  this.ofVoidParticles = !this.ofVoidParticles;
            }

            if (par1EnumOptions == GameSettings.Options.WATER_PARTICLES) {
                  this.ofWaterParticles = !this.ofWaterParticles;
            }

            if (par1EnumOptions == GameSettings.Options.PORTAL_PARTICLES) {
                  this.ofPortalParticles = !this.ofPortalParticles;
            }

            if (par1EnumOptions == GameSettings.Options.POTION_PARTICLES) {
                  this.ofPotionParticles = !this.ofPotionParticles;
            }

            if (par1EnumOptions == GameSettings.Options.DRIPPING_WATER_LAVA) {
                  this.ofDrippingWaterLava = !this.ofDrippingWaterLava;
            }

            if (par1EnumOptions == GameSettings.Options.ANIMATED_TERRAIN) {
                  this.ofAnimatedTerrain = !this.ofAnimatedTerrain;
            }

            if (par1EnumOptions == GameSettings.Options.ANIMATED_TEXTURES) {
                  this.ofAnimatedTextures = !this.ofAnimatedTextures;
            }

            if (par1EnumOptions == GameSettings.Options.ANIMATED_ITEMS) {
                  this.ofAnimatedItems = !this.ofAnimatedItems;
            }

            if (par1EnumOptions == GameSettings.Options.RAIN_SPLASH) {
                  this.ofRainSplash = !this.ofRainSplash;
            }

            if (par1EnumOptions == GameSettings.Options.LAGOMETER) {
                  this.ofLagometer = !this.ofLagometer;
            }

            if (par1EnumOptions == GameSettings.Options.SHOW_FPS) {
                  this.ofShowFps = !this.ofShowFps;
            }

            if (par1EnumOptions == GameSettings.Options.AUTOSAVE_TICKS) {
                  this.ofAutoSaveTicks *= 10;
                  if (this.ofAutoSaveTicks > 40000) {
                        this.ofAutoSaveTicks = 40;
                  }
            }

            if (par1EnumOptions == GameSettings.Options.BETTER_GRASS) {
                  ++this.ofBetterGrass;
                  if (this.ofBetterGrass > 3) {
                        this.ofBetterGrass = 1;
                  }

                  this.mc.renderGlobal.loadRenderers();
            }

            if (par1EnumOptions == GameSettings.Options.CONNECTED_TEXTURES) {
                  ++this.ofConnectedTextures;
                  if (this.ofConnectedTextures > 3) {
                        this.ofConnectedTextures = 1;
                  }

                  this.mc.renderGlobal.loadRenderers();
            }

            if (par1EnumOptions == GameSettings.Options.WEATHER) {
                  this.ofWeather = !this.ofWeather;
            }

            if (par1EnumOptions == GameSettings.Options.SKY) {
                  this.ofSky = !this.ofSky;
            }

            if (par1EnumOptions == GameSettings.Options.STARS) {
                  this.ofStars = !this.ofStars;
            }

            if (par1EnumOptions == GameSettings.Options.SUN_MOON) {
                  this.ofSunMoon = !this.ofSunMoon;
            }

            if (par1EnumOptions == GameSettings.Options.VIGNETTE) {
                  ++this.ofVignette;
                  if (this.ofVignette > 2) {
                        this.ofVignette = 0;
                  }
            }

            if (par1EnumOptions == GameSettings.Options.CHUNK_UPDATES) {
                  ++this.ofChunkUpdates;
                  if (this.ofChunkUpdates > 5) {
                        this.ofChunkUpdates = 1;
                  }
            }

            if (par1EnumOptions == GameSettings.Options.CHUNK_LOADING) {
                  ++this.ofChunkLoading;
                  if (this.ofChunkLoading > 2) {
                        this.ofChunkLoading = 0;
                  }

                  this.updateChunkLoading();
            }

            if (par1EnumOptions == GameSettings.Options.CHUNK_UPDATES_DYNAMIC) {
                  this.ofChunkUpdatesDynamic = !this.ofChunkUpdatesDynamic;
            }

            if (par1EnumOptions == GameSettings.Options.TIME) {
                  ++this.ofTime;
                  if (this.ofTime > 3) {
                        this.ofTime = 0;
                  }
            }

            if (par1EnumOptions == GameSettings.Options.CLEAR_WATER) {
                  this.ofClearWater = !this.ofClearWater;
                  this.updateWaterOpacity();
            }

            if (par1EnumOptions == GameSettings.Options.DEPTH_FOG) {
                  this.ofDepthFog = !this.ofDepthFog;
            }

            if (par1EnumOptions == GameSettings.Options.AA_LEVEL) {
                  this.ofAaLevel = 0;
            }

            if (par1EnumOptions == GameSettings.Options.PROFILER) {
                  this.ofProfiler = !this.ofProfiler;
            }

            if (par1EnumOptions == GameSettings.Options.BETTER_SNOW) {
                  this.ofBetterSnow = !this.ofBetterSnow;
                  this.mc.renderGlobal.loadRenderers();
            }

            if (par1EnumOptions == GameSettings.Options.SWAMP_COLORS) {
                  this.ofSwampColors = !this.ofSwampColors;
                  CustomColorizer.updateUseDefaultColorMultiplier();
                  this.mc.renderGlobal.loadRenderers();
            }

            if (par1EnumOptions == GameSettings.Options.RANDOM_MOBS) {
                  this.ofRandomMobs = !this.ofRandomMobs;
                  RandomMobs.resetTextures();
            }

            if (par1EnumOptions == GameSettings.Options.SMOOTH_BIOMES) {
                  this.ofSmoothBiomes = !this.ofSmoothBiomes;
                  CustomColorizer.updateUseDefaultColorMultiplier();
                  this.mc.renderGlobal.loadRenderers();
            }

            if (par1EnumOptions == GameSettings.Options.CUSTOM_FONTS) {
                  this.ofCustomFonts = !this.ofCustomFonts;
                  this.mc.fontRenderer.onResourceManagerReload(Config.getResourceManager());
                  this.mc.standardGalacticFontRenderer.onResourceManagerReload(Config.getResourceManager());
            }

            if (par1EnumOptions == GameSettings.Options.CUSTOM_COLORS) {
                  this.ofCustomColors = !this.ofCustomColors;
                  CustomColorizer.update();
                  this.mc.renderGlobal.loadRenderers();
            }

            if (par1EnumOptions == GameSettings.Options.CUSTOM_SKY) {
                  this.ofCustomSky = !this.ofCustomSky;
                  CustomSky.update();
            }

            if (par1EnumOptions == GameSettings.Options.SHOW_CAPES) {
                  this.ofShowCapes = !this.ofShowCapes;
                  this.mc.renderGlobal.updateCapes();
            }

            if (par1EnumOptions == GameSettings.Options.NATURAL_TEXTURES) {
                  this.ofNaturalTextures = !this.ofNaturalTextures;
                  NaturalTextures.update();
                  this.mc.renderGlobal.loadRenderers();
            }

            if (par1EnumOptions == GameSettings.Options.FAST_MATH) {
                  this.ofFastMath = !this.ofFastMath;
                  MathHelper.fastMath = this.ofFastMath;
            }

            if (par1EnumOptions == GameSettings.Options.FAST_RENDER) {
                  if (!this.ofFastRender && Config.isShaders()) {
                        Config.showGuiMessage(Lang.get("of.message.fr.shaders1"), Lang.get("of.message.fr.shaders2"));
                        return;
                  }

                  this.ofFastRender = !this.ofFastRender;
                  if (this.ofFastRender) {
                        this.mc.entityRenderer.stopUseShader();
                  }

                  Config.updateFramebufferSize();
                  this.mc.renderGlobal.loadRenderers();
            }

            if (par1EnumOptions == GameSettings.Options.TRANSLUCENT_BLOCKS) {
                  if (this.ofTranslucentBlocks == 1) {
                        this.ofTranslucentBlocks = 2;
                  } else {
                        this.ofTranslucentBlocks = 1;
                  }

                  this.mc.renderGlobal.loadRenderers();
            }

            if (par1EnumOptions == GameSettings.Options.LAZY_CHUNK_LOADING) {
                  this.ofLazyChunkLoading = !this.ofLazyChunkLoading;
                  Config.updateAvailableProcessors();
                  if (!Config.isSingleProcessor()) {
                        this.ofLazyChunkLoading = false;
                  }

                  this.mc.renderGlobal.loadRenderers();
            }

            if (par1EnumOptions == GameSettings.Options.FULLSCREEN_MODE) {
                  List modeList = Arrays.asList(Config.getDisplayModeNames());
                  if (this.ofFullscreenMode.equals("Default")) {
                        this.ofFullscreenMode = (String)modeList.get(0);
                  } else {
                        int index = modeList.indexOf(this.ofFullscreenMode);
                        if (index < 0) {
                              this.ofFullscreenMode = "Default";
                        } else {
                              ++index;
                              if (index >= modeList.size()) {
                                    this.ofFullscreenMode = "Default";
                              } else {
                                    this.ofFullscreenMode = (String)modeList.get(index);
                              }
                        }
                  }
            }

            if (par1EnumOptions == GameSettings.Options.DYNAMIC_FOV) {
                  this.ofDynamicFov = !this.ofDynamicFov;
            }

            if (par1EnumOptions == GameSettings.Options.DYNAMIC_LIGHTS) {
                  this.ofDynamicLights = nextValue(this.ofDynamicLights, OF_DYNAMIC_LIGHTS);
                  DynamicLights.removeLights(this.mc.renderGlobal);
            }

            if (par1EnumOptions == GameSettings.Options.HELD_ITEM_TOOLTIPS) {
                  this.heldItemTooltips = !this.heldItemTooltips;
            }

            if (par1EnumOptions == GameSettings.Options.INVERT_MOUSE) {
                  this.invertMouse = !this.invertMouse;
            }

            if (par1EnumOptions == GameSettings.Options.GUI_SCALE) {
                  this.guiScale = this.guiScale + par2 & 3;
            }

            if (par1EnumOptions == GameSettings.Options.PARTICLES) {
                  this.particleSetting = (this.particleSetting + par2) % 3;
            }

            if (par1EnumOptions == GameSettings.Options.VIEW_BOBBING) {
                  this.viewBobbing = !this.viewBobbing;
            }

            if (par1EnumOptions == GameSettings.Options.RENDER_CLOUDS) {
                  this.clouds = !this.clouds;
            }

            if (par1EnumOptions == GameSettings.Options.FORCE_UNICODE_FONT) {
                  this.forceUnicodeFont = !this.forceUnicodeFont;
                  this.mc.fontRenderer.setUnicodeFlag(this.mc.getLanguageManager().isCurrentLocaleUnicode() || this.forceUnicodeFont);
            }

            if (par1EnumOptions == GameSettings.Options.ADVANCED_OPENGL) {
                  if (!Config.isOcclusionAvailable()) {
                        this.ofOcclusionFancy = false;
                        this.advancedOpengl = false;
                  } else if (!this.advancedOpengl) {
                        this.advancedOpengl = true;
                        this.ofOcclusionFancy = false;
                  } else if (!this.ofOcclusionFancy) {
                        this.ofOcclusionFancy = true;
                  } else {
                        this.ofOcclusionFancy = false;
                        this.advancedOpengl = false;
                  }

                  this.mc.renderGlobal.setAllRenderersVisible();
            }

            if (par1EnumOptions == GameSettings.Options.FBO_ENABLE) {
                  this.fboEnable = !this.fboEnable;
            }

            if (par1EnumOptions == GameSettings.Options.ANAGLYPH) {
                  if (!this.anaglyph && Config.isShaders()) {
                        Config.showGuiMessage(Lang.get("of.message.an.shaders1"), Lang.get("of.message.an.shaders2"));
                        return;
                  }

                  this.anaglyph = !this.anaglyph;
                  this.mc.refreshResources();
            }

            if (par1EnumOptions == GameSettings.Options.DIFFICULTY) {
                  this.difficulty = EnumDifficulty.getDifficultyEnum(this.difficulty.getDifficultyId() + par2 & 3);
            }

            if (par1EnumOptions == GameSettings.Options.GRAPHICS) {
                  this.fancyGraphics = !this.fancyGraphics;
                  this.mc.renderGlobal.loadRenderers();
            }

            if (par1EnumOptions == GameSettings.Options.AMBIENT_OCCLUSION) {
                  this.ambientOcclusion = (this.ambientOcclusion + par2) % 3;
                  this.mc.renderGlobal.loadRenderers();
            }

            if (par1EnumOptions == GameSettings.Options.CHAT_VISIBILITY) {
                  this.chatVisibility = EnumChatVisibility.getEnumChatVisibility((this.chatVisibility.getChatVisibility() + par2) % 3);
            }

            if (par1EnumOptions == GameSettings.Options.STREAM_COMPRESSION) {
                  this.field_152405_O = (this.field_152405_O + par2) % 3;
            }

            if (par1EnumOptions == GameSettings.Options.STREAM_SEND_METADATA) {
                  this.field_152406_P = !this.field_152406_P;
            }

            if (par1EnumOptions == GameSettings.Options.STREAM_CHAT_ENABLED) {
                  this.field_152408_R = (this.field_152408_R + par2) % 3;
            }

            if (par1EnumOptions == GameSettings.Options.STREAM_CHAT_USER_FILTER) {
                  this.field_152409_S = (this.field_152409_S + par2) % 3;
            }

            if (par1EnumOptions == GameSettings.Options.STREAM_MIC_TOGGLE_BEHAVIOR) {
                  this.field_152410_T = (this.field_152410_T + par2) % 2;
            }

            if (par1EnumOptions == GameSettings.Options.CHAT_COLOR) {
                  this.chatColours = !this.chatColours;
            }

            if (par1EnumOptions == GameSettings.Options.CHAT_LINKS) {
                  this.chatLinks = !this.chatLinks;
            }

            if (par1EnumOptions == GameSettings.Options.CHAT_LINKS_PROMPT) {
                  this.chatLinksPrompt = !this.chatLinksPrompt;
            }

            if (par1EnumOptions == GameSettings.Options.SNOOPER_ENABLED) {
                  this.snooperEnabled = !this.snooperEnabled;
            }

            if (par1EnumOptions == GameSettings.Options.SHOW_CAPE) {
                  this.showCape = !this.showCape;
            }

            if (par1EnumOptions == GameSettings.Options.TOUCHSCREEN) {
                  this.touchscreen = !this.touchscreen;
            }

            if (par1EnumOptions == GameSettings.Options.USE_FULLSCREEN) {
                  this.fullScreen = !this.fullScreen;
                  if (this.mc.isFullScreen() != this.fullScreen) {
                        this.mc.toggleFullscreen();
                  }
            }

            if (par1EnumOptions == GameSettings.Options.ENABLE_VSYNC) {
                  this.enableVsync = !this.enableVsync;
                  Display.setVSyncEnabled(this.enableVsync);
            }

            this.saveOptions();
      }

    public float getOptionFloatValue(GameSettings.Options par1EnumOptions) {
        if (par1EnumOptions == GameSettings.Options.CLOUD_HEIGHT) {
              return this.ofCloudsHeight;
        } else if (par1EnumOptions == GameSettings.Options.AO_LEVEL) {
              return this.ofAoLevel;
        } else if (par1EnumOptions == GameSettings.Options.FRAMERATE_LIMIT) {
              return (float)this.limitFramerate == GameSettings.Options.FRAMERATE_LIMIT.getValueMax() && this.enableVsync ? 0.0F : (float)this.limitFramerate;
        } else if (par1EnumOptions == GameSettings.Options.MIPMAP_TYPE) {
              return (float)this.ofMipmapType;
        } else {
              return par1EnumOptions == GameSettings.Options.FOV ? this.fovSetting : (par1EnumOptions == GameSettings.Options.GAMMA ? this.gammaSetting : (par1EnumOptions == GameSettings.Options.SATURATION ? this.saturation : (par1EnumOptions == GameSettings.Options.SENSITIVITY ? this.mouseSensitivity : (par1EnumOptions == GameSettings.Options.CHAT_OPACITY ? this.chatOpacity : (par1EnumOptions == GameSettings.Options.CHAT_HEIGHT_FOCUSED ? this.chatHeightFocused : (par1EnumOptions == GameSettings.Options.CHAT_HEIGHT_UNFOCUSED ? this.chatHeightUnfocused : (par1EnumOptions == GameSettings.Options.CHAT_SCALE ? this.chatScale : (par1EnumOptions == GameSettings.Options.CHAT_WIDTH ? this.chatWidth : (par1EnumOptions == GameSettings.Options.FRAMERATE_LIMIT ? (float)this.limitFramerate : (par1EnumOptions == GameSettings.Options.ANISOTROPIC_FILTERING ? (float)this.anisotropicFiltering : (par1EnumOptions == GameSettings.Options.MIPMAP_LEVELS ? (float)this.mipmapLevels : (par1EnumOptions == GameSettings.Options.RENDER_DISTANCE ? (float)this.renderDistanceChunks : (par1EnumOptions == GameSettings.Options.STREAM_BYTES_PER_PIXEL ? this.field_152400_J : (par1EnumOptions == GameSettings.Options.STREAM_VOLUME_MIC ? this.field_152401_K : (par1EnumOptions == GameSettings.Options.STREAM_VOLUME_SYSTEM ? this.field_152402_L : (par1EnumOptions == GameSettings.Options.STREAM_KBPS ? this.field_152403_M : (par1EnumOptions == GameSettings.Options.STREAM_FPS ? this.field_152404_N : 0.0F)))))))))))))))));
        }
  }

    public boolean getOptionOrdinalValue(GameSettings.Options par1EnumOptions) {
            switch(par1EnumOptions) {
            case INVERT_MOUSE:
                  return this.invertMouse;
            case VIEW_BOBBING:
                  return this.viewBobbing;
            case ANAGLYPH:
                  return this.anaglyph;
            case ADVANCED_OPENGL:
                  return this.advancedOpengl;
            case FBO_ENABLE:
                  return this.fboEnable;
            case RENDER_CLOUDS:
                  return this.clouds;
            case CHAT_COLOR:
                  return this.chatColours;
            case CHAT_LINKS:
                  return this.chatLinks;
            case CHAT_LINKS_PROMPT:
                  return this.chatLinksPrompt;
            case SNOOPER_ENABLED:
                  return this.snooperEnabled;
            case USE_FULLSCREEN:
                  return this.fullScreen;
            case ENABLE_VSYNC:
                  return this.enableVsync;
            case SHOW_CAPE:
                  return this.showCape;
            case TOUCHSCREEN:
                  return this.touchscreen;
            case STREAM_SEND_METADATA:
                  return this.field_152406_P;
            case FORCE_UNICODE_FONT:
                  return this.forceUnicodeFont;
            default:
                  return false;
            }
      }

    /**
     * Returns the translation of the given index in the given String array. If the index is smaller than 0 or greater
     * than/equal to the length of the String array, it is changed to 0.
     */
    private static String getTranslation(String[] par0ArrayOfStr, int par1) {
        if (par1 < 0 || par1 >= par0ArrayOfStr.length) {
              par1 = 0;
        }

        return I18n.format(par0ArrayOfStr[par1]);
  }

    /**
     * Gets a key binding.
     */
      public String getKeyBinding(GameSettings.Options par1EnumOptions) {
            String var2 = I18n.format(par1EnumOptions.getEnumString()) + ": ";
            if (var2 == null) {
                  var2 = par1EnumOptions.getEnumString();
            }

            int index;
            if (par1EnumOptions == GameSettings.Options.RENDER_DISTANCE) {
                  index = (int)this.getOptionFloatValue(par1EnumOptions);
                  String str = I18n.format("options.renderDistance.tiny");
                  int baseDist = 2;
                  if (index >= 4) {
                        str = I18n.format("options.renderDistance.short");
                        baseDist = 4;
                  }

                  if (index >= 8) {
                        str = I18n.format("options.renderDistance.normal");
                        baseDist = 8;
                  }

                  if (index >= 16) {
                        str = I18n.format("options.renderDistance.far");
                        baseDist = 16;
                  }

                  if (index >= 32) {
                        str = Lang.get("of.options.renderDistance.extreme");
                        baseDist = 32;
                  }

                  int diff = this.renderDistanceChunks - baseDist;
                  String descr = str;
                  if (diff > 0) {
                        descr = str + "+";
                  }

                  return var2 + index + " " + descr + "";
            } else if (par1EnumOptions == GameSettings.Options.ADVANCED_OPENGL) {
                  if (!this.advancedOpengl) {
                        return var2 + Lang.getOff();
                  } else {
                        return this.ofOcclusionFancy ? var2 + Lang.getFancy() : var2 + Lang.getFast();
                  }
            } else if (par1EnumOptions == GameSettings.Options.FOG_FANCY) {
                  switch(this.ofFogType) {
                  case 1:
                        return var2 + Lang.getFast();
                  case 2:
                        return var2 + Lang.getFancy();
                  case 3:
                        return var2 + Lang.getOff();
                  default:
                        return var2 + Lang.getOff();
                  }
            } else if (par1EnumOptions == GameSettings.Options.FOG_START) {
                  return var2 + this.ofFogStart;
            } else if (par1EnumOptions == GameSettings.Options.MIPMAP_TYPE) {
                  switch(this.ofMipmapType) {
                  case 0:
                        return var2 + Lang.get("of.options.mipmap.nearest");
                  case 1:
                        return var2 + Lang.get("of.options.mipmap.linear");
                  case 2:
                        return var2 + Lang.get("of.options.mipmap.bilinear");
                  case 3:
                        return var2 + Lang.get("of.options.mipmap.trilinear");
                  default:
                        return var2 + Lang.get("of.options.mipmap.nearest");
                  }
            } else if (par1EnumOptions == GameSettings.Options.LOAD_FAR) {
                  return this.ofLoadFar ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.PRELOADED_CHUNKS) {
                  return this.ofPreloadedChunks == 0 ? var2 + Lang.getOff() : var2 + this.ofPreloadedChunks;
            } else if (par1EnumOptions == GameSettings.Options.SMOOTH_FPS) {
                  return this.ofSmoothFps ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.SMOOTH_WORLD) {
                  return this.ofSmoothWorld ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.CLOUDS) {
                  switch(this.ofClouds) {
                  case 1:
                        return var2 + Lang.getFast();
                  case 2:
                        return var2 + Lang.getFancy();
                  case 3:
                        return var2 + Lang.getOff();
                  default:
                        return var2 + Lang.getDefault();
                  }
            } else if (par1EnumOptions == GameSettings.Options.TREES) {
                  switch(this.ofTrees) {
                  case 1:
                        return var2 + Lang.getFast();
                  case 2:
                        return var2 + Lang.getFancy();
                  default:
                        return var2 + Lang.getDefault();
                  }
            } else if (par1EnumOptions == GameSettings.Options.GRASS) {
                  switch(this.ofGrass) {
                  case 1:
                        return var2 + Lang.getFast();
                  case 2:
                        return var2 + Lang.getFancy();
                  default:
                        return var2 + Lang.getDefault();
                  }
            } else if (par1EnumOptions == GameSettings.Options.DROPPED_ITEMS) {
                  switch(this.ofDroppedItems) {
                  case 1:
                        return var2 + Lang.getFast();
                  case 2:
                        return var2 + Lang.getFancy();
                  default:
                        return var2 + Lang.getDefault();
                  }
            } else if (par1EnumOptions == GameSettings.Options.RAIN) {
                  switch(this.ofRain) {
                  case 1:
                        return var2 + Lang.getFast();
                  case 2:
                        return var2 + Lang.getFancy();
                  case 3:
                        return var2 + Lang.getOff();
                  default:
                        return var2 + Lang.getDefault();
                  }
            } else if (par1EnumOptions == GameSettings.Options.WATER) {
                  switch(this.ofWater) {
                  case 1:
                        return var2 + Lang.getFast();
                  case 2:
                        return var2 + Lang.getFancy();
                  case 3:
                        return var2 + Lang.getOff();
                  default:
                        return var2 + Lang.getDefault();
                  }
            } else if (par1EnumOptions == GameSettings.Options.ANIMATED_WATER) {
                  switch(this.ofAnimatedWater) {
                  case 1:
                        return var2 + Lang.get("of.options.animation.dynamic");
                  case 2:
                        return var2 + Lang.getOff();
                  default:
                        return var2 + Lang.getOn();
                  }
            } else if (par1EnumOptions == GameSettings.Options.ANIMATED_LAVA) {
                  switch(this.ofAnimatedLava) {
                  case 1:
                        return var2 + Lang.get("of.options.animation.dynamic");
                  case 2:
                        return var2 + Lang.getOff();
                  default:
                        return var2 + Lang.getOn();
                  }
            } else if (par1EnumOptions == GameSettings.Options.ANIMATED_FIRE) {
                  return this.ofAnimatedFire ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.ANIMATED_PORTAL) {
                  return this.ofAnimatedPortal ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.ANIMATED_REDSTONE) {
                  return this.ofAnimatedRedstone ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.ANIMATED_EXPLOSION) {
                  return this.ofAnimatedExplosion ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.ANIMATED_FLAME) {
                  return this.ofAnimatedFlame ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.ANIMATED_SMOKE) {
                  return this.ofAnimatedSmoke ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.VOID_PARTICLES) {
                  return this.ofVoidParticles ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.WATER_PARTICLES) {
                  return this.ofWaterParticles ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.PORTAL_PARTICLES) {
                  return this.ofPortalParticles ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.POTION_PARTICLES) {
                  return this.ofPotionParticles ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.DRIPPING_WATER_LAVA) {
                  return this.ofDrippingWaterLava ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.ANIMATED_TERRAIN) {
                  return this.ofAnimatedTerrain ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.ANIMATED_TEXTURES) {
                  return this.ofAnimatedTextures ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.ANIMATED_ITEMS) {
                  return this.ofAnimatedItems ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.RAIN_SPLASH) {
                  return this.ofRainSplash ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.LAGOMETER) {
                  return this.ofLagometer ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.SHOW_FPS) {
                  return this.ofShowFps ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.AUTOSAVE_TICKS) {
                  if (this.ofAutoSaveTicks <= 40) {
                        return var2 + Lang.get("of.options.save.default");
                  } else if (this.ofAutoSaveTicks <= 400) {
                        return var2 + Lang.get("of.options.save.20s");
                  } else {
                        return this.ofAutoSaveTicks <= 4000 ? var2 + Lang.get("of.options.save.3min") : var2 + Lang.get("of.options.save.30min");
                  }
            } else if (par1EnumOptions == GameSettings.Options.BETTER_GRASS) {
                  switch(this.ofBetterGrass) {
                  case 1:
                        return var2 + Lang.getFast();
                  case 2:
                        return var2 + Lang.getFancy();
                  default:
                        return var2 + Lang.getOff();
                  }
            } else if (par1EnumOptions == GameSettings.Options.CONNECTED_TEXTURES) {
                  switch(this.ofConnectedTextures) {
                  case 1:
                        return var2 + Lang.getFast();
                  case 2:
                        return var2 + Lang.getFancy();
                  default:
                        return var2 + Lang.getOff();
                  }
            } else if (par1EnumOptions == GameSettings.Options.WEATHER) {
                  return this.ofWeather ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.SKY) {
                  return this.ofSky ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.STARS) {
                  return this.ofStars ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.SUN_MOON) {
                  return this.ofSunMoon ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.VIGNETTE) {
                  switch(this.ofVignette) {
                  case 1:
                        return var2 + Lang.getFast();
                  case 2:
                        return var2 + Lang.getFancy();
                  default:
                        return var2 + Lang.getDefault();
                  }
            } else if (par1EnumOptions == GameSettings.Options.CHUNK_UPDATES) {
                  return var2 + this.ofChunkUpdates;
            } else if (par1EnumOptions == GameSettings.Options.CHUNK_LOADING) {
                  if (this.ofChunkLoading == 1) {
                        return var2 + "Smooth";
                  } else {
                        return this.ofChunkLoading == 2 ? var2 + "Multi-Core" : var2 + "Default";
                  }
            } else if (par1EnumOptions == GameSettings.Options.CHUNK_UPDATES_DYNAMIC) {
                  return this.ofChunkUpdatesDynamic ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.TIME) {
                  if (this.ofTime == 1) {
                        return var2 + Lang.get("of.options.time.dayOnly");
                  } else {
                        return this.ofTime == 3 ? var2 + Lang.get("of.options.time.nightOnly") : var2 + Lang.getDefault();
                  }
            } else if (par1EnumOptions == GameSettings.Options.CLEAR_WATER) {
                  return this.ofClearWater ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.DEPTH_FOG) {
                  return this.ofDepthFog ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.AA_LEVEL) {
                  return this.ofAaLevel == 0 ? var2 + Lang.getOff() : var2 + this.ofAaLevel;
            } else if (par1EnumOptions == GameSettings.Options.PROFILER) {
                  return this.ofProfiler ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.BETTER_SNOW) {
                  return this.ofBetterSnow ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.SWAMP_COLORS) {
                  return this.ofSwampColors ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.RANDOM_MOBS) {
                  return this.ofRandomMobs ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.SMOOTH_BIOMES) {
                  return this.ofSmoothBiomes ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.CUSTOM_FONTS) {
                  return this.ofCustomFonts ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.CUSTOM_COLORS) {
                  return this.ofCustomColors ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.CUSTOM_SKY) {
                  return this.ofCustomSky ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.SHOW_CAPES) {
                  return this.ofShowCapes ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.NATURAL_TEXTURES) {
                  return this.ofNaturalTextures ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.FAST_MATH) {
                  return this.ofFastMath ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.FAST_RENDER) {
                  return this.ofFastRender ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.TRANSLUCENT_BLOCKS) {
                  return this.ofTranslucentBlocks == 1 ? var2 + Lang.getFast() : var2 + Lang.getFancy();
            } else if (par1EnumOptions == GameSettings.Options.LAZY_CHUNK_LOADING) {
                  return this.ofLazyChunkLoading ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.DYNAMIC_FOV) {
                  return this.ofDynamicFov ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else if (par1EnumOptions == GameSettings.Options.DYNAMIC_LIGHTS) {
                  index = indexOf(this.ofDynamicLights, OF_DYNAMIC_LIGHTS);
                  return var2 + getTranslation(KEYS_DYNAMIC_LIGHTS, index);
            } else if (par1EnumOptions == GameSettings.Options.FULLSCREEN_MODE) {
                  return this.ofFullscreenMode.equals("Default") ? var2 + Lang.getDefault() : var2 + this.ofFullscreenMode;
            } else if (par1EnumOptions == GameSettings.Options.HELD_ITEM_TOOLTIPS) {
                  return this.heldItemTooltips ? var2 + Lang.getOn() : var2 + Lang.getOff();
            } else {
                  float var6;
                  if (par1EnumOptions == GameSettings.Options.FRAMERATE_LIMIT) {
                        var6 = this.getOptionFloatValue(par1EnumOptions);
                        if (var6 == 0.0F) {
                              return var2 + Lang.get("of.options.framerateLimit.vsync");
                        } else {
                              return var6 == par1EnumOptions.valueMax ? var2 + I18n.format("options.framerateLimit.max") : var2 + (int)var6 + " fps";
                        }
                  } else if (par1EnumOptions.getEnumFloat()) {
                        var6 = this.getOptionFloatValue(par1EnumOptions);
                        float var4 = par1EnumOptions.normalizeValue(var6);
                        return par1EnumOptions == GameSettings.Options.SENSITIVITY ? (var4 == 0.0F ? var2 + I18n.format("options.sensitivity.min") : (var4 == 1.0F ? var2 + I18n.format("options.sensitivity.max") : var2 + (int)(var4 * 200.0F) + "%")) : (par1EnumOptions == GameSettings.Options.FOV ? (var6 == 70.0F ? var2 + I18n.format("options.fov.min") : (var6 == 110.0F ? var2 + I18n.format("options.fov.max") : var2 + (int)var6)) : (par1EnumOptions == GameSettings.Options.FRAMERATE_LIMIT ? (var6 == par1EnumOptions.valueMax ? var2 + I18n.format("options.framerateLimit.max") : var2 + (int)var6 + " fps") : (par1EnumOptions == GameSettings.Options.GAMMA ? (var4 == 0.0F ? var2 + I18n.format("options.gamma.min") : (var4 == 1.0F ? var2 + I18n.format("options.gamma.max") : var2 + "+" + (int)(var4 * 100.0F) + "%")) : (par1EnumOptions == GameSettings.Options.SATURATION ? var2 + (int)(var4 * 400.0F) + "%" : (par1EnumOptions == GameSettings.Options.CHAT_OPACITY ? var2 + (int)(var4 * 90.0F + 10.0F) + "%" : (par1EnumOptions == GameSettings.Options.CHAT_HEIGHT_UNFOCUSED ? var2 + GuiNewChat.func_146243_b(var4) + "px" : (par1EnumOptions == GameSettings.Options.CHAT_HEIGHT_FOCUSED ? var2 + GuiNewChat.func_146243_b(var4) + "px" : (par1EnumOptions == GameSettings.Options.CHAT_WIDTH ? var2 + GuiNewChat.func_146233_a(var4) + "px" : (par1EnumOptions == GameSettings.Options.RENDER_DISTANCE ? var2 + (int)var6 + " chunks" : (par1EnumOptions == GameSettings.Options.ANISOTROPIC_FILTERING ? (var6 == 1.0F ? var2 + I18n.format("options.off") : var2 + (int)var6) : (par1EnumOptions == GameSettings.Options.MIPMAP_LEVELS ? (var6 == 0.0F ? var2 + I18n.format("options.off") : var2 + (int)var6) : (par1EnumOptions == GameSettings.Options.STREAM_FPS ? var2 + TwitchStream.func_152948_a(var4) + " fps" : (par1EnumOptions == GameSettings.Options.STREAM_KBPS ? var2 + TwitchStream.func_152946_b(var4) + " Kbps" : (par1EnumOptions == GameSettings.Options.STREAM_BYTES_PER_PIXEL ? var2 + String.format("%.3f bpp", TwitchStream.func_152947_c(var4)) : (var4 == 0.0F ? var2 + I18n.format("options.off") : var2 + (int)(var4 * 100.0F) + "%")))))))))))))));
                  } else if (par1EnumOptions.getEnumBoolean()) {
                        boolean var5 = this.getOptionOrdinalValue(par1EnumOptions);
                        return var5 ? var2 + I18n.format("options.on") : var2 + I18n.format("options.off");
                  } else if (par1EnumOptions == GameSettings.Options.DIFFICULTY) {
                        return var2 + I18n.format(this.difficulty.getDifficultyResourceKey());
                  } else if (par1EnumOptions == GameSettings.Options.GUI_SCALE) {
                        return var2 + getTranslation(GUISCALES, this.guiScale);
                  } else if (par1EnumOptions == GameSettings.Options.CHAT_VISIBILITY) {
                        return var2 + I18n.format(this.chatVisibility.getResourceKey());
                  } else if (par1EnumOptions == GameSettings.Options.PARTICLES) {
                        return var2 + getTranslation(PARTICLES, this.particleSetting);
                  } else if (par1EnumOptions == GameSettings.Options.AMBIENT_OCCLUSION) {
                        return var2 + getTranslation(AMBIENT_OCCLUSIONS, this.ambientOcclusion);
                  } else if (par1EnumOptions == GameSettings.Options.STREAM_COMPRESSION) {
                        return var2 + getTranslation(field_152391_aS, this.field_152405_O);
                  } else if (par1EnumOptions == GameSettings.Options.STREAM_CHAT_ENABLED) {
                        return var2 + getTranslation(field_152392_aT, this.field_152408_R);
                  } else if (par1EnumOptions == GameSettings.Options.STREAM_CHAT_USER_FILTER) {
                        return var2 + getTranslation(field_152393_aU, this.field_152409_S);
                  } else if (par1EnumOptions == GameSettings.Options.STREAM_MIC_TOGGLE_BEHAVIOR) {
                        return var2 + getTranslation(field_152394_aV, this.field_152410_T);
                  } else if (par1EnumOptions == GameSettings.Options.GRAPHICS) {
                        if (this.fancyGraphics) {
                              return var2 + I18n.format("options.graphics.fancy");
                        } else {
                              String var3 = "options.graphics.fast";
                              return var2 + I18n.format("options.graphics.fast");
                        }
                  } else {
                        return var2;
                  }
            }
      }

    /**
     * Loads the options from the options file. It appears that this has replaced the previous 'loadOptions'
     */
      public void loadOptions() {
            try {
                  if (!this.optionsFile.exists()) {
                        return;
                  }

                  BufferedReader var1 = new BufferedReader(new FileReader(this.optionsFile));
                  String var2 = "";
                  this.mapSoundLevels.clear();

                  while((var2 = var1.readLine()) != null) {
                        try {
                              String[] var3 = var2.split(":");
                              if (var3[0].equals("mouseSensitivity")) {
                                    this.mouseSensitivity = this.parseFloat(var3[1]);
                              }

                              if (var3[0].equals("invertYMouse")) {
                                    this.invertMouse = var3[1].equals("true");
                              }

                              if (var3[0].equals("fov")) {
                                    this.fovSetting = this.parseFloat(var3[1]);
                              }

                              if (var3[0].equals("gamma")) {
                                    this.gammaSetting = this.parseFloat(var3[1]);
                              }

                              if (var3[0].equals("saturation")) {
                                    this.saturation = this.parseFloat(var3[1]);
                              }

                              if (var3[0].equals("fov")) {
                                    this.fovSetting = this.parseFloat(var3[1]) * 40.0F + 70.0F;
                              }

                              if (var3[0].equals("renderDistance")) {
                                    this.renderDistanceChunks = Integer.parseInt(var3[1]);
                              }

                              if (var3[0].equals("guiScale")) {
                                    this.guiScale = Integer.parseInt(var3[1]);
                              }

                              if (var3[0].equals("particles")) {
                                    this.particleSetting = Integer.parseInt(var3[1]);
                              }

                              if (var3[0].equals("bobView")) {
                                    this.viewBobbing = var3[1].equals("true");
                              }

                              if (var3[0].equals("anaglyph3d")) {
                                    this.anaglyph = var3[1].equals("true");
                              }

                              if (var3[0].equals("advancedOpengl")) {
                                    this.advancedOpengl = var3[1].equals("true");
                              }

                              if (var3[0].equals("maxFps")) {
                                    this.limitFramerate = Integer.parseInt(var3[1]);
                                    this.enableVsync = false;
                                    if (this.limitFramerate <= 0) {
                                          this.limitFramerate = (int)GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
                                          this.enableVsync = true;
                                    }

                                    this.updateVSync();
                              }

                              if (var3[0].equals("fboEnable")) {
                                    this.fboEnable = var3[1].equals("true");
                              }

                              if (var3[0].equals("difficulty")) {
                                    this.difficulty = EnumDifficulty.getDifficultyEnum(Integer.parseInt(var3[1]));
                              }

                              if (var3[0].equals("fancyGraphics")) {
                                    this.fancyGraphics = var3[1].equals("true");
                              }

                              if (var3[0].equals("ao")) {
                                    if (var3[1].equals("true")) {
                                          this.ambientOcclusion = 2;
                                    } else if (var3[1].equals("false")) {
                                          this.ambientOcclusion = 0;
                                    } else {
                                          this.ambientOcclusion = Integer.parseInt(var3[1]);
                                    }
                              }

                              if (var3[0].equals("clouds")) {
                                    this.clouds = var3[1].equals("true");
                              }

                              if (var3[0].equals("resourcePacks")) {
                                    this.resourcePacks = (List)gson.fromJson(var2.substring(var2.indexOf(58) + 1), typeListString);
                                    if (this.resourcePacks == null) {
                                          this.resourcePacks = new ArrayList();
                                    }
                              }

                              if (var3[0].equals("lastServer") && var3.length >= 2) {
                                    this.lastServer = var2.substring(var2.indexOf(58) + 1);
                              }

                              if (var3[0].equals("lang") && var3.length >= 2) {
                                    this.language = var3[1];
                              }

                              if (var3[0].equals("chatVisibility")) {
                                    this.chatVisibility = EnumChatVisibility.getEnumChatVisibility(Integer.parseInt(var3[1]));
                              }

                              if (var3[0].equals("chatColors")) {
                                    this.chatColours = var3[1].equals("true");
                              }

                              if (var3[0].equals("chatLinks")) {
                                    this.chatLinks = var3[1].equals("true");
                              }

                              if (var3[0].equals("chatLinksPrompt")) {
                                    this.chatLinksPrompt = var3[1].equals("true");
                              }

                              if (var3[0].equals("chatOpacity")) {
                                    this.chatOpacity = this.parseFloat(var3[1]);
                              }

                              if (var3[0].equals("snooperEnabled")) {
                                    this.snooperEnabled = var3[1].equals("true");
                              }

                              if (var3[0].equals("fullscreen")) {
                                    this.fullScreen = var3[1].equals("true");
                              }

                              if (var3[0].equals("enableVsync")) {
                                    this.enableVsync = var3[1].equals("true");
                                    this.updateVSync();
                              }

                              if (var3[0].equals("hideServerAddress")) {
                                    this.hideServerAddress = var3[1].equals("true");
                              }

                              if (var3[0].equals("advancedItemTooltips")) {
                                    this.advancedItemTooltips = var3[1].equals("true");
                              }

                              if (var3[0].equals("pauseOnLostFocus")) {
                                    this.pauseOnLostFocus = var3[1].equals("true");
                              }

                              if (var3[0].equals("showCape")) {
                                    this.showCape = var3[1].equals("true");
                              }

                              if (var3[0].equals("touchscreen")) {
                                    this.touchscreen = var3[1].equals("true");
                              }

                              if (var3[0].equals("overrideHeight")) {
                                    this.overrideHeight = Integer.parseInt(var3[1]);
                              }

                              if (var3[0].equals("overrideWidth")) {
                                    this.overrideWidth = Integer.parseInt(var3[1]);
                              }

                              if (var3[0].equals("heldItemTooltips")) {
                                    this.heldItemTooltips = var3[1].equals("true");
                              }

                              if (var3[0].equals("chatHeightFocused")) {
                                    this.chatHeightFocused = this.parseFloat(var3[1]);
                              }

                              if (var3[0].equals("chatHeightUnfocused")) {
                                    this.chatHeightUnfocused = this.parseFloat(var3[1]);
                              }

                              if (var3[0].equals("chatScale")) {
                                    this.chatScale = this.parseFloat(var3[1]);
                              }

                              if (var3[0].equals("chatWidth")) {
                                    this.chatWidth = this.parseFloat(var3[1]);
                              }

                              if (var3[0].equals("showInventoryAchievementHint")) {
                                    this.showInventoryAchievementHint = var3[1].equals("true");
                              }

                              if (var3[0].equals("mipmapLevels")) {
                                    this.mipmapLevels = Integer.parseInt(var3[1]);
                              }

                              if (var3[0].equals("anisotropicFiltering")) {
                                    this.anisotropicFiltering = Integer.parseInt(var3[1]);
                              }

                              if (var3[0].equals("streamBytesPerPixel")) {
                                    this.field_152400_J = this.parseFloat(var3[1]);
                              }

                              if (var3[0].equals("streamMicVolume")) {
                                    this.field_152401_K = this.parseFloat(var3[1]);
                              }

                              if (var3[0].equals("streamSystemVolume")) {
                                    this.field_152402_L = this.parseFloat(var3[1]);
                              }

                              if (var3[0].equals("streamKbps")) {
                                    this.field_152403_M = this.parseFloat(var3[1]);
                              }

                              if (var3[0].equals("streamFps")) {
                                    this.field_152404_N = this.parseFloat(var3[1]);
                              }

                              if (var3[0].equals("streamCompression")) {
                                    this.field_152405_O = Integer.parseInt(var3[1]);
                              }

                              if (var3[0].equals("streamSendMetadata")) {
                                    this.field_152406_P = var3[1].equals("true");
                              }

                              if (var3[0].equals("streamPreferredServer") && var3.length >= 2) {
                                    this.field_152407_Q = var2.substring(var2.indexOf(58) + 1);
                              }

                              if (var3[0].equals("streamChatEnabled")) {
                                    this.field_152408_R = Integer.parseInt(var3[1]);
                              }

                              if (var3[0].equals("streamChatUserFilter")) {
                                    this.field_152409_S = Integer.parseInt(var3[1]);
                              }

                              if (var3[0].equals("streamMicToggleBehavior")) {
                                    this.field_152410_T = Integer.parseInt(var3[1]);
                              }

                              if (var3[0].equals("forceUnicodeFont")) {
                                    this.forceUnicodeFont = var3[1].equals("true");
                              }

                              KeyBinding[] var4 = this.keyBindings;
                              int var5 = var4.length;

                              int var6;
                              for(var6 = 0; var6 < var5; ++var6) {
                                    KeyBinding var7 = var4[var6];
                                    if (var3[0].equals("key_" + var7.getKeyDescription())) {
                                          var7.setKeyCode(Integer.parseInt(var3[1]));
                                    }
                              }

                              SoundCategory[] var10 = SoundCategory.values();
                              var5 = var10.length;

                              for(var6 = 0; var6 < var5; ++var6) {
                                    SoundCategory var11 = var10[var6];
                                    if (var3[0].equals("soundCategory_" + var11.getCategoryName())) {
                                          this.mapSoundLevels.put(var11, this.parseFloat(var3[1]));
                                    }
                              }
                        } catch (Exception var9) {
                              logger.warn("Skipping bad option: " + var2);
                              var9.printStackTrace();
                        }
                  }

                  KeyBinding.resetKeyBindingArrayAndHash();
                  var1.close();
            } catch (Exception var10) {
                  logger.error("Failed to load options", var10);
            }

            this.loadOfOptions();
      }

    /**
     * Parses a string into a float.
     */
      private float parseFloat(String par1Str) {
          return par1Str.equals("true") ? 1.0F : (par1Str.equals("false") ? 0.0F : Float.parseFloat(par1Str));
    }

    /**
     * Saves the options to the options file.
     */
    public void saveOptions()
    {
        if (FMLClientHandler.instance().isLoading()) return;
        try
        {
            PrintWriter printwriter = new PrintWriter(new FileWriter(this.optionsFile));
            printwriter.println("invertYMouse:" + this.invertMouse);
            printwriter.println("mouseSensitivity:" + this.mouseSensitivity);
            printwriter.println("fov:" + (this.fovSetting - 70.0F) / 40.0F);
            printwriter.println("gamma:" + this.gammaSetting);
            printwriter.println("saturation:" + this.saturation);
            printwriter.println("renderDistance:" + this.renderDistanceChunks);
            printwriter.println("guiScale:" + this.guiScale);
            printwriter.println("particles:" + this.particleSetting);
            printwriter.println("bobView:" + this.viewBobbing);
            printwriter.println("anaglyph3d:" + this.anaglyph);
            printwriter.println("advancedOpengl:" + this.advancedOpengl);
            printwriter.println("maxFps:" + this.limitFramerate);
            printwriter.println("fboEnable:" + this.fboEnable);
            printwriter.println("difficulty:" + this.difficulty.getDifficultyId());
            printwriter.println("fancyGraphics:" + this.fancyGraphics);
            printwriter.println("ao:" + this.ambientOcclusion);
            printwriter.println("clouds:" + this.clouds);
            printwriter.println("resourcePacks:" + gson.toJson(this.resourcePacks));
            printwriter.println("lastServer:" + this.lastServer);
            printwriter.println("lang:" + this.language);
            printwriter.println("chatVisibility:" + this.chatVisibility.getChatVisibility());
            printwriter.println("chatColors:" + this.chatColours);
            printwriter.println("chatLinks:" + this.chatLinks);
            printwriter.println("chatLinksPrompt:" + this.chatLinksPrompt);
            printwriter.println("chatOpacity:" + this.chatOpacity);
            printwriter.println("snooperEnabled:" + this.snooperEnabled);
            printwriter.println("fullscreen:" + this.fullScreen);
            printwriter.println("enableVsync:" + this.enableVsync);
            printwriter.println("hideServerAddress:" + this.hideServerAddress);
            printwriter.println("advancedItemTooltips:" + this.advancedItemTooltips);
            printwriter.println("pauseOnLostFocus:" + this.pauseOnLostFocus);
            printwriter.println("showCape:" + this.showCape);
            printwriter.println("touchscreen:" + this.touchscreen);
            printwriter.println("overrideWidth:" + this.overrideWidth);
            printwriter.println("overrideHeight:" + this.overrideHeight);
            printwriter.println("heldItemTooltips:" + this.heldItemTooltips);
            printwriter.println("chatHeightFocused:" + this.chatHeightFocused);
            printwriter.println("chatHeightUnfocused:" + this.chatHeightUnfocused);
            printwriter.println("chatScale:" + this.chatScale);
            printwriter.println("chatWidth:" + this.chatWidth);
            printwriter.println("showInventoryAchievementHint:" + this.showInventoryAchievementHint);
            printwriter.println("mipmapLevels:" + this.mipmapLevels);
            printwriter.println("anisotropicFiltering:" + this.anisotropicFiltering);
            printwriter.println("streamBytesPerPixel:" + this.field_152400_J);
            printwriter.println("streamMicVolume:" + this.field_152401_K);
            printwriter.println("streamSystemVolume:" + this.field_152402_L);
            printwriter.println("streamKbps:" + this.field_152403_M);
            printwriter.println("streamFps:" + this.field_152404_N);
            printwriter.println("streamCompression:" + this.field_152405_O);
            printwriter.println("streamSendMetadata:" + this.field_152406_P);
            printwriter.println("streamPreferredServer:" + this.field_152407_Q);
            printwriter.println("streamChatEnabled:" + this.field_152408_R);
            printwriter.println("streamChatUserFilter:" + this.field_152409_S);
            printwriter.println("streamMicToggleBehavior:" + this.field_152410_T);
            printwriter.println("forceUnicodeFont:" + this.forceUnicodeFont);
            KeyBinding[] akeybinding = this.keyBindings;
            int i = akeybinding.length;
            int j;

            for (j = 0; j < i; ++j)
            {
                KeyBinding keybinding = akeybinding[j];
                printwriter.println("key_" + keybinding.getKeyDescription() + ":" + keybinding.getKeyCode());
            }

            SoundCategory[] asoundcategory = SoundCategory.values();
            i = asoundcategory.length;

            for (j = 0; j < i; ++j)
            {
                SoundCategory soundcategory = asoundcategory[j];
                printwriter.println("soundCategory_" + soundcategory.getCategoryName() + ":" + this.getSoundLevel(soundcategory));
            }

            printwriter.close();
        }
        catch (Exception exception)
        {
            logger.error("Failed to save options", exception);
        }

        this.sendSettingsToServer();
    }

    public float getSoundLevel(SoundCategory p_151438_1_) {
        return this.mapSoundLevels.containsKey(p_151438_1_) ? (Float)this.mapSoundLevels.get(p_151438_1_) : 1.0F;
  }

    public void setSoundLevel(SoundCategory p_151439_1_, float p_151439_2_)
    {
        this.mc.getSoundHandler().setSoundLevel(p_151439_1_, p_151439_2_);
        this.mapSoundLevels.put(p_151439_1_, Float.valueOf(p_151439_2_));
    }

    /**
     * Send a client info packet with settings information to the server
     */
    public void sendSettingsToServer() {
        if (this.mc.thePlayer != null) {
              this.mc.thePlayer.sendQueue.addToSendQueue(new C15PacketClientSettings(this.language, this.renderDistanceChunks, this.chatVisibility, this.chatColours, this.difficulty, this.showCape));
        }

  }

    /**
     * Should render clouds
     */
    public boolean shouldRenderClouds()
    {
        return this.renderDistanceChunks >= 4 && this.clouds;
    }

    /*
     * Optifine 
     **/ 
    public void loadOfOptions() {
        try {
              File ofReadFile = this.optionsFileOF;
              if (!ofReadFile.exists()) {
                    ofReadFile = this.optionsFile;
              }

              if (!ofReadFile.exists()) {
                    return;
              }

              BufferedReader bufferedreader = new BufferedReader(new FileReader(ofReadFile));
              String s = "";

              while((s = bufferedreader.readLine()) != null) {
                    try {
                          String[] as = s.split(":");
                          if (as[0].equals("ofRenderDistanceChunks") && as.length >= 2) {
                                this.renderDistanceChunks = Integer.valueOf(as[1]);
                                this.renderDistanceChunks = Config.limit(this.renderDistanceChunks, 2, 32);
                          }

                          if (as[0].equals("ofFogType") && as.length >= 2) {
                                this.ofFogType = Integer.valueOf(as[1]);
                                this.ofFogType = Config.limit(this.ofFogType, 1, 3);
                          }

                          if (as[0].equals("ofFogStart") && as.length >= 2) {
                                this.ofFogStart = Float.valueOf(as[1]);
                                if (this.ofFogStart < 0.2F) {
                                      this.ofFogStart = 0.2F;
                                }

                                if (this.ofFogStart > 0.81F) {
                                      this.ofFogStart = 0.8F;
                                }
                          }

                          if (as[0].equals("ofMipmapType") && as.length >= 2) {
                                this.ofMipmapType = Integer.valueOf(as[1]);
                                this.ofMipmapType = Config.limit(this.ofMipmapType, 0, 3);
                          }

                          if (as[0].equals("ofLoadFar") && as.length >= 2) {
                                this.ofLoadFar = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofPreloadedChunks") && as.length >= 2) {
                                this.ofPreloadedChunks = Integer.valueOf(as[1]);
                                if (this.ofPreloadedChunks < 0) {
                                      this.ofPreloadedChunks = 0;
                                }

                                if (this.ofPreloadedChunks > 8) {
                                      this.ofPreloadedChunks = 8;
                                }
                          }

                          if (as[0].equals("ofOcclusionFancy") && as.length >= 2) {
                                this.ofOcclusionFancy = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofSmoothFps") && as.length >= 2) {
                                this.ofSmoothFps = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofSmoothWorld") && as.length >= 2) {
                                this.ofSmoothWorld = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofAoLevel") && as.length >= 2) {
                                this.ofAoLevel = Float.valueOf(as[1]);
                                this.ofAoLevel = Config.limit(this.ofAoLevel, 0.0F, 1.0F);
                          }

                          if (as[0].equals("ofClouds") && as.length >= 2) {
                                this.ofClouds = Integer.valueOf(as[1]);
                                this.ofClouds = Config.limit(this.ofClouds, 0, 3);
                          }

                          if (as[0].equals("ofCloudsHeight") && as.length >= 2) {
                                this.ofCloudsHeight = Float.valueOf(as[1]);
                                this.ofCloudsHeight = Config.limit(this.ofCloudsHeight, 0.0F, 1.0F);
                          }

                          if (as[0].equals("ofTrees") && as.length >= 2) {
                                this.ofTrees = Integer.valueOf(as[1]);
                                this.ofTrees = Config.limit(this.ofTrees, 0, 2);
                          }

                          if (as[0].equals("ofGrass") && as.length >= 2) {
                                this.ofGrass = Integer.valueOf(as[1]);
                                this.ofGrass = Config.limit(this.ofGrass, 0, 2);
                          }

                          if (as[0].equals("ofDroppedItems") && as.length >= 2) {
                                this.ofDroppedItems = Integer.valueOf(as[1]);
                                this.ofDroppedItems = Config.limit(this.ofDroppedItems, 0, 2);
                          }

                          if (as[0].equals("ofRain") && as.length >= 2) {
                                this.ofRain = Integer.valueOf(as[1]);
                                this.ofRain = Config.limit(this.ofRain, 0, 3);
                          }

                          if (as[0].equals("ofWater") && as.length >= 2) {
                                this.ofWater = Integer.valueOf(as[1]);
                                this.ofWater = Config.limit(this.ofWater, 0, 3);
                          }

                          if (as[0].equals("ofAnimatedWater") && as.length >= 2) {
                                this.ofAnimatedWater = Integer.valueOf(as[1]);
                                this.ofAnimatedWater = Config.limit(this.ofAnimatedWater, 0, 2);
                          }

                          if (as[0].equals("ofAnimatedLava") && as.length >= 2) {
                                this.ofAnimatedLava = Integer.valueOf(as[1]);
                                this.ofAnimatedLava = Config.limit(this.ofAnimatedLava, 0, 2);
                          }

                          if (as[0].equals("ofAnimatedFire") && as.length >= 2) {
                                this.ofAnimatedFire = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofAnimatedPortal") && as.length >= 2) {
                                this.ofAnimatedPortal = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofAnimatedRedstone") && as.length >= 2) {
                                this.ofAnimatedRedstone = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofAnimatedExplosion") && as.length >= 2) {
                                this.ofAnimatedExplosion = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofAnimatedFlame") && as.length >= 2) {
                                this.ofAnimatedFlame = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofAnimatedSmoke") && as.length >= 2) {
                                this.ofAnimatedSmoke = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofVoidParticles") && as.length >= 2) {
                                this.ofVoidParticles = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofWaterParticles") && as.length >= 2) {
                                this.ofWaterParticles = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofPortalParticles") && as.length >= 2) {
                                this.ofPortalParticles = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofPotionParticles") && as.length >= 2) {
                                this.ofPotionParticles = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofDrippingWaterLava") && as.length >= 2) {
                                this.ofDrippingWaterLava = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofAnimatedTerrain") && as.length >= 2) {
                                this.ofAnimatedTerrain = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofAnimatedTextures") && as.length >= 2) {
                                this.ofAnimatedTextures = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofAnimatedItems") && as.length >= 2) {
                                this.ofAnimatedItems = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofRainSplash") && as.length >= 2) {
                                this.ofRainSplash = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofLagometer") && as.length >= 2) {
                                this.ofLagometer = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofShowFps") && as.length >= 2) {
                                this.ofShowFps = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofAutoSaveTicks") && as.length >= 2) {
                                this.ofAutoSaveTicks = Integer.valueOf(as[1]);
                                this.ofAutoSaveTicks = Config.limit(this.ofAutoSaveTicks, 40, 40000);
                          }

                          if (as[0].equals("ofBetterGrass") && as.length >= 2) {
                                this.ofBetterGrass = Integer.valueOf(as[1]);
                                this.ofBetterGrass = Config.limit(this.ofBetterGrass, 1, 3);
                          }

                          if (as[0].equals("ofConnectedTextures") && as.length >= 2) {
                                this.ofConnectedTextures = Integer.valueOf(as[1]);
                                this.ofConnectedTextures = Config.limit(this.ofConnectedTextures, 1, 3);
                          }

                          if (as[0].equals("ofWeather") && as.length >= 2) {
                                this.ofWeather = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofSky") && as.length >= 2) {
                                this.ofSky = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofStars") && as.length >= 2) {
                                this.ofStars = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofSunMoon") && as.length >= 2) {
                                this.ofSunMoon = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofVignette") && as.length >= 2) {
                                this.ofVignette = Integer.valueOf(as[1]);
                                this.ofVignette = Config.limit(this.ofVignette, 0, 2);
                          }

                          if (as[0].equals("ofChunkUpdates") && as.length >= 2) {
                                this.ofChunkUpdates = Integer.valueOf(as[1]);
                                this.ofChunkUpdates = Config.limit(this.ofChunkUpdates, 1, 5);
                          }

                          if (as[0].equals("ofChunkLoading") && as.length >= 2) {
                                this.ofChunkLoading = Integer.valueOf(as[1]);
                                this.ofChunkLoading = Config.limit(this.ofChunkLoading, 0, 2);
                                this.updateChunkLoading();
                          }

                          if (as[0].equals("ofChunkUpdatesDynamic") && as.length >= 2) {
                                this.ofChunkUpdatesDynamic = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofTime") && as.length >= 2) {
                                this.ofTime = Integer.valueOf(as[1]);
                                this.ofTime = Config.limit(this.ofTime, 0, 3);
                          }

                          if (as[0].equals("ofClearWater") && as.length >= 2) {
                                this.ofClearWater = Boolean.valueOf(as[1]);
                                this.updateWaterOpacity();
                          }

                          if (as[0].equals("ofDepthFog") && as.length >= 2) {
                                this.ofDepthFog = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofAaLevel") && as.length >= 2) {
                                this.ofAaLevel = Integer.valueOf(as[1]);
                                this.ofAaLevel = Config.limit(this.ofAaLevel, 0, 16);
                          }

                          if (as[0].equals("ofProfiler") && as.length >= 2) {
                                this.ofProfiler = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofBetterSnow") && as.length >= 2) {
                                this.ofBetterSnow = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofSwampColors") && as.length >= 2) {
                                this.ofSwampColors = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofRandomMobs") && as.length >= 2) {
                                this.ofRandomMobs = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofSmoothBiomes") && as.length >= 2) {
                                this.ofSmoothBiomes = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofCustomFonts") && as.length >= 2) {
                                this.ofCustomFonts = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofCustomColors") && as.length >= 2) {
                                this.ofCustomColors = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofCustomSky") && as.length >= 2) {
                                this.ofCustomSky = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofShowCapes") && as.length >= 2) {
                                this.ofShowCapes = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofNaturalTextures") && as.length >= 2) {
                                this.ofNaturalTextures = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofLazyChunkLoading") && as.length >= 2) {
                                this.ofLazyChunkLoading = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofDynamicFov") && as.length >= 2) {
                                this.ofDynamicFov = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofDynamicLights") && as.length >= 2) {
                                this.ofDynamicLights = Integer.valueOf(as[1]);
                                this.ofDynamicLights = Config.limit(this.ofDynamicLights, 1, 3);
                          }

                          if (as[0].equals("ofFullscreenMode") && as.length >= 2) {
                                this.ofFullscreenMode = as[1];
                          }

                          if (as[0].equals("ofFastMath") && as.length >= 2) {
                                this.ofFastMath = Boolean.valueOf(as[1]);
                                MathHelper.fastMath = this.ofFastMath;
                          }

                          if (as[0].equals("ofFastRender") && as.length >= 2) {
                                this.ofFastRender = Boolean.valueOf(as[1]);
                          }

                          if (as[0].equals("ofTranslucentBlocks") && as.length >= 2) {
                                this.ofTranslucentBlocks = Integer.valueOf(as[1]);
                                this.ofTranslucentBlocks = Config.limit(this.ofTranslucentBlocks, 1, 2);
                          }
                    } catch (Exception var5) {
                          Config.dbg("Skipping bad option: " + s);
                          var5.printStackTrace();
                    }
              }

              KeyBinding.resetKeyBindingArrayAndHash();
              bufferedreader.close();
        } catch (Exception var6) {
              Config.warn("Failed to load options");
              var6.printStackTrace();
        }

  }

    /*
     * Optifine 
     **/ 
    public void saveOfOptions() {
        try {
              PrintWriter printwriter = new PrintWriter(new FileWriter(this.optionsFileOF));
              printwriter.println("ofRenderDistanceChunks:" + this.renderDistanceChunks);
              printwriter.println("ofFogType:" + this.ofFogType);
              printwriter.println("ofFogStart:" + this.ofFogStart);
              printwriter.println("ofMipmapType:" + this.ofMipmapType);
              printwriter.println("ofLoadFar:" + this.ofLoadFar);
              printwriter.println("ofPreloadedChunks:" + this.ofPreloadedChunks);
              printwriter.println("ofOcclusionFancy:" + this.ofOcclusionFancy);
              printwriter.println("ofSmoothFps:" + this.ofSmoothFps);
              printwriter.println("ofSmoothWorld:" + this.ofSmoothWorld);
              printwriter.println("ofAoLevel:" + this.ofAoLevel);
              printwriter.println("ofClouds:" + this.ofClouds);
              printwriter.println("ofCloudsHeight:" + this.ofCloudsHeight);
              printwriter.println("ofTrees:" + this.ofTrees);
              printwriter.println("ofGrass:" + this.ofGrass);
              printwriter.println("ofDroppedItems:" + this.ofDroppedItems);
              printwriter.println("ofRain:" + this.ofRain);
              printwriter.println("ofWater:" + this.ofWater);
              printwriter.println("ofAnimatedWater:" + this.ofAnimatedWater);
              printwriter.println("ofAnimatedLava:" + this.ofAnimatedLava);
              printwriter.println("ofAnimatedFire:" + this.ofAnimatedFire);
              printwriter.println("ofAnimatedPortal:" + this.ofAnimatedPortal);
              printwriter.println("ofAnimatedRedstone:" + this.ofAnimatedRedstone);
              printwriter.println("ofAnimatedExplosion:" + this.ofAnimatedExplosion);
              printwriter.println("ofAnimatedFlame:" + this.ofAnimatedFlame);
              printwriter.println("ofAnimatedSmoke:" + this.ofAnimatedSmoke);
              printwriter.println("ofVoidParticles:" + this.ofVoidParticles);
              printwriter.println("ofWaterParticles:" + this.ofWaterParticles);
              printwriter.println("ofPortalParticles:" + this.ofPortalParticles);
              printwriter.println("ofPotionParticles:" + this.ofPotionParticles);
              printwriter.println("ofDrippingWaterLava:" + this.ofDrippingWaterLava);
              printwriter.println("ofAnimatedTerrain:" + this.ofAnimatedTerrain);
              printwriter.println("ofAnimatedTextures:" + this.ofAnimatedTextures);
              printwriter.println("ofAnimatedItems:" + this.ofAnimatedItems);
              printwriter.println("ofRainSplash:" + this.ofRainSplash);
              printwriter.println("ofLagometer:" + this.ofLagometer);
              printwriter.println("ofShowFps:" + this.ofShowFps);
              printwriter.println("ofAutoSaveTicks:" + this.ofAutoSaveTicks);
              printwriter.println("ofBetterGrass:" + this.ofBetterGrass);
              printwriter.println("ofConnectedTextures:" + this.ofConnectedTextures);
              printwriter.println("ofWeather:" + this.ofWeather);
              printwriter.println("ofSky:" + this.ofSky);
              printwriter.println("ofStars:" + this.ofStars);
              printwriter.println("ofSunMoon:" + this.ofSunMoon);
              printwriter.println("ofVignette:" + this.ofVignette);
              printwriter.println("ofChunkUpdates:" + this.ofChunkUpdates);
              printwriter.println("ofChunkLoading:" + this.ofChunkLoading);
              printwriter.println("ofChunkUpdatesDynamic:" + this.ofChunkUpdatesDynamic);
              printwriter.println("ofTime:" + this.ofTime);
              printwriter.println("ofClearWater:" + this.ofClearWater);
              printwriter.println("ofDepthFog:" + this.ofDepthFog);
              printwriter.println("ofAaLevel:" + this.ofAaLevel);
              printwriter.println("ofProfiler:" + this.ofProfiler);
              printwriter.println("ofBetterSnow:" + this.ofBetterSnow);
              printwriter.println("ofSwampColors:" + this.ofSwampColors);
              printwriter.println("ofRandomMobs:" + this.ofRandomMobs);
              printwriter.println("ofSmoothBiomes:" + this.ofSmoothBiomes);
              printwriter.println("ofCustomFonts:" + this.ofCustomFonts);
              printwriter.println("ofCustomColors:" + this.ofCustomColors);
              printwriter.println("ofCustomSky:" + this.ofCustomSky);
              printwriter.println("ofShowCapes:" + this.ofShowCapes);
              printwriter.println("ofNaturalTextures:" + this.ofNaturalTextures);
              printwriter.println("ofLazyChunkLoading:" + this.ofLazyChunkLoading);
              printwriter.println("ofDynamicFov:" + this.ofDynamicFov);
              printwriter.println("ofDynamicLights:" + this.ofDynamicLights);
              printwriter.println("ofFullscreenMode:" + this.ofFullscreenMode);
              printwriter.println("ofFastMath:" + this.ofFastMath);
              printwriter.println("ofFastRender:" + this.ofFastRender);
              printwriter.println("ofTranslucentBlocks:" + this.ofTranslucentBlocks);
              printwriter.close();
        } catch (Exception var2) {
              Config.warn("Failed to save options");
              var2.printStackTrace();
        }

  }

    public void resetSettings()
    {
        this.renderDistanceChunks = 8;
        this.viewBobbing = true;
        this.anaglyph = false;
        this.advancedOpengl = false;
        this.limitFramerate = (int)GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
        this.enableVsync = false;
        this.updateVSync();
        this.mipmapLevels = 4;
        this.anisotropicFiltering = 1;
        this.fancyGraphics = true;
        this.ambientOcclusion = 2;
        this.clouds = true;
        this.fovSetting = 70.0F;
        this.gammaSetting = 0.0F;
        this.guiScale = 0;
        this.particleSetting = 0;
        this.heldItemTooltips = true;
        this.ofFogType = 1;
        this.ofFogStart = 0.8F;
        this.ofMipmapType = 0;
        this.ofLoadFar = false;
        this.ofPreloadedChunks = 0;
        this.ofOcclusionFancy = false;
        this.ofSmoothFps = false;
        Config.updateAvailableProcessors();
        this.ofSmoothWorld = Config.isSingleProcessor();
        this.ofLazyChunkLoading = Config.isSingleProcessor();
        this.ofFastMath = false;
        this.ofFastRender = false;
        this.ofTranslucentBlocks = 2;
        this.ofDynamicFov = true;
        this.ofDynamicLights = 3;
        this.ofAoLevel = 1.0F;
        this.ofAaLevel = 0;
        this.ofClouds = 0;
        this.ofCloudsHeight = 0.0F;
        this.ofTrees = 0;
        this.ofGrass = 0;
        this.ofRain = 0;
        this.ofWater = 0;
        this.ofBetterGrass = 3;
        this.ofAutoSaveTicks = 4000;
        this.ofLagometer = false;
        this.ofShowFps = false;
        this.ofProfiler = false;
        this.ofWeather = true;
        this.ofSky = true;
        this.ofStars = true;
        this.ofSunMoon = true;
        this.ofVignette = 0;
        this.ofChunkUpdates = 1;
        this.ofChunkLoading = 0;
        this.ofChunkUpdatesDynamic = false;
        this.ofTime = 0;
        this.ofClearWater = false;
        this.ofDepthFog = true;
        this.ofBetterSnow = false;
        this.ofFullscreenMode = "Default";
        this.ofSwampColors = true;
        this.ofRandomMobs = true;
        this.ofSmoothBiomes = true;
        this.ofCustomFonts = true;
        this.ofCustomColors = true;
        this.ofCustomSky = true;
        this.ofShowCapes = true;
        this.ofConnectedTextures = 2;
        this.ofNaturalTextures = false;
        this.ofAnimatedWater = 0;
        this.ofAnimatedLava = 0;
        this.ofAnimatedFire = true;
        this.ofAnimatedPortal = true;
        this.ofAnimatedRedstone = true;
        this.ofAnimatedExplosion = true;
        this.ofAnimatedFlame = true;
        this.ofAnimatedSmoke = true;
        this.ofVoidParticles = true;
        this.ofWaterParticles = true;
        this.ofRainSplash = true;
        this.ofPortalParticles = true;
        this.ofPotionParticles = true;
        this.ofDrippingWaterLava = true;
        this.ofAnimatedTerrain = true;
        this.ofAnimatedItems = true;
        this.ofAnimatedTextures = true;
        Shaders.setShaderPack(Shaders.packNameNone);
        Shaders.configAntialiasingLevel = 0;
        Shaders.uninit();
        Shaders.storeConfig();
        this.mc.renderGlobal.updateCapes();
        this.updateWaterOpacity();
        this.mc.renderGlobal.setAllRenderersVisible();
        this.mc.refreshResources();
        this.saveOptions();
    }

    
    public void updateVSync() {
        Display.setVSyncEnabled(this.enableVsync);
  }

    private void updateWaterOpacity() {
        if (this.mc.isIntegratedServerRunning() && this.mc.getIntegratedServer() != null) {
              Config.waterOpacityChanged = true;
        }

        ClearWater.updateWaterOpacity(this, this.mc.theWorld);
  }

    public void updateChunkLoading() {
        switch(this.ofChunkLoading) {
        case 1:
              WrUpdates.setWrUpdater(new WrUpdaterSmooth());
              break;
        case 2:
              WrUpdates.setWrUpdater(new WrUpdaterThreaded());
              break;
        default:
              WrUpdates.setWrUpdater((IWrUpdater)null);
        }

        if (this.mc.renderGlobal != null) {
              this.mc.renderGlobal.loadRenderers();
        }

  }

    public void setAllAnimations(boolean flag) {
        int animVal = flag ? 0 : 2;
        this.ofAnimatedWater = animVal;
        this.ofAnimatedLava = animVal;
        this.ofAnimatedFire = flag;
        this.ofAnimatedPortal = flag;
        this.ofAnimatedRedstone = flag;
        this.ofAnimatedExplosion = flag;
        this.ofAnimatedFlame = flag;
        this.ofAnimatedSmoke = flag;
        this.ofVoidParticles = flag;
        this.ofWaterParticles = flag;
        this.ofRainSplash = flag;
        this.ofPortalParticles = flag;
        this.ofPotionParticles = flag;
        this.particleSetting = flag ? 0 : 2;
        this.ofDrippingWaterLava = flag;
        this.ofAnimatedTerrain = flag;
        this.ofAnimatedItems = flag;
        this.ofAnimatedTextures = flag;
  }

    private static int nextValue(int val, int[] vals) {
        int index = indexOf(val, vals);
        if (index < 0) {
              return vals[0];
        } else {
              ++index;
              if (index >= vals.length) {
                    index = 0;
              }

              return vals[index];
        }
  }

    private static int limit(int val, int[] vals) {
        int index = indexOf(val, vals);
        return index < 0 ? vals[0] : val;
  }

    private static int indexOf(int val, int[] vals) {
        for(int i = 0; i < vals.length; ++i) {
              if (vals[i] == val) {
                    return i;
              }
        }

        return -1;
  }

    //END OPTIFINE CLASSES
    
    @SideOnly(Side.CLIENT)
    public static enum Options {
            INVERT_MOUSE("INVERT_MOUSE", 0, "options.invertMouse", false, true),
            SENSITIVITY("SENSITIVITY", 1, "options.sensitivity", true, false),
            FOV("FOV", 2, "options.fov", true, false, 30.0F, 110.0F, 1.0F),
            GAMMA("GAMMA", 3, "options.gamma", true, false),
            SATURATION("SATURATION", 4, "options.saturation", true, false),
            RENDER_DISTANCE("RENDER_DISTANCE", 5, "options.renderDistance", true, false, 2.0F, 16.0F, 1.0F),
            VIEW_BOBBING("VIEW_BOBBING", 6, "options.viewBobbing", false, true),
            ANAGLYPH("ANAGLYPH", 7, "options.anaglyph", false, true),
            ADVANCED_OPENGL("ADVANCED_OPENGL", 8, "options.advancedOpengl", false, true),
            FRAMERATE_LIMIT("FRAMERATE_LIMIT", 9, "options.framerateLimit", true, false, 0.0F, 260.0F, 5.0F),
            FBO_ENABLE("FBO_ENABLE", 10, "options.fboEnable", false, true),
            DIFFICULTY("DIFFICULTY", 11, "options.difficulty", false, false),
            GRAPHICS("GRAPHICS", 12, "options.graphics", false, false),
            AMBIENT_OCCLUSION("AMBIENT_OCCLUSION", 13, "options.ao", false, false),
            GUI_SCALE("GUI_SCALE", 14, "options.guiScale", false, false),
            RENDER_CLOUDS("RENDER_CLOUDS", 15, "options.renderClouds", false, true),
            PARTICLES("PARTICLES", 16, "options.particles", false, false),
            CHAT_VISIBILITY("CHAT_VISIBILITY", 17, "options.chat.visibility", false, false),
            CHAT_COLOR("CHAT_COLOR", 18, "options.chat.color", false, true),
            CHAT_LINKS("CHAT_LINKS", 19, "options.chat.links", false, true),
            CHAT_OPACITY("CHAT_OPACITY", 20, "options.chat.opacity", true, false),
            CHAT_LINKS_PROMPT("CHAT_LINKS_PROMPT", 21, "options.chat.links.prompt", false, true),
            SNOOPER_ENABLED("SNOOPER_ENABLED", 22, "options.snooper", false, true),
            USE_FULLSCREEN("USE_FULLSCREEN", 23, "options.fullscreen", false, true),
            ENABLE_VSYNC("ENABLE_VSYNC", 24, "options.vsync", false, true),
            SHOW_CAPE("SHOW_CAPE", 25, "options.showCape", false, true),
            TOUCHSCREEN("TOUCHSCREEN", 26, "options.touchscreen", false, true),
            CHAT_SCALE("CHAT_SCALE", 27, "options.chat.scale", true, false),
            CHAT_WIDTH("CHAT_WIDTH", 28, "options.chat.width", true, false),
            CHAT_HEIGHT_FOCUSED("CHAT_HEIGHT_FOCUSED", 29, "options.chat.height.focused", true, false),
            CHAT_HEIGHT_UNFOCUSED("CHAT_HEIGHT_UNFOCUSED", 30, "options.chat.height.unfocused", true, false),
            MIPMAP_LEVELS("MIPMAP_LEVELS", 31, "options.mipmapLevels", true, false, 0.0F, 4.0F, 1.0F),
            ANISOTROPIC_FILTERING("ANISOTROPIC_FILTERING", 32, "options.anisotropicFiltering", true, false, 1.0F, 16.0F, 0.0F, (Object)null) {
                  private static final String __OBFID = "CL_00000654";

                  protected float snapToStep(float p_148264_1_) {
                        return (float)MathHelper.roundUpToPowerOfTwo((int)p_148264_1_);
                  }
            },
            FORCE_UNICODE_FONT("FORCE_UNICODE_FONT", 33, "options.forceUnicodeFont", false, true),
            STREAM_BYTES_PER_PIXEL("STREAM_BYTES_PER_PIXEL", 34, "options.stream.bytesPerPixel", true, false),
            STREAM_VOLUME_MIC("STREAM_VOLUME_MIC", 35, "options.stream.micVolumne", true, false),
            STREAM_VOLUME_SYSTEM("STREAM_VOLUME_SYSTEM", 36, "options.stream.systemVolume", true, false),
            STREAM_KBPS("STREAM_KBPS", 37, "options.stream.kbps", true, false),
            STREAM_FPS("STREAM_FPS", 38, "options.stream.fps", true, false),
            STREAM_COMPRESSION("STREAM_COMPRESSION", 39, "options.stream.compression", false, false),
            STREAM_SEND_METADATA("STREAM_SEND_METADATA", 40, "options.stream.sendMetadata", false, true),
            STREAM_CHAT_ENABLED("STREAM_CHAT_ENABLED", 41, "options.stream.chat.enabled", false, false),
            STREAM_CHAT_USER_FILTER("STREAM_CHAT_USER_FILTER", 42, "options.stream.chat.userFilter", false, false),
            STREAM_MIC_TOGGLE_BEHAVIOR("STREAM_MIC_TOGGLE_BEHAVIOR", 43, "options.stream.micToggleBehavior", false, false),
            FOG_FANCY("", 999, "of.options.FOG_FANCY", false, false),
            FOG_START("", 999, "of.options.FOG_START", false, false),
            MIPMAP_TYPE("", 999, "of.options.MIPMAP_TYPE", true, false, 0.0F, 3.0F, 1.0F),
            LOAD_FAR("", 999, "Load Far", false, false),
            PRELOADED_CHUNKS("", 999, "Preloaded Chunks", false, false),
            SMOOTH_FPS("", 999, "of.options.SMOOTH_FPS", false, false),
            CLOUDS("", 999, "of.options.CLOUDS", false, false),
            CLOUD_HEIGHT("", 999, "of.options.CLOUD_HEIGHT", true, false),
            TREES("", 999, "of.options.TREES", false, false),
            GRASS("", 999, "Grass", false, false),
            RAIN("", 999, "of.options.RAIN", false, false),
            WATER("", 999, "Water", false, false),
            ANIMATED_WATER("", 999, "of.options.ANIMATED_WATER", false, false),
            ANIMATED_LAVA("", 999, "of.options.ANIMATED_LAVA", false, false),
            ANIMATED_FIRE("", 999, "of.options.ANIMATED_FIRE", false, false),
            ANIMATED_PORTAL("", 999, "of.options.ANIMATED_PORTAL", false, false),
            AO_LEVEL("", 999, "of.options.AO_LEVEL", true, false),
            LAGOMETER("", 999, "of.options.LAGOMETER", false, false),
            SHOW_FPS("", 999, "of.options.SHOW_FPS", false, false),
            AUTOSAVE_TICKS("", 999, "of.options.AUTOSAVE_TICKS", false, false),
            BETTER_GRASS("", 999, "of.options.BETTER_GRASS", false, false),
            ANIMATED_REDSTONE("", 999, "of.options.ANIMATED_REDSTONE", false, false),
            ANIMATED_EXPLOSION("", 999, "of.options.ANIMATED_EXPLOSION", false, false),
            ANIMATED_FLAME("", 999, "of.options.ANIMATED_FLAME", false, false),
            ANIMATED_SMOKE("", 999, "of.options.ANIMATED_SMOKE", false, false),
            WEATHER("", 999, "of.options.WEATHER", false, false),
            SKY("", 999, "of.options.SKY", false, false),
            STARS("", 999, "of.options.STARS", false, false),
            SUN_MOON("", 999, "of.options.SUN_MOON", false, false),
            VIGNETTE("", 999, "of.options.VIGNETTE", false, false),
            CHUNK_UPDATES("", 999, "of.options.CHUNK_UPDATES", false, false),
            CHUNK_UPDATES_DYNAMIC("", 999, "of.options.CHUNK_UPDATES_DYNAMIC", false, false),
            TIME("", 999, "of.options.TIME", false, false),
            CLEAR_WATER("", 999, "of.options.CLEAR_WATER", false, false),
            SMOOTH_WORLD("", 999, "of.options.SMOOTH_WORLD", false, false),
            DEPTH_FOG("", 999, "Depth Fog", false, false),
            VOID_PARTICLES("", 999, "of.options.VOID_PARTICLES", false, false),
            WATER_PARTICLES("", 999, "of.options.WATER_PARTICLES", false, false),
            RAIN_SPLASH("", 999, "of.options.RAIN_SPLASH", false, false),
            PORTAL_PARTICLES("", 999, "of.options.PORTAL_PARTICLES", false, false),
            POTION_PARTICLES("", 999, "of.options.POTION_PARTICLES", false, false),
            PROFILER("", 999, "of.options.PROFILER", false, false),
            DRIPPING_WATER_LAVA("", 999, "of.options.DRIPPING_WATER_LAVA", false, false),
            BETTER_SNOW("", 999, "of.options.BETTER_SNOW", false, false),
            FULLSCREEN_MODE("", 999, "of.options.FULLSCREEN_MODE", false, false),
            ANIMATED_TERRAIN("", 999, "of.options.ANIMATED_TERRAIN", false, false),
            ANIMATED_ITEMS("", 999, "Items Animated", false, false),
            SWAMP_COLORS("", 999, "of.options.SWAMP_COLORS", false, false),
            RANDOM_MOBS("", 999, "of.options.RANDOM_MOBS", false, false),
            SMOOTH_BIOMES("", 999, "of.options.SMOOTH_BIOMES", false, false),
            CUSTOM_FONTS("", 999, "of.options.CUSTOM_FONTS", false, false),
            CUSTOM_COLORS("", 999, "of.options.CUSTOM_COLORS", false, false),
            SHOW_CAPES("", 999, "of.options.SHOW_CAPES", false, false),
            CONNECTED_TEXTURES("", 999, "of.options.CONNECTED_TEXTURES", false, false),
            AA_LEVEL("", 999, "of.options.AA_LEVEL", true, false, 0.0F, 16.0F, 1.0F),
            ANIMATED_TEXTURES("", 999, "of.options.ANIMATED_TEXTURES", false, false),
            NATURAL_TEXTURES("", 999, "of.options.NATURAL_TEXTURES", false, false),
            CHUNK_LOADING("", 999, "Chunk Loading", false, false),
            HELD_ITEM_TOOLTIPS("", 999, "of.options.HELD_ITEM_TOOLTIPS", false, false),
            DROPPED_ITEMS("", 999, "of.options.DROPPED_ITEMS", false, false),
            LAZY_CHUNK_LOADING("", 999, "of.options.LAZY_CHUNK_LOADING", false, false),
            CUSTOM_SKY("", 999, "of.options.CUSTOM_SKY", false, false),
            FAST_MATH("", 999, "of.options.FAST_MATH", false, false),
            FAST_RENDER("", 999, "of.options.FAST_RENDER", false, false),
            TRANSLUCENT_BLOCKS("", 999, "of.options.TRANSLUCENT_BLOCKS", false, false),
            DYNAMIC_FOV("", 999, "of.options.DYNAMIC_FOV", false, false),
            DYNAMIC_LIGHTS("", 999, "of.options.DYNAMIC_LIGHTS", false, false);

            private final boolean enumFloat;
            private final boolean enumBoolean;
            private final String enumString;
            private final float valueStep;
            private float valueMin;
            private float valueMax;
            private static final GameSettings.Options[] $VALUES = new GameSettings.Options[]{INVERT_MOUSE, SENSITIVITY, FOV, GAMMA, SATURATION, RENDER_DISTANCE, VIEW_BOBBING, ANAGLYPH, ADVANCED_OPENGL, FRAMERATE_LIMIT, FBO_ENABLE, DIFFICULTY, GRAPHICS, AMBIENT_OCCLUSION, GUI_SCALE, RENDER_CLOUDS, PARTICLES, CHAT_VISIBILITY, CHAT_COLOR, CHAT_LINKS, CHAT_OPACITY, CHAT_LINKS_PROMPT, SNOOPER_ENABLED, USE_FULLSCREEN, ENABLE_VSYNC, SHOW_CAPE, TOUCHSCREEN, CHAT_SCALE, CHAT_WIDTH, CHAT_HEIGHT_FOCUSED, CHAT_HEIGHT_UNFOCUSED, MIPMAP_LEVELS, ANISOTROPIC_FILTERING, FORCE_UNICODE_FONT, STREAM_BYTES_PER_PIXEL, STREAM_VOLUME_MIC, STREAM_VOLUME_SYSTEM, STREAM_KBPS, STREAM_FPS, STREAM_COMPRESSION, STREAM_SEND_METADATA, STREAM_CHAT_ENABLED, STREAM_CHAT_USER_FILTER, STREAM_MIC_TOGGLE_BEHAVIOR};
            private static final String __OBFID = "CL_00000653";

            public static GameSettings.Options getEnumOptions(int par0) {
                  GameSettings.Options[] var1 = values();
                  int var2 = var1.length;

                  for(int var3 = 0; var3 < var2; ++var3) {
                        GameSettings.Options var4 = var1[var3];
                        if (var4.returnEnumOrdinal() == par0) {
                              return var4;
                        }
                  }

                  return null;
            }

            private Options(String par1Str, int par2, String par3Str, boolean par4, boolean par5) {
                  this(par1Str, par2, par3Str, par4, par5, 0.0F, 1.0F, 0.0F);
            }

            private Options(String p_i45004_1_, int p_i45004_2_, String p_i45004_3_, boolean p_i45004_4_, boolean p_i45004_5_, float p_i45004_6_, float p_i45004_7_, float p_i45004_8_) {
                  this.enumString = p_i45004_3_;
                  this.enumFloat = p_i45004_4_;
                  this.enumBoolean = p_i45004_5_;
                  this.valueMin = p_i45004_6_;
                  this.valueMax = p_i45004_7_;
                  this.valueStep = p_i45004_8_;
            }

            public boolean getEnumFloat() {
                  return this.enumFloat;
            }

            public boolean getEnumBoolean() {
                  return this.enumBoolean;
            }

            public int returnEnumOrdinal() {
                  return this.ordinal();
            }

            public String getEnumString() {
                  return this.enumString;
            }

            public float getValueMax() {
                  return this.valueMax;
            }

            public void setValueMax(float p_148263_1_) {
                  this.valueMax = p_148263_1_;
            }

            public float normalizeValue(float p_148266_1_) {
                  return MathHelper.clamp_float((this.snapToStepClamp(p_148266_1_) - this.valueMin) / (this.valueMax - this.valueMin), 0.0F, 1.0F);
            }

            public float denormalizeValue(float p_148262_1_) {
                  return this.snapToStepClamp(this.valueMin + (this.valueMax - this.valueMin) * MathHelper.clamp_float(p_148262_1_, 0.0F, 1.0F));
            }

            public float snapToStepClamp(float p_148268_1_) {
                  p_148268_1_ = this.snapToStep(p_148268_1_);
                  return MathHelper.clamp_float(p_148268_1_, this.valueMin, this.valueMax);
            }

            protected float snapToStep(float p_148264_1_) {
                  if (this.valueStep > 0.0F) {
                        p_148264_1_ = this.valueStep * (float)Math.round(p_148264_1_ / this.valueStep);
                  }

                  return p_148264_1_;
            }

            private Options(String p_i45005_1_, int p_i45005_2_, String p_i45005_3_, boolean p_i45005_4_, boolean p_i45005_5_, float p_i45005_6_, float p_i45005_7_, float p_i45005_8_, Object p_i45005_9_) {
                  this(p_i45005_1_, p_i45005_2_, p_i45005_3_, p_i45005_4_, p_i45005_5_, p_i45005_6_, p_i45005_7_, p_i45005_8_);
            }

            // $FF: synthetic method
            Options(String x2, int x3, String x4, boolean x5, boolean x6, float x7, float x8, float x9, Object x10, Object x11) {
                  this(x2, x3, x4, x5, x6, x7, x8, x9, x10);
            }
      }

    static final class SwitchOptions {
        static final int[] optionIds = new int[GameSettings.Options.values().length];
        private static final String __OBFID = "CL_00000652";

        static {
              try {
                    optionIds[GameSettings.Options.INVERT_MOUSE.ordinal()] = 1;
              } catch (NoSuchFieldError var16) {
              }

              try {
                    optionIds[GameSettings.Options.VIEW_BOBBING.ordinal()] = 2;
              } catch (NoSuchFieldError var15) {
              }

              try {
                    optionIds[GameSettings.Options.ANAGLYPH.ordinal()] = 3;
              } catch (NoSuchFieldError var14) {
              }

              try {
                    optionIds[GameSettings.Options.ADVANCED_OPENGL.ordinal()] = 4;
              } catch (NoSuchFieldError var13) {
              }

              try {
                    optionIds[GameSettings.Options.FBO_ENABLE.ordinal()] = 5;
              } catch (NoSuchFieldError var12) {
              }

              try {
                    optionIds[GameSettings.Options.RENDER_CLOUDS.ordinal()] = 6;
              } catch (NoSuchFieldError var11) {
              }

              try {
                    optionIds[GameSettings.Options.CHAT_COLOR.ordinal()] = 7;
              } catch (NoSuchFieldError var10) {
              }

              try {
                    optionIds[GameSettings.Options.CHAT_LINKS.ordinal()] = 8;
              } catch (NoSuchFieldError var9) {
              }

              try {
                    optionIds[GameSettings.Options.CHAT_LINKS_PROMPT.ordinal()] = 9;
              } catch (NoSuchFieldError var8) {
              }

              try {
                    optionIds[GameSettings.Options.SNOOPER_ENABLED.ordinal()] = 10;
              } catch (NoSuchFieldError var7) {
              }

              try {
                    optionIds[GameSettings.Options.USE_FULLSCREEN.ordinal()] = 11;
              } catch (NoSuchFieldError var6) {
              }

              try {
                    optionIds[GameSettings.Options.ENABLE_VSYNC.ordinal()] = 12;
              } catch (NoSuchFieldError var5) {
              }

              try {
                    optionIds[GameSettings.Options.SHOW_CAPE.ordinal()] = 13;
              } catch (NoSuchFieldError var4) {
              }

              try {
                    optionIds[GameSettings.Options.TOUCHSCREEN.ordinal()] = 14;
              } catch (NoSuchFieldError var3) {
              }

              try {
                    optionIds[GameSettings.Options.STREAM_SEND_METADATA.ordinal()] = 15;
              } catch (NoSuchFieldError var2) {
              }

              try {
                    optionIds[GameSettings.Options.FORCE_UNICODE_FONT.ordinal()] = 16;
              } catch (NoSuchFieldError var1) {
              }

        }
    }
    
    
}