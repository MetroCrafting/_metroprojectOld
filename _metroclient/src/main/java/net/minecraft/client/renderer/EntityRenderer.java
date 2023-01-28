package net.minecraft.client.renderer;

import com.google.gson.JsonSyntaxException;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.FloatBuffer;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityRainFX;
import net.minecraft.client.particle.EntitySmokeFX;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.culling.Frustrum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderLinkHelper;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.potion.Potion;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.src.Config;
import net.minecraft.src.CustomColorizer;
import net.minecraft.src.ItemRendererOF;
import net.minecraft.src.RandomMobs;
import net.minecraft.src.RenderPlayerOF;
import net.minecraft.src.TextureUtils;
import net.minecraft.src.WrUpdates;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MouseFilter;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.Project;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import shadersmod.client.Shaders;
import shadersmod.client.ShadersRender;

@SideOnly(Side.CLIENT)
public class EntityRenderer implements IResourceManagerReloadListener
{
    private static final Logger logger = LogManager.getLogger();
    private static final ResourceLocation locationRainPng = new ResourceLocation("textures/environment/rain.png");
    private static final ResourceLocation locationSnowPng = new ResourceLocation("textures/environment/snow.png");
    public static boolean anaglyphEnable;
    /** Anaglyph field (0=R, 1=GB) */
    public static int anaglyphField;
    /** A reference to the Minecraft object. */
    private Minecraft mc;
    private float farPlaneDistance;
    public ItemRenderer itemRenderer;
    private final MapItemRenderer theMapItemRenderer;
    /** Entity renderer update count */
    private int rendererUpdateCount;
    /** Pointed entity */
    private Entity pointedEntity;
    private MouseFilter mouseFilterXAxis = new MouseFilter();
    private MouseFilter mouseFilterYAxis = new MouseFilter();
    /** Mouse filter dummy 1 */
    private MouseFilter mouseFilterDummy1 = new MouseFilter();
    /** Mouse filter dummy 2 */
    private MouseFilter mouseFilterDummy2 = new MouseFilter();
    /** Mouse filter dummy 3 */
    private MouseFilter mouseFilterDummy3 = new MouseFilter();
    /** Mouse filter dummy 4 */
    private MouseFilter mouseFilterDummy4 = new MouseFilter();
    private float thirdPersonDistance = 4.0F;
    /** Third person distance temp */
    private float thirdPersonDistanceTemp = 4.0F;
    private float debugCamYaw;
    private float prevDebugCamYaw;
    private float debugCamPitch;
    private float prevDebugCamPitch;
    /** Smooth cam yaw */
    private float smoothCamYaw;
    /** Smooth cam pitch */
    private float smoothCamPitch;
    /** Smooth cam filter X */
    private float smoothCamFilterX;
    /** Smooth cam filter Y */
    private float smoothCamFilterY;
    /** Smooth cam partial ticks */
    private float smoothCamPartialTicks;
    private float debugCamFOV;
    private float prevDebugCamFOV;
    private float camRoll;
    private float prevCamRoll;
    /** The texture id of the blocklight/skylight texture used for lighting effects */
    private final DynamicTexture lightmapTexture;
    /** Colors computed in updateLightmap() and loaded into the lightmap emptyTexture */
    private final int[] lightmapColors;
    private final ResourceLocation locationLightMap;
    /** FOV modifier hand */
    private float fovModifierHand;
    /** FOV modifier hand prev */
    private float fovModifierHandPrev;
    /** FOV multiplier temp */
    private float fovMultiplierTemp;
    private float bossColorModifier;
    private float bossColorModifierPrev;
    /** Cloud fog mode */
    private boolean cloudFog;
    private final IResourceManager resourceManager;
    public ShaderGroup theShaderGroup;
    private static final ResourceLocation[] shaderResourceLocations = new ResourceLocation[] {new ResourceLocation("shaders/post/notch.json"), new ResourceLocation("shaders/post/fxaa.json"), new ResourceLocation("shaders/post/art.json"), new ResourceLocation("shaders/post/bumpy.json"), new ResourceLocation("shaders/post/blobs2.json"), new ResourceLocation("shaders/post/pencil.json"), new ResourceLocation("shaders/post/color_convolve.json"), new ResourceLocation("shaders/post/deconverge.json"), new ResourceLocation("shaders/post/flip.json"), new ResourceLocation("shaders/post/invert.json"), new ResourceLocation("shaders/post/ntsc.json"), new ResourceLocation("shaders/post/outline.json"), new ResourceLocation("shaders/post/phosphor.json"), new ResourceLocation("shaders/post/scan_pincushion.json"), new ResourceLocation("shaders/post/sobel.json"), new ResourceLocation("shaders/post/bits.json"), new ResourceLocation("shaders/post/desaturate.json"), new ResourceLocation("shaders/post/green.json"), new ResourceLocation("shaders/post/blur.json"), new ResourceLocation("shaders/post/wobble.json"), new ResourceLocation("shaders/post/blobs.json"), new ResourceLocation("shaders/post/antialias.json")};
    public static final int shaderCount = shaderResourceLocations.length;
    private int shaderIndex;
    private double cameraZoom;
    private double cameraYaw;
    private double cameraPitch;
    /** Previous frame time in milliseconds */
    private long prevFrameTime;
    /** End time of last render (ns) */
    private long renderEndNanoTime;
    /** Is set, updateCameraAndRender() calls updateLightmap(); set by updateTorchFlicker() */
    private boolean lightmapUpdateNeeded;
    /** Torch flicker X */
    float torchFlickerX;
    /** Torch flicker DX */
    float torchFlickerDX;
    /** Torch flicker Y */
    float torchFlickerY;
    /** Torch flicker DY */
    float torchFlickerDY;
    private Random random;
    /** Rain sound counter */
    private int rainSoundCounter;
    /** Rain X coords */
    float[] rainXCoords;
    /** Rain Y coords */
    float[] rainYCoords;
    /** Fog color buffer */
    FloatBuffer fogColorBuffer;
    /** red component of the fog color */
    float fogColorRed;
    /** green component of the fog color */
    float fogColorGreen;
    /** blue component of the fog color */
    float fogColorBlue;
    /** Fog color 2 */
    private float fogColor2;
    /** Fog color 1 */
    private float fogColor1;
    /** Debug view direction (0=OFF, 1=Front, 2=Right, 3=Back, 4=Left, 5=TiltLeft, 6=TiltRight) */
    public int debugViewDirection;
    private boolean initialized = false;
    private World updatedWorld = null;
    private boolean showDebugInfo = false;
    public boolean fogStandard = false;
    private long lastServerTime = 0L;
    private int lastServerTicks = 0;
    private int serverWaitTime = 0;
    private int serverWaitTimeCurrent = 0;
    private float avgServerTimeDiff = 0.0F;
    private float avgServerTickDiff = 0.0F;
    public long[] frameTimes = new long[512];
    public long[] tickTimes = new long[512];
    public long[] chunkTimes = new long[512];
    public long[] serverTimes = new long[512];
    public int numRecordedFrameTimes = 0;
    public long prevFrameTimeNano = -1L;
    private boolean lastShowDebugInfo = false;
    private boolean showExtendedDebugInfo = false;
    private long lastErrorCheckTimeMs = 0L;
    private ShaderGroup[] fxaaShaders = new ShaderGroup[10];
    public int frameCount;

    public EntityRenderer(Minecraft p_i45076_1_, IResourceManager p_i45076_2_)
    {
        this.shaderIndex = shaderCount;
        this.cameraZoom = 1.0D;
        this.prevFrameTime = Minecraft.getSystemTime();
        this.random = new Random();
        this.fogColorBuffer = GLAllocation.createDirectFloatBuffer(16);
        this.mc = p_i45076_1_;
        this.resourceManager = p_i45076_2_;
        this.theMapItemRenderer = new MapItemRenderer(p_i45076_1_.getTextureManager());
        this.itemRenderer = new ItemRenderer(p_i45076_1_);
        this.lightmapTexture = new DynamicTexture(16, 16);
        this.locationLightMap = p_i45076_1_.getTextureManager().getDynamicTextureLocation("lightMap", this.lightmapTexture);
        this.lightmapColors = this.lightmapTexture.getTextureData();
        this.theShaderGroup = null;
    }

    public boolean isShaderActive()
    {
        return OpenGlHelper.shadersSupported && this.theShaderGroup != null;
    }

    public void stopUseShader() {
        if (this.theShaderGroup != null) {
              this.theShaderGroup.deleteShaderGroup();
        }

        this.theShaderGroup = null;
        this.shaderIndex = shaderCount;
  }
    
    public void deactivateShader()
    {
        if (this.theShaderGroup != null)
        {
            this.theShaderGroup.deleteShaderGroup();
        }

        this.theShaderGroup = null;
        this.shaderIndex = shaderCount;
    }

    public void activateNextShader()
    {
    	if (OpenGlHelper.isFramebufferEnabled()) {
    		if (OpenGlHelper.shadersSupported)
    		{
            if (this.theShaderGroup != null)
            {
                this.theShaderGroup.deleteShaderGroup();
            }

            this.shaderIndex = (this.shaderIndex + 1) % (shaderResourceLocations.length + 1);

            if (this.shaderIndex != shaderCount)
            {
                try
                {
                    logger.info("Selecting effect " + shaderResourceLocations[this.shaderIndex]);
                    this.theShaderGroup = new ShaderGroup(this.mc.getTextureManager(), this.resourceManager, this.mc.getFramebuffer(), shaderResourceLocations[this.shaderIndex]);
                    this.theShaderGroup.createBindFramebuffers(this.mc.displayWidth, this.mc.displayHeight);
                }
                catch (IOException ioexception)
                {
                    logger.warn("Failed to load shader: " + shaderResourceLocations[this.shaderIndex], ioexception);
                    this.shaderIndex = shaderCount;
                }
                catch (JsonSyntaxException jsonsyntaxexception)
                {
                    logger.warn("Failed to load shader: " + shaderResourceLocations[this.shaderIndex], jsonsyntaxexception);
                    this.shaderIndex = shaderCount;
                }
            }
            else
            {
                this.theShaderGroup = null;
                logger.info("No effect selected");
            	}
        	}
        }
    }

    public void onResourceManagerReload(IResourceManager par1ResourceManager) {
        if (this.theShaderGroup != null) {
              this.theShaderGroup.deleteShaderGroup();
        }

        if (OpenGlHelper.isFramebufferEnabled()) {
              if (this.shaderIndex != shaderCount) {
                    try {
                          this.theShaderGroup = new ShaderGroup(this.mc.getTextureManager(), par1ResourceManager, this.mc.getFramebuffer(), shaderResourceLocations[this.shaderIndex]);
                          this.theShaderGroup.createBindFramebuffers(this.mc.displayWidth, this.mc.displayHeight);
                    } catch (IOException var3) {
                          logger.warn("Failed to load shader: " + shaderResourceLocations[this.shaderIndex], var3);
                          this.shaderIndex = shaderCount;
                    }
              }

        }
  }


    /**
     * Updates the entity renderer
     */
    public void updateRenderer()
    {
        if (OpenGlHelper.shadersSupported && ShaderLinkHelper.getStaticShaderLinkHelper() == null)
        {
            ShaderLinkHelper.setNewStaticShaderLinkHelper();
        }

        this.updateFovModifierHand();
        this.updateTorchFlicker();
        this.fogColor2 = this.fogColor1;
        this.thirdPersonDistanceTemp = this.thirdPersonDistance;
        this.prevDebugCamYaw = this.debugCamYaw;
        this.prevDebugCamPitch = this.debugCamPitch;
        this.prevDebugCamFOV = this.debugCamFOV;
        this.prevCamRoll = this.camRoll;
        float f;
        float f1;

        if (this.mc.gameSettings.smoothCamera)
        {
            f = this.mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
            f1 = f * f * f * 8.0F;
            this.smoothCamFilterX = this.mouseFilterXAxis.smooth(this.smoothCamYaw, 0.05F * f1);
            this.smoothCamFilterY = this.mouseFilterYAxis.smooth(this.smoothCamPitch, 0.05F * f1);
            this.smoothCamPartialTicks = 0.0F;
            this.smoothCamYaw = 0.0F;
            this.smoothCamPitch = 0.0F;
        }

        if (this.mc.renderViewEntity == null)
        {
            this.mc.renderViewEntity = this.mc.thePlayer;
        }

        f = this.mc.theWorld.getLightBrightness(MathHelper.floor_double(this.mc.renderViewEntity.posX), MathHelper.floor_double(this.mc.renderViewEntity.posY), MathHelper.floor_double(this.mc.renderViewEntity.posZ));
        f1 = (float)this.mc.gameSettings.renderDistanceChunks / 16.0F;
        float f2 = f * (1.0F - f1) + f1;
        this.fogColor1 += (f2 - this.fogColor1) * 0.1F;
        ++this.rendererUpdateCount;
        this.itemRenderer.updateEquippedItem();
        this.addRainParticles();
        this.bossColorModifierPrev = this.bossColorModifier;

        if (BossStatus.hasColorModifier)
        {
            this.bossColorModifier += 0.05F;

            if (this.bossColorModifier > 1.0F)
            {
                this.bossColorModifier = 1.0F;
            }

            BossStatus.hasColorModifier = false;
        }
        else if (this.bossColorModifier > 0.0F)
        {
            this.bossColorModifier -= 0.0125F;
        }
    }

    public ShaderGroup getShaderGroup()
    {
        return this.theShaderGroup;
    }

    public void updateShaderGroupSize(int p_147704_1_, int p_147704_2_) {
        if (OpenGlHelper.shadersSupported && this.theShaderGroup != null) 
        {
              this.theShaderGroup.createBindFramebuffers(p_147704_1_, p_147704_2_);
        }

  }
    
    /**
     * Finds what block or object the mouse is over at the specified partial tick time. Args: partialTickTime
     */
    public void getMouseOver(float p_78473_1_)
    {
        if (this.mc.renderViewEntity != null && this.mc.theWorld != null)
        {
            this.mc.pointedEntity = null;
            double d0 = this.mc.playerController.getBlockReachDistance();
            this.mc.objectMouseOver = this.mc.renderViewEntity.rayTrace(d0, p_78473_1_);
            double d1 = d0;
            Vec3 vec3 = this.mc.renderViewEntity.getPosition(p_78473_1_);

            if (this.mc.playerController.extendedReach())
            {
                d0 = 6.0D;
                d1 = 6.0D;
            }
            else
            {
                if (d0 > 3.0D)
                {
                    d1 = 3.0D;
                }

                d0 = d1;
            }

            if (this.mc.objectMouseOver != null)
            {
                d1 = this.mc.objectMouseOver.hitVec.distanceTo(vec3);
            }

            Vec3 vec31 = this.mc.renderViewEntity.getLook(p_78473_1_);
            Vec3 vec32 = vec3.addVector(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0);
            this.pointedEntity = null;
            Vec3 vec33 = null;
            float f1 = 1.0F;
            List list = this.mc.theWorld.getEntitiesWithinAABBExcludingEntity(this.mc.renderViewEntity, this.mc.renderViewEntity.boundingBox.addCoord(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0).expand((double)f1, (double)f1, (double)f1));
            double d2 = d1;

            for (int i = 0; i < list.size(); ++i)
            {
                Entity entity = (Entity)list.get(i);

                if (entity.canBeCollidedWith() || entity instanceof EntityItem)
                {
                    float f2 = entity.getCollisionBorderSize();
                    AxisAlignedBB axisalignedbb = entity.boundingBox.expand((double)f2, (double)f2, (double)f2);
                    MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);

                    if (axisalignedbb.isVecInside(vec3))
                    {
                        if (0.0D < d2 || d2 == 0.0D)
                        {
                            this.pointedEntity = entity;
                            vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                            d2 = 0.0D;
                        }
                    }
                    else if (movingobjectposition != null)
                    {
                        double d3 = vec3.distanceTo(movingobjectposition.hitVec);

                        if (d3 < d2 || d2 == 0.0D)
                        {
                            if (entity == this.mc.renderViewEntity.ridingEntity && !entity.canRiderInteract())
                            {
                                if (d2 == 0.0D)
                                {
                                    this.pointedEntity = entity;
                                    vec33 = movingobjectposition.hitVec;
                                }
                            }
                            else
                            {
                                this.pointedEntity = entity;
                                vec33 = movingobjectposition.hitVec;
                                d2 = d3;
                            }
                        }
                    }
                }
            }

            if (this.pointedEntity != null && (d2 < d1 || this.mc.objectMouseOver == null))
            {
                this.mc.objectMouseOver = new MovingObjectPosition(this.pointedEntity, vec33);

                if (this.pointedEntity instanceof EntityLivingBase || this.pointedEntity instanceof EntityItemFrame)
                {
                    this.mc.pointedEntity = this.pointedEntity;
                }
            }
        }
    }

    /**
     * Update FOV modifier hand
     */
    private void updateFovModifierHand() {
        if (this.mc.renderViewEntity instanceof EntityPlayerSP) {
              EntityPlayerSP var1 = (EntityPlayerSP)this.mc.renderViewEntity;
              this.fovMultiplierTemp = var1.getFOVMultiplier();
        } else {
              this.fovMultiplierTemp = this.mc.thePlayer.getFOVMultiplier();
        }

        this.fovModifierHandPrev = this.fovModifierHand;
        this.fovModifierHand += (this.fovMultiplierTemp - this.fovModifierHand) * 0.5F;
        if (this.fovModifierHand > 1.5F) {
              this.fovModifierHand = 1.5F;
        }

        if (this.fovModifierHand < 0.1F) {
              this.fovModifierHand = 0.1F;
        }

  }
    
    /**
     * Changes the field of view of the player depending on if they are underwater or not
     */
    private float getFOVModifier(float par1, boolean par2)
    {
        if (this.debugViewDirection > 0)
        {
            return 90.0F;
        }
        else
        {
            EntityLivingBase var3 = this.mc.renderViewEntity;
            float var4 = 70.0F;

            if (par2)
            {
                var4 = this.mc.gameSettings.fovSetting;

                if (Config.isDynamicFov())
                {
                    var4 *= this.fovModifierHandPrev + (this.fovModifierHand - this.fovModifierHandPrev) * par1;
                }
            }

            boolean zoomActive = false;

            if (this.mc.currentScreen == null)
            {
                if (this.mc.gameSettings.ofKeyBindZoom.getKeyCode() < 0)
                {
                    zoomActive = Mouse.isButtonDown(this.mc.gameSettings.ofKeyBindZoom.getKeyCode() + 100);
                }
                else
                {
                    zoomActive = Keyboard.isKeyDown(this.mc.gameSettings.ofKeyBindZoom.getKeyCode());
                }
            }

            if (zoomActive)
            {
                if (!Config.zoomMode)
                {
                    Config.zoomMode = true;
                    this.mc.gameSettings.smoothCamera = true;
                }

                var4 /= 4.0F / 3;
            }
            else if (Config.zoomMode)
            {
                Config.zoomMode = false;
                this.mc.gameSettings.smoothCamera = false;
                this.mouseFilterXAxis = new MouseFilter();
                this.mouseFilterYAxis = new MouseFilter();
            }

            if (var3.getHealth() <= 0.0F)
            {
                float var6 = (float)var3.deathTime + par1;
                var4 /= (1.0F - 500.0F / (var6 + 500.0F)) * 2.0F + 1.0F;
            }

            Block var61 = ActiveRenderInfo.getBlockAtEntityViewpoint(this.mc.theWorld, var3, par1);

            if (var61.getMaterial() == Material.water)
            {
                var4 = var4 * 60.0F / 70.0F;
            }

            return var4 + this.prevDebugCamFOV + (this.debugCamFOV - this.prevDebugCamFOV) * par1;
        }
    }

    private void hurtCameraEffect(float par1) {
    	
        EntityLivingBase var2 = this.mc.renderViewEntity;
        float var3 = (float)var2.hurtTime - par1;
        float var4;
        
        if (var2.getHealth() <= 0.0F) {
              var4 = (float)var2.deathTime + par1;
              GL11.glRotatef(40.0F - 8000.0F / (var4 + 200.0F), 0.0F, 0.0F, 1.0F);
        }

        if (var3 >= 0.0F) {
              var3 /= (float)var2.maxHurtTime;
              var3 = MathHelper.sin(var3 * var3 * var3 * var3 * 3.1415927F);
              var4 = var2.attackedAtYaw;
              GL11.glRotatef(-var4, 0.0F, 1.0F, 0.0F);
              GL11.glRotatef(-var3 * 14.0F, 0.0F, 0.0F, 1.0F);
              GL11.glRotatef(var4, 0.0F, 1.0F, 0.0F);
        }

  }

    /**
     * Setups all the GL settings for view bobbing. Args: partialTickTime
     */
    private void setupViewBobbing(float par1) {
        if (this.mc.renderViewEntity instanceof EntityPlayer) 
        {
              EntityPlayer var2 = (EntityPlayer)this.mc.renderViewEntity;
              float var3 = var2.distanceWalkedModified - var2.prevDistanceWalkedModified;
              float var4 = -(var2.distanceWalkedModified + var3 * par1);
              float var5 = var2.prevCameraYaw + (var2.cameraYaw - var2.prevCameraYaw) * par1;
              float var6 = var2.prevCameraPitch + (var2.cameraPitch - var2.prevCameraPitch) * par1;
              GL11.glTranslatef(MathHelper.sin(var4 * 3.1415927F) * var5 * 0.5F, -Math.abs(MathHelper.cos(var4 * 3.1415927F) * var5), 0.0F);
              GL11.glRotatef(MathHelper.sin(var4 * 3.1415927F) * var5 * 3.0F, 0.0F, 0.0F, 1.0F);
              GL11.glRotatef(Math.abs(MathHelper.cos(var4 * 3.1415927F - 0.2F) * var5) * 5.0F, 1.0F, 0.0F, 0.0F);
              GL11.glRotatef(var6, 1.0F, 0.0F, 0.0F);
        }

  }

    /**
     * sets up player's eye (or camera in third person mode)
     */
    private void orientCamera(float p_78467_1_)
    {
        EntityLivingBase entitylivingbase = this.mc.renderViewEntity;
        float f1 = entitylivingbase.yOffset - 1.62F;
        double d0 = entitylivingbase.prevPosX + (entitylivingbase.posX - entitylivingbase.prevPosX) * (double)p_78467_1_;
        double d1 = entitylivingbase.prevPosY + (entitylivingbase.posY - entitylivingbase.prevPosY) * (double)p_78467_1_ - (double)f1;
        double d2 = entitylivingbase.prevPosZ + (entitylivingbase.posZ - entitylivingbase.prevPosZ) * (double)p_78467_1_;
        GL11.glRotatef(this.prevCamRoll + (this.camRoll - this.prevCamRoll) * p_78467_1_, 0.0F, 0.0F, 1.0F);

        if (entitylivingbase.isPlayerSleeping())
        {
            f1 = (float)((double)f1 + 1.0D);
            GL11.glTranslatef(0.0F, 0.3F, 0.0F);

            if (!this.mc.gameSettings.debugCamEnable)
            {
                ForgeHooksClient.orientBedCamera(mc, entitylivingbase);
                GL11.glRotatef(entitylivingbase.prevRotationYaw + (entitylivingbase.rotationYaw - entitylivingbase.prevRotationYaw) * p_78467_1_ + 180.0F, 0.0F, -1.0F, 0.0F);
                GL11.glRotatef(entitylivingbase.prevRotationPitch + (entitylivingbase.rotationPitch - entitylivingbase.prevRotationPitch) * p_78467_1_, -1.0F, 0.0F, 0.0F);
            }
        }
        else if (this.mc.gameSettings.thirdPersonView > 0)
        {
            double d7 = (double)(this.thirdPersonDistanceTemp + (this.thirdPersonDistance - this.thirdPersonDistanceTemp) * p_78467_1_);
            float f2;
            float f6;

            if (this.mc.gameSettings.debugCamEnable)
            {
                f6 = this.prevDebugCamYaw + (this.debugCamYaw - this.prevDebugCamYaw) * p_78467_1_;
                f2 = this.prevDebugCamPitch + (this.debugCamPitch - this.prevDebugCamPitch) * p_78467_1_;
                GL11.glTranslatef(0.0F, 0.0F, (float)(-d7));
                GL11.glRotatef(f2, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(f6, 0.0F, 1.0F, 0.0F);
            }
            else
            {
                f6 = entitylivingbase.rotationYaw;
                f2 = entitylivingbase.rotationPitch;

                if (this.mc.gameSettings.thirdPersonView == 2)
                {
                    f2 += 180.0F;
                }

                double d3 = (double)(-MathHelper.sin(f6 / 180.0F * (float)Math.PI) * MathHelper.cos(f2 / 180.0F * (float)Math.PI)) * d7;
                double d4 = (double)(MathHelper.cos(f6 / 180.0F * (float)Math.PI) * MathHelper.cos(f2 / 180.0F * (float)Math.PI)) * d7;
                double d5 = (double)(-MathHelper.sin(f2 / 180.0F * (float)Math.PI)) * d7;

                for (int k = 0; k < 8; ++k)
                {
                    float f3 = (float)((k & 1) * 2 - 1);
                    float f4 = (float)((k >> 1 & 1) * 2 - 1);
                    float f5 = (float)((k >> 2 & 1) * 2 - 1);
                    f3 *= 0.1F;
                    f4 *= 0.1F;
                    f5 *= 0.1F;
                    MovingObjectPosition movingobjectposition = this.mc.theWorld.rayTraceBlocks(Vec3.createVectorHelper(d0 + (double)f3, d1 + (double)f4, d2 + (double)f5), Vec3.createVectorHelper(d0 - d3 + (double)f3 + (double)f5, d1 - d5 + (double)f4, d2 - d4 + (double)f5));

                    if (movingobjectposition != null)
                    {
                        double d6 = movingobjectposition.hitVec.distanceTo(Vec3.createVectorHelper(d0, d1, d2));

                        if (d6 < d7)
                        {
                            d7 = d6;
                        }
                    }
                }

                if (this.mc.gameSettings.thirdPersonView == 2)
                {
                    GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                }

                GL11.glRotatef(entitylivingbase.rotationPitch - f2, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(entitylivingbase.rotationYaw - f6, 0.0F, 1.0F, 0.0F);
                GL11.glTranslatef(0.0F, 0.0F, (float)(-d7));
                GL11.glRotatef(f6 - entitylivingbase.rotationYaw, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(f2 - entitylivingbase.rotationPitch, 1.0F, 0.0F, 0.0F);
            }
        }
        else
        {
            GL11.glTranslatef(0.0F, 0.0F, -0.1F);
        }

        if (!this.mc.gameSettings.debugCamEnable)
        {
            GL11.glRotatef(entitylivingbase.prevRotationPitch + (entitylivingbase.rotationPitch - entitylivingbase.prevRotationPitch) * p_78467_1_, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(entitylivingbase.prevRotationYaw + (entitylivingbase.rotationYaw - entitylivingbase.prevRotationYaw) * p_78467_1_ + 180.0F, 0.0F, 1.0F, 0.0F);
        }

        GL11.glTranslatef(0.0F, f1, 0.0F);
        d0 = entitylivingbase.prevPosX + (entitylivingbase.posX - entitylivingbase.prevPosX) * (double)p_78467_1_;
        d1 = entitylivingbase.prevPosY + (entitylivingbase.posY - entitylivingbase.prevPosY) * (double)p_78467_1_ - (double)f1;
        d2 = entitylivingbase.prevPosZ + (entitylivingbase.posZ - entitylivingbase.prevPosZ) * (double)p_78467_1_;
        this.cloudFog = this.mc.renderGlobal.hasCloudFog(d0, d1, d2, p_78467_1_);
    }

    /**
     * sets up projection, view effects, camera position/rotation
     */
    public void setupCameraTransform(float par1, int par2) {
        this.farPlaneDistance = (float)(this.mc.gameSettings.renderDistanceChunks * 16);
        
        if (Config.isFogFancy()) {
              this.farPlaneDistance *= 0.95F;
        }

        if (Config.isFogFast()) {
              this.farPlaneDistance *= 0.83F;
        }

        GL11.glMatrixMode(5889);
        GL11.glLoadIdentity();
        float var3 = 0.07F;
        
        if (this.mc.gameSettings.anaglyph) {
              GL11.glTranslatef((float)(-(par2 * 2 - 1)) * var3, 0.0F, 0.0F);
        }

        float clipDistance = this.farPlaneDistance * 2.0F;
        if (clipDistance < 128.0F) {
              clipDistance = 128.0F;
        }

        if (this.mc.theWorld.provider.dimensionId == 1) {
              clipDistance = 256.0F;
        }

        if (this.cameraZoom != 1.0D) {
              GL11.glTranslatef((float)this.cameraYaw, (float)(-this.cameraPitch), 0.0F);
              GL11.glScaled(this.cameraZoom, this.cameraZoom, 1.0D);
        }

        Project.gluPerspective(this.getFOVModifier(par1, true), (float)this.mc.displayWidth / (float)this.mc.displayHeight, 0.05F, clipDistance);
        float var4;
        if (this.mc.playerController.enableEverythingIsScrewedUpMode()) {
              var4 = 0.6666667F;
              GL11.glScalef(1.0F, var4, 1.0F);
        }

        GL11.glMatrixMode(5888);
        GL11.glLoadIdentity();
        
        if (this.mc.gameSettings.anaglyph) {
              GL11.glTranslatef((float)(par2 * 2 - 1) * 0.1F, 0.0F, 0.0F);
        }

        this.hurtCameraEffect(par1);
        
        if (this.mc.gameSettings.viewBobbing) {
              this.setupViewBobbing(par1);
        }

        var4 = this.mc.thePlayer.prevTimeInPortal + (this.mc.thePlayer.timeInPortal - this.mc.thePlayer.prevTimeInPortal) * par1;
        
        if (var4 > 0.0F) {
              byte var5 = 20;
              if (this.mc.thePlayer.isPotionActive(Potion.confusion)) {
                    var5 = 7;
              }

              float var6 = 5.0F / (var4 * var4 + 5.0F) - var4 * 0.04F;
              var6 *= var6;
              GL11.glRotatef(((float)this.rendererUpdateCount + par1) * (float)var5, 0.0F, 1.0F, 1.0F);
              GL11.glScalef(1.0F / var6, 1.0F, 1.0F);
              GL11.glRotatef(-((float)this.rendererUpdateCount + par1) * (float)var5, 0.0F, 1.0F, 1.0F);
        }

        this.orientCamera(par1);
        
        if (this.debugViewDirection > 0) {
              int var7 = this.debugViewDirection - 1;
              if (var7 == 1) {
                    GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
              }

              if (var7 == 2) {
                    GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
              }

              if (var7 == 3) {
                    GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
              }

              if (var7 == 4) {
                    GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
              }

              if (var7 == 5) {
                    GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
              }
        }

  }

    public void renderHand(float partialTicks, int pass) {
        this.renderHand(partialTicks, pass, true, true, false);
  }
    
    /**
     * Render player hand
     */
    public void renderHand(float par1, int par2, boolean renderItem, boolean renderOverlay, boolean renderTranslucent) {
        if (this.debugViewDirection <= 0) {
              GL11.glMatrixMode(5889);
              GL11.glLoadIdentity();
              float var3 = 0.07F;
              
              if (this.mc.gameSettings.anaglyph) {
                    GL11.glTranslatef((float)(-(par2 * 2 - 1)) * var3, 0.0F, 0.0F);
              }

              if (this.cameraZoom != 1.0D) {
                    GL11.glTranslatef((float)this.cameraYaw, (float)(-this.cameraPitch), 0.0F);
                    GL11.glScaled(this.cameraZoom, this.cameraZoom, 1.0D);
              }

              if (Config.isShaders()) {
                    Shaders.applyHandDepth();
              }

              Project.gluPerspective(this.getFOVModifier(par1, false), (float)this.mc.displayWidth / (float)this.mc.displayHeight, 0.05F, this.farPlaneDistance * 2.0F);
              if (this.mc.playerController.enableEverythingIsScrewedUpMode()) {
                    float var4 = 0.6666667F;
                    GL11.glScalef(1.0F, var4, 1.0F);
              }

              GL11.glMatrixMode(5888);
              GL11.glLoadIdentity();
              if (this.mc.gameSettings.anaglyph) {
                    GL11.glTranslatef((float)(par2 * 2 - 1) * 0.1F, 0.0F, 0.0F);
              }

              if (renderItem) {
                    GL11.glPushMatrix();
                    this.hurtCameraEffect(par1);
                    if (this.mc.gameSettings.viewBobbing) {
                          this.setupViewBobbing(par1);
                    }

                    boolean shouldRenderHand = !ForgeHooksClient.renderFirstPersonHand(this.mc.renderGlobal, par1, par2);
                    if (shouldRenderHand && this.mc.gameSettings.thirdPersonView == 0 && !this.mc.renderViewEntity.isPlayerSleeping() && !this.mc.gameSettings.hideGUI && !this.mc.playerController.enableEverythingIsScrewedUpMode()) {
                          this.enableLightmap((double)par1);
                          if (Config.isShaders()) {
                                ShadersRender.renderItemFP(this.itemRenderer, par1, renderTranslucent);
                          } else {
                                this.itemRenderer.renderItemInFirstPerson(par1);
                          }

                          this.disableLightmap((double)par1);
                    }

                    GL11.glPopMatrix();
              }

              if (!renderOverlay) {
                    return;
              }

              this.disableLightmap((double)par1);
              if (this.mc.gameSettings.thirdPersonView == 0 && !this.mc.renderViewEntity.isPlayerSleeping()) {
                    this.itemRenderer.renderOverlays(par1);
                    this.hurtCameraEffect(par1);
              }

              if (this.mc.gameSettings.viewBobbing) {
                    this.setupViewBobbing(par1);
              }
        }

  }

    /**
     * Disable secondary texture unit used by lightmap
     */
    public void disableLightmap(double par1) {
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glDisable(3553);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
        
        if (Config.isShaders()) {
              Shaders.disableLightmap();
        }

  }

    /**
     * Enable lightmap in secondary texture unit
     */
    public void enableLightmap(double par1) {
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glMatrixMode(5890);
        GL11.glLoadIdentity();
        float var3 = 0.00390625F;
        GL11.glScalef(var3, var3, var3);
        GL11.glTranslatef(8.0F, 8.0F, 8.0F);
        GL11.glMatrixMode(5888);
        this.mc.getTextureManager().bindTexture(this.locationLightMap);
        GL11.glTexParameteri(3553, 10241, 9729);
        GL11.glTexParameteri(3553, 10240, 9729);
        GL11.glTexParameteri(3553, 10242, 10496);
        GL11.glTexParameteri(3553, 10243, 10496);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(3553);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
        
        if (Config.isShaders()) {
              Shaders.enableLightmap();
        }

  }

    /**
     * Recompute a random value that is applied to block color in updateLightmap()
     */
    private void updateTorchFlicker()
    {
        this.torchFlickerDX = (float)((double)this.torchFlickerDX + (Math.random() - Math.random()) * Math.random() * Math.random());
        this.torchFlickerDY = (float)((double)this.torchFlickerDY + (Math.random() - Math.random()) * Math.random() * Math.random());
        this.torchFlickerDX = (float)((double)this.torchFlickerDX * 0.9D);
        this.torchFlickerDY = (float)((double)this.torchFlickerDY * 0.9D);
        this.torchFlickerX += (this.torchFlickerDX - this.torchFlickerX) * 1.0F;
        this.torchFlickerY += (this.torchFlickerDY - this.torchFlickerY) * 1.0F;
        this.lightmapUpdateNeeded = true;
    }

    private void updateLightmap(float p_78472_1_)
    {
        WorldClient worldclient = this.mc.theWorld;

        if (worldclient != null)
        {
            if (CustomColorizer.updateLightmap(worldclient, this.torchFlickerX, this.lightmapColors, this.mc.thePlayer.isPotionActive(Potion.nightVision)))
            {
                this.lightmapTexture.updateDynamicTexture();
                this.lightmapUpdateNeeded = false;
                return;
            }

            for (int i = 0; i < 256; ++i)
            {
                float f1 = worldclient.getSunBrightness(1.0F) * 0.95F + 0.05F;
                float f2 = worldclient.provider.lightBrightnessTable[i / 16] * f1;
                float f3 = worldclient.provider.lightBrightnessTable[i % 16] * (this.torchFlickerX * 0.1F + 1.5F);

                if (worldclient.lastLightningBolt > 0)
                {
                    f2 = worldclient.provider.lightBrightnessTable[i / 16];
                }

                float f4 = f2 * (worldclient.getSunBrightness(1.0F) * 0.65F + 0.35F);
                float f5 = f2 * (worldclient.getSunBrightness(1.0F) * 0.65F + 0.35F);
                float f6 = f3 * ((f3 * 0.6F + 0.4F) * 0.6F + 0.4F);
                float f7 = f3 * (f3 * f3 * 0.6F + 0.4F);
                float f8 = f4 + f3;
                float f9 = f5 + f6;
                float f10 = f2 + f7;
                f8 = f8 * 0.96F + 0.03F;
                f9 = f9 * 0.96F + 0.03F;
                f10 = f10 * 0.96F + 0.03F;
                float f11;

                if (this.bossColorModifier > 0.0F)
                {
                    f11 = this.bossColorModifierPrev + (this.bossColorModifier - this.bossColorModifierPrev) * p_78472_1_;
                    f8 = f8 * (1.0F - f11) + f8 * 0.7F * f11;
                    f9 = f9 * (1.0F - f11) + f9 * 0.6F * f11;
                    f10 = f10 * (1.0F - f11) + f10 * 0.6F * f11;
                }

                if (worldclient.provider.dimensionId == 1)
                {
                    f8 = 0.22F + f3 * 0.75F;
                    f9 = 0.28F + f6 * 0.75F;
                    f10 = 0.25F + f7 * 0.75F;
                }

                float f12;

                if (this.mc.thePlayer.isPotionActive(Potion.nightVision))
                {
                    f11 = this.getNightVisionBrightness(this.mc.thePlayer, p_78472_1_);
                    f12 = 1.0F / f8;

                    if (f12 > 1.0F / f9)
                    {
                        f12 = 1.0F / f9;
                    }

                    if (f12 > 1.0F / f10)
                    {
                        f12 = 1.0F / f10;
                    }

                    f8 = f8 * (1.0F - f11) + f8 * f12 * f11;
                    f9 = f9 * (1.0F - f11) + f9 * f12 * f11;
                    f10 = f10 * (1.0F - f11) + f10 * f12 * f11;
                }

                if (f8 > 1.0F)
                {
                    f8 = 1.0F;
                }

                if (f9 > 1.0F)
                {
                    f9 = 1.0F;
                }

                if (f10 > 1.0F)
                {
                    f10 = 1.0F;
                }

                f11 = this.mc.gameSettings.gammaSetting;
                f12 = 1.0F - f8;
                float f13 = 1.0F - f9;
                float f14 = 1.0F - f10;
                f12 = 1.0F - f12 * f12 * f12 * f12;
                f13 = 1.0F - f13 * f13 * f13 * f13;
                f14 = 1.0F - f14 * f14 * f14 * f14;
                f8 = f8 * (1.0F - f11) + f12 * f11;
                f9 = f9 * (1.0F - f11) + f13 * f11;
                f10 = f10 * (1.0F - f11) + f14 * f11;
                f8 = f8 * 0.96F + 0.03F;
                f9 = f9 * 0.96F + 0.03F;
                f10 = f10 * 0.96F + 0.03F;

                if (f8 > 1.0F)
                {
                    f8 = 1.0F;
                }

                if (f9 > 1.0F)
                {
                    f9 = 1.0F;
                }

                if (f10 > 1.0F)
                {
                    f10 = 1.0F;
                }

                if (f8 < 0.0F)
                {
                    f8 = 0.0F;
                }

                if (f9 < 0.0F)
                {
                    f9 = 0.0F;
                }

                if (f10 < 0.0F)
                {
                    f10 = 0.0F;
                }

                short short1 = 255;
                int j = (int)(f8 * 255.0F);
                int k = (int)(f9 * 255.0F);
                int l = (int)(f10 * 255.0F);
                this.lightmapColors[i] = short1 << 24 | j << 16 | k << 8 | l;
            }

            this.lightmapTexture.updateDynamicTexture();
            this.lightmapUpdateNeeded = false;
        }
    }

    /**
     * Gets the night vision brightness
     */
    public float getNightVisionBrightness(EntityPlayer p_82830_1_, float p_82830_2_)
    {
        int i = p_82830_1_.getActivePotionEffect(Potion.nightVision).getDuration();
        return i > 200 ? 1.0F : 0.7F + MathHelper.sin(((float)i - p_82830_2_) * (float)Math.PI * 0.2F) * 0.3F;
    }

    /**
     * Will update any inputs that effect the camera angle (mouse) and then render the world and GUI
     */
    public void updateCameraAndRender(float p_78480_1_)
    {
        this.mc.mcProfiler.startSection("lightTex");

        if (!this.initialized)
        {
            TextureUtils.registerResourceListener();
            RenderPlayerOF.register();
            ItemRendererOF world = new ItemRendererOF(this.mc);
            this.itemRenderer = world;
            RenderManager.instance.itemRenderer = world;
            this.initialized = true;
        }

        Config.checkDisplayMode();
        WorldClient world1 = this.mc.theWorld;

        if (this.updatedWorld != world1)
        {
            RandomMobs.worldChanged(this.updatedWorld, world1);
            Config.updateThreadPriorities();
            this.lastServerTime = 0L;
            this.lastServerTicks = 0;
            this.updatedWorld = world1;
        }

        if (ShaderLinkHelper.getStaticShaderLinkHelper() != null && !this.setFxaaShader(Shaders.configAntialiasingLevel))
        {
            Shaders.configAntialiasingLevel = 0;
        }

        RenderBlocks.fancyGrass = Config.isGrassFancy() || Config.isBetterGrassFancy();
        Blocks.leaves.setGraphicsLevel(Config.isTreesFancy());

        if (this.lightmapUpdateNeeded)
        {
            this.updateLightmap(p_78480_1_);
        }

        this.mc.mcProfiler.endSection();
        boolean flag = Display.isActive();

        if (!flag && this.mc.gameSettings.pauseOnLostFocus && (!this.mc.gameSettings.touchscreen || !Mouse.isButtonDown(1)))
        {
            if (Minecraft.getSystemTime() - this.prevFrameTime > 500L)
            {
                this.mc.displayInGameMenu();
            }
        }
        else
        {
            this.prevFrameTime = Minecraft.getSystemTime();
        }

        this.mc.mcProfiler.startSection("mouse");

        if (this.mc.inGameHasFocus && flag)
        {
            this.mc.mouseHelper.mouseXYChange();
            float f1 = this.mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
            float f2 = f1 * f1 * f1 * 8.0F;
            float f3 = (float)this.mc.mouseHelper.deltaX * f2;
            float f4 = (float)this.mc.mouseHelper.deltaY * f2;
            byte b0 = 1;

            if (this.mc.gameSettings.invertMouse)
            {
                b0 = -1;
            }

            if (this.mc.gameSettings.smoothCamera)
            {
                this.smoothCamYaw += f3;
                this.smoothCamPitch += f4;
                float f5 = p_78480_1_ - this.smoothCamPartialTicks;
                this.smoothCamPartialTicks = p_78480_1_;
                f3 = this.smoothCamFilterX * f5;
                f4 = this.smoothCamFilterY * f5;
                this.mc.thePlayer.setAngles(f3, f4 * (float)b0);
            }
            else
            {
                this.mc.thePlayer.setAngles(f3, f4 * (float)b0);
            }
        }

        this.mc.mcProfiler.endSection();

        if (!this.mc.skipRenderWorld)
        {
            anaglyphEnable = this.mc.gameSettings.anaglyph;
            final ScaledResolution scaledresolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
            int i = scaledresolution.getScaledWidth();
            int j = scaledresolution.getScaledHeight();
            final int k = Mouse.getX() * i / this.mc.displayWidth;
            final int l = j - Mouse.getY() * j / this.mc.displayHeight - 1;
            int i1 = this.mc.gameSettings.limitFramerate;
            boolean var12;

            if (this.mc.theWorld != null)
            {
                this.mc.mcProfiler.startSection("level");

                if (this.mc.isFramerateLimitBelowMax())
                {
                    this.renderWorld(p_78480_1_, this.renderEndNanoTime + (long)(1000000000 / i1));
                }
                else
                {
                    this.renderWorld(p_78480_1_, 0L);
                }

                if (OpenGlHelper.shadersSupported)
                {
                    if (this.theShaderGroup != null)
                    {
                        GL11.glMatrixMode(GL11.GL_TEXTURE);
                        GL11.glPushMatrix();
                        GL11.glLoadIdentity();
                        this.theShaderGroup.loadShaderGroup(p_78480_1_);
                        GL11.glPopMatrix();
                    }

                    this.mc.getFramebuffer().bindFramebuffer(true);
                }

                this.renderEndNanoTime = System.nanoTime();
                this.mc.mcProfiler.endStartSection("gui");

                if (!this.mc.gameSettings.hideGUI || this.mc.currentScreen != null)
                {
                    GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
                    var12 = this.mc.gameSettings.fancyGraphics;

                    if (!Config.isVignetteEnabled())
                    {
                        this.mc.gameSettings.fancyGraphics = false;
                    }

                    this.mc.ingameGUI.renderGameOverlay(p_78480_1_, this.mc.currentScreen != null, k, l);
                    this.mc.gameSettings.fancyGraphics = var12;

                    if (this.mc.gameSettings.ofShowFps && !this.mc.gameSettings.showDebugInfo)
                    {
                        Config.drawFps();
                    }
                }

                this.mc.mcProfiler.endSection();
            }
            else
            {
                GL11.glViewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
                GL11.glMatrixMode(GL11.GL_PROJECTION);
                GL11.glLoadIdentity();
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                GL11.glLoadIdentity();
                this.setupOverlayRendering();
                this.renderEndNanoTime = System.nanoTime();
            }

            if (this.mc.currentScreen != null)
            {
                GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

                try
                {
                    if (!MinecraftForge.EVENT_BUS.post(new DrawScreenEvent.Pre(this.mc.currentScreen, k, l, p_78480_1_)))
                        this.mc.currentScreen.drawScreen(k, l, p_78480_1_);
                    MinecraftForge.EVENT_BUS.post(new DrawScreenEvent.Post(this.mc.currentScreen, k, l, p_78480_1_));
                }
                catch (Throwable throwable)
                {
                    CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering screen");
                    CrashReportCategory crashreportcategory = crashreport.makeCategory("Screen render details");
                    crashreportcategory.addCrashSectionCallable("Screen name", () -> EntityRenderer.this.mc.currentScreen.getClass().getCanonicalName());
                    crashreportcategory.addCrashSectionCallable("Mouse location", () -> String.format("Scaled: (%d, %d). Absolute: (%d, %d)", k, l, Mouse.getX(), Mouse.getY()));
                    crashreportcategory.addCrashSectionCallable("Screen size", () -> String.format("Scaled: (%d, %d). Absolute: (%d, %d). Scale factor of %d", scaledresolution.getScaledWidth(),
                    scaledresolution.getScaledHeight(), EntityRenderer.this.mc.displayWidth, EntityRenderer.this.mc.displayHeight, scaledresolution.getScaleFactor()));
                    throw new ReportedException(crashreport);
                }
            }
        }
        this.frameFinish();
        this.waitForServerThread();

        if (this.mc.gameSettings.showDebugInfo != this.lastShowDebugInfo)
        {
            this.showExtendedDebugInfo = this.mc.gameSettings.showDebugProfilerChart;
            this.lastShowDebugInfo = this.mc.gameSettings.showDebugInfo;
        }

        if (this.mc.gameSettings.showDebugInfo)
        {
            this.showLagometer(this.mc.mcProfiler.timeTickNano, this.mc.mcProfiler.timeUpdateChunksNano);
        }

        if (this.mc.gameSettings.ofProfiler)
        {
            this.mc.gameSettings.showDebugProfilerChart = true;
        }
    }

    public void func_152430_c(float p_152430_1_)
    {
        this.setupOverlayRendering();
        ScaledResolution scaledresolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
        int i = scaledresolution.getScaledWidth();
        int j = scaledresolution.getScaledHeight();
        this.mc.ingameGUI.func_152126_a((float)i, (float)j);
    }

    public void renderWorld(float tickTime, long p_78471_2_) {
        boolean isShaders = Config.isShaders();

        if (isShaders) {
            Shaders.beginRender(this.mc, tickTime, p_78471_2_);
        }

        float partialTicks = tickTime;
        this.mc.mcProfiler.startSection("lightTex");

        if (this.lightmapUpdateNeeded) {
            this.updateLightmap(tickTime);
        }

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.5F);

        if (this.mc.renderViewEntity == null) {
            this.mc.renderViewEntity = this.mc.thePlayer;
        }

        this.mc.mcProfiler.endStartSection("pick");
        this.getMouseOver(tickTime);
        EntityLivingBase entitylivingbase = this.mc.renderViewEntity;
        RenderGlobal renderglobal = this.mc.renderGlobal;
        EffectRenderer effectrenderer = this.mc.effectRenderer;
        double d0 = entitylivingbase.lastTickPosX + (entitylivingbase.posX - entitylivingbase.lastTickPosX) * (double) tickTime;
        double d1 = entitylivingbase.lastTickPosY + (entitylivingbase.posY - entitylivingbase.lastTickPosY) * (double) tickTime;
        double d2 = entitylivingbase.lastTickPosZ + (entitylivingbase.posZ - entitylivingbase.lastTickPosZ) * (double) tickTime;
        this.mc.mcProfiler.endStartSection("center");

        for (int j = 0; j < 2; ++j) {
            if (this.mc.gameSettings.anaglyph) {
                anaglyphField = j;

                if (anaglyphField == 0) {
                    GL11.glColorMask(false, true, true, false);
                } else {
                    GL11.glColorMask(true, false, false, false);
                }
            }

            if (isShaders) {
                Shaders.beginRenderPass(2, tickTime, p_78471_2_);
            }

            this.mc.mcProfiler.endStartSection("clear");

            if (isShaders) {
                Shaders.setViewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
            } else {
                GL11.glViewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
            }

            this.updateFogColor(tickTime);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            if (isShaders) {
                Shaders.clearRenderBuffer();
            }

            GL11.glEnable(GL11.GL_CULL_FACE);
            this.mc.mcProfiler.endStartSection("camera");
            this.setupCameraTransform(tickTime, j);

            if (isShaders) {
                Shaders.setCamera(tickTime);
            }

            ActiveRenderInfo.updateRenderInfo(this.mc.thePlayer, this.mc.gameSettings.thirdPersonView == 2);
            this.mc.mcProfiler.endStartSection("frustrum");
            ClippingHelperImpl.getInstance();

            if ((Config.isSkyEnabled() || Config.isSunMoonEnabled() || Config.isStarsEnabled()) && !Shaders.isShadowPass) {
                this.setupFog(-1, tickTime);
                this.mc.mcProfiler.endStartSection("sky");

                if (isShaders) {
                    Shaders.beginSky();
                }

                renderglobal.renderSky(tickTime);

                if (isShaders) {
                    Shaders.endSky();
                }
            } else {
                GL11.glDisable(GL11.GL_BLEND);
            }

            GL11.glEnable(GL11.GL_FOG);
            this.setupFog(1, tickTime);

            if (this.mc.gameSettings.ambientOcclusion != 0) {
                GL11.glShadeModel(GL11.GL_SMOOTH);
            }

            this.mc.mcProfiler.endStartSection("culling");
            Frustrum frustrum = new Frustrum();

            if (isShaders) {
                ShadersRender.setFrustrumPosition(frustrum, d0, d1, d2);
            }

            frustrum.setPosition(d0, d1, d2);
            this.mc.renderGlobal.clipRenderersByFrustum(frustrum, tickTime);

            if (j == 0) {
                if (Config.isShaders()) {
                    Shaders.beginUpdateChunks();
                }

                ++this.frameCount;
                this.mc.mcProfiler.endStartSection("updatechunks");

                while (!this.mc.renderGlobal.updateRenderers(entitylivingbase, false) && p_78471_2_ != 0L) {
                    long k = p_78471_2_ - System.nanoTime();

                    if (k < 0L || k > 1000000000L) {
                        break;
                    }
                }

                if (Config.isShaders()) {
                    Shaders.endUpdateChunks();
                }
            }

            if (entitylivingbase.posY < 128.0D) {
                this.renderCloudsCheck(renderglobal, tickTime);
            }

            this.mc.mcProfiler.endStartSection("prepareterrain");
            this.setupFog(0, tickTime);
            GL11.glEnable(GL11.GL_FOG);
            this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
            RenderHelper.disableStandardItemLighting();
            this.mc.mcProfiler.endStartSection("terrain");
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPushMatrix();

            if (isShaders) {
                shadersmod.client.Shaders.beginTerrain();
            }

            renderglobal.sortAndRender(entitylivingbase, 0, tickTime);
            mc.checkGLError("terrain");

            if (isShaders) {
                shadersmod.client.Shaders.endTerrain();
            }

            GL11.glShadeModel(GL11.GL_FLAT);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
            EntityPlayer entityplayer;

            if (this.debugViewDirection == 0) {
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                GL11.glPopMatrix();
                GL11.glPushMatrix();
                RenderHelper.enableStandardItemLighting();
                this.mc.mcProfiler.endStartSection("entities");
                net.minecraftforge.client.ForgeHooksClient.setRenderPass(0);
                renderglobal.renderEntities(entitylivingbase, frustrum, tickTime);
                mc.checkGLError("entities");
                net.minecraftforge.client.ForgeHooksClient.setRenderPass(0);
                RenderHelper.disableStandardItemLighting();
                this.disableLightmap(tickTime);
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                GL11.glPopMatrix();
                GL11.glPushMatrix();

                if (this.mc.objectMouseOver != null && entitylivingbase.isInsideOfMaterial(Material.water) && entitylivingbase instanceof EntityPlayer && !this.mc.gameSettings.hideGUI) {
                    entityplayer = (EntityPlayer) entitylivingbase;
                    GL11.glDisable(GL11.GL_ALPHA_TEST);
                    this.mc.mcProfiler.endStartSection("outline");
                    
                    if (!ForgeHooksClient.onDrawBlockHighlight(renderglobal, entityplayer, mc.objectMouseOver, 0, entityplayer.inventory.getCurrentItem(), tickTime)) {
                        renderglobal.drawSelectionBox(entityplayer, this.mc.objectMouseOver, 0, tickTime);
                    }
                    GL11.glEnable(GL11.GL_ALPHA_TEST);
                }
            }

            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPopMatrix();

            if (isShaders) {
                ShadersRender.renderHand0(this, tickTime, 2);
                Shaders.preWater();
            }

            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_CULL_FACE);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
            this.setupFog(0, tickTime);
            GL11.glEnable(GL11.GL_BLEND);
            this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
            WrUpdates.resumeBackgroundUpdates();

            if (Config.isWaterFancy()) {
                this.mc.mcProfiler.endStartSection("water");

                if (this.mc.gameSettings.ambientOcclusion != 0) {
                    GL11.glShadeModel(GL11.GL_SMOOTH);
                }

                GL11.glEnable(GL11.GL_BLEND);
                OpenGlHelper.glBlendFunc(770, 771, 1, 0);

                if (this.mc.gameSettings.anaglyph) {
                    if (anaglyphField == 0) {
                        GL11.glColorMask(false, true, true, true);
                    } else {
                        GL11.glColorMask(true, false, false, true);
                    }

                    if (isShaders) {
                        Shaders.beginWater();
                    }

                    renderglobal.renderAllSortedRenderers(1, tickTime);

                    if (isShaders) {
                        Shaders.endWater();
                    } else {
                    if (isShaders) {
                        Shaders.beginWater();
                    }

                    renderglobal.renderAllSortedRenderers(1, tickTime);

                    if (isShaders) {
                        Shaders.endWater();
                    }

                GL11.glDisable(GL11.GL_BLEND);
                GL11.glShadeModel(GL11.GL_FLAT);
                }
            }
            else 
            {
                this.mc.mcProfiler.endStartSection("water");

                if (isShaders) {
                    shadersmod.client.Shaders.beginWater();
                }

                renderglobal.renderAllSortedRenderers(1, tickTime);

                if (isShaders) {
                    shadersmod.client.Shaders.endWater();
                }

            mc.checkGLError("water");
            WrUpdates.pauseBackgroundUpdates();

            if (this.cameraZoom == 1.0D && entitylivingbase instanceof EntityPlayer && !this.mc.gameSettings.hideGUI && this.mc.objectMouseOver != null && !entitylivingbase.isInsideOfMaterial(Material.water)) {
                entityplayer = (EntityPlayer) entitylivingbase;
                GL11.glDisable(GL11.GL_ALPHA_TEST);
                this.mc.mcProfiler.endStartSection("outline");
                if (!ForgeHooksClient.onDrawBlockHighlight(renderglobal, entityplayer, mc.objectMouseOver, 0, entityplayer.inventory.getCurrentItem(), tickTime)) {
                    renderglobal.drawSelectionBox(entityplayer, this.mc.objectMouseOver, 0, tickTime);
                }
                GL11.glEnable(GL11.GL_ALPHA_TEST);
            }



            this.mc.mcProfiler.endStartSection("destroyProgress");
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 1, 1, 0);
            renderglobal.drawBlockDamageTexture(Tessellator.instance, entitylivingbase, tickTime);
            GL11.glDisable(GL11.GL_BLEND);

            if (this.debugViewDirection == 0) {
                this.enableLightmap(tickTime);
                this.mc.mcProfiler.endStartSection("litParticles");

                if (isShaders) {
                    Shaders.beginLitParticles();
                }

                effectrenderer.renderLitParticles(entitylivingbase, tickTime);
                RenderHelper.disableStandardItemLighting();
                this.setupFog(0, tickTime);
                this.mc.mcProfiler.endStartSection("particles");

                if (isShaders) {
                    Shaders.beginParticles();
                }

                effectrenderer.renderParticles(entitylivingbase, tickTime);
                mc.checkGLError("particles");

                if (isShaders) {
                    Shaders.endParticles();
                }

                this.disableLightmap(tickTime);
            }

            GL11.glDepthMask(false);
            GL11.glEnable(GL11.GL_CULL_FACE);
            this.mc.mcProfiler.endStartSection("weather");

            if (isShaders) {
                Shaders.beginWeather();
            }

            this.renderRainSnow(tickTime);

            if (isShaders) {
                Shaders.endWeather();
            }

            GL11.glDepthMask(true);

            if (this.debugViewDirection == 0) //Only render if render pass 0 happens as well.
            {
                RenderHelper.enableStandardItemLighting();
                this.mc.mcProfiler.endStartSection("entities");
                ForgeHooksClient.setRenderPass(1);
                renderglobal.renderEntities(entitylivingbase, frustrum, tickTime);
                ForgeHooksClient.setRenderPass(-1);
                RenderHelper.disableStandardItemLighting();
            }
            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_FOG);

            if (entitylivingbase.posY >= 128.0D + (double) (this.mc.gameSettings.ofCloudsHeight * 128.0F)) {
                this.mc.mcProfiler.endStartSection("aboveClouds");
                this.renderCloudsCheck(renderglobal, tickTime);
            }

            this.mc.mcProfiler.endStartSection("FRenderLast");
            ForgeHooksClient.dispatchRenderLast(renderglobal, tickTime);
            mc.checkGLError("renderLast");
            this.mc.mcProfiler.endStartSection("hand");

            if (!ForgeHooksClient.renderFirstPersonHand(renderglobal, tickTime, j) && this.cameraZoom == 1.0D && !Shaders.isShadowPass) {
                if (isShaders) {
                    ShadersRender.renderHand1(this, tickTime, j);
                    Shaders.renderCompositeFinal();
                } else {
                    GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
                    if (isShaders) {
                        ShadersRender.renderFPOverlay(this, partialTicks, j);
                  } else {
                        this.renderHand(tickTime, j);
                  }
                }
            }

            if (isShaders) {
                Shaders.endRender();
            }

            if (!this.mc.gameSettings.anaglyph) {
                this.mc.mcProfiler.endSection();
                return;
            }
        }

        GL11.glColorMask(true, true, true, false);
        this.mc.mcProfiler.endSection();
            	}
        	}
    	}

    /**
     * Render clouds if enabled
     */
    private void renderCloudsCheck(RenderGlobal par1RenderGlobal, float par2) {
            if (this.mc.gameSettings.shouldRenderClouds() && Shaders.shouldRenderClouds(this.mc.gameSettings)) {
                  this.mc.mcProfiler.endStartSection("clouds");
                  GL11.glPushMatrix();
                  this.setupFog(0, par2);
                  GL11.glEnable(2912);
                  
                  if (Config.isShaders()) {
                        Shaders.beginClouds();
                  }

                  par1RenderGlobal.renderClouds(par2);
                  
                  if (Config.isShaders()) {
                        Shaders.endClouds();
                  }

                  GL11.glDisable(2912);
                  this.setupFog(1, par2);
                  GL11.glPopMatrix();
            }

      }

    private void addRainParticles() {
    	
            float var1 = this.mc.theWorld.getRainStrength(1.0F);
            
            if (!Config.isRainFancy()) {
                  var1 /= 2.0F;
            }

            if (var1 != 0.0F && Config.isRainSplash()) {
                  this.random.setSeed((long)this.rendererUpdateCount * 312987231L);
                  EntityLivingBase var2 = this.mc.renderViewEntity;
                  WorldClient var3 = this.mc.theWorld;
                  int var4 = MathHelper.floor_double(var2.posX);
                  int var5 = MathHelper.floor_double(var2.posY);
                  int var6 = MathHelper.floor_double(var2.posZ);
                  byte var7 = 10;
                  double var8 = 0.0D;
                  double var10 = 0.0D;
                  double var12 = 0.0D;
                  int var14 = 0;
                  int var15 = (int)(100.0F * var1 * var1);
                  
                  if (this.mc.gameSettings.particleSetting == 1) {
                        var15 >>= 1;
                  } else if (this.mc.gameSettings.particleSetting == 2) {
                        var15 = 0;
                  }

                  for(int var16 = 0; var16 < var15; ++var16) {
                        int var17 = var4 + this.random.nextInt(var7) - this.random.nextInt(var7);
                        int var18 = var6 + this.random.nextInt(var7) - this.random.nextInt(var7);
                        int var19 = var3.getPrecipitationHeight(var17, var18);
                        Block var20 = var3.getBlock(var17, var19 - 1, var18);
                        BiomeGenBase var21 = var3.getBiomeGenForCoords(var17, var18);
                        
                        if (var19 <= var5 + var7 && var19 >= var5 - var7 && var21.canSpawnLightningBolt() && var21.getFloatTemperature(var17, var19, var18) >= 0.15F) {
                              float var22 = this.random.nextFloat();
                              float var23 = this.random.nextFloat();
                              
                              if (var20.getMaterial() == Material.lava) {
                                    this.mc.effectRenderer.addEffect(new EntitySmokeFX(var3, (double)((float)var17 + var22), (double)((float)var19 + 0.1F) - var20.getBlockBoundsMinY(), (double)((float)var18 + var23), 0.0D, 0.0D, 0.0D));
                              } 
                              else if (var20.getMaterial() != Material.air) 
                              {
                                    ++var14;
                                    
                                    if (this.random.nextInt(var14) == 0) {
                                    	
                                          var8 = (double)((float)var17 + var22);
                                          var10 = (double)((float)var19 + 0.1F) - var20.getBlockBoundsMinY();
                                          var12 = (double)((float)var18 + var23);
                                    }

                                    EntityRainFX fx = new EntityRainFX(var3, (double)((float)var17 + var22), (double)((float)var19 + 0.1F) - var20.getBlockBoundsMinY(), (double)((float)var18 + var23));
                                    CustomColorizer.updateWaterFX(fx, var3);
                                    this.mc.effectRenderer.addEffect(fx);
                              }
                        }
                  }

                  if (var14 > 0 && this.random.nextInt(3) < this.rainSoundCounter++) {
                	  
                        this.rainSoundCounter = 0;
                        
                        if (var10 > var2.posY + 1.0D && var3.getPrecipitationHeight(MathHelper.floor_double(var2.posX), MathHelper.floor_double(var2.posZ)) > MathHelper.floor_double(var2.posY)) 
                        {
                              this.mc.theWorld.playSound(var8, var10, var12, "ambient.weather.rain", 0.1F, 0.5F, false);
                        } else 
                        {
                              this.mc.theWorld.playSound(var8, var10, var12, "ambient.weather.rain", 0.2F, 1.0F, false);
                        }
                  }
            }

      }

    /**
     * Render rain and snow
     */
    protected void renderRainSnow(float p_78474_1_)
    {
        IRenderHandler renderer = null;
        
        if ((renderer = this.mc.theWorld.provider.getWeatherRenderer()) != null)
        {
            renderer.render(p_78474_1_, this.mc.theWorld, mc);
            return;
        }

        float f1 = this.mc.theWorld.getRainStrength(p_78474_1_);

        if (f1 > 0.0F)
        {
            this.enableLightmap((double)p_78474_1_);

            if (this.rainXCoords == null)
            {
                this.rainXCoords = new float[1024];
                this.rainYCoords = new float[1024];

                for (int i = 0; i < 32; ++i)
                {
                    for (int j = 0; j < 32; ++j)
                    {
                        float f2 = (float)(j - 16);
                        float f3 = (float)(i - 16);
                        float f4 = MathHelper.sqrt_float(f2 * f2 + f3 * f3);
                        this.rainXCoords[i << 5 | j] = -f3 / f4;
                        this.rainYCoords[i << 5 | j] = f2 / f4;
                    }
                }
            }

            if (Config.isRainOff())
            {
                return;
            }

            EntityLivingBase entitylivingbase = this.mc.renderViewEntity;
            WorldClient worldclient = this.mc.theWorld;
            int k2 = MathHelper.floor_double(entitylivingbase.posX);
            int l2 = MathHelper.floor_double(entitylivingbase.posY);
            int i3 = MathHelper.floor_double(entitylivingbase.posZ);
            Tessellator tessellator = Tessellator.instance;
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glNormal3f(0.0F, 1.0F, 0.0F);
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
            double d0 = entitylivingbase.lastTickPosX + (entitylivingbase.posX - entitylivingbase.lastTickPosX) * (double)p_78474_1_;
            double d1 = entitylivingbase.lastTickPosY + (entitylivingbase.posY - entitylivingbase.lastTickPosY) * (double)p_78474_1_;
            double d2 = entitylivingbase.lastTickPosZ + (entitylivingbase.posZ - entitylivingbase.lastTickPosZ) * (double)p_78474_1_;
            int k = MathHelper.floor_double(d1);
            byte b0 = 5;

            if (Config.isRainFancy())
            {
                b0 = 10;
            }

            boolean flag = false;
            byte b1 = -1;
            float f5 = (float)this.rendererUpdateCount + p_78474_1_;

            if (Config.isRainFancy())
            {
                b0 = 10;
            }

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            flag = false;

            for (int l = i3 - b0; l <= i3 + b0; ++l)
            {
                for (int i1 = k2 - b0; i1 <= k2 + b0; ++i1)
                {
                    int j1 = (l - i3 + 16) * 32 + i1 - k2 + 16;
                    float f6 = this.rainXCoords[j1] * 0.5F;
                    float f7 = this.rainYCoords[j1] * 0.5F;
                    BiomeGenBase biomegenbase = worldclient.getBiomeGenForCoords(i1, l);

                    if (biomegenbase.canSpawnLightningBolt() || biomegenbase.getEnableSnow())
                    {
                        int k1 = worldclient.getPrecipitationHeight(i1, l);
                        int l1 = l2 - b0;
                        int i2 = l2 + b0;

                        if (l1 < k1)
                        {
                            l1 = k1;
                        }

                        if (i2 < k1)
                        {
                            i2 = k1;
                        }

                        float f8 = 1.0F;
                        int j2 = k1;

                        if (k1 < k)
                        {
                            j2 = k;
                        }

                        if (l1 != i2)
                        {
                            this.random.setSeed((long)(i1 * i1 * 3121 + i1 * 45238971 ^ l * l * 418711 + l * 13761));
                            float f9 = biomegenbase.getFloatTemperature(i1, l1, l);
                            float f10;
                            double d4;

                            if (worldclient.getWorldChunkManager().getTemperatureAtHeight(f9, k1) >= 0.15F)
                            {
                                if (b1 != 0)
                                {
                                    if (b1 >= 0)
                                    {
                                        tessellator.draw();
                                    }

                                    b1 = 0;
                                    this.mc.getTextureManager().bindTexture(locationRainPng);
                                    tessellator.startDrawingQuads();
                                }

                                f10 = ((float)(this.rendererUpdateCount + i1 * i1 * 3121 + i1 * 45238971 + l * l * 418711 + l * 13761 & 31) + p_78474_1_) / 32.0F * (3.0F + this.random.nextFloat());
                                double d3 = (double)((float)i1 + 0.5F) - entitylivingbase.posX;
                                d4 = (double)((float)l + 0.5F) - entitylivingbase.posZ;
                                float f12 = MathHelper.sqrt_double(d3 * d3 + d4 * d4) / (float)b0;
                                float f13 = 1.0F;
                                tessellator.setBrightness(worldclient.getLightBrightnessForSkyBlocks(i1, j2, l, 0));
                                tessellator.setColorRGBA_F(f13, f13, f13, ((1.0F - f12 * f12) * 0.5F + 0.5F) * f1);
                                tessellator.setTranslation(-d0 * 1.0D, -d1 * 1.0D, -d2 * 1.0D);
                                tessellator.addVertexWithUV((double)((float)i1 - f6) + 0.5D, (double)l1, (double)((float)l - f7) + 0.5D, (double)(0.0F * f8), (double)((float)l1 * f8 / 4.0F + f10 * f8));
                                tessellator.addVertexWithUV((double)((float)i1 + f6) + 0.5D, (double)l1, (double)((float)l + f7) + 0.5D, (double)(1.0F * f8), (double)((float)l1 * f8 / 4.0F + f10 * f8));
                                tessellator.addVertexWithUV((double)((float)i1 + f6) + 0.5D, (double)i2, (double)((float)l + f7) + 0.5D, (double)(1.0F * f8), (double)((float)i2 * f8 / 4.0F + f10 * f8));
                                tessellator.addVertexWithUV((double)((float)i1 - f6) + 0.5D, (double)i2, (double)((float)l - f7) + 0.5D, (double)(0.0F * f8), (double)((float)i2 * f8 / 4.0F + f10 * f8));
                                tessellator.setTranslation(0.0D, 0.0D, 0.0D);
                            }
                            else
                            {
                                if (b1 != 1)
                                {
                                    if (b1 >= 0)
                                    {
                                        tessellator.draw();
                                    }

                                    b1 = 1;
                                    this.mc.getTextureManager().bindTexture(locationSnowPng);
                                    tessellator.startDrawingQuads();
                                }

                                f10 = ((float)(this.rendererUpdateCount & 511) + p_78474_1_) / 512.0F;
                                float f16 = this.random.nextFloat() + f5 * 0.01F * (float)this.random.nextGaussian();
                                float f11 = this.random.nextFloat() + f5 * (float)this.random.nextGaussian() * 0.001F;
                                d4 = (double)((float)i1 + 0.5F) - entitylivingbase.posX;
                                double d5 = (double)((float)l + 0.5F) - entitylivingbase.posZ;
                                float f14 = MathHelper.sqrt_double(d4 * d4 + d5 * d5) / (float)b0;
                                float f15 = 1.0F;
                                tessellator.setBrightness((worldclient.getLightBrightnessForSkyBlocks(i1, j2, l, 0) * 3 + 15728880) / 4);
                                tessellator.setColorRGBA_F(f15, f15, f15, ((1.0F - f14 * f14) * 0.3F + 0.5F) * f1);
                                tessellator.setTranslation(-d0 * 1.0D, -d1 * 1.0D, -d2 * 1.0D);
                                tessellator.addVertexWithUV((double)((float)i1 - f6) + 0.5D, (double)l1, (double)((float)l - f7) + 0.5D, (double)(0.0F * f8 + f16), (double)((float)l1 * f8 / 4.0F + f10 * f8 + f11));
                                tessellator.addVertexWithUV((double)((float)i1 + f6) + 0.5D, (double)l1, (double)((float)l + f7) + 0.5D, (double)(1.0F * f8 + f16), (double)((float)l1 * f8 / 4.0F + f10 * f8 + f11));
                                tessellator.addVertexWithUV((double)((float)i1 + f6) + 0.5D, (double)i2, (double)((float)l + f7) + 0.5D, (double)(1.0F * f8 + f16), (double)((float)i2 * f8 / 4.0F + f10 * f8 + f11));
                                tessellator.addVertexWithUV((double)((float)i1 - f6) + 0.5D, (double)i2, (double)((float)l - f7) + 0.5D, (double)(0.0F * f8 + f16), (double)((float)i2 * f8 / 4.0F + f10 * f8 + f11));
                                tessellator.setTranslation(0.0D, 0.0D, 0.0D);
                            }
                        }
                    }
                }
            }

            if (b1 >= 0)
            {
                tessellator.draw();
            }

            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
            this.disableLightmap(p_78474_1_);
        }
    }

    /**
     * Setup orthogonal projection for rendering GUI screen overlays
     */
    public void setupOverlayRendering() {
        ScaledResolution var1 = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
        GL11.glClear(256);
        GL11.glMatrixMode(5889);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0D, var1.getScaledWidth_double(), var1.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D);
        GL11.glMatrixMode(5888);
        GL11.glLoadIdentity();
        GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
  }

    /**
     * calculates fog and calls glClearColor
     */
      private void updateFogColor(float par1) {
            WorldClient var2 = this.mc.theWorld;
            EntityLivingBase var3 = this.mc.renderViewEntity;
            float var4 = 0.25F + 0.75F * (float)this.mc.gameSettings.renderDistanceChunks / 16.0F;
            var4 = 1.0F - (float)Math.pow((double)var4, 0.25D);
            Vec3 var5 = var2.getSkyColor(this.mc.renderViewEntity, par1);
            var5 = CustomColorizer.getWorldSkyColor(var5, var2, this.mc.renderViewEntity, par1);
            float var6 = (float)var5.xCoord;
            float var7 = (float)var5.yCoord;
            float var8 = (float)var5.zCoord;
            Vec3 var9 = var2.getFogColor(par1);
            var9 = CustomColorizer.getWorldFogColor(var9, var2, par1);
            this.fogColorRed = (float)var9.xCoord;
            this.fogColorGreen = (float)var9.yCoord;
            this.fogColorBlue = (float)var9.zCoord;
            float var11;
            
            if (this.mc.gameSettings.renderDistanceChunks >= 4) 
            {
                  Vec3 var10 = MathHelper.sin(var2.getCelestialAngleRadians(par1)) > 0.0F ? Vec3.createVectorHelper(-1.0D, 0.0D, 0.0D) : Vec3.createVectorHelper(1.0D, 0.0D, 0.0D);
                  var11 = (float)var3.getLook(par1).dotProduct(var10);
                  if (var11 < 0.0F) {
                        var11 = 0.0F;
                  }

                  if (var11 > 0.0F) {
                        float[] var12 = var2.provider.calcSunriseSunsetColors(var2.getCelestialAngle(par1), par1);
                        if (var12 != null) {
                              var11 *= var12[3];
                              this.fogColorRed = this.fogColorRed * (1.0F - var11) + var12[0] * var11;
                              this.fogColorGreen = this.fogColorGreen * (1.0F - var11) + var12[1] * var11;
                              this.fogColorBlue = this.fogColorBlue * (1.0F - var11) + var12[2] * var11;
                        }
                  }
            }

            this.fogColorRed += (var6 - this.fogColorRed) * var4;
            this.fogColorGreen += (var7 - this.fogColorGreen) * var4;
            this.fogColorBlue += (var8 - this.fogColorBlue) * var4;
            float var19 = var2.getRainStrength(par1);
            float var20;
            
            if (var19 > 0.0F) {
                  var11 = 1.0F - var19 * 0.5F;
                  var20 = 1.0F - var19 * 0.4F;
                  this.fogColorRed *= var11;
                  this.fogColorGreen *= var11;
                  this.fogColorBlue *= var20;
            }

            var11 = var2.getWeightedThunderStrength(par1);
            
            if (var11 > 0.0F) {
                  var20 = 1.0F - var11 * 0.5F;
                  this.fogColorRed *= var20;
                  this.fogColorGreen *= var20;
                  this.fogColorBlue *= var20;
            }

            Block var21 = ActiveRenderInfo.getBlockAtEntityViewpoint(this.mc.theWorld, var3, par1);
            float var22;
            Vec3 colUnderwater;
            
            if (this.cloudFog) {
                  colUnderwater = var2.getCloudColour(par1);
                  this.fogColorRed = (float)colUnderwater.xCoord;
                  this.fogColorGreen = (float)colUnderwater.yCoord;
                  this.fogColorBlue = (float)colUnderwater.zCoord;
            } else if (var21.getMaterial() == Material.water) {
                  var22 = (float)EnchantmentHelper.getRespiration(var3) * 0.2F;
                  this.fogColorRed = 0.02F + var22;
                  this.fogColorGreen = 0.02F + var22;
                  this.fogColorBlue = 0.2F + var22;
                  colUnderwater = CustomColorizer.getUnderwaterColor(this.mc.theWorld, this.mc.renderViewEntity.posX, this.mc.renderViewEntity.posY + 1.0D, this.mc.renderViewEntity.posZ);
                  if (colUnderwater != null) {
                        this.fogColorRed = (float)colUnderwater.xCoord;
                        this.fogColorGreen = (float)colUnderwater.yCoord;
                        this.fogColorBlue = (float)colUnderwater.zCoord;
                  }
            } else if (var21.getMaterial() == Material.lava) {
                  this.fogColorRed = 0.6F;
                  this.fogColorGreen = 0.1F;
                  this.fogColorBlue = 0.0F;
            }

            var22 = this.fogColor2 + (this.fogColor1 - this.fogColor2) * par1;
            this.fogColorRed *= var22;
            this.fogColorGreen *= var22;
            this.fogColorBlue *= var22;
            double fogYFactor = var2.provider.getVoidFogYFactor();
            
            if (!Config.isDepthFog()) {
                  fogYFactor = 1.0D;
            }

            double var14 = (var3.lastTickPosY + (var3.posY - var3.lastTickPosY) * (double)par1) * fogYFactor;
            if (var3.isPotionActive(Potion.blindness)) 
            {
                  int var16 = var3.getActivePotionEffect(Potion.blindness).getDuration();
                  
                  if (var16 < 20) 
                  {
                        var14 *= (double)(1.0F - (float)var16 / 20.0F);
                  } 
                  else 
                  {
                        var14 = 0.0D;
                  }
            }

            if (var14 < 1.0D) {
                  if (var14 < 0.0D) {
                        var14 = 0.0D;
                  }

                  var14 *= var14;
                  this.fogColorRed = (float)((double)this.fogColorRed * var14);
                  this.fogColorGreen = (float)((double)this.fogColorGreen * var14);
                  this.fogColorBlue = (float)((double)this.fogColorBlue * var14);
            }

            float var23;
            if (this.bossColorModifier > 0.0F) {
                  var23 = this.bossColorModifierPrev + (this.bossColorModifier - this.bossColorModifierPrev) * par1;
                  this.fogColorRed = this.fogColorRed * (1.0F - var23) + this.fogColorRed * 0.7F * var23;
                  this.fogColorGreen = this.fogColorGreen * (1.0F - var23) + this.fogColorGreen * 0.6F * var23;
                  this.fogColorBlue = this.fogColorBlue * (1.0F - var23) + this.fogColorBlue * 0.6F * var23;
            }

            float var17;
            
            if (var3.isPotionActive(Potion.nightVision)) {
                  var23 = this.getNightVisionBrightness(this.mc.thePlayer, par1);
                  var17 = 1.0F / this.fogColorRed;
                  
                  if (var17 > 1.0F / this.fogColorGreen) {
                        var17 = 1.0F / this.fogColorGreen;
                  }

                  if (var17 > 1.0F / this.fogColorBlue) {
                        var17 = 1.0F / this.fogColorBlue;
                  }

                  this.fogColorRed = this.fogColorRed * (1.0F - var23) + this.fogColorRed * var17 * var23;
                  this.fogColorGreen = this.fogColorGreen * (1.0F - var23) + this.fogColorGreen * var17 * var23;
                  this.fogColorBlue = this.fogColorBlue * (1.0F - var23) + this.fogColorBlue * var17 * var23;
            }

            if (this.mc.gameSettings.anaglyph) {
                  var23 = (this.fogColorRed * 30.0F + this.fogColorGreen * 59.0F + this.fogColorBlue * 11.0F) / 100.0F;
                  var17 = (this.fogColorRed * 30.0F + this.fogColorGreen * 70.0F) / 100.0F;
                  float var18 = (this.fogColorRed * 30.0F + this.fogColorBlue * 70.0F) / 100.0F;
                  this.fogColorRed = var23;
                  this.fogColorGreen = var17;
                  this.fogColorBlue = var18;
            }

            net.minecraftforge.client.event.EntityViewRenderEvent.FogColors event = new net.minecraftforge.client.event.EntityViewRenderEvent.FogColors(this, var3, var21, par1, this.fogColorRed, this.fogColorGreen, this.fogColorBlue);
            MinecraftForge.EVENT_BUS.post(event);

            this.fogColorRed = event.red;
            this.fogColorBlue = event.blue;
            this.fogColorGreen = event.green;

            if (Config.isShaders()) {
                  Shaders.setClearColor(this.fogColorRed, this.fogColorGreen, this.fogColorBlue, 0.0F);
            } else {
                  GL11.glClearColor(this.fogColorRed, this.fogColorGreen, this.fogColorBlue, 0.0F);
            }

      }

    /**
     * Sets up the fog to be rendered. If the arg passed in is -1 the fog starts at 0 and goes to 80% of far plane
     * distance and is used for sky rendering.
     */
    private void setupFog(int par1, float par2)
    {
        EntityLivingBase var3 = this.mc.renderViewEntity;
        boolean var4 = false;
        this.fogStandard = false;

        if (var3 instanceof EntityPlayer)
        {
            var4 = ((EntityPlayer)var3).capabilities.isCreativeMode;
        }

        if (par1 == 999)
        {
            GL11.glFog(GL11.GL_FOG_COLOR, this.setFogColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));

            if (Config.isShaders())
            {
                Shaders.sglFogi(2917, 9729);
            }
            else
            {
                GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR);
            }

            GL11.glFogf(GL11.GL_FOG_START, 0.0F);
            GL11.glFogf(GL11.GL_FOG_END, 8.0F);

            if (GLContext.getCapabilities().GL_NV_fog_distance)
            {
                if (Config.isShaders())
                {
                    Shaders.sglFogi(34138, 34139);
                }
                else
                {
                    GL11.glFogi(34138, 34139);
                }
            }

            GL11.glFogf(GL11.GL_FOG_START, 0.0F);
        }
        else
        {
            GL11.glFog(GL11.GL_FOG_COLOR, this.setFogColorBuffer(this.fogColorRed, this.fogColorGreen, this.fogColorBlue, 1.0F));
            GL11.glNormal3f(0.0F, -1.0F, 0.0F);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            Block var5 = ActiveRenderInfo.getBlockAtEntityViewpoint(this.mc.theWorld, var3, par2);
            float f1;

            net.minecraftforge.client.event.EntityViewRenderEvent.FogDensity event = new net.minecraftforge.client.event.EntityViewRenderEvent.FogDensity(this, var3, var5, par2, 0.1F);

            if (MinecraftForge.EVENT_BUS.post(event))
            {
                GL11.glFogf(GL11.GL_FOG_DENSITY, event.density);
            }
            else
            {
                float var6;

                if (var3.isPotionActive(Potion.blindness))
                {
                    var6 = 5.0F;
                    int var101 = var3.getActivePotionEffect(Potion.blindness).getDuration();

                    if (var101 < 20)
                    {
                        var6 = 5.0F + (this.farPlaneDistance - 5.0F) * (1.0F - (float)var101 / 20.0F);
                    }

                    if (Config.isShaders())
                    {
                        Shaders.sglFogi(2917, 9729);
                    }
                    else
                    {
                        GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR);
                    }

                    if (par1 < 0)
                    {
                        GL11.glFogf(GL11.GL_FOG_START, 0.0F);
                        GL11.glFogf(GL11.GL_FOG_END, var6 * 0.8F);
                    }
                    else
                    {
                        GL11.glFogf(GL11.GL_FOG_START, var6 * 0.25F);
                        GL11.glFogf(GL11.GL_FOG_END, var6);
                    }

                    if (Config.isFogFancy())
                    {
                        if (Config.isShaders())
                        {
                            Shaders.sglFogi(34138, 34139);
                        }
                        else
                        {
                            GL11.glFogi(34138, 34139);
                        }
                    }
                }
                else if (this.cloudFog)
                {
                    if (Config.isShaders())
                    {
                        Shaders.sglFogi(2917, 2048);
                    }
                    else
                    {
                        GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
                    }

                    GL11.glFogf(GL11.GL_FOG_DENSITY, 0.1F);
                }
                else if (var5.getMaterial() == Material.water)
                {
                    if (Config.isShaders())
                    {
                        Shaders.sglFogi(2917, 2048);
                    }
                    else
                    {
                        GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
                    }

                    if (var3.isPotionActive(Potion.waterBreathing))
                    {
                        GL11.glFogf(GL11.GL_FOG_DENSITY, 0.05F);
                    }
                    else
                    {
                        GL11.glFogf(GL11.GL_FOG_DENSITY, 0.1F - (float)EnchantmentHelper.getRespiration(var3) * 0.03F);
                    }

                    if (Config.isClearWater())
                    {
                        GL11.glFogf(GL11.GL_FOG_DENSITY, 0.02F);
                    }
                }
                else if (var5.getMaterial() == Material.lava)
                {
                    if (Config.isShaders())
                    {
                        Shaders.sglFogi(2917, 2048);
                    }
                    else
                    {
                        GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
                    }

                    GL11.glFogf(GL11.GL_FOG_DENSITY, 2.0F);
                }
                else
                {
                    var6 = this.farPlaneDistance;
                    this.fogStandard = true;

                    if (Config.isDepthFog() && this.mc.theWorld.provider.getWorldHasVoidParticles() && !var4)
                    {
                        double var102 = (double)((var3.getBrightnessForRender(par2) & 15728640) >> 20) / 16.0D + (var3.lastTickPosY + (var3.posY - var3.lastTickPosY) * (double)par2 + 4.0D) / 32.0D;

                        if (var102 < 1.0D)
                        {
                            if (var102 < 0.0D)
                            {
                                var102 = 0.0D;
                            }

                            var102 *= var102;
                            float var9 = 100.0F * (float)var102;

                            if (var9 < 5.0F)
                            {
                                var9 = 5.0F;
                            }

                            if (var6 > var9)
                            {
                                var6 = var9;
                            }
                        }
                    }

                    if (Config.isShaders())
                    {
                        Shaders.sglFogi(2917, 9729);
                    }
                    else
                    {
                        GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR);
                    }

                    if (par1 < 0)
                    {
                        GL11.glFogf(GL11.GL_FOG_START, 0.0F);
                        GL11.glFogf(GL11.GL_FOG_END, var6);
                    }
                    else
                    {
                        GL11.glFogf(GL11.GL_FOG_START, var6 * Config.getFogStart());
                        GL11.glFogf(GL11.GL_FOG_END, var6);
                    }

                    if (GLContext.getCapabilities().GL_NV_fog_distance)
                    {
                        if (Config.isFogFancy())
                        {
                            if (Config.isShaders())
                            {
                                Shaders.sglFogi(34138, 34139);
                            }
                            else
                            {
                                GL11.glFogi(34138, 34139);
                            }
                        }

                        if (Config.isFogFast())
                        {
                            if (Config.isShaders())
                            {
                                Shaders.sglFogi(34138, 34140);
                            }
                            else
                            {
                                GL11.glFogi(34138, 34140);
                            }
                        }
                    }

                    if (this.mc.theWorld.provider.doesXZShowFog((int)var3.posX, (int)var3.posZ))
                    {
                        var6 = this.farPlaneDistance;
                        GL11.glFogf(GL11.GL_FOG_START, var6 * 0.05F);
                        GL11.glFogf(GL11.GL_FOG_END, var6);
                    }

                    MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.EntityViewRenderEvent.RenderFogEvent(this, var3, var5, par2, par1, var6));
                }
            }

            GL11.glEnable(GL11.GL_COLOR_MATERIAL);
            GL11.glColorMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT);
        }
    }

    /**
     * Update and return fogColorBuffer with the RGBA values passed as arguments
     */
    private FloatBuffer setFogColorBuffer(float par1, float par2, float par3, float par4) {
        if (Config.isShaders()) {
              Shaders.setFogColor(par1, par2, par3);
        }

        this.fogColorBuffer.clear();
        this.fogColorBuffer.put(par1).put(par2).put(par3).put(par4);
        this.fogColorBuffer.flip();
        return this.fogColorBuffer;
  }

    public MapItemRenderer getMapItemRenderer() 
    {
        return this.theMapItemRenderer;
    }
    
    private void waitForServerThread() {
        this.serverWaitTimeCurrent = 0;
        if (!Config.isSmoothWorld()) {
              this.lastServerTime = 0L;
              this.lastServerTicks = 0;
        } else if (this.mc.getIntegratedServer() != null) {
              IntegratedServer srv = this.mc.getIntegratedServer();
              boolean paused = this.mc.isGamePaused();
              
              if (!paused && !(this.mc.currentScreen instanceof GuiDownloadTerrain)) {
                    if (this.serverWaitTime > 0) {
                          Config.sleep((long)this.serverWaitTime);
                          this.serverWaitTimeCurrent = this.serverWaitTime;
                    }

                    long timeNow = System.nanoTime() / 1000000L;
                    
                    if (this.lastServerTime != 0L && this.lastServerTicks != 0) {
                          long timeDiff = timeNow - this.lastServerTime;
                          if (timeDiff < 0L) {
                                this.lastServerTime = timeNow;
                                timeDiff = 0L;
                          }

                          if (timeDiff >= 50L) {
                                this.lastServerTime = timeNow;
                                int ticks = srv.getTickCounter();
                                int tickDiff = ticks - this.lastServerTicks;
                                if (tickDiff < 0) {
                                      this.lastServerTicks = ticks;
                                      tickDiff = 0;
                                }

                                if (tickDiff < 1 && this.serverWaitTime < 100) {
                                      this.serverWaitTime += 2;
                                }

                                if (tickDiff > 1 && this.serverWaitTime > 0) {
                                      --this.serverWaitTime;
                                }

                                this.lastServerTicks = ticks;
                          }
                    } else {
                          this.lastServerTime = timeNow;
                          this.lastServerTicks = srv.getTickCounter();
                          this.avgServerTickDiff = 1.0F;
                          this.avgServerTimeDiff = 50.0F;
                    }
              } else {
                    if (this.mc.currentScreen instanceof GuiDownloadTerrain) {
                          Config.sleep(20L);
                    }

                    this.lastServerTime = 0L;
                    this.lastServerTicks = 0;
              }
        }
  }

    private void showLagometer(long tickTimeNano, long chunkTimeNano) {
        if (this.mc.gameSettings.ofLagometer || this.showExtendedDebugInfo) {
              if (this.prevFrameTimeNano == -1L) {
                    this.prevFrameTimeNano = System.nanoTime();
              }

              long timeNowNano = System.nanoTime();
              int currFrameIndex = this.numRecordedFrameTimes & this.frameTimes.length - 1;
              this.tickTimes[currFrameIndex] = tickTimeNano;
              this.chunkTimes[currFrameIndex] = chunkTimeNano;
              this.serverTimes[currFrameIndex] = (long)this.serverWaitTimeCurrent;
              this.frameTimes[currFrameIndex] = timeNowNano - this.prevFrameTimeNano;
              ++this.numRecordedFrameTimes;
              this.prevFrameTimeNano = timeNowNano;
              GL11.glClear(256);
              GL11.glMatrixMode(5889);
              GL11.glPushMatrix();
              GL11.glEnable(2903);
              GL11.glLoadIdentity();
              GL11.glOrtho(0.0D, (double)this.mc.displayWidth, (double)this.mc.displayHeight, 0.0D, 1000.0D, 3000.0D);
              GL11.glMatrixMode(5888);
              GL11.glPushMatrix();
              GL11.glLoadIdentity();
              GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
              GL11.glLineWidth(1.0F);
              GL11.glDisable(3553);
              Tessellator tessellator = Tessellator.instance;
              tessellator.startDrawing(1);

              for(int frameNum = 0; frameNum < this.frameTimes.length; ++frameNum) {
                    int lum = (frameNum - this.numRecordedFrameTimes & this.frameTimes.length - 1) * 255 / this.frameTimes.length;
                    long heightFrame = this.frameTimes[frameNum] / 200000L;
                    float baseHeight = (float)this.mc.displayHeight;
                    tessellator.setColorOpaque_I(-16777216 + lum * 256);
                    tessellator.addVertex((double)((float)frameNum + 0.5F), (double)(baseHeight - (float)heightFrame + 0.5F), 0.0D);
                    tessellator.addVertex((double)((float)frameNum + 0.5F), (double)(baseHeight + 0.5F), 0.0D);
                    baseHeight -= (float)heightFrame;
                    long heightTick = this.tickTimes[frameNum] / 200000L;
                    tessellator.setColorOpaque_I(-16777216 + lum * 65536 + lum * 256 + lum * 1);
                    tessellator.addVertex((double)((float)frameNum + 0.5F), (double)(baseHeight + 0.5F), 0.0D);
                    tessellator.addVertex((double)((float)frameNum + 0.5F), (double)(baseHeight + (float)heightTick + 0.5F), 0.0D);
                    baseHeight += (float)heightTick;
                    long heightChunk = this.chunkTimes[frameNum] / 200000L;
                    tessellator.setColorOpaque_I(-16777216 + lum * 65536);
                    tessellator.addVertex((double)((float)frameNum + 0.5F), (double)(baseHeight + 0.5F), 0.0D);
                    tessellator.addVertex((double)((float)frameNum + 0.5F), (double)(baseHeight + (float)heightChunk + 0.5F), 0.0D);
                    baseHeight += (float)heightChunk;
                    long srvTime = this.serverTimes[frameNum];
                    if (srvTime > 0L) {
                          long heightSrv = srvTime * 1000000L / 200000L;
                          tessellator.setColorOpaque_I(-16777216 + lum * 1);
                          tessellator.addVertex((double)((float)frameNum + 0.5F), (double)(baseHeight + 0.5F), 0.0D);
                          tessellator.addVertex((double)((float)frameNum + 0.5F), (double)(baseHeight + (float)heightSrv + 0.5F), 0.0D);
                    }
              }

              tessellator.draw();
              GL11.glMatrixMode(5889);
              GL11.glPopMatrix();
              GL11.glMatrixMode(5888);
              GL11.glPopMatrix();
              GL11.glEnable(3553);
        }
  }

    //TODO: можно выпилить нахуй
    private void frameFinish() {
        if (this.mc.theWorld != null) {
              long now = System.currentTimeMillis();
              if (now > this.lastErrorCheckTimeMs + 10000L) {
                    this.lastErrorCheckTimeMs = now;
                    int err = GL11.glGetError();
                    if (err != 0) {
                          String text = GLU.gluErrorString(err);
                          ChatComponentText msg = new ChatComponentText(I18n.format("of.message.openglError", err, text));
                          this.mc.ingameGUI.getChatGUI().printChatMessage(msg);
                    }
              }
        }

  }

    private void updateMainMenu(GuiMainMenu mainGui) {
        try {
              String str = null;
              Calendar calendar = Calendar.getInstance();
              calendar.setTime(new Date());
              int day = calendar.get(5);
              int month = calendar.get(2) + 1;
              if (day == 8 && month == 4) {
                    str = "Happy birthday, OptiFine!";
              }

              if (day == 14 && month == 8) {
                    str = "Happy birthday, sp614x!";
              }

              if (str == null) {
                    return;
              }

              Field[] fs = GuiMainMenu.class.getDeclaredFields();

              for(int i = 0; i < fs.length; ++i) {
                    if (fs[i].getType() == String.class) {
                          fs[i].setAccessible(true);
                          fs[i].set(mainGui, str);
                          break;
                    }
              }
        } catch (Throwable var8) {
        }

  }

    public boolean setFxaaShader(int fxaaLevel) {
        if (!OpenGlHelper.isFramebufferEnabled()) {
              return false;
        } else if (this.theShaderGroup != null && this.theShaderGroup != this.fxaaShaders[2] && this.theShaderGroup != this.fxaaShaders[4]) {
              return true;
        } else if (fxaaLevel != 2 && fxaaLevel != 4) {
              if (this.theShaderGroup == null) {
                    return true;
              } else {
                    this.theShaderGroup.deleteShaderGroup();
                    this.theShaderGroup = null;
                    return true;
              }
        } else if (this.theShaderGroup != null && this.theShaderGroup == this.fxaaShaders[fxaaLevel]) {
              return true;
        } else if (this.mc.theWorld == null) {
              return true;
        } else {
              this.loadShader(new ResourceLocation("shaders/post/fxaa_of_" + fxaaLevel + "x.json"));
              this.fxaaShaders[fxaaLevel] = this.theShaderGroup;
              return this.theShaderGroup != null;
        }
  }

    private void loadShader(ResourceLocation p_175069_1_) {
        if (OpenGlHelper.isFramebufferEnabled()) {
              try {
                    this.theShaderGroup = new ShaderGroup(this.mc.getTextureManager(), this.resourceManager, this.mc.getFramebuffer(), p_175069_1_);
                    this.theShaderGroup.createBindFramebuffers(this.mc.displayWidth, this.mc.displayHeight);
              } catch (IOException var3) {
                    logger.warn("Failed to load shader: " + p_175069_1_, var3);
                    this.shaderIndex = shaderCount;
              } catch (JsonSyntaxException var4) {
                    logger.warn("Failed to load shader: " + p_175069_1_, var4);
                    this.shaderIndex = shaderCount;
              }

        }
  }
    
    
}