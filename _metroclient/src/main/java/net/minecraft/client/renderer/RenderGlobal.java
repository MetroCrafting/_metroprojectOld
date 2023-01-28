package net.minecraft.client.renderer;

import com.google.common.collect.Maps;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.util.RenderDistanceSorter;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemRecord;
import net.minecraft.profiler.Profiler;
import net.minecraft.src.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.*;
import net.minecraft.world.IWorldAccess;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.client.MinecraftForgeClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ARBOcclusionQuery;
import org.lwjgl.opengl.GL11;
import shadersmod.client.Shaders;

import java.nio.IntBuffer;
import java.util.*;
import java.util.concurrent.Callable;

@SideOnly(Side.CLIENT)
public class RenderGlobal implements IWorldAccess
{
    private static final Logger logger = LogManager.getLogger();
    private static final ResourceLocation locationMoonPhasesPng = new ResourceLocation("textures/environment/moon_phases.png");
    private static final ResourceLocation locationSunPng = new ResourceLocation("textures/environment/sun.png");
    private static final ResourceLocation locationCloudsPng = new ResourceLocation("textures/environment/clouds.png");
    private static final ResourceLocation locationEndSkyPng = new ResourceLocation("textures/environment/end_sky.png");
    public List tileEntities = new ArrayList();
    public WorldClient theWorld;
    /** The RenderEngine instance used by RenderGlobal */
    private final TextureManager renderEngine;
    public CompactArrayList worldRenderersToUpdate = new CompactArrayList(100, 0.8F);
    public WorldRenderer[] sortedWorldRenderers;
    public WorldRenderer[] worldRenderers;
    private int renderChunksWide;
    private int renderChunksTall;
    private int renderChunksDeep;
    /** OpenGL render lists base */
    private int glRenderListBase;
    /** A reference to the Minecraft object. */
    private Minecraft mc;
    private RenderBlocks renderBlocksRg;
    /** OpenGL occlusion query base */
    private IntBuffer glOcclusionQueryBase;
    /** Is occlusion testing enabled */
    public boolean occlusionEnabled;
    /** counts the cloud render updates. Used with mod to stagger some updates */
    private int cloudTickCounter;
    /** The star GL Call list */
    private int starGLCallList;
    /** OpenGL sky list */
    private int glSkyList;
    /** OpenGL sky list 2 */
    private int glSkyList2;
    /** Minimum block X */
    private int minBlockX;
    /** Minimum block Y */
    private int minBlockY;
    /** Minimum block Z */
    private int minBlockZ;
    /** Maximum block X */
    private int maxBlockX;
    /** Maximum block Y */
    private int maxBlockY;
    /** Maximum block Z */
    private int maxBlockZ;
    /**
     * Stores blocks currently being broken. Key is entity ID of the thing doing the breaking. Value is a
     * DestroyBlockProgress
     */
    private final Map damagedBlocks = new HashMap();
    /** Currently playing sounds.  Type:  HashMap<ChunkCoordinates, ISound> */
    private final Map mapSoundPositions = Maps.newHashMap();
    private IIcon[] destroyBlockIcons;
    private boolean displayListEntitiesDirty;
    private int displayListEntities;
    private int renderDistanceChunks = -1;
    /** Render entities startup counter (init value=2) */
    private int renderEntitiesStartupCounter = 2;
    /** Count entities total */
    private int countEntitiesTotal;
    /** Count entities rendered */
    private int countEntitiesRendered;
    /** Count entities hidden */
    private int countEntitiesHidden;
    /** Occlusion query result */
    IntBuffer occlusionResult = GLAllocation.createDirectIntBuffer(64);
    /** How many renderers are loaded this frame that try to be rendered */
    private int renderersLoaded;
    /** How many renderers are being clipped by the frustrum this frame */
    private int renderersBeingClipped;
    /** How many renderers are being occluded this frame */
    private int renderersBeingOccluded;
    /** How many renderers are actually being rendered this frame */
    private int renderersBeingRendered;
    /** How many renderers are skipping rendering due to not having a render pass this frame */
    private int renderersSkippingRenderPass;
    /** Dummy render int */
    private int dummyRenderInt;
    /** World renderers check index */
    private int worldRenderersCheckIndex;
    /** List of OpenGL lists for the current render pass */
    private List glRenderLists = new ArrayList();
    /** All render lists (fixed length 4) */
    private RenderList[] allRenderLists = new RenderList[] {new RenderList(), new RenderList(), new RenderList(), new RenderList()};
    /**
     * Previous x position when the renderers were sorted. (Once the distance moves more than 4 units they will be
     * resorted)
     */
    double prevSortX = -9999.0D;
    /**
     * Previous y position when the renderers were sorted. (Once the distance moves more than 4 units they will be
     * resorted)
     */
    double prevSortY = -9999.0D;
    /**
     * Previous Z position when the renderers were sorted. (Once the distance moves more than 4 units they will be
     * resorted)
     */
    double prevSortZ = -9999.0D;
    double prevRenderSortX = -9999.0D;
    double prevRenderSortY = -9999.0D;
    double prevRenderSortZ = -9999.0D;
    int prevChunkSortX = -999;
    int prevChunkSortY = -999;
    int prevChunkSortZ = -999;
    /** The offset used to determine if a renderer is one of the sixteenth that are being updated this frame */
    int frustumCheckOffset;
    private IntBuffer glListBuffer = BufferUtils.createIntBuffer(65536);
    double prevReposX;
    double prevReposY;
    double prevReposZ;
    public Entity renderedEntity;
    private int glListClouds = -1;
    private boolean isFancyGlListClouds = false;
    private int cloudTickCounterGlList = -99999;
    private double cloudPlayerX = 0.0D;
    private double cloudPlayerY = 0.0D;
    private double cloudPlayerZ = 0.0D;
    private int countSortedWorldRenderers = 0;
    private int effectivePreloadedChunks = 0;
    private int vertexResortCounter = 30;
    public WrDisplayListAllocator displayListAllocator = new WrDisplayListAllocator();
    public EntityLivingBase renderViewEntity;
    private int countTileEntitiesRendered;
    private long lastMovedTime = System.currentTimeMillis();
    private long lastActionTime = System.currentTimeMillis();
    private static AxisAlignedBB AABB_INFINITE = AxisAlignedBB.getBoundingBox(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

    public RenderGlobal(Minecraft p_i1249_1_)
    {
        this.glListClouds = GLAllocation.generateDisplayLists(1);
        this.mc = p_i1249_1_;
        this.renderEngine = p_i1249_1_.getTextureManager();
        byte maxChunkDim = 65;
        byte maxChunkHeight = 16;
        int countWorldRenderers = maxChunkDim * maxChunkDim * maxChunkHeight;
        int countStandardRenderLists = countWorldRenderers * 3;
        this.glRenderListBase = GLAllocation.generateDisplayLists(countStandardRenderLists);
        this.displayListEntitiesDirty = false;
        this.displayListEntities = GLAllocation.generateDisplayLists(1);
        this.occlusionEnabled = OpenGlCapsChecker.checkARBOcclusion();

        if (this.occlusionEnabled)
        {
            this.occlusionResult.clear();
            this.glOcclusionQueryBase = GLAllocation.createDirectIntBuffer(maxChunkDim * maxChunkDim * maxChunkHeight);
            this.glOcclusionQueryBase.clear();
            this.glOcclusionQueryBase.position(0);
            this.glOcclusionQueryBase.limit(maxChunkDim * maxChunkDim * maxChunkHeight);
            ARBOcclusionQuery.glGenQueriesARB(this.glOcclusionQueryBase);
        }

        this.starGLCallList = GLAllocation.generateDisplayLists(3);
        GL11.glPushMatrix();
        GL11.glNewList(this.starGLCallList, GL11.GL_COMPILE);
        this.renderStars();
        GL11.glEndList();
        GL11.glPopMatrix();
        Tessellator tessellator = Tessellator.instance;
        this.glSkyList = this.starGLCallList + 1;
        GL11.glNewList(this.glSkyList, GL11.GL_COMPILE);
        byte b2 = 64;
        int i = 256 / b2 + 2;
        float f = 16.0F;
        int j;
        int k;

        for (j = -b2 * i; j <= b2 * i; j += b2)
        {
            for (k = -b2 * i; k <= b2 * i; k += b2)
            {
                tessellator.startDrawingQuads();
                tessellator.addVertex((double)(j + 0), (double)f, (double)(k + 0));
                tessellator.addVertex((double)(j + b2), (double)f, (double)(k + 0));
                tessellator.addVertex((double)(j + b2), (double)f, (double)(k + b2));
                tessellator.addVertex((double)(j + 0), (double)f, (double)(k + b2));
                tessellator.draw();
            }
        }

        GL11.glEndList();
        this.glSkyList2 = this.starGLCallList + 2;
        GL11.glNewList(this.glSkyList2, GL11.GL_COMPILE);
        f = -16.0F;
        tessellator.startDrawingQuads();

        for (j = -b2 * i; j <= b2 * i; j += b2)
        {
            for (k = -b2 * i; k <= b2 * i; k += b2)
            {
                tessellator.addVertex((double)(j + b2), (double)f, (double)(k + 0));
                tessellator.addVertex((double)(j + 0), (double)f, (double)(k + 0));
                tessellator.addVertex((double)(j + 0), (double)f, (double)(k + b2));
                tessellator.addVertex((double)(j + b2), (double)f, (double)(k + b2));
            }
        }

        tessellator.draw();
        GL11.glEndList();
    }

    private void renderStars()
    {
        Random random = new Random(10842L);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();

        for (int i = 0; i < 1500; ++i)
        {
            double d0 = (double)(random.nextFloat() * 2.0F - 1.0F);
            double d1 = (double)(random.nextFloat() * 2.0F - 1.0F);
            double d2 = (double)(random.nextFloat() * 2.0F - 1.0F);
            double d3 = (double)(0.15F + random.nextFloat() * 0.1F);
            double d4 = d0 * d0 + d1 * d1 + d2 * d2;

            if (d4 < 1.0D && d4 > 0.01D)
            {
                d4 = 1.0D / Math.sqrt(d4);
                d0 *= d4;
                d1 *= d4;
                d2 *= d4;
                double d5 = d0 * 100.0D;
                double d6 = d1 * 100.0D;
                double d7 = d2 * 100.0D;
                double d8 = Math.atan2(d0, d2);
                double d9 = Math.sin(d8);
                double d10 = Math.cos(d8);
                double d11 = Math.atan2(Math.sqrt(d0 * d0 + d2 * d2), d1);
                double d12 = Math.sin(d11);
                double d13 = Math.cos(d11);
                double d14 = random.nextDouble() * Math.PI * 2.0D;
                double d15 = Math.sin(d14);
                double d16 = Math.cos(d14);

                for (int j = 0; j < 4; ++j)
                {
                    double d17 = 0.0D;
                    double d18 = (double)((j & 2) - 1) * d3;
                    double d19 = (double)((j + 1 & 2) - 1) * d3;
                    double d20 = d18 * d16 - d19 * d15;
                    double d21 = d19 * d16 + d18 * d15;
                    double d22 = d20 * d12 + d17 * d13;
                    double d23 = d17 * d12 - d20 * d13;
                    double d24 = d23 * d9 - d21 * d10;
                    double d25 = d21 * d9 + d23 * d10;
                    tessellator.addVertex(d5 + d24, d6 + d22, d7 + d25);
                }
            }
        }

        tessellator.draw();
    }

    /**
     * set null to clear
     */
    public void setWorldAndLoadRenderers(WorldClient p_72732_1_)
    {
        if (this.theWorld != null)
        {
            this.theWorld.removeWorldAccess(this);
        }

        this.prevSortX = -9999.0D;
        this.prevSortY = -9999.0D;
        this.prevSortZ = -9999.0D;
        this.prevRenderSortX = -9999.0D;
        this.prevRenderSortY = -9999.0D;
        this.prevRenderSortZ = -9999.0D;
        this.prevChunkSortX = -9999;
        this.prevChunkSortY = -9999;
        this.prevChunkSortZ = -9999;
        RenderManager.instance.set(p_72732_1_);
        this.theWorld = p_72732_1_;
        this.renderBlocksRg = new RenderBlocks(p_72732_1_);

        if (Config.isDynamicLights())
        {
            DynamicLights.clear();
        }

        if (p_72732_1_ != null)
        {
            p_72732_1_.addWorldAccess(this);
            this.loadRenderers();
        }
    }

    /**
     * Loads all the renderers and sets up the basic settings usage
     */
    public void loadRenderers()
    {
        if (this.theWorld != null)
        {
            Blocks.leaves.setGraphicsLevel(this.mc.gameSettings.fancyGraphics);
            Blocks.leaves2.setGraphicsLevel(this.mc.gameSettings.fancyGraphics);
            this.renderDistanceChunks = this.mc.gameSettings.renderDistanceChunks;
            WrUpdates.clearAllUpdates();
            int numChunks;

            if (this.worldRenderers != null)
            {
                for (numChunks = 0; numChunks < this.worldRenderers.length; ++numChunks)
                {
                    this.worldRenderers[numChunks].stopRendering();
                }
            }

            if (Config.isDynamicLights())
            {
                DynamicLights.clear();
            }

            numChunks = this.mc.gameSettings.renderDistanceChunks;
            byte numChunksFar = 16;

            if (Config.isLoadChunksFar() && numChunks < numChunksFar)
            {
                numChunks = numChunksFar;
            }

            int maxPreloadedChunks = Config.limit(numChunksFar - numChunks, 0, 8);
            this.effectivePreloadedChunks = Config.limit(Config.getPreloadedChunks(), 0, maxPreloadedChunks);
            numChunks += this.effectivePreloadedChunks;
            byte limit = 32;

            if (numChunks > limit)
            {
                numChunks = limit;
            }

            this.prevReposX = -9999.0D;
            this.prevReposY = -9999.0D;
            this.prevReposZ = -9999.0D;
            int var1 = numChunks * 2 + 1;
            this.renderChunksWide = var1;
            this.renderChunksTall = 16;
            this.renderChunksDeep = var1;
            this.worldRenderers = new WorldRenderer[this.renderChunksWide * this.renderChunksTall * this.renderChunksDeep];
            this.sortedWorldRenderers = new WorldRenderer[this.renderChunksWide * this.renderChunksTall * this.renderChunksDeep];
            this.countSortedWorldRenderers = 0;
            this.displayListAllocator.resetAllocatedLists();
            int var2 = 0;
            int var3 = 0;
            this.minBlockX = 0;
            this.minBlockY = 0;
            this.minBlockZ = 0;
            this.maxBlockX = this.renderChunksWide;
            this.maxBlockY = this.renderChunksTall;
            this.maxBlockZ = this.renderChunksDeep;
            int var10;

            for (var10 = 0; var10 < this.worldRenderersToUpdate.size(); ++var10)
            {
                WorldRenderer esf = (WorldRenderer)this.worldRenderersToUpdate.get(var10);

                if (esf != null)
                {
                    esf.needsUpdate = false;
                }
            }

            this.worldRenderersToUpdate.clear();
            this.tileEntities.clear();
            this.onStaticEntitiesChanged();

            for (var10 = 0; var10 < this.renderChunksWide; ++var10)
            {
                for (int var14 = 0; var14 < this.renderChunksTall; ++var14)
                {
                    for (int cz = 0; cz < this.renderChunksDeep; ++cz)
                    {
                        int wri = (cz * this.renderChunksTall + var14) * this.renderChunksWide + var10;
                        this.worldRenderers[wri] = WrUpdates.makeWorldRenderer(this.theWorld, this.tileEntities, var10 * 16, var14 * 16, cz * 16, this.glRenderListBase + var2);

                        if (this.occlusionEnabled)
                        {
                            this.worldRenderers[wri].glOcclusionQuery = this.glOcclusionQueryBase.get(var3);
                        }

                        this.worldRenderers[wri].isWaitingOnOcclusionQuery = false;
                        this.worldRenderers[wri].isVisible = true;
                        this.worldRenderers[wri].isInFrustum = false;
                        this.worldRenderers[wri].chunkIndex = var3++;

                        if (this.theWorld.blockExists(var10 << 4, 0, cz << 4))
                        {
                            this.worldRenderers[wri].markDirty();
                            this.worldRenderersToUpdate.add(this.worldRenderers[wri]);
                        }

                        var2 += 3;
                    }
                }
            }

            if (this.theWorld != null)
            {
                Object var13 = this.mc.renderViewEntity;

                if (var13 == null)
                {
                    var13 = this.mc.thePlayer;
                }

                if (var13 != null)
                {
                    this.markRenderersForNewPosition(MathHelper.floor_double(((EntityLivingBase)var13).posX), MathHelper.floor_double(((EntityLivingBase)var13).posY), MathHelper.floor_double(((EntityLivingBase)var13).posZ));
                    EntitySorterFast var15 = new EntitySorterFast((Entity)var13);
                    var15.prepareToSort(this.sortedWorldRenderers, this.countSortedWorldRenderers);
                    Arrays.sort(this.sortedWorldRenderers, 0, this.countSortedWorldRenderers, var15);
                }
            }

            this.renderEntitiesStartupCounter = 2;
        }
    }

    private final List<Entity> entitiesForPostRender = new ArrayList<>();
    private final List<TileEntity> tileEntitiesForPostRender = new ArrayList<>();

    /**
     * Renders all entities within range and within the frustrum. Args: pos, frustrum, partialTickTime
     */
    public void renderEntities(EntityLivingBase livingBase, ICamera frustrum, float tickTime) {
        int pass = MinecraftForgeClient.getRenderPass();
        if (this.renderEntitiesStartupCounter > 0) {
            if (pass > 0) return;
            --this.renderEntitiesStartupCounter;
        } else {
            double d0 = livingBase.prevPosX + (livingBase.posX - livingBase.prevPosX) * (double) tickTime;
            double d1 = livingBase.prevPosY + (livingBase.posY - livingBase.prevPosY) * (double) tickTime;
            double d2 = livingBase.prevPosZ + (livingBase.posZ - livingBase.prevPosZ) * (double) tickTime;
            this.theWorld.theProfiler.startSection("prepare");
            TileEntityRendererDispatcher.instance.cacheActiveRenderInfo(this.theWorld, this.mc.getTextureManager(), this.mc.fontRenderer, this.mc.renderViewEntity, tickTime);
            RenderManager.instance.cacheActiveRenderInfo(this.theWorld, this.mc.getTextureManager(), this.mc.fontRenderer, this.mc.renderViewEntity, this.mc.pointedEntity, this.mc.gameSettings, tickTime);

            if (pass == 0) // no indentation to shrink patch
            {
                this.countEntitiesTotal = 0;
                this.countEntitiesRendered = 0;
                this.countEntitiesHidden = 0;
                this.countTileEntitiesRendered = 0;
                EntityLivingBase var17 = this.mc.renderViewEntity;
                double var18 = var17.lastTickPosX + (var17.posX - var17.lastTickPosX) * (double) tickTime;
                double isShaders = var17.lastTickPosY + (var17.posY - var17.lastTickPosY) * (double) tickTime;
                double te = var17.lastTickPosZ + (var17.posZ - var17.lastTickPosZ) * (double) tickTime;
                TileEntityRendererDispatcher.staticPlayerX = var18;
                TileEntityRendererDispatcher.staticPlayerY = isShaders;
                TileEntityRendererDispatcher.staticPlayerZ = te;
                this.theWorld.theProfiler.endStartSection("staticentities");

                if (this.displayListEntitiesDirty) {
                    RenderManager.renderPosX = 0.0D;
                    RenderManager.renderPosY = 0.0D;
                    RenderManager.renderPosZ = 0.0D;
                    this.rebuildDisplayListEntities();
                }

                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                GL11.glPushMatrix();
                GL11.glTranslated(-var18, -isShaders, -te);
                GL11.glCallList(this.displayListEntities);
                GL11.glPopMatrix();
                RenderManager.renderPosX = var18;
                RenderManager.renderPosY = isShaders;
                RenderManager.renderPosZ = te;
            }

            this.mc.entityRenderer.enableLightmap((double) tickTime);
            this.theWorld.theProfiler.endStartSection("global");
            List<?> list = this.theWorld.getLoadedEntityList();

            if (pass == 0) {
                this.countEntitiesTotal = list.size();
                entitiesForPostRender.clear();
                tileEntitiesForPostRender.clear();
            }

            if (Config.isFogOff() && this.mc.entityRenderer.fogStandard) {
                GL11.glDisable(GL11.GL_FOG);
            }

            Entity entity;
            int i;

            for (i = 0; i < this.theWorld.weatherEffects.size(); ++i) {
                entity = (Entity) this.theWorld.weatherEffects.get(i);
                if (!entity.shouldRenderInPass(pass)) continue;
                ++this.countEntitiesRendered;

                if (entity.isInRangeToRender3d(d0, d1, d2)) {
                    RenderManager.instance.renderEntitySimple(entity, tickTime);
                }
            }

            this.theWorld.theProfiler.endStartSection("entities");
            boolean var27 = Config.isShaders();

            if (var27) {
                Shaders.beginEntities();
            }

            boolean oldFancyGraphics = this.mc.gameSettings.fancyGraphics;
            this.mc.gameSettings.fancyGraphics = Config.isDroppedItemsFancy();

            for (i = 0; i < list.size(); ++i) {
                entity = (Entity) list.get(i);
                if (!entity.shouldRenderInPass(pass)) continue;
                boolean flag = entity.isInRangeToRender3d(d0, d1, d2) && (entity.ignoreFrustumCheck || frustrum.isBoundingBoxInFrustum(entity.boundingBox) || entity.riddenByEntity == this.mc.thePlayer);

                if (!flag && entity instanceof EntityLiving) {
                    EntityLiving entityliving = (EntityLiving) entity;

                    if (entityliving.getLeashed() && entityliving.getLeashedToEntity() != null) {
                        Entity entity1 = entityliving.getLeashedToEntity();
                        flag = frustrum.isBoundingBoxInFrustum(entity1.boundingBox);
                    }
                }

                if (flag && (entity != this.mc.renderViewEntity || this.mc.gameSettings.thirdPersonView != 0 || this.mc.renderViewEntity.isPlayerSleeping()) && this.theWorld.blockExists(MathHelper.floor_double(entity.posX), 0, MathHelper.floor_double(entity.posZ))) {
                    ++this.countEntitiesRendered;

                    if (entity.getClass() == EntityItemFrame.class) {
                        entity.renderDistanceWeight = 0.06D;
                    }

                    this.renderedEntity = entity;

                    if (var27) {
                        Shaders.nextEntity(entity);
                    }

                    RenderManager.instance.renderEntitySimple(entity, tickTime);
                    this.renderedEntity = null;
                }
            }

            this.mc.gameSettings.fancyGraphics = oldFancyGraphics;
            this.theWorld.theProfiler.endStartSection("blockentities");

            if (var27) {
                Shaders.endEntities();
                Shaders.beginBlockEntities();
            }

            RenderHelper.enableStandardItemLighting();

            for (i = 0; i < this.tileEntities.size(); ++i) {
                TileEntity tile = (TileEntity) this.tileEntities.get(i);
                if (tile.shouldRenderInPass(pass) && frustrum.isBoundingBoxInFrustum(tile.getRenderBoundingBox())) {
                    AxisAlignedBB var30 = this.getTileEntityBoundingBox(tile);

                    if (var30 == AABB_INFINITE || frustrum.isBoundingBoxInFrustum(var30)) {
                        Class var31 = tile.getClass();

                        if (var31 == TileEntitySign.class && !Config.zoomMode) {
                            EntityClientPlayerMP block = this.mc.thePlayer;
                            double distSq = tile.getDistanceFrom(block.posX, block.posY, block.posZ);

                            if (distSq > 256.0D) {
                                FontRenderer fr = TileEntityRendererDispatcher.instance.getFontRenderer();
                                fr.enabled = false;

                                if (Config.isShaders()) {
                                    Shaders.nextBlockEntity(tile);
                                }

                                TileEntityRendererDispatcher.instance.renderTileEntity(tile, tickTime);
                                ++this.countTileEntitiesRendered;
                                fr.enabled = true;
                                continue;
                            }
                        }

                        if (var31 == TileEntityChest.class) {
                            Block var32 = this.theWorld.getBlock(tile.xCoord, tile.yCoord, tile.zCoord);

                            if (!(var32 instanceof BlockChest)) {
                                continue;
                            }
                        }

                        if (Config.isShaders()) {
                            Shaders.nextBlockEntity(tile);
                        }

                        TileEntityRendererDispatcher.instance.renderTileEntity(tile, tickTime);
                        ++this.countTileEntitiesRendered;
                    }
                }
            }

            this.mc.entityRenderer.disableLightmap(tickTime);

            if (var27) {
                Shaders.endBlockEntities();
            }

            this.theWorld.theProfiler.endSection();
        }
    }

    /**
     * Gets the render info for use on the Debug screen
     */
    public String getDebugInfoRenders()
    {
        return "C: " + this.renderersBeingRendered + "/" + this.renderersLoaded + ". F: " + this.renderersBeingClipped + ", O: " + this.renderersBeingOccluded + ", E: " + this.renderersSkippingRenderPass;
    }

    /**
     * Gets the entities info for use on the Debug screen
     */
    public String getDebugInfoEntities()
    {
        return "E: " + this.countEntitiesRendered + "/" + this.countEntitiesTotal + ". B: " + this.countEntitiesHidden + ", I: " + (this.countEntitiesTotal - this.countEntitiesHidden - this.countEntitiesRendered);
    }

    public void onStaticEntitiesChanged()
    {
        this.displayListEntitiesDirty = true;
    }

    public void rebuildDisplayListEntities()
    {
        this.theWorld.theProfiler.startSection("staticentityrebuild");
        GL11.glPushMatrix();
        GL11.glNewList(this.displayListEntities, GL11.GL_COMPILE);
        List<?> list = this.theWorld.getLoadedEntityList();
        this.displayListEntitiesDirty = false;

        for (Object o : list) {
            Entity entity = (Entity) o;

            if (RenderManager.instance.getEntityRenderObject(entity).isStaticEntity()) {
                this.displayListEntitiesDirty = this.displayListEntitiesDirty || !RenderManager.instance.renderEntityStatic(entity, 0.0F, true);
            }
        }

        GL11.glEndList();
        GL11.glPopMatrix();
        this.theWorld.theProfiler.endSection();
    }

    /**
     * Goes through all the renderers setting new positions on them and those that have their position changed are
     * adding to be updated
     */
    private void markRenderersForNewPosition(int x, int y, int z)
    {
        x -= 8;
        y -= 8;
        z -= 8;
        this.minBlockX = Integer.MAX_VALUE;
        this.minBlockY = Integer.MAX_VALUE;
        this.minBlockZ = Integer.MAX_VALUE;
        this.maxBlockX = Integer.MIN_VALUE;
        this.maxBlockY = Integer.MIN_VALUE;
        this.maxBlockZ = Integer.MIN_VALUE;
        int blocksWide = this.renderChunksWide * 16;
        int blocksWide2 = blocksWide / 2;

        for (int ix = 0; ix < this.renderChunksWide; ++ix)
        {
            int blockX = ix * 16;
            int blockXAbs = blockX + blocksWide2 - x;

            if (blockXAbs < 0)
            {
                blockXAbs -= blocksWide - 1;
            }

            blockXAbs /= blocksWide;
            blockX -= blockXAbs * blocksWide;

            if (blockX < this.minBlockX)
            {
                this.minBlockX = blockX;
            }

            if (blockX > this.maxBlockX)
            {
                this.maxBlockX = blockX;
            }

            for (int iz = 0; iz < this.renderChunksDeep; ++iz)
            {
                int blockZ = iz * 16;
                int blockZAbs = blockZ + blocksWide2 - z;

                if (blockZAbs < 0)
                {
                    blockZAbs -= blocksWide - 1;
                }

                blockZAbs /= blocksWide;
                blockZ -= blockZAbs * blocksWide;

                if (blockZ < this.minBlockZ)
                {
                    this.minBlockZ = blockZ;
                }

                if (blockZ > this.maxBlockZ)
                {
                    this.maxBlockZ = blockZ;
                }

                for (int iy = 0; iy < this.renderChunksTall; ++iy)
                {
                    int blockY = iy * 16;

                    if (blockY < this.minBlockY)
                    {
                        this.minBlockY = blockY;
                    }

                    if (blockY > this.maxBlockY)
                    {
                        this.maxBlockY = blockY;
                    }

                    WorldRenderer worldrenderer = this.worldRenderers[(iz * this.renderChunksTall + iy) * this.renderChunksWide + ix];
                    boolean wasNeedingUpdate = worldrenderer.needsUpdate;
                    worldrenderer.setPosition(blockX, blockY, blockZ);

                    if (!wasNeedingUpdate && worldrenderer.needsUpdate)
                    {
                        this.worldRenderersToUpdate.add(worldrenderer);
                    }
                }
            }
        }
    }

    /**
     * Sorts all renderers based on the passed in entity. Args: entityLiving, renderPass, partialTickTime
     */
    public int sortAndRender(EntityLivingBase player, int renderPass, double partialTicks)
    {
        this.renderViewEntity = player;

        if (Config.isDynamicLights())
        {
            DynamicLights.update(this);
        }

        Profiler profiler = this.theWorld.theProfiler;
        profiler.startSection("sortchunks");
        int num;

        if (this.worldRenderersToUpdate.size() < 10)
        {
            byte shouldSort = 10;

            for (num = 0; num < shouldSort; ++num)
            {
                this.worldRenderersCheckIndex = (this.worldRenderersCheckIndex + 1) % this.worldRenderers.length;
                WorldRenderer ocReq = this.worldRenderers[this.worldRenderersCheckIndex];

                if (ocReq.needsUpdate && !this.worldRenderersToUpdate.contains(ocReq))
                {
                    this.worldRenderersToUpdate.add(ocReq);
                }
            }
        }

        if (this.mc.gameSettings.renderDistanceChunks != this.renderDistanceChunks && !Config.isLoadChunksFar())
        {
            this.loadRenderers();
        }

        if (renderPass == 0)
        {
            this.renderersLoaded = 0;
            this.dummyRenderInt = 0;
            this.renderersBeingClipped = 0;
            this.renderersBeingOccluded = 0;
            this.renderersBeingRendered = 0;
            this.renderersSkippingRenderPass = 0;
        }

        boolean var33 = this.prevChunkSortX != player.chunkCoordX || this.prevChunkSortY != player.chunkCoordY || this.prevChunkSortZ != player.chunkCoordZ;
        double partialX;
        double partialY;
        double partialZ;
        double var34;

        if (!var33)
        {
            var34 = player.posX - this.prevSortX;
            partialX = player.posY - this.prevSortY;
            partialY = player.posZ - this.prevSortZ;
            partialZ = var34 * var34 + partialX * partialX + partialY * partialY;
            var33 = partialZ > 16.0D;
        }

        int endIndex;
        int stepNum;

        if (var33)
        {
            this.prevChunkSortX = player.chunkCoordX;
            this.prevChunkSortY = player.chunkCoordY;
            this.prevChunkSortZ = player.chunkCoordZ;
            this.prevSortX = player.posX;
            this.prevSortY = player.posY;
            this.prevSortZ = player.posZ;
            num = this.effectivePreloadedChunks * 16;
            double var36 = player.posX - this.prevReposX;
            double dReposY = player.posY - this.prevReposY;
            double dReposZ = player.posZ - this.prevReposZ;
            double countResort = var36 * var36 + dReposY * dReposY + dReposZ * dReposZ;

            if (countResort > (double)(num * num) + 16.0D)
            {
                this.prevReposX = player.posX;
                this.prevReposY = player.posY;
                this.prevReposZ = player.posZ;
                this.markRenderersForNewPosition(MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ));
            }

            EntitySorterFast lastIndex = new EntitySorterFast(player);
            lastIndex.prepareToSort(this.sortedWorldRenderers, this.countSortedWorldRenderers);
            Arrays.sort(this.sortedWorldRenderers, 0, this.countSortedWorldRenderers, lastIndex);

            if (Config.isFastRender())
            {
                endIndex = (int)player.posX;
                stepNum = (int)player.posZ;
                short step = 2000;

                if (Math.abs(endIndex - WorldRenderer.globalChunkOffsetX) > step || Math.abs(stepNum - WorldRenderer.globalChunkOffsetZ) > step)
                {
                    WorldRenderer.globalChunkOffsetX = endIndex;
                    WorldRenderer.globalChunkOffsetZ = stepNum;
                    this.loadRenderers();
                }
            }
        }

        if (Config.isTranslucentBlocksFancy())
        {
            var34 = player.posX - this.prevRenderSortX;
            partialX = player.posY - this.prevRenderSortY;
            partialY = player.posZ - this.prevRenderSortZ;
            int var39 = Math.min(27, this.countSortedWorldRenderers);
            WorldRenderer firstIndex;

            if (var34 * var34 + partialX * partialX + partialY * partialY > 1.0D)
            {
                this.prevRenderSortX = player.posX;
                this.prevRenderSortY = player.posY;
                this.prevRenderSortZ = player.posZ;

                for (int var38 = 0; var38 < var39; ++var38)
                {
                    firstIndex = this.sortedWorldRenderers[var38];

                    if (firstIndex.vertexState != null && firstIndex.sortDistanceToEntitySquared < 1152.0F)
                    {
                        firstIndex.needVertexStateResort = true;

                        if (this.vertexResortCounter > var38)
                        {
                            this.vertexResortCounter = var38;
                        }
                    }
                }
            }

            if (this.vertexResortCounter < var39)
            {
                while (this.vertexResortCounter < var39)
                {
                    firstIndex = this.sortedWorldRenderers[this.vertexResortCounter];
                    ++this.vertexResortCounter;

                    if (firstIndex.needVertexStateResort)
                    {
                        firstIndex.updateRendererSort(player);
                        break;
                    }
                }
            }
        }

        RenderHelper.disableStandardItemLighting();
        WrUpdates.preRender(this, player);

        if (this.mc.gameSettings.ofSmoothFps && renderPass == 0)
        {
            GL11.glFinish();
        }

        byte var35 = 0;
        int var37 = 0;

        if (this.occlusionEnabled && this.mc.gameSettings.advancedOpengl && !this.mc.gameSettings.anaglyph && renderPass == 0)
        {
            partialX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
            partialY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
            partialZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
            byte var40 = 0;
            int var41 = Math.min(20, this.countSortedWorldRenderers);
            this.checkOcclusionQueryResult(var40, var41, player.posX, player.posY, player.posZ);

            for (endIndex = var40; endIndex < var41; ++endIndex)
            {
                this.sortedWorldRenderers[endIndex].isVisible = true;
            }

            profiler.endStartSection("render");
            num = var35 + this.renderSortedRenderers(var40, var41, renderPass, partialTicks);
            endIndex = var41;
            stepNum = 0;
            byte var42 = 40;
            int startIndex;

            for (int switchStep = this.renderChunksWide; endIndex < this.countSortedWorldRenderers; num += this.renderSortedRenderers(startIndex, endIndex, renderPass, partialTicks))
            {
                profiler.endStartSection("occ");
                startIndex = endIndex;

                if (stepNum < switchStep)
                {
                    ++stepNum;
                }
                else
                {
                    --stepNum;
                }

                endIndex += stepNum * var42;

                if (endIndex <= startIndex)
                {
                    endIndex = startIndex + 10;
                }

                if (endIndex > this.countSortedWorldRenderers)
                {
                    endIndex = this.countSortedWorldRenderers;
                }

                GL11.glDisable(GL11.GL_TEXTURE_2D);

                if (Config.isShaders())
                {
                    Shaders.disableTexture2D();
                }

                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_ALPHA_TEST);
                GL11.glDisable(GL11.GL_FOG);

                if (Config.isShaders())
                {
                    Shaders.disableFog();
                }

                GL11.glColorMask(false, false, false, false);
                GL11.glDepthMask(false);
                profiler.startSection("check");
                this.checkOcclusionQueryResult(startIndex, endIndex, player.posX, player.posY, player.posZ);
                profiler.endSection();
                GL11.glPushMatrix();
                float sumTX = 0.0F;
                float sumTY = 0.0F;
                float sumTZ = 0.0F;

                for (int k = startIndex; k < endIndex; ++k)
                {
                    WorldRenderer wr = this.sortedWorldRenderers[k];

                    if (wr.skipAllRenderPasses())
                    {
                        wr.isInFrustum = false;
                    }
                    else if (!wr.isUpdating && !wr.needsBoxUpdate)
                    {
                        if (wr.isInFrustum)
                        {
                            if (Config.isOcclusionFancy() && !wr.isInFrustrumFully)
                            {
                                wr.isVisible = true;
                            }
                            else if (wr.isInFrustum && !wr.isWaitingOnOcclusionQuery)
                            {
                                float bbX;
                                float bbY;
                                float bbZ;
                                float tX;

                                if (wr.isVisibleFromPosition)
                                {
                                    bbX = Math.abs((float)(wr.visibleFromX - player.posX));
                                    bbY = Math.abs((float)(wr.visibleFromY - player.posY));
                                    bbZ = Math.abs((float)(wr.visibleFromZ - player.posZ));
                                    tX = bbX + bbY + bbZ;

                                    if ((double)tX < 10.0D + (double)k / 1000.0D)
                                    {
                                        wr.isVisible = true;
                                        continue;
                                    }

                                    wr.isVisibleFromPosition = false;
                                }

                                bbX = (float)((double)wr.posXMinus - partialX);
                                bbY = (float)((double)wr.posYMinus - partialY);
                                bbZ = (float)((double)wr.posZMinus - partialZ);
                                tX = bbX - sumTX;
                                float tY = bbY - sumTY;
                                float tZ = bbZ - sumTZ;

                                if (tX != 0.0F || tY != 0.0F || tZ != 0.0F)
                                {
                                    GL11.glTranslatef(tX, tY, tZ);
                                    sumTX += tX;
                                    sumTY += tY;
                                    sumTZ += tZ;
                                }

                                profiler.startSection("bb");
                                ARBOcclusionQuery.glBeginQueryARB(ARBOcclusionQuery.GL_SAMPLES_PASSED_ARB, wr.glOcclusionQuery);
                                wr.callOcclusionQueryList();
                                ARBOcclusionQuery.glEndQueryARB(ARBOcclusionQuery.GL_SAMPLES_PASSED_ARB);
                                profiler.endSection();
                                wr.isWaitingOnOcclusionQuery = true;
                                ++var37;
                            }
                        }
                    }
                    else
                    {
                        wr.isVisible = true;
                    }
                }

                GL11.glPopMatrix();

                if (this.mc.gameSettings.anaglyph)
                {
                    if (EntityRenderer.anaglyphField == 0)
                    {
                        GL11.glColorMask(false, true, true, true);
                    }
                    else
                    {
                        GL11.glColorMask(true, false, false, true);
                    }
                }
                else
                {
                    GL11.glColorMask(true, true, true, true);
                }

                GL11.glDepthMask(true);
                GL11.glEnable(GL11.GL_TEXTURE_2D);

                if (Config.isShaders())
                {
                    Shaders.enableTexture2D();
                }

                GL11.glEnable(GL11.GL_ALPHA_TEST);
                GL11.glEnable(GL11.GL_FOG);

                if (Config.isShaders())
                {
                    Shaders.enableFog();
                }

                profiler.endStartSection("render");
            }
        }
        else
        {
            profiler.endStartSection("render");
            num = var35 + this.renderSortedRenderers(0, this.countSortedWorldRenderers, renderPass, partialTicks);
        }

        profiler.endSection();
        WrUpdates.postRender();
        return num;
    }

    private void checkOcclusionQueryResult(int startIndex, int endIndex, double px, double py, double pz)
    {
        for (int k = startIndex; k < endIndex; ++k)
        {
            WorldRenderer wr = this.sortedWorldRenderers[k];

            if (wr.isWaitingOnOcclusionQuery)
            {
                this.occlusionResult.clear();
                ARBOcclusionQuery.glGetQueryObjectuARB(wr.glOcclusionQuery, ARBOcclusionQuery.GL_QUERY_RESULT_AVAILABLE_ARB, this.occlusionResult);

                if (this.occlusionResult.get(0) != 0)
                {
                    wr.isWaitingOnOcclusionQuery = false;
                    this.occlusionResult.clear();
                    ARBOcclusionQuery.glGetQueryObjectuARB(wr.glOcclusionQuery, ARBOcclusionQuery.GL_QUERY_RESULT_ARB, this.occlusionResult);

                    if (!wr.isUpdating && !wr.needsBoxUpdate)
                    {
                        boolean wasVisible = wr.isVisible;
                        wr.isVisible = this.occlusionResult.get(0) > 0;

                        if (wasVisible && wr.isVisible)
                        {
                            wr.isVisibleFromPosition = true;
                            wr.visibleFromX = px;
                            wr.visibleFromY = py;
                            wr.visibleFromZ = pz;
                        }
                    }
                    else
                    {
                        wr.isVisible = true;
                    }
                }
            }
        }
    }

    /**
     * Renders the sorted renders for the specified render pass. Args: startRenderer, numRenderers, renderPass,
     * partialTickTime
     */
    private int renderSortedRenderers(int par1, int par2, int par3, double par4)
    {
        if (Config.isFastRender())
        {
            return this.renderSortedRenderersFast(par1, par2, par3, par4);
        }
        else
        {
            this.glRenderLists.clear();
            int var6 = 0;
            int var7 = par1;
            int var8 = par2;
            byte var9 = 1;

            if (par3 == 1)
            {
                var7 = this.countSortedWorldRenderers - 1 - par1;
                var8 = this.countSortedWorldRenderers - 1 - par2;
                var9 = -1;
            }

            for (int var22 = var7; var22 != var8; var22 += var9)
            {
                if (par3 == 0)
                {
                    ++this.renderersLoaded;

                    if (this.sortedWorldRenderers[var22].skipRenderPass[par3])
                    {
                        ++this.renderersSkippingRenderPass;
                    }
                    else if (!this.sortedWorldRenderers[var22].isInFrustum)
                    {
                        ++this.renderersBeingClipped;
                    }
                    else if (this.occlusionEnabled && !this.sortedWorldRenderers[var22].isVisible)
                    {
                        ++this.renderersBeingOccluded;
                    }
                    else
                    {
                        ++this.renderersBeingRendered;
                    }
                }

                if (!this.sortedWorldRenderers[var22].skipRenderPass[par3] && this.sortedWorldRenderers[var22].isInFrustum && (!this.occlusionEnabled || this.sortedWorldRenderers[var22].isVisible))
                {
                    int var23 = this.sortedWorldRenderers[var22].getGLCallListForPass(par3);

                    if (var23 >= 0)
                    {
                        this.glRenderLists.add(this.sortedWorldRenderers[var22]);
                        ++var6;
                    }
                }
            }

            if (var6 == 0)
            {
                return 0;
            }
            else
            {
                EntityLivingBase var221 = this.mc.renderViewEntity;
                double var231 = var221.lastTickPosX + (var221.posX - var221.lastTickPosX) * par4;
                double var13 = var221.lastTickPosY + (var221.posY - var221.lastTickPosY) * par4;
                double var15 = var221.lastTickPosZ + (var221.posZ - var221.lastTickPosZ) * par4;
                int var17 = 0;
                int var18;

                for (var18 = 0; var18 < this.allRenderLists.length; ++var18)
                {
                    this.allRenderLists[var18].resetList();
                }

                int var20;
                int var21;

                for (var18 = 0; var18 < this.glRenderLists.size(); ++var18)
                {
                    WorldRenderer var24 = (WorldRenderer)this.glRenderLists.get(var18);
                    var20 = -1;

                    for (var21 = 0; var21 < var17; ++var21)
                    {
                        if (this.allRenderLists[var21].rendersChunk(var24.posXMinus, var24.posYMinus, var24.posZMinus))
                        {
                            var20 = var21;
                        }
                    }

                    if (var20 < 0)
                    {
                        var20 = var17++;
                        this.allRenderLists[var20].setupRenderList(var24.posXMinus, var24.posYMinus, var24.posZMinus, var231, var13, var15);
                    }

                    this.allRenderLists[var20].addGLRenderList(var24.getGLCallListForPass(par3));
                }

                if (Config.isFogOff() && this.mc.entityRenderer.fogStandard)
                {
                    GL11.glDisable(GL11.GL_FOG);
                }

                var18 = MathHelper.floor_double(var231);
                int var241 = MathHelper.floor_double(var15);
                var20 = var18 - (var18 & 1023);
                var21 = var241 - (var241 & 1023);
                Arrays.sort(this.allRenderLists, new RenderDistanceSorter(var20, var21));
                this.renderAllRenderLists(par3, par4);
                return var6;
            }
        }
    }

    private int renderSortedRenderersFast(int startIndex, int endIndex, int renderPass, double partialTicks)
    {
        this.glListBuffer.clear();
        int l = 0;
        boolean debug = this.mc.gameSettings.showDebugInfo;
        int loopStart = startIndex;
        int loopEnd = endIndex;
        byte loopInc = 1;

        if (renderPass == 1)
        {
            loopStart = this.countSortedWorldRenderers - 1 - startIndex;
            loopEnd = this.countSortedWorldRenderers - 1 - endIndex;
            loopInc = -1;
        }

        for (int entityliving = loopStart; entityliving != loopEnd; entityliving += loopInc)
        {
            WorldRenderer partialX = this.sortedWorldRenderers[entityliving];

            if (debug && renderPass == 0)
            {
                ++this.renderersLoaded;

                if (partialX.skipRenderPass[renderPass])
                {
                    ++this.renderersSkippingRenderPass;
                }
                else if (!partialX.isInFrustum)
                {
                    ++this.renderersBeingClipped;
                }
                else if (this.occlusionEnabled && !partialX.isVisible)
                {
                    ++this.renderersBeingOccluded;
                }
                else
                {
                    ++this.renderersBeingRendered;
                }
            }

            if (partialX.isInFrustum && !partialX.skipRenderPass[renderPass] && (!this.occlusionEnabled || partialX.isVisible))
            {
                int glCallList = partialX.getGLCallListForPass(renderPass);

                if (glCallList >= 0)
                {
                    this.glListBuffer.put(glCallList);
                    ++l;
                }
            }
        }

        if (l == 0)
        {
            return 0;
        }
        else
        {
            if (Config.isFogOff() && this.mc.entityRenderer.fogStandard)
            {
                GL11.glDisable(GL11.GL_FOG);
            }

            this.glListBuffer.flip();
            EntityLivingBase var18 = this.mc.renderViewEntity;
            double var19 = var18.lastTickPosX + (var18.posX - var18.lastTickPosX) * partialTicks - (double)WorldRenderer.globalChunkOffsetX;
            double partialY = var18.lastTickPosY + (var18.posY - var18.lastTickPosY) * partialTicks;
            double partialZ = var18.lastTickPosZ + (var18.posZ - var18.lastTickPosZ) * partialTicks - (double)WorldRenderer.globalChunkOffsetZ;
            this.mc.entityRenderer.enableLightmap(partialTicks);
            GL11.glTranslatef((float)(-var19), (float)(-partialY), (float)(-partialZ));
            GL11.glCallLists(this.glListBuffer);
            GL11.glTranslatef((float)var19, (float)partialY, (float)partialZ);
            this.mc.entityRenderer.disableLightmap(partialTicks);
            return l;
        }
    }

    /**
     * Render all render lists
     */
    public void renderAllRenderLists(int par1, double par2)
    {
        this.mc.entityRenderer.enableLightmap(par2);

        for (int var4 = 0; var4 < this.allRenderLists.length; ++var4)
        {
            this.allRenderLists[var4].callLists();
        }

        this.mc.entityRenderer.disableLightmap(par2);
    }

    public void updateClouds()
    {
        if (Config.isShaders() && Keyboard.isKeyDown(61) && Keyboard.isKeyDown(19))
        {
            Shaders.uninit();
            Shaders.loadShaderPack();
        }

        ++this.cloudTickCounter;

        if (this.cloudTickCounter % 20 == 0)
        {
            Iterator var1 = this.damagedBlocks.values().iterator();

            while (var1.hasNext())
            {
                DestroyBlockProgress var2 = (DestroyBlockProgress)var1.next();
                int var3 = var2.getCreationCloudUpdateTick();

                if (this.cloudTickCounter - var3 > 400)
                {
                    var1.remove();
                }
            }
        }
    }

    /**
     * Renders the sky with the partial tick time. Args: partialTickTime
     */
    public void renderSky(float partialTicks)
    {
        IRenderHandler skyProvider = null;
        if ((skyProvider = this.mc.theWorld.provider.getSkyRenderer()) != null)
        {
            skyProvider.render(partialTicks, this.theWorld, mc);
            return;
        }
        if (this.mc.theWorld.provider.dimensionId == 1)
        {
            if (!Config.isSkyEnabled())
            {
                return;
            }

            GL11.glDisable(GL11.GL_FOG);

            if (Config.isShaders())
            {
                Shaders.disableFog();
            }

            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            RenderHelper.disableStandardItemLighting();
            GL11.glDepthMask(false);
            this.renderEngine.bindTexture(locationEndSkyPng);
            Tessellator tessellator = Tessellator.instance;

            for (int i = 0; i < 6; ++i)
            {
                GL11.glPushMatrix();

                if (i == 1)
                {
                    GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
                }

                if (i == 2)
                {
                    GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
                }

                if (i == 3)
                {
                    GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
                }

                if (i == 4)
                {
                    GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
                }

                if (i == 5)
                {
                    GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
                }

                tessellator.startDrawingQuads();
                tessellator.setColorOpaque_I(2631720);
                tessellator.addVertexWithUV(-100.0D, -100.0D, -100.0D, 0.0D, 0.0D);
                tessellator.addVertexWithUV(-100.0D, -100.0D, 100.0D, 0.0D, 16.0D);
                tessellator.addVertexWithUV(100.0D, -100.0D, 100.0D, 16.0D, 16.0D);
                tessellator.addVertexWithUV(100.0D, -100.0D, -100.0D, 16.0D, 0.0D);
                tessellator.draw();
                GL11.glPopMatrix();
            }

            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_TEXTURE_2D);

            if (Config.isShaders())
            {
                Shaders.enableTexture2D();
            }

            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glDisable(GL11.GL_BLEND);
        }
        else if (this.mc.theWorld.provider.isSurfaceWorld())
        {
            GL11.glDisable(GL11.GL_TEXTURE_2D);

            if (Config.isShaders())
            {
                Shaders.disableTexture2D();
            }

            Vec3 var21 = this.theWorld.getSkyColor(this.mc.renderViewEntity, partialTicks);
            var21 = CustomColorizer.getSkyColor(var21, this.mc.theWorld, this.mc.renderViewEntity.posX, this.mc.renderViewEntity.posY + 1.0D, this.mc.renderViewEntity.posZ);

            if (Config.isShaders())
            {
                Shaders.setSkyColor(var21);
            }

            float var231 = (float)var21.xCoord;
            float var4 = (float)var21.yCoord;
            float var5 = (float)var21.zCoord;
            float var8;

            if (this.mc.gameSettings.anaglyph)
            {
                float var23 = (var231 * 30.0F + var4 * 59.0F + var5 * 11.0F) / 100.0F;
                float var24 = (var231 * 30.0F + var4 * 70.0F) / 100.0F;
                var8 = (var231 * 30.0F + var5 * 70.0F) / 100.0F;
                var231 = var23;
                var4 = var24;
                var5 = var8;
            }

            GL11.glColor3f(var231, var4, var5);
            Tessellator tessellator = Tessellator.instance;
            GL11.glDepthMask(false);
            GL11.glEnable(GL11.GL_FOG);

            if (Config.isShaders())
            {
                Shaders.enableFog();
            }

            GL11.glColor3f(var231, var4, var5);

            if (Config.isShaders())
            {
                Shaders.preSkyList();
            }

            if (Config.isSkyEnabled())
            {
                GL11.glCallList(this.glSkyList);
            }

            GL11.glDisable(GL11.GL_FOG);

            if (Config.isShaders())
            {
                Shaders.disableFog();
            }

            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            RenderHelper.disableStandardItemLighting();
            float[] afloat = this.theWorld.provider.calcSunriseSunsetColors(this.theWorld.getCelestialAngle(partialTicks), partialTicks);
            float var9;
            float var10;
            float var11;
            float var12;
            float var20;
            int var30;
            float var16;
            float var17;

            if (afloat != null && Config.isSunMoonEnabled())
            {
                GL11.glDisable(GL11.GL_TEXTURE_2D);

                if (Config.isShaders())
                {
                    Shaders.disableTexture2D();
                }

                GL11.glShadeModel(GL11.GL_SMOOTH);
                GL11.glPushMatrix();
                GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(MathHelper.sin(this.theWorld.getCelestialAngleRadians(partialTicks)) < 0.0F ? 180.0F : 0.0F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
                var8 = afloat[0];
                var9 = afloat[1];
                var10 = afloat[2];

                if (this.mc.gameSettings.anaglyph)
                {
                    var11 = (var8 * 30.0F + var9 * 59.0F + var10 * 11.0F) / 100.0F;
                    var12 = (var8 * 30.0F + var9 * 70.0F) / 100.0F;
                    var20 = (var8 * 30.0F + var10 * 70.0F) / 100.0F;
                    var8 = var11;
                    var9 = var12;
                    var10 = var20;
                }

                tessellator.startDrawing(6);
                tessellator.setColorRGBA_F(var8, var9, var10, afloat[3]);
                tessellator.addVertex(0.0D, 100.0D, 0.0D);
                byte var25 = 16;
                tessellator.setColorRGBA_F(afloat[0], afloat[1], afloat[2], 0.0F);

                for (var30 = 0; var30 <= var25; ++var30)
                {
                    var20 = (float)var30 * (float)Math.PI * 2.0F / (float)var25;
                    var16 = MathHelper.sin(var20);
                    var17 = MathHelper.cos(var20);
                    tessellator.addVertex((double)(var16 * 120.0F), (double)(var17 * 120.0F), (double)(-var17 * 40.0F * afloat[3]));
                }

                tessellator.draw();
                GL11.glPopMatrix();
                GL11.glShadeModel(GL11.GL_FLAT);
            }

            GL11.glEnable(GL11.GL_TEXTURE_2D);

            if (Config.isShaders())
            {
                Shaders.enableTexture2D();
            }

            OpenGlHelper.glBlendFunc(770, 1, 1, 0);
            GL11.glPushMatrix();
            var8 = 1.0F - this.theWorld.getRainStrength(partialTicks);
            var9 = 0.0F;
            var10 = 0.0F;
            var11 = 0.0F;
            GL11.glColor4f(1.0F, 1.0F, 1.0F, var8);
            GL11.glTranslatef(var9, var10, var11);
            GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
            CustomSky.renderSky(this.theWorld, this.renderEngine, partialTicks);

            if (Config.isShaders())
            {
                Shaders.preCelestialRotate();
            }

            GL11.glRotatef(this.theWorld.getCelestialAngle(partialTicks) * 360.0F, 1.0F, 0.0F, 0.0F);

            if (Config.isShaders())
            {
                Shaders.postCelestialRotate();
            }

            var12 = 30.0F;

            if (Config.isSunTexture())
            {
                this.renderEngine.bindTexture(locationSunPng);
                tessellator.startDrawingQuads();
                tessellator.addVertexWithUV((double)(-var12), 100.0D, (double)(-var12), 0.0D, 0.0D);
                tessellator.addVertexWithUV((double)var12, 100.0D, (double)(-var12), 1.0D, 0.0D);
                tessellator.addVertexWithUV((double)var12, 100.0D, (double)var12, 1.0D, 1.0D);
                tessellator.addVertexWithUV((double)(-var12), 100.0D, (double)var12, 0.0D, 1.0D);
                tessellator.draw();

                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
                tessellator.startDrawingQuads();
                tessellator.addVertexWithUV((double)(-var12), 100.0D, (double)(-var12), 0.0D, 0.0D);
                tessellator.addVertexWithUV((double)var12, 100.0D, (double)(-var12), 1.0D, 0.0D);
                tessellator.addVertexWithUV((double)var12, 100.0D, (double)var12, 1.0D, 1.0D);
                tessellator.addVertexWithUV((double)(-var12), 100.0D, (double)var12, 0.0D, 1.0D);
                tessellator.draw();
                OpenGlHelper.glBlendFunc(770, 1, 1, 0);
            }

            var12 = 20.0F;

            if (Config.isMoonTexture())
            {
                this.renderEngine.bindTexture(locationMoonPhasesPng);
                int var26 = this.theWorld.getMoonPhase();
                int var27 = var26 % 4;
                var30 = var26 / 4 % 2;
                var16 = var27 / 4.0F;
                var17 = var30 / 2.0F;
                float var18 = (float)(var27 + 1) / 4.0F;
                float var19 = (float)(var30 + 1) / 2.0F;
                tessellator.startDrawingQuads();
                tessellator.addVertexWithUV((double)(-var12), -100.0D, (double)var12, (double)var18, (double)var19);
                tessellator.addVertexWithUV((double)var12, -100.0D, (double)var12, (double)var16, (double)var19);
                tessellator.addVertexWithUV((double)var12, -100.0D, (double)(-var12), (double)var16, (double)var17);
                tessellator.addVertexWithUV((double)(-var12), -100.0D, (double)(-var12), (double)var18, (double)var17);
                tessellator.draw();

                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
                tessellator.startDrawingQuads();
                tessellator.addVertexWithUV((double)(-var12), -100.0D, (double)var12, (double)var18, (double)var19);
                tessellator.addVertexWithUV((double)var12, -100.0D, (double)var12, (double)var16, (double)var19);
                tessellator.addVertexWithUV((double)var12, -100.0D, (double)(-var12), (double)var16, (double)var17);
                tessellator.addVertexWithUV((double)(-var12), -100.0D, (double)(-var12), (double)var18, (double)var17);
                tessellator.draw();
                OpenGlHelper.glBlendFunc(770, 1, 1, 0);
            }

            GL11.glDisable(GL11.GL_TEXTURE_2D);

            if (Config.isShaders())
            {
                Shaders.disableTexture2D();
            }

            var20 = this.theWorld.getStarBrightness(partialTicks) * var8;

            if (var20 > 0.0F && Config.isStarsEnabled() && !CustomSky.hasSkyLayers(this.theWorld))
            {
                GL11.glColor4f(var20, var20, var20, var20);
                GL11.glCallList(this.starGLCallList);
            }

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_FOG);

            if (Config.isShaders())
            {
                Shaders.enableFog();
            }

            GL11.glPopMatrix();
            GL11.glDisable(GL11.GL_TEXTURE_2D);

            if (Config.isShaders())
            {
                Shaders.disableTexture2D();
            }

            GL11.glColor3f(0.0F, 0.0F, 0.0F);
            double var28 = this.mc.thePlayer.getPosition(partialTicks).yCoord - this.theWorld.getHorizon();

            if (var28 < 0.0D)
            {
                GL11.glPushMatrix();
                GL11.glTranslatef(0.0F, 12.0F, 0.0F);
                GL11.glCallList(this.glSkyList2);
                GL11.glPopMatrix();
                var10 = 1.0F;
                var11 = -((float)(var28 + 65.0D));
                var12 = -var10;
                tessellator.startDrawingQuads();
                tessellator.setColorRGBA_I(0, 255);
                tessellator.addVertex((double)(-var10), (double)var11, (double)var10);
                tessellator.addVertex((double)var10, (double)var11, (double)var10);
                tessellator.addVertex((double)var10, (double)var12, (double)var10);
                tessellator.addVertex((double)(-var10), (double)var12, (double)var10);
                tessellator.addVertex((double)(-var10), (double)var12, (double)(-var10));
                tessellator.addVertex((double)var10, (double)var12, (double)(-var10));
                tessellator.addVertex((double)var10, (double)var11, (double)(-var10));
                tessellator.addVertex((double)(-var10), (double)var11, (double)(-var10));
                tessellator.addVertex((double)var10, (double)var12, (double)(-var10));
                tessellator.addVertex((double)var10, (double)var12, (double)var10);
                tessellator.addVertex((double)var10, (double)var11, (double)var10);
                tessellator.addVertex((double)var10, (double)var11, (double)(-var10));
                tessellator.addVertex((double)(-var10), (double)var11, (double)(-var10));
                tessellator.addVertex((double)(-var10), (double)var11, (double)var10);
                tessellator.addVertex((double)(-var10), (double)var12, (double)var10);
                tessellator.addVertex((double)(-var10), (double)var12, (double)(-var10));
                tessellator.addVertex((double)(-var10), (double)var12, (double)(-var10));
                tessellator.addVertex((double)(-var10), (double)var12, (double)var10);
                tessellator.addVertex((double)var10, (double)var12, (double)var10);
                tessellator.addVertex((double)var10, (double)var12, (double)(-var10));
                tessellator.draw();
            }

            if (this.theWorld.provider.isSkyColored())
            {
                GL11.glColor3f(var231 * 0.2F + 0.04F, var4 * 0.2F + 0.04F, var5 * 0.6F + 0.1F);
            }
            else
            {
                GL11.glColor3f(var231, var4, var5);
            }

            if (this.mc.gameSettings.renderDistanceChunks <= 4)
            {
                GL11.glColor3f(this.mc.entityRenderer.fogColorRed, this.mc.entityRenderer.fogColorGreen, this.mc.entityRenderer.fogColorBlue);
            }

            GL11.glPushMatrix();
            GL11.glTranslatef(0.0F, -((float)(var28 - 16.0D)), 0.0F);

            if (Config.isSkyEnabled())
            {
                GL11.glCallList(this.glSkyList2);
            }

            GL11.glPopMatrix();
            GL11.glEnable(GL11.GL_TEXTURE_2D);

            if (Config.isShaders())
            {
                Shaders.enableTexture2D();
            }

            GL11.glDepthMask(true);
        }
    }

    public void renderClouds(float par1)
    {
        if (!Config.isCloudsOff())
        {
            IRenderHandler renderer = null;
            if ((renderer = theWorld.provider.getCloudRenderer()) != null)
            {
                renderer.render(par1, theWorld, mc);
                return;
            }
            if (this.mc.theWorld.provider.isSurfaceWorld())
            {
                if (Config.isCloudsFancy())
                {
                    this.renderCloudsFancy(par1);
                }
                else
                {
                    float partialTicks1 = par1;
                    par1 = 0.0F;
                    GL11.glDisable(GL11.GL_CULL_FACE);
                    float var21 = (float)(this.mc.renderViewEntity.lastTickPosY + (this.mc.renderViewEntity.posY - this.mc.renderViewEntity.lastTickPosY) * (double)par1);
                    byte var3 = 32;
                    int var4 = 256 / var3;
                    Tessellator var5 = Tessellator.instance;
                    this.renderEngine.bindTexture(locationCloudsPng);
                    GL11.glEnable(GL11.GL_BLEND);
                    OpenGlHelper.glBlendFunc(770, 771, 1, 0);
                    double dc;
                    double exactPlayerZ1;

                    if (this.isFancyGlListClouds || this.cloudTickCounter >= this.cloudTickCounterGlList + 20)
                    {
                        GL11.glNewList(this.glListClouds, GL11.GL_COMPILE);
                        Vec3 entityliving = this.theWorld.getCloudColour(par1);
                        float exactPlayerX = (float)entityliving.xCoord;
                        float var8 = (float)entityliving.yCoord;
                        float exactPlayerY = (float)entityliving.zCoord;
                        float var10;

                        if (this.mc.gameSettings.anaglyph)
                        {
                            var10 = (exactPlayerX * 30.0F + var8 * 59.0F + exactPlayerY * 11.0F) / 100.0F;
                            float exactPlayerZ = (exactPlayerX * 30.0F + var8 * 70.0F) / 100.0F;
                            float var12 = (exactPlayerX * 30.0F + exactPlayerY * 70.0F) / 100.0F;
                            exactPlayerX = var10;
                            var8 = exactPlayerZ;
                            exactPlayerY = var12;
                        }

                        var10 = 4.8828125E-4F;
                        exactPlayerZ1 = (double)((float)this.cloudTickCounter + par1);
                        dc = this.mc.renderViewEntity.prevPosX + (this.mc.renderViewEntity.posX - this.mc.renderViewEntity.prevPosX) * (double)par1 + exactPlayerZ1 * 0.029999999329447746D;
                        double cdx = this.mc.renderViewEntity.prevPosZ + (this.mc.renderViewEntity.posZ - this.mc.renderViewEntity.prevPosZ) * (double)par1;
                        int cdz = MathHelper.floor_double(dc / 2048.0D);
                        int var18 = MathHelper.floor_double(cdx / 2048.0D);
                        dc -= (double)(cdz * 2048);
                        cdx -= (double)(var18 * 2048);
                        float var19 = this.theWorld.provider.getCloudHeight() - var21 + 0.33F;
                        var19 += this.mc.gameSettings.ofCloudsHeight * 128.0F;
                        float var20 = (float)(dc * (double)var10);
                        float f10 = (float)(cdx * (double)var10);
                        var5.startDrawingQuads();
                        var5.setColorRGBA_F(exactPlayerX, var8, exactPlayerY, 0.8F);

                        for (int var22 = -var3 * var4; var22 < var3 * var4; var22 += var3)
                        {
                            for (int var23 = -var3 * var4; var23 < var3 * var4; var23 += var3)
                            {
                                var5.addVertexWithUV((double)(var22 + 0), (double)var19, (double)(var23 + var3), (double)((float)(var22 + 0) * var10 + var20), (double)((float)(var23 + var3) * var10 + f10));
                                var5.addVertexWithUV((double)(var22 + var3), (double)var19, (double)(var23 + var3), (double)((float)(var22 + var3) * var10 + var20), (double)((float)(var23 + var3) * var10 + f10));
                                var5.addVertexWithUV((double)(var22 + var3), (double)var19, (double)(var23 + 0), (double)((float)(var22 + var3) * var10 + var20), (double)((float)(var23 + 0) * var10 + f10));
                                var5.addVertexWithUV((double)(var22 + 0), (double)var19, (double)(var23 + 0), (double)((float)(var22 + 0) * var10 + var20), (double)((float)(var23 + 0) * var10 + f10));
                            }
                        }

                        var5.draw();
                        GL11.glEndList();
                        this.isFancyGlListClouds = false;
                        this.cloudTickCounterGlList = this.cloudTickCounter;
                        this.cloudPlayerX = this.mc.renderViewEntity.prevPosX;
                        this.cloudPlayerY = this.mc.renderViewEntity.prevPosY;
                        this.cloudPlayerZ = this.mc.renderViewEntity.prevPosZ;
                    }

                    EntityLivingBase entityliving1 = this.mc.renderViewEntity;
                    double exactPlayerX1 = entityliving1.prevPosX + (entityliving1.posX - entityliving1.prevPosX) * (double)partialTicks1;
                    double exactPlayerY1 = entityliving1.prevPosY + (entityliving1.posY - entityliving1.prevPosY) * (double)partialTicks1;
                    exactPlayerZ1 = entityliving1.prevPosZ + (entityliving1.posZ - entityliving1.prevPosZ) * (double)partialTicks1;
                    dc = (double)((float)(this.cloudTickCounter - this.cloudTickCounterGlList) + partialTicks1);
                    float cdx1 = (float)(exactPlayerX1 - this.cloudPlayerX + dc * 0.03D);
                    float cdy = (float)(exactPlayerY1 - this.cloudPlayerY);
                    float cdz1 = (float)(exactPlayerZ1 - this.cloudPlayerZ);
                    GL11.glTranslatef(-cdx1, -cdy, -cdz1);
                    GL11.glCallList(this.glListClouds);
                    GL11.glTranslatef(cdx1, cdy, cdz1);
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    GL11.glDisable(GL11.GL_BLEND);
                    GL11.glEnable(GL11.GL_CULL_FACE);
                }
            }
        }
    }

    /**
     * Checks if the given position is to be rendered with cloud fog
     */
    public boolean hasCloudFog(double p_72721_1_, double p_72721_3_, double p_72721_5_, float p_72721_7_)
    {
        return false;
    }

    /**
     * Renders the 3d fancy clouds
     */
    public void renderCloudsFancy(float par1)
    {
        float partialTicks = par1;
        par1 = 0.0F;
        GL11.glDisable(GL11.GL_CULL_FACE);
        float var2 = (float)(this.mc.renderViewEntity.lastTickPosY + (this.mc.renderViewEntity.posY - this.mc.renderViewEntity.lastTickPosY) * (double)par1);
        Tessellator var3 = Tessellator.instance;
        float var4 = 12.0F;
        float var5 = 4.0F;
        double var6 = (double)((float)this.cloudTickCounter + par1);
        double var8 = (this.mc.renderViewEntity.prevPosX + (this.mc.renderViewEntity.posX - this.mc.renderViewEntity.prevPosX) * (double)par1 + var6 * 0.029999999329447746D) / (double)var4;
        double var10 = (this.mc.renderViewEntity.prevPosZ + (this.mc.renderViewEntity.posZ - this.mc.renderViewEntity.prevPosZ) * (double)par1) / (double)var4 + 0.33000001311302185D;
        float var12 = this.theWorld.provider.getCloudHeight() - var2 + 0.33F;
        var12 += this.mc.gameSettings.ofCloudsHeight * 128.0F;
        int var13 = MathHelper.floor_double(var8 / 2048.0D);
        int var14 = MathHelper.floor_double(var10 / 2048.0D);
        var8 -= (double)(var13 * 2048);
        var10 -= (double)(var14 * 2048);
        this.renderEngine.bindTexture(locationCloudsPng);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        float cdz;

        if (!this.isFancyGlListClouds || this.cloudTickCounter >= this.cloudTickCounterGlList + 20)
        {
            GL11.glNewList(this.glListClouds, GL11.GL_COMPILE);
            Vec3 entityliving = this.theWorld.getCloudColour(par1);
            float exactPlayerX = (float)entityliving.xCoord;
            float var17 = (float)entityliving.yCoord;
            float exactPlayerY = (float)entityliving.zCoord;
            float var19;
            float exactPlayerZ;
            float var21;

            if (this.mc.gameSettings.anaglyph)
            {
                var19 = (exactPlayerX * 30.0F + var17 * 59.0F + exactPlayerY * 11.0F) / 100.0F;
                exactPlayerZ = (exactPlayerX * 30.0F + var17 * 70.0F) / 100.0F;
                var21 = (exactPlayerX * 30.0F + exactPlayerY * 70.0F) / 100.0F;
                exactPlayerX = var19;
                var17 = exactPlayerZ;
                exactPlayerY = var21;
            }

            var19 = (float)(var8 * 0.0D);
            exactPlayerZ = (float)(var10 * 0.0D);
            var21 = 0.00390625F;
            var19 = (float)MathHelper.floor_double(var8) * var21;
            exactPlayerZ = (float)MathHelper.floor_double(var10) * var21;
            float dc = (float)(var8 - (double)MathHelper.floor_double(var8));
            float var23 = (float)(var10 - (double)MathHelper.floor_double(var10));
            byte cdx = 8;
            byte cdy = 4;
            cdz = 9.765625E-4F;
            GL11.glScalef(var4, 1.0F, var4);

            for (int var27 = 0; var27 < 2; ++var27)
            {
                if (var27 == 0)
                {
                    GL11.glColorMask(false, false, false, false);
                }
                else if (this.mc.gameSettings.anaglyph)
                {
                    if (EntityRenderer.anaglyphField == 0)
                    {
                        GL11.glColorMask(false, true, true, true);
                    }
                    else
                    {
                        GL11.glColorMask(true, false, false, true);
                    }
                }
                else
                {
                    GL11.glColorMask(true, true, true, true);
                }

                for (int var28 = -cdy + 1; var28 <= cdy; ++var28)
                {
                    for (int var29 = -cdy + 1; var29 <= cdy; ++var29)
                    {
                        var3.startDrawingQuads();
                        float var30 = (float)(var28 * cdx);
                        float var31 = (float)(var29 * cdx);
                        float var32 = var30 - dc;
                        float var33 = var31 - var23;

                        if (var12 > -var5 - 1.0F)
                        {
                            var3.setColorRGBA_F(exactPlayerX * 0.7F, var17 * 0.7F, exactPlayerY * 0.7F, 0.8F);
                            var3.setNormal(0.0F, -1.0F, 0.0F);
                            var3.addVertexWithUV((double)(var32 + 0.0F), (double)(var12 + 0.0F), (double)(var33 + (float)cdx), (double)((var30 + 0.0F) * var21 + var19), (double)((var31 + (float)cdx) * var21 + exactPlayerZ));
                            var3.addVertexWithUV((double)(var32 + (float)cdx), (double)(var12 + 0.0F), (double)(var33 + (float)cdx), (double)((var30 + (float)cdx) * var21 + var19), (double)((var31 + (float)cdx) * var21 + exactPlayerZ));
                            var3.addVertexWithUV((double)(var32 + (float)cdx), (double)(var12 + 0.0F), (double)(var33 + 0.0F), (double)((var30 + (float)cdx) * var21 + var19), (double)((var31 + 0.0F) * var21 + exactPlayerZ));
                            var3.addVertexWithUV((double)(var32 + 0.0F), (double)(var12 + 0.0F), (double)(var33 + 0.0F), (double)((var30 + 0.0F) * var21 + var19), (double)((var31 + 0.0F) * var21 + exactPlayerZ));
                        }

                        if (var12 <= var5 + 1.0F)
                        {
                            var3.setColorRGBA_F(exactPlayerX, var17, exactPlayerY, 0.8F);
                            var3.setNormal(0.0F, 1.0F, 0.0F);
                            var3.addVertexWithUV((double)(var32 + 0.0F), (double)(var12 + var5 - cdz), (double)(var33 + (float)cdx), (double)((var30 + 0.0F) * var21 + var19), (double)((var31 + (float)cdx) * var21 + exactPlayerZ));
                            var3.addVertexWithUV((double)(var32 + (float)cdx), (double)(var12 + var5 - cdz), (double)(var33 + (float)cdx), (double)((var30 + (float)cdx) * var21 + var19), (double)((var31 + (float)cdx) * var21 + exactPlayerZ));
                            var3.addVertexWithUV((double)(var32 + (float)cdx), (double)(var12 + var5 - cdz), (double)(var33 + 0.0F), (double)((var30 + (float)cdx) * var21 + var19), (double)((var31 + 0.0F) * var21 + exactPlayerZ));
                            var3.addVertexWithUV((double)(var32 + 0.0F), (double)(var12 + var5 - cdz), (double)(var33 + 0.0F), (double)((var30 + 0.0F) * var21 + var19), (double)((var31 + 0.0F) * var21 + exactPlayerZ));
                        }

                        var3.setColorRGBA_F(exactPlayerX * 0.9F, var17 * 0.9F, exactPlayerY * 0.9F, 0.8F);
                        int var34;

                        if (var28 > -1)
                        {
                            var3.setNormal(-1.0F, 0.0F, 0.0F);

                            for (var34 = 0; var34 < cdx; ++var34)
                            {
                                var3.addVertexWithUV((double)(var32 + (float)var34 + 0.0F), (double)(var12 + 0.0F), (double)(var33 + (float)cdx), (double)((var30 + (float)var34 + 0.5F) * var21 + var19), (double)((var31 + (float)cdx) * var21 + exactPlayerZ));
                                var3.addVertexWithUV((double)(var32 + (float)var34 + 0.0F), (double)(var12 + var5), (double)(var33 + (float)cdx), (double)((var30 + (float)var34 + 0.5F) * var21 + var19), (double)((var31 + (float)cdx) * var21 + exactPlayerZ));
                                var3.addVertexWithUV((double)(var32 + (float)var34 + 0.0F), (double)(var12 + var5), (double)(var33 + 0.0F), (double)((var30 + (float)var34 + 0.5F) * var21 + var19), (double)((var31 + 0.0F) * var21 + exactPlayerZ));
                                var3.addVertexWithUV((double)(var32 + (float)var34 + 0.0F), (double)(var12 + 0.0F), (double)(var33 + 0.0F), (double)((var30 + (float)var34 + 0.5F) * var21 + var19), (double)((var31 + 0.0F) * var21 + exactPlayerZ));
                            }
                        }

                        if (var28 <= 1)
                        {
                            var3.setNormal(1.0F, 0.0F, 0.0F);

                            for (var34 = 0; var34 < cdx; ++var34)
                            {
                                var3.addVertexWithUV((double)(var32 + (float)var34 + 1.0F - cdz), (double)(var12 + 0.0F), (double)(var33 + (float)cdx), (double)((var30 + (float)var34 + 0.5F) * var21 + var19), (double)((var31 + (float)cdx) * var21 + exactPlayerZ));
                                var3.addVertexWithUV((double)(var32 + (float)var34 + 1.0F - cdz), (double)(var12 + var5), (double)(var33 + (float)cdx), (double)((var30 + (float)var34 + 0.5F) * var21 + var19), (double)((var31 + (float)cdx) * var21 + exactPlayerZ));
                                var3.addVertexWithUV((double)(var32 + (float)var34 + 1.0F - cdz), (double)(var12 + var5), (double)(var33 + 0.0F), (double)((var30 + (float)var34 + 0.5F) * var21 + var19), (double)((var31 + 0.0F) * var21 + exactPlayerZ));
                                var3.addVertexWithUV((double)(var32 + (float)var34 + 1.0F - cdz), (double)(var12 + 0.0F), (double)(var33 + 0.0F), (double)((var30 + (float)var34 + 0.5F) * var21 + var19), (double)((var31 + 0.0F) * var21 + exactPlayerZ));
                            }
                        }

                        var3.setColorRGBA_F(exactPlayerX * 0.8F, var17 * 0.8F, exactPlayerY * 0.8F, 0.8F);

                        if (var29 > -1)
                        {
                            var3.setNormal(0.0F, 0.0F, -1.0F);

                            for (var34 = 0; var34 < cdx; ++var34)
                            {
                                var3.addVertexWithUV((double)(var32 + 0.0F), (double)(var12 + var5), (double)(var33 + (float)var34 + 0.0F), (double)((var30 + 0.0F) * var21 + var19), (double)((var31 + (float)var34 + 0.5F) * var21 + exactPlayerZ));
                                var3.addVertexWithUV((double)(var32 + (float)cdx), (double)(var12 + var5), (double)(var33 + (float)var34 + 0.0F), (double)((var30 + (float)cdx) * var21 + var19), (double)((var31 + (float)var34 + 0.5F) * var21 + exactPlayerZ));
                                var3.addVertexWithUV((double)(var32 + (float)cdx), (double)(var12 + 0.0F), (double)(var33 + (float)var34 + 0.0F), (double)((var30 + (float)cdx) * var21 + var19), (double)((var31 + (float)var34 + 0.5F) * var21 + exactPlayerZ));
                                var3.addVertexWithUV((double)(var32 + 0.0F), (double)(var12 + 0.0F), (double)(var33 + (float)var34 + 0.0F), (double)((var30 + 0.0F) * var21 + var19), (double)((var31 + (float)var34 + 0.5F) * var21 + exactPlayerZ));
                            }
                        }

                        if (var29 <= 1)
                        {
                            var3.setNormal(0.0F, 0.0F, 1.0F);

                            for (var34 = 0; var34 < cdx; ++var34)
                            {
                                var3.addVertexWithUV((double)(var32 + 0.0F), (double)(var12 + var5), (double)(var33 + (float)var34 + 1.0F - cdz), (double)((var30 + 0.0F) * var21 + var19), (double)((var31 + (float)var34 + 0.5F) * var21 + exactPlayerZ));
                                var3.addVertexWithUV((double)(var32 + (float)cdx), (double)(var12 + var5), (double)(var33 + (float)var34 + 1.0F - cdz), (double)((var30 + (float)cdx) * var21 + var19), (double)((var31 + (float)var34 + 0.5F) * var21 + exactPlayerZ));
                                var3.addVertexWithUV((double)(var32 + (float)cdx), (double)(var12 + 0.0F), (double)(var33 + (float)var34 + 1.0F - cdz), (double)((var30 + (float)cdx) * var21 + var19), (double)((var31 + (float)var34 + 0.5F) * var21 + exactPlayerZ));
                                var3.addVertexWithUV((double)(var32 + 0.0F), (double)(var12 + 0.0F), (double)(var33 + (float)var34 + 1.0F - cdz), (double)((var30 + 0.0F) * var21 + var19), (double)((var31 + (float)var34 + 0.5F) * var21 + exactPlayerZ));
                            }
                        }

                        var3.draw();
                    }
                }
            }

            GL11.glEndList();
            this.isFancyGlListClouds = true;
            this.cloudTickCounterGlList = this.cloudTickCounter;
            this.cloudPlayerX = this.mc.renderViewEntity.prevPosX;
            this.cloudPlayerY = this.mc.renderViewEntity.prevPosY;
            this.cloudPlayerZ = this.mc.renderViewEntity.prevPosZ;
        }

        EntityLivingBase var36 = this.mc.renderViewEntity;
        double var37 = var36.prevPosX + (var36.posX - var36.prevPosX) * (double)partialTicks;
        double var38 = var36.prevPosY + (var36.posY - var36.prevPosY) * (double)partialTicks;
        double var39 = var36.prevPosZ + (var36.posZ - var36.prevPosZ) * (double)partialTicks;
        double var40 = (double)((float)(this.cloudTickCounter - this.cloudTickCounterGlList) + partialTicks);
        float var41 = (float)(var37 - this.cloudPlayerX + var40 * 0.03D);
        float var42 = (float)(var38 - this.cloudPlayerY);
        cdz = (float)(var39 - this.cloudPlayerZ);
        GL11.glTranslatef(-var41, -var42, -cdz);
        GL11.glCallList(this.glListClouds);
        GL11.glTranslatef(var41, var42, cdz);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    /**
     * Updates some of the renderers sorted by distance from the player
     */
    public boolean updateRenderers(EntityLivingBase entityliving, boolean flag)
    {
        this.renderViewEntity = entityliving;

        if (WrUpdates.hasWrUpdater())
        {
            return WrUpdates.updateRenderers(this, entityliving, flag);
        }
        else if (this.worldRenderersToUpdate.size() <= 0)
        {
            return false;
        }
        else
        {
            int num = 0;
            int maxNum = Config.getUpdatesPerFrame();

            if (Config.isDynamicUpdates() && !this.isMoving(entityliving))
            {
                maxNum *= 3;
            }

            byte NOT_IN_FRUSTRUM_MUL = 4;
            int numValid = 0;
            WorldRenderer wrBest = null;
            float distSqBest = Float.MAX_VALUE;
            int indexBest = -1;

            for (int maxDiffDistSq = 0; maxDiffDistSq < this.worldRenderersToUpdate.size(); ++maxDiffDistSq)
            {
                WorldRenderer i = (WorldRenderer)this.worldRenderersToUpdate.get(maxDiffDistSq);

                if (i != null)
                {
                    ++numValid;

                    if (!i.needsUpdate)
                    {
                        this.worldRenderersToUpdate.set(maxDiffDistSq, (Object)null);
                    }
                    else
                    {
                        float wr = i.distanceToEntitySquared(entityliving);

                        if (wr <= 256.0F && this.isActingNow())
                        {
                            i.updateRenderer(entityliving);
                            i.needsUpdate = false;
                            this.worldRenderersToUpdate.set(maxDiffDistSq, (Object)null);
                            ++num;
                        }
                        else
                        {
                            if (wr > 256.0F && num >= maxNum)
                            {
                                break;
                            }

                            if (!i.isInFrustum)
                            {
                                wr *= (float)NOT_IN_FRUSTRUM_MUL;
                            }

                            if (wrBest == null)
                            {
                                wrBest = i;
                                distSqBest = wr;
                                indexBest = maxDiffDistSq;
                            }
                            else if (wr < distSqBest)
                            {
                                wrBest = i;
                                distSqBest = wr;
                                indexBest = maxDiffDistSq;
                            }
                        }
                    }
                }
            }

            if (wrBest != null)
            {
                wrBest.updateRenderer(entityliving);
                wrBest.needsUpdate = false;
                this.worldRenderersToUpdate.set(indexBest, (Object)null);
                ++num;
                float var15 = distSqBest / 5.0F;

                for (int var16 = 0; var16 < this.worldRenderersToUpdate.size() && num < maxNum; ++var16)
                {
                    WorldRenderer var17 = (WorldRenderer)this.worldRenderersToUpdate.get(var16);

                    if (var17 != null)
                    {
                        float distSq = var17.distanceToEntitySquared(entityliving);

                        if (!var17.isInFrustum)
                        {
                            distSq *= (float)NOT_IN_FRUSTRUM_MUL;
                        }

                        float diffDistSq = Math.abs(distSq - distSqBest);

                        if (diffDistSq < var15)
                        {
                            var17.updateRenderer(entityliving);
                            var17.needsUpdate = false;
                            this.worldRenderersToUpdate.set(var16, (Object)null);
                            ++num;
                        }
                    }
                }
            }

            if (numValid == 0)
            {
                this.worldRenderersToUpdate.clear();
            }

            this.worldRenderersToUpdate.compact();
            return true;
        }
    }

    public void drawBlockDamageTexture(Tessellator p_72717_1_, EntityPlayer p_72717_2_, float p_72717_3_)
    {
        drawBlockDamageTexture(p_72717_1_, (EntityLivingBase)p_72717_2_, p_72717_3_);
    }

    public void drawBlockDamageTexture(Tessellator par1Tessellator, EntityLivingBase par2EntityPlayer, float par3)
    {
        double var4 = par2EntityPlayer.lastTickPosX + (par2EntityPlayer.posX - par2EntityPlayer.lastTickPosX) * (double)par3;
        double var6 = par2EntityPlayer.lastTickPosY + (par2EntityPlayer.posY - par2EntityPlayer.lastTickPosY) * (double)par3;
        double var8 = par2EntityPlayer.lastTickPosZ + (par2EntityPlayer.posZ - par2EntityPlayer.lastTickPosZ) * (double)par3;

        if (!this.damagedBlocks.isEmpty())
        {
            OpenGlHelper.glBlendFunc(774, 768, 1, 0);
            this.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
            GL11.glPushMatrix();
            GL11.glPolygonOffset(-3.0F, -3.0F);
            GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
            GL11.glEnable(GL11.GL_ALPHA_TEST);

            if (Config.isShaders())
            {
                Shaders.beginBlockDestroyProgress();
            }

            par1Tessellator.startDrawingQuads();
            par1Tessellator.setTranslation(-var4, -var6, -var8);
            par1Tessellator.disableColor();
            Iterator var10 = this.damagedBlocks.values().iterator();

            while (var10.hasNext())
            {
                DestroyBlockProgress var11 = (DestroyBlockProgress)var10.next();
                double var12 = (double)var11.getPartialBlockX() - var4;
                double var14 = (double)var11.getPartialBlockY() - var6;
                double var16 = (double)var11.getPartialBlockZ() - var8;

                if (var12 * var12 + var14 * var14 + var16 * var16 > 1024.0D)
                {
                    var10.remove();
                }
                else
                {
                    Block var18 = this.theWorld.getBlock(var11.getPartialBlockX(), var11.getPartialBlockY(), var11.getPartialBlockZ());

                    if (var18.getMaterial() != Material.air)
                    {
                        this.renderBlocksRg.renderBlockUsingTexture(var18, var11.getPartialBlockX(), var11.getPartialBlockY(), var11.getPartialBlockZ(), this.destroyBlockIcons[var11.getPartialBlockDamage()]);
                    }
                }
            }

            par1Tessellator.draw();
            par1Tessellator.setTranslation(0.0D, 0.0D, 0.0D);

            if (Config.isShaders())
            {
                Shaders.endBlockDestroyProgress();
            }

            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glPolygonOffset(0.0F, 0.0F);
            GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glDepthMask(true);
            GL11.glPopMatrix();
        }
    }

    /**
     * Draws the selection box for the player. Args: entityPlayer, rayTraceHit, i, itemStack, partialTickTime
     */
    public void drawSelectionBox(EntityPlayer p_72731_1_, MovingObjectPosition p_72731_2_, int p_72731_3_, float p_72731_4_)
    {
        if (p_72731_3_ == 0 && p_72731_2_.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
        {
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
            GL11.glLineWidth(2.0F);
            GL11.glDisable(GL11.GL_TEXTURE_2D);

            if (Config.isShaders())
            {
                Shaders.disableTexture2D();
            }

            GL11.glDepthMask(false);
            float f1 = 0.002F;
            Block block = this.theWorld.getBlock(p_72731_2_.blockX, p_72731_2_.blockY, p_72731_2_.blockZ);

            if (block.getMaterial() != Material.air)
            {
                block.setBlockBoundsBasedOnState(this.theWorld, p_72731_2_.blockX, p_72731_2_.blockY, p_72731_2_.blockZ);
                double d0 = p_72731_1_.lastTickPosX + (p_72731_1_.posX - p_72731_1_.lastTickPosX) * (double)p_72731_4_;
                double d1 = p_72731_1_.lastTickPosY + (p_72731_1_.posY - p_72731_1_.lastTickPosY) * (double)p_72731_4_;
                double d2 = p_72731_1_.lastTickPosZ + (p_72731_1_.posZ - p_72731_1_.lastTickPosZ) * (double)p_72731_4_;
                drawOutlinedBoundingBox(block.getSelectedBoundingBoxFromPool(this.theWorld, p_72731_2_.blockX, p_72731_2_.blockY, p_72731_2_.blockZ).expand((double)f1, (double)f1, (double)f1).getOffsetBoundingBox(-d0, -d1, -d2), -1);
            }

            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_TEXTURE_2D);

            if (Config.isShaders())
            {
                Shaders.enableTexture2D();
            }

            GL11.glDisable(GL11.GL_BLEND);
        }
    }

    /**
     * Draws lines for the edges of the bounding box.
     */
    public static void drawOutlinedBoundingBox(AxisAlignedBB p_147590_0_, int p_147590_1_)
    {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawing(3);

        if (p_147590_1_ != -1)
        {
            tessellator.setColorOpaque_I(p_147590_1_);
        }

        tessellator.addVertex(p_147590_0_.minX, p_147590_0_.minY, p_147590_0_.minZ);
        tessellator.addVertex(p_147590_0_.maxX, p_147590_0_.minY, p_147590_0_.minZ);
        tessellator.addVertex(p_147590_0_.maxX, p_147590_0_.minY, p_147590_0_.maxZ);
        tessellator.addVertex(p_147590_0_.minX, p_147590_0_.minY, p_147590_0_.maxZ);
        tessellator.addVertex(p_147590_0_.minX, p_147590_0_.minY, p_147590_0_.minZ);
        tessellator.draw();
        tessellator.startDrawing(3);

        if (p_147590_1_ != -1)
        {
            tessellator.setColorOpaque_I(p_147590_1_);
        }

        tessellator.addVertex(p_147590_0_.minX, p_147590_0_.maxY, p_147590_0_.minZ);
        tessellator.addVertex(p_147590_0_.maxX, p_147590_0_.maxY, p_147590_0_.minZ);
        tessellator.addVertex(p_147590_0_.maxX, p_147590_0_.maxY, p_147590_0_.maxZ);
        tessellator.addVertex(p_147590_0_.minX, p_147590_0_.maxY, p_147590_0_.maxZ);
        tessellator.addVertex(p_147590_0_.minX, p_147590_0_.maxY, p_147590_0_.minZ);
        tessellator.draw();
        tessellator.startDrawing(1);

        if (p_147590_1_ != -1)
        {
            tessellator.setColorOpaque_I(p_147590_1_);
        }

        tessellator.addVertex(p_147590_0_.minX, p_147590_0_.minY, p_147590_0_.minZ);
        tessellator.addVertex(p_147590_0_.minX, p_147590_0_.maxY, p_147590_0_.minZ);
        tessellator.addVertex(p_147590_0_.maxX, p_147590_0_.minY, p_147590_0_.minZ);
        tessellator.addVertex(p_147590_0_.maxX, p_147590_0_.maxY, p_147590_0_.minZ);
        tessellator.addVertex(p_147590_0_.maxX, p_147590_0_.minY, p_147590_0_.maxZ);
        tessellator.addVertex(p_147590_0_.maxX, p_147590_0_.maxY, p_147590_0_.maxZ);
        tessellator.addVertex(p_147590_0_.minX, p_147590_0_.minY, p_147590_0_.maxZ);
        tessellator.addVertex(p_147590_0_.minX, p_147590_0_.maxY, p_147590_0_.maxZ);
        tessellator.draw();
    }

    /**
     * Marks the blocks in the given range for update
     */
    public void markBlocksForUpdate(int p_72725_1_, int p_72725_2_, int p_72725_3_, int p_72725_4_, int p_72725_5_, int p_72725_6_)
    {
        int k1 = MathHelper.bucketInt(p_72725_1_, 16);
        int l1 = MathHelper.bucketInt(p_72725_2_, 16);
        int i2 = MathHelper.bucketInt(p_72725_3_, 16);
        int j2 = MathHelper.bucketInt(p_72725_4_, 16);
        int k2 = MathHelper.bucketInt(p_72725_5_, 16);
        int l2 = MathHelper.bucketInt(p_72725_6_, 16);

        for (int i3 = k1; i3 <= j2; ++i3)
        {
            int j3 = i3 % this.renderChunksWide;

            if (j3 < 0)
            {
                j3 += this.renderChunksWide;
            }

            for (int k3 = l1; k3 <= k2; ++k3)
            {
                int l3 = k3 % this.renderChunksTall;

                if (l3 < 0)
                {
                    l3 += this.renderChunksTall;
                }

                for (int i4 = i2; i4 <= l2; ++i4)
                {
                    int j4 = i4 % this.renderChunksDeep;

                    if (j4 < 0)
                    {
                        j4 += this.renderChunksDeep;
                    }

                    int k4 = (j4 * this.renderChunksTall + l3) * this.renderChunksWide + j3;
                    WorldRenderer worldrenderer = this.worldRenderers[k4];

                    if (worldrenderer != null && !worldrenderer.needsUpdate)
                    {
                        this.worldRenderersToUpdate.add(worldrenderer);
                        worldrenderer.markDirty();
                    }
                }
            }
        }
    }

    /**
     * On the client, re-renders the block. On the server, sends the block to the client (which will re-render it),
     * including the tile entity description packet if applicable. Args: x, y, z
     */
    public void markBlockForUpdate(int p_147586_1_, int p_147586_2_, int p_147586_3_)
    {
        this.markBlocksForUpdate(p_147586_1_ - 1, p_147586_2_ - 1, p_147586_3_ - 1, p_147586_1_ + 1, p_147586_2_ + 1, p_147586_3_ + 1);
    }

    /**
     * On the client, re-renders this block. On the server, does nothing. Used for lighting updates.
     */
    public void markBlockForRenderUpdate(int p_147588_1_, int p_147588_2_, int p_147588_3_)
    {
        this.markBlocksForUpdate(p_147588_1_ - 1, p_147588_2_ - 1, p_147588_3_ - 1, p_147588_1_ + 1, p_147588_2_ + 1, p_147588_3_ + 1);
    }

    /**
     * On the client, re-renders all blocks in this range, inclusive. On the server, does nothing. Args: min x, min y,
     * min z, max x, max y, max z
     */
    public void markBlockRangeForRenderUpdate(int p_147585_1_, int p_147585_2_, int p_147585_3_, int p_147585_4_, int p_147585_5_, int p_147585_6_)
    {
        this.markBlocksForUpdate(p_147585_1_ - 1, p_147585_2_ - 1, p_147585_3_ - 1, p_147585_4_ + 1, p_147585_5_ + 1, p_147585_6_ + 1);
    }

    /**
     * Checks all renderers that previously weren't in the frustum and 1/16th of those that previously were in the
     * frustum for frustum clipping Args: frustum, partialTickTime
     */
    public void clipRenderersByFrustum(ICamera par1ICamera, float par2)
    {
        boolean checkDistanceXz = !Config.isFogOff();
        double renderDistSq = (double)(this.renderDistanceChunks * 16 * this.renderDistanceChunks * 16);

        for (int var3 = 0; var3 < this.countSortedWorldRenderers; ++var3)
        {
            WorldRenderer wr = this.sortedWorldRenderers[var3];

            if (!wr.skipAllRenderPasses())
            {
                if (checkDistanceXz && wr.distanceToEntityXzSq > renderDistSq)
                {
                    wr.isInFrustum = false;
                }
                else
                {
                    wr.updateInFrustum(par1ICamera);
                }
            }
        }
    }

    /**
     * Plays the specified record. Arg: recordName, x, y, z
     */
    public void playRecord(String p_72702_1_, int p_72702_2_, int p_72702_3_, int p_72702_4_)
    {
        ChunkCoordinates chunkcoordinates = new ChunkCoordinates(p_72702_2_, p_72702_3_, p_72702_4_);
        ISound isound = (ISound)this.mapSoundPositions.get(chunkcoordinates);

        if (isound != null)
        {
            this.mc.getSoundHandler().stopSound(isound);
            this.mapSoundPositions.remove(chunkcoordinates);
        }

        if (p_72702_1_ != null)
        {
            ItemRecord itemrecord = ItemRecord.getRecord(p_72702_1_);

            ResourceLocation resource = null;
            if (itemrecord != null)
            {
                this.mc.ingameGUI.setRecordPlayingMessage(itemrecord.getRecordNameLocal());
                resource = itemrecord.getRecordResource(p_72702_1_);
            }

            if (resource == null) resource = new ResourceLocation(p_72702_1_);
            PositionedSoundRecord positionedsoundrecord = PositionedSoundRecord.func_147675_a(resource, (float)p_72702_2_, (float)p_72702_3_, (float)p_72702_4_);
            this.mapSoundPositions.put(chunkcoordinates, positionedsoundrecord);
            this.mc.getSoundHandler().playSound(positionedsoundrecord);
        }
    }

    /**
     * Plays the specified sound. Arg: soundName, x, y, z, volume, pitch
     */
    public void playSound(String p_72704_1_, double p_72704_2_, double p_72704_4_, double p_72704_6_, float p_72704_8_, float p_72704_9_) {}

    /**
     * Plays sound to all near players except the player reference given
     */
    public void playSoundToNearExcept(EntityPlayer p_85102_1_, String p_85102_2_, double p_85102_3_, double p_85102_5_, double p_85102_7_, float p_85102_9_, float p_85102_10_) {}

    /**
     * Spawns a particle. Arg: particleType, x, y, z, velX, velY, velZ
     */
    public void spawnParticle(String p_72708_1_, final double p_72708_2_, final double p_72708_4_, final double p_72708_6_, double p_72708_8_, double p_72708_10_, double p_72708_12_)
    {
        try
        {
            this.doSpawnParticle(p_72708_1_, p_72708_2_, p_72708_4_, p_72708_6_, p_72708_8_, p_72708_10_, p_72708_12_);
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception while adding particle");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being added");
            crashreportcategory.addCrashSection("Name", p_72708_1_);
            crashreportcategory.addCrashSectionCallable("Position", new Callable()
            {
                //private static final String __OBFID = "CL_00000955";
                public String call()
                {
                    return CrashReportCategory.func_85074_a(p_72708_2_, p_72708_4_, p_72708_6_);
                }
            });
            throw new ReportedException(crashreport);
        }
    }

    /**
     * Spawns a particle. Arg: particleType, x, y, z, velX, velY, velZ
     */
    public EntityFX doSpawnParticle(String par1Str, double par2, double par4, double par6, double par8, double par10, double par12)
    {
        if (this.mc != null && this.mc.renderViewEntity != null && this.mc.effectRenderer != null)
        {
            int var14 = this.mc.gameSettings.particleSetting;

            if (var14 == 1 && this.theWorld.rand.nextInt(3) == 0)
            {
                var14 = 2;
            }

            double var15 = this.mc.renderViewEntity.posX - par2;
            double var17 = this.mc.renderViewEntity.posY - par4;
            double var19 = this.mc.renderViewEntity.posZ - par6;
            Object var21 = null;

            if (par1Str.equals("hugeexplosion"))
            {
                if (Config.isAnimatedExplosion())
                {
                    this.mc.effectRenderer.addEffect((EntityFX) (var21 = new EntityHugeExplodeFX(this.theWorld, par2, par4, par6, par8, par10, par12)));
                }
            }
            else if (par1Str.equals("largeexplode"))
            {
                if (Config.isAnimatedExplosion())
                {
                    this.mc.effectRenderer.addEffect((EntityFX) (var21 = new EntityLargeExplodeFX(this.renderEngine, this.theWorld, par2, par4, par6, par8, par10, par12)));
                }
            }
            else if (par1Str.equals("fireworksSpark"))
            {
                this.mc.effectRenderer.addEffect((EntityFX) (var21 = new EntityFireworkSparkFX(this.theWorld, par2, par4, par6, par8, par10, par12, this.mc.effectRenderer)));
            }

            if (var21 != null)
            {
                return (EntityFX)var21;
            }
            else
            {
                double var22 = 16.0D;
                double d3 = 16.0D;

                if (par1Str.equals("crit"))
                {
                    var22 = 196.0D;
                }

                if (var15 * var15 + var17 * var17 + var19 * var19 > var22 * var22)
                {
                    return null;
                }
                else if (var14 > 1)
                {
                    return null;
                }
                else
                {
                    if (par1Str.equals("bubble"))
                    {
                        var21 = new EntityBubbleFX(this.theWorld, par2, par4, par6, par8, par10, par12);
                        CustomColorizer.updateWaterFX((EntityFX)var21, this.theWorld);
                    }
                    else if (par1Str.equals("suspended"))
                    {
                        if (Config.isWaterParticles())
                        {
                            var21 = new EntitySuspendFX(this.theWorld, par2, par4, par6, par8, par10, par12);
                        }
                    }
                    else if (par1Str.equals("depthsuspend"))
                    {
                        if (Config.isVoidParticles())
                        {
                            var21 = new EntityAuraFX(this.theWorld, par2, par4, par6, par8, par10, par12);
                        }
                    }
                    else if (par1Str.equals("townaura"))
                    {
                        var21 = new EntityAuraFX(this.theWorld, par2, par4, par6, par8, par10, par12);
                        CustomColorizer.updateMyceliumFX((EntityFX)var21);
                    }
                    else if (par1Str.equals("crit"))
                    {
                        var21 = new EntityCritFX(this.theWorld, par2, par4, par6, par8, par10, par12);
                    }
                    else if (par1Str.equals("magicCrit"))
                    {
                        var21 = new EntityCritFX(this.theWorld, par2, par4, par6, par8, par10, par12);
                        ((EntityFX)var21).setRBGColorF(((EntityFX)var21).getRedColorF() * 0.3F, ((EntityFX)var21).getGreenColorF() * 0.8F, ((EntityFX)var21).getBlueColorF());
                        ((EntityFX)var21).nextTextureIndexX();
                    }
                    else if (par1Str.equals("smoke"))
                    {
                        if (Config.isAnimatedSmoke())
                        {
                            var21 = new EntitySmokeFX(this.theWorld, par2, par4, par6, par8, par10, par12);
                        }
                    }
                    else if (par1Str.equals("mobSpell"))
                    {
                        if (Config.isPotionParticles())
                        {
                            var21 = new EntitySpellParticleFX(this.theWorld, par2, par4, par6, 0.0D, 0.0D, 0.0D);
                            ((EntityFX)var21).setRBGColorF((float)par8, (float)par10, (float)par12);
                        }
                    }
                    else if (par1Str.equals("mobSpellAmbient"))
                    {
                        if (Config.isPotionParticles())
                        {
                            var21 = new EntitySpellParticleFX(this.theWorld, par2, par4, par6, 0.0D, 0.0D, 0.0D);
                            ((EntityFX)var21).setAlphaF(0.15F);
                            ((EntityFX)var21).setRBGColorF((float)par8, (float)par10, (float)par12);
                        }
                    }
                    else if (par1Str.equals("spell"))
                    {
                        if (Config.isPotionParticles())
                        {
                            var21 = new EntitySpellParticleFX(this.theWorld, par2, par4, par6, par8, par10, par12);
                        }
                    }
                    else if (par1Str.equals("instantSpell"))
                    {
                        if (Config.isPotionParticles())
                        {
                            var21 = new EntitySpellParticleFX(this.theWorld, par2, par4, par6, par8, par10, par12);
                            ((EntitySpellParticleFX)var21).setBaseSpellTextureIndex(144);
                        }
                    }
                    else if (par1Str.equals("witchMagic"))
                    {
                        if (Config.isPotionParticles())
                        {
                            var21 = new EntitySpellParticleFX(this.theWorld, par2, par4, par6, par8, par10, par12);
                            ((EntitySpellParticleFX)var21).setBaseSpellTextureIndex(144);
                            float var26 = this.theWorld.rand.nextFloat() * 0.5F + 0.35F;
                            ((EntityFX)var21).setRBGColorF(1.0F * var26, 0.0F * var26, 1.0F * var26);
                        }
                    }
                    else if (par1Str.equals("note"))
                    {
                        var21 = new EntityNoteFX(this.theWorld, par2, par4, par6, par8, par10, par12);
                    }
                    else if (par1Str.equals("portal"))
                    {
                        if (Config.isPortalParticles())
                        {
                            var21 = new EntityPortalFX(this.theWorld, par2, par4, par6, par8, par10, par12);
                            CustomColorizer.updatePortalFX((EntityFX)var21);
                        }
                    }
                    else if (par1Str.equals("enchantmenttable"))
                    {
                        var21 = new EntityEnchantmentTableParticleFX(this.theWorld, par2, par4, par6, par8, par10, par12);
                    }
                    else if (par1Str.equals("explode"))
                    {
                        if (Config.isAnimatedExplosion())
                        {
                            var21 = new EntityExplodeFX(this.theWorld, par2, par4, par6, par8, par10, par12);
                        }
                    }
                    else if (par1Str.equals("flame"))
                    {
                        if (Config.isAnimatedFlame())
                        {
                            var21 = new EntityFlameFX(this.theWorld, par2, par4, par6, par8, par10, par12);
                        }
                    }
                    else if (par1Str.equals("lava"))
                    {
                        var21 = new EntityLavaFX(this.theWorld, par2, par4, par6);
                    }
                    else if (par1Str.equals("footstep"))
                    {
                        var21 = new EntityFootStepFX(this.renderEngine, this.theWorld, par2, par4, par6);
                    }
                    else if (par1Str.equals("splash"))
                    {
                        var21 = new EntitySplashFX(this.theWorld, par2, par4, par6, par8, par10, par12);
                        CustomColorizer.updateWaterFX((EntityFX)var21, this.theWorld);
                    }
                    else if (par1Str.equals("wake"))
                    {
                        var21 = new EntityFishWakeFX(this.theWorld, par2, par4, par6, par8, par10, par12);
                    }
                    else if (par1Str.equals("largesmoke"))
                    {
                        if (Config.isAnimatedSmoke())
                        {
                            var21 = new EntitySmokeFX(this.theWorld, par2, par4, par6, par8, par10, par12, 2.5F);
                        }
                    }
                    else if (par1Str.equals("cloud"))
                    {
                        var21 = new EntityCloudFX(this.theWorld, par2, par4, par6, par8, par10, par12);
                    }
                    else if (par1Str.equals("reddust"))
                    {
                        if (Config.isAnimatedRedstone())
                        {
                            var21 = new EntityReddustFX(this.theWorld, par2, par4, par6, (float)par8, (float)par10, (float)par12);
                            CustomColorizer.updateReddustFX((EntityFX)var21, this.theWorld, var15, var17, var19);
                        }
                    }
                    else if (par1Str.equals("snowballpoof"))
                    {
                        var21 = new EntityBreakingFX(this.theWorld, par2, par4, par6, Items.snowball);
                    }
                    else if (par1Str.equals("dripWater"))
                    {
                        if (Config.isDrippingWaterLava())
                        {
                            var21 = new EntityDropParticleFX(this.theWorld, par2, par4, par6, Material.water);
                        }
                    }
                    else if (par1Str.equals("dripLava"))
                    {
                        if (Config.isDrippingWaterLava())
                        {
                            var21 = new EntityDropParticleFX(this.theWorld, par2, par4, par6, Material.lava);
                        }
                    }
                    else if (par1Str.equals("snowshovel"))
                    {
                        var21 = new EntitySnowShovelFX(this.theWorld, par2, par4, par6, par8, par10, par12);
                    }
                    else if (par1Str.equals("slime"))
                    {
                        var21 = new EntityBreakingFX(this.theWorld, par2, par4, par6, Items.slime_ball);
                    }
                    else if (par1Str.equals("heart"))
                    {
                        var21 = new EntityHeartFX(this.theWorld, par2, par4, par6, par8, par10, par12);
                    }
                    else if (par1Str.equals("angryVillager"))
                    {
                        var21 = new EntityHeartFX(this.theWorld, par2, par4 + 0.5D, par6, par8, par10, par12);
                        ((EntityFX)var21).setParticleTextureIndex(81);
                        ((EntityFX)var21).setRBGColorF(1.0F, 1.0F, 1.0F);
                    }
                    else if (par1Str.equals("happyVillager"))
                    {
                        var21 = new EntityAuraFX(this.theWorld, par2, par4, par6, par8, par10, par12);
                        ((EntityFX)var21).setParticleTextureIndex(82);
                        ((EntityFX)var21).setRBGColorF(1.0F, 1.0F, 1.0F);
                    }
                    else
                    {
                        String[] var27;
                        int var261;

                        if (par1Str.startsWith("iconcrack_"))
                        {
                            var27 = par1Str.split("_", 3);
                            int var28 = Integer.parseInt(var27[1]);

                            if (var27.length > 2)
                            {
                                var261 = Integer.parseInt(var27[2]);
                                var21 = new EntityBreakingFX(this.theWorld, par2, par4, par6, par8, par10, par12, Item.getItemById(var28), var261);
                            }
                            else
                            {
                                var21 = new EntityBreakingFX(this.theWorld, par2, par4, par6, par8, par10, par12, Item.getItemById(var28), 0);
                            }
                        }
                        else
                        {
                            Block var281;

                            if (par1Str.startsWith("blockcrack_"))
                            {
                                var27 = par1Str.split("_", 3);
                                var281 = Block.getBlockById(Integer.parseInt(var27[1]));
                                var261 = Integer.parseInt(var27[2]);
                                var21 = (new EntityDiggingFX(this.theWorld, par2, par4, par6, par8, par10, par12, var281, var261)).applyRenderColor(var261);
                            }
                            else if (par1Str.startsWith("blockdust_"))
                            {
                                var27 = par1Str.split("_", 3);
                                var281 = Block.getBlockById(Integer.parseInt(var27[1]));
                                var261 = Integer.parseInt(var27[2]);
                                var21 = (new EntityBlockDustFX(this.theWorld, par2, par4, par6, par8, par10, par12, var281, var261)).applyRenderColor(var261);
                            }
                        }
                    }

                    if (var21 != null)
                    {
                        this.mc.effectRenderer.addEffect((EntityFX)var21);
                    }

                    return (EntityFX)var21;
                }
            }
        }
        else
        {
            return null;
        }
    }

    /**
     * Called on all IWorldAccesses when an entity is created or loaded. On client worlds, starts downloading any
     * necessary textures. On server worlds, adds the entity to the entity tracker.
     */
    public void onEntityCreate(Entity par1Entity)
    {
        RandomMobs.entityLoaded(par1Entity, this.theWorld);

        if (Config.isDynamicLights())
        {
            DynamicLights.entityAdded(par1Entity, this);
        }
    }

    /**
     * Called on all IWorldAccesses when an entity is unloaded or destroyed. On client worlds, releases any downloaded
     * textures. On server worlds, removes the entity from the entity tracker.
     */
    public void onEntityDestroy(Entity par1Entity)
    {
        if (Config.isDynamicLights())
        {
            DynamicLights.entityRemoved(par1Entity, this);
        }
    }

    /**
     * Deletes all display lists
     */
    public void deleteAllDisplayLists()
    {
        GLAllocation.deleteDisplayLists(this.glRenderListBase);
        this.displayListAllocator.deleteDisplayLists();
    }

    public void broadcastSound(int p_82746_1_, int p_82746_2_, int p_82746_3_, int p_82746_4_, int p_82746_5_)
    {
        Random random = this.theWorld.rand;

        switch (p_82746_1_)
        {
            case 1013:
            case 1018:
                if (this.mc.renderViewEntity != null)
                {
                    double d0 = (double)p_82746_2_ - this.mc.renderViewEntity.posX;
                    double d1 = (double)p_82746_3_ - this.mc.renderViewEntity.posY;
                    double d2 = (double)p_82746_4_ - this.mc.renderViewEntity.posZ;
                    double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                    double d4 = this.mc.renderViewEntity.posX;
                    double d5 = this.mc.renderViewEntity.posY;
                    double d6 = this.mc.renderViewEntity.posZ;

                    if (d3 > 0.0D)
                    {
                        d4 += d0 / d3 * 2.0D;
                        d5 += d1 / d3 * 2.0D;
                        d6 += d2 / d3 * 2.0D;
                    }

                    if (p_82746_1_ == 1013)
                    {
                        this.theWorld.playSound(d4, d5, d6, "mob.wither.spawn", 1.0F, 1.0F, false);
                    }
                    else if (p_82746_1_ == 1018)
                    {
                        this.theWorld.playSound(d4, d5, d6, "mob.enderdragon.end", 5.0F, 1.0F, false);
                    }
                }
            default:
        }
    }

    /**
     * Plays a pre-canned sound effect along with potentially auxiliary data-driven one-shot behaviour (particles, etc).
     */
    public void playAuxSFX(EntityPlayer p_72706_1_, int p_72706_2_, int p_72706_3_, int p_72706_4_, int p_72706_5_, int p_72706_6_)
    {
        Random random = this.theWorld.rand;
        Block block = null;
        double d0;
        double d1;
        double d2;
        String s;
        int k1;
        double d4;
        double d5;
        double d6;
        double d7;
        int l2;
        double d13;

        switch (p_72706_2_)
        {
            case 1000:
                this.theWorld.playSound((double)p_72706_3_, (double)p_72706_4_, (double)p_72706_5_, "random.click", 1.0F, 1.0F, false);
                break;
            case 1001:
                this.theWorld.playSound((double)p_72706_3_, (double)p_72706_4_, (double)p_72706_5_, "random.click", 1.0F, 1.2F, false);
                break;
            case 1002:
                this.theWorld.playSound((double)p_72706_3_, (double)p_72706_4_, (double)p_72706_5_, "random.bow", 1.0F, 1.2F, false);
                break;
            case 1003:
                if (Math.random() < 0.5D)
                {
                    this.theWorld.playSound((double)p_72706_3_ + 0.5D, (double)p_72706_4_ + 0.5D, (double)p_72706_5_ + 0.5D, "random.door_open", 1.0F, this.theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
                }
                else
                {
                    this.theWorld.playSound((double)p_72706_3_ + 0.5D, (double)p_72706_4_ + 0.5D, (double)p_72706_5_ + 0.5D, "random.door_close", 1.0F, this.theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
                }

                break;
            case 1004:
                this.theWorld.playSound((double)((float)p_72706_3_ + 0.5F), (double)((float)p_72706_4_ + 0.5F), (double)((float)p_72706_5_ + 0.5F), "random.fizz", 0.5F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F, false);
                break;
            case 1005:
                if (Item.getItemById(p_72706_6_) instanceof ItemRecord)
                {
                    this.theWorld.playRecord("records." + ((ItemRecord)Item.getItemById(p_72706_6_)).recordName, p_72706_3_, p_72706_4_, p_72706_5_);
                }
                else
                {
                    this.theWorld.playRecord((String)null, p_72706_3_, p_72706_4_, p_72706_5_);
                }

                break;
            case 1007:
                this.theWorld.playSound((double)p_72706_3_ + 0.5D, (double)p_72706_4_ + 0.5D, (double)p_72706_5_ + 0.5D, "mob.ghast.charge", 10.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1008:
                this.theWorld.playSound((double)p_72706_3_ + 0.5D, (double)p_72706_4_ + 0.5D, (double)p_72706_5_ + 0.5D, "mob.ghast.fireball", 10.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1009:
                this.theWorld.playSound((double)p_72706_3_ + 0.5D, (double)p_72706_4_ + 0.5D, (double)p_72706_5_ + 0.5D, "mob.ghast.fireball", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1010:
                this.theWorld.playSound((double)p_72706_3_ + 0.5D, (double)p_72706_4_ + 0.5D, (double)p_72706_5_ + 0.5D, "mob.zombie.wood", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1011:
                this.theWorld.playSound((double)p_72706_3_ + 0.5D, (double)p_72706_4_ + 0.5D, (double)p_72706_5_ + 0.5D, "mob.zombie.metal", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1012:
                this.theWorld.playSound((double)p_72706_3_ + 0.5D, (double)p_72706_4_ + 0.5D, (double)p_72706_5_ + 0.5D, "mob.zombie.woodbreak", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1014:
                this.theWorld.playSound((double)p_72706_3_ + 0.5D, (double)p_72706_4_ + 0.5D, (double)p_72706_5_ + 0.5D, "mob.wither.shoot", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1015:
                this.theWorld.playSound((double)p_72706_3_ + 0.5D, (double)p_72706_4_ + 0.5D, (double)p_72706_5_ + 0.5D, "mob.bat.takeoff", 0.05F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1016:
                this.theWorld.playSound((double)p_72706_3_ + 0.5D, (double)p_72706_4_ + 0.5D, (double)p_72706_5_ + 0.5D, "mob.zombie.infect", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1017:
                this.theWorld.playSound((double)p_72706_3_ + 0.5D, (double)p_72706_4_ + 0.5D, (double)p_72706_5_ + 0.5D, "mob.zombie.unfect", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;
            case 1020:
                this.theWorld.playSound((double)((float)p_72706_3_ + 0.5F), (double)((float)p_72706_4_ + 0.5F), (double)((float)p_72706_5_ + 0.5F), "random.anvil_break", 1.0F, this.theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1021:
                this.theWorld.playSound((double)((float)p_72706_3_ + 0.5F), (double)((float)p_72706_4_ + 0.5F), (double)((float)p_72706_5_ + 0.5F), "random.anvil_use", 1.0F, this.theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 1022:
                this.theWorld.playSound((double)((float)p_72706_3_ + 0.5F), (double)((float)p_72706_4_ + 0.5F), (double)((float)p_72706_5_ + 0.5F), "random.anvil_land", 0.3F, this.theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 2000:
                int j2 = p_72706_6_ % 3 - 1;
                int j1 = p_72706_6_ / 3 % 3 - 1;
                d1 = (double)p_72706_3_ + (double)j2 * 0.6D + 0.5D;
                d2 = (double)p_72706_4_ + 0.5D;
                double d9 = (double)p_72706_5_ + (double)j1 * 0.6D + 0.5D;

                for (int k2 = 0; k2 < 10; ++k2)
                {
                    double d11 = random.nextDouble() * 0.2D + 0.01D;
                    double d12 = d1 + (double)j2 * 0.01D + (random.nextDouble() - 0.5D) * (double)j1 * 0.5D;
                    d4 = d2 + (random.nextDouble() - 0.5D) * 0.5D;
                    d13 = d9 + (double)j1 * 0.01D + (random.nextDouble() - 0.5D) * (double)j2 * 0.5D;
                    d5 = (double)j2 * d11 + random.nextGaussian() * 0.01D;
                    d6 = -0.03D + random.nextGaussian() * 0.01D;
                    d7 = (double)j1 * d11 + random.nextGaussian() * 0.01D;
                    this.spawnParticle("smoke", d12, d4, d13, d5, d6, d7);
                }

                return;
            case 2001:
                block = Block.getBlockById(p_72706_6_ & 4095);

                if (block.getMaterial() != Material.air)
                {
                    this.mc.getSoundHandler().playSound(new PositionedSoundRecord(new ResourceLocation(block.stepSound.getBreakSound()), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getPitch() * 0.8F, (float)p_72706_3_ + 0.5F, (float)p_72706_4_ + 0.5F, (float)p_72706_5_ + 0.5F));
                }

                this.mc.effectRenderer.addBlockDestroyEffects(p_72706_3_, p_72706_4_, p_72706_5_, block, p_72706_6_ >> 12 & 255);
                break;
            case 2002:
                d0 = (double)p_72706_3_;
                d1 = (double)p_72706_4_;
                d2 = (double)p_72706_5_;
                s = "iconcrack_" + Item.getIdFromItem(Items.potionitem) + "_" + p_72706_6_;

                for (k1 = 0; k1 < 8; ++k1)
                {
                    this.spawnParticle(s, d0, d1, d2, random.nextGaussian() * 0.15D, random.nextDouble() * 0.2D, random.nextGaussian() * 0.15D);
                }

                k1 = Items.potionitem.getColorFromDamage(p_72706_6_);
                float f = (float)(k1 >> 16 & 255) / 255.0F;
                float f1 = (float)(k1 >> 8 & 255) / 255.0F;
                float f2 = (float)(k1 >> 0 & 255) / 255.0F;
                String s1 = "spell";

                if (Items.potionitem.isEffectInstant(p_72706_6_))
                {
                    s1 = "instantSpell";
                }

                for (l2 = 0; l2 < 100; ++l2)
                {
                    d4 = random.nextDouble() * 4.0D;
                    d13 = random.nextDouble() * Math.PI * 2.0D;
                    d5 = Math.cos(d13) * d4;
                    d6 = 0.01D + random.nextDouble() * 0.5D;
                    d7 = Math.sin(d13) * d4;
                    EntityFX entityfx = this.doSpawnParticle(s1, d0 + d5 * 0.1D, d1 + 0.3D, d2 + d7 * 0.1D, d5, d6, d7);

                    if (entityfx != null)
                    {
                        float f4 = 0.75F + random.nextFloat() * 0.25F;
                        entityfx.setRBGColorF(f * f4, f1 * f4, f2 * f4);
                        entityfx.multiplyVelocity((float)d4);
                    }
                }

                this.theWorld.playSound((double)p_72706_3_ + 0.5D, (double)p_72706_4_ + 0.5D, (double)p_72706_5_ + 0.5D, "game.potion.smash", 1.0F, this.theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
                break;
            case 2003:
                d0 = (double)p_72706_3_ + 0.5D;
                d1 = (double)p_72706_4_;
                d2 = (double)p_72706_5_ + 0.5D;
                s = "iconcrack_" + Item.getIdFromItem(Items.ender_eye);

                for (k1 = 0; k1 < 8; ++k1)
                {
                    this.spawnParticle(s, d0, d1, d2, random.nextGaussian() * 0.15D, random.nextDouble() * 0.2D, random.nextGaussian() * 0.15D);
                }

                for (double d10 = 0.0D; d10 < (Math.PI * 2D); d10 += 0.15707963267948966D)
                {
                    this.spawnParticle("portal", d0 + Math.cos(d10) * 5.0D, d1 - 0.4D, d2 + Math.sin(d10) * 5.0D, Math.cos(d10) * -5.0D, 0.0D, Math.sin(d10) * -5.0D);
                    this.spawnParticle("portal", d0 + Math.cos(d10) * 5.0D, d1 - 0.4D, d2 + Math.sin(d10) * 5.0D, Math.cos(d10) * -7.0D, 0.0D, Math.sin(d10) * -7.0D);
                }

                return;
            case 2004:
                for (l2 = 0; l2 < 20; ++l2)
                {
                    d4 = (double)p_72706_3_ + 0.5D + ((double)this.theWorld.rand.nextFloat() - 0.5D) * 2.0D;
                    d13 = (double)p_72706_4_ + 0.5D + ((double)this.theWorld.rand.nextFloat() - 0.5D) * 2.0D;
                    d5 = (double)p_72706_5_ + 0.5D + ((double)this.theWorld.rand.nextFloat() - 0.5D) * 2.0D;
                    this.theWorld.spawnParticle("smoke", d4, d13, d5, 0.0D, 0.0D, 0.0D);
                    this.theWorld.spawnParticle("flame", d4, d13, d5, 0.0D, 0.0D, 0.0D);
                }

                return;
            case 2005:
                ItemDye.func_150918_a(this.theWorld, p_72706_3_, p_72706_4_, p_72706_5_, p_72706_6_);
                break;
            case 2006:
                block = this.theWorld.getBlock(p_72706_3_, p_72706_4_, p_72706_5_);

                if (block.getMaterial() != Material.air)
                {
                    double d3 = (double)Math.min(0.2F + (float)p_72706_6_ / 15.0F, 10.0F);

                    if (d3 > 2.5D)
                    {
                        d3 = 2.5D;
                    }

                    int l1 = (int)(150.0D * d3);

                    for (int i2 = 0; i2 < l1; ++i2)
                    {
                        float f3 = MathHelper.randomFloatClamp(random, 0.0F, ((float)Math.PI * 2F));
                        d5 = (double)MathHelper.randomFloatClamp(random, 0.75F, 1.0F);
                        d6 = 0.20000000298023224D + d3 / 100.0D;
                        d7 = (double)(MathHelper.cos(f3) * 0.2F) * d5 * d5 * (d3 + 0.2D);
                        double d8 = (double)(MathHelper.sin(f3) * 0.2F) * d5 * d5 * (d3 + 0.2D);
                        this.theWorld.spawnParticle("blockdust_" + Block.getIdFromBlock(block) + "_" + this.theWorld.getBlockMetadata(p_72706_3_, p_72706_4_, p_72706_5_), (double)((float)p_72706_3_ + 0.5F), (double)((float)p_72706_4_ + 1.0F), (double)((float)p_72706_5_ + 0.5F), d7, d6, d8);
                    }
                }
        }
    }

    /**
     * Starts (or continues) destroying a block with given ID at the given coordinates for the given partially destroyed
     * value
     */
    public void destroyBlockPartially(int p_147587_1_, int p_147587_2_, int p_147587_3_, int p_147587_4_, int p_147587_5_)
    {
        if (p_147587_5_ >= 0 && p_147587_5_ < 10)
        {
            DestroyBlockProgress destroyblockprogress = (DestroyBlockProgress)this.damagedBlocks.get(Integer.valueOf(p_147587_1_));

            if (destroyblockprogress == null || destroyblockprogress.getPartialBlockX() != p_147587_2_ || destroyblockprogress.getPartialBlockY() != p_147587_3_ || destroyblockprogress.getPartialBlockZ() != p_147587_4_)
            {
                destroyblockprogress = new DestroyBlockProgress(p_147587_1_, p_147587_2_, p_147587_3_, p_147587_4_);
                this.damagedBlocks.put(Integer.valueOf(p_147587_1_), destroyblockprogress);
            }

            destroyblockprogress.setPartialBlockDamage(p_147587_5_);
            destroyblockprogress.setCloudUpdateTick(this.cloudTickCounter);
        }
        else
        {
            this.damagedBlocks.remove(Integer.valueOf(p_147587_1_));
        }
    }

    public void registerDestroyBlockIcons(IIconRegister par1IconRegister)
    {
        this.destroyBlockIcons = new IIcon[10];

        for (int var2 = 0; var2 < this.destroyBlockIcons.length; ++var2)
        {
            this.destroyBlockIcons[var2] = par1IconRegister.registerIcon("destroy_stage_" + var2);
        }
    }

    public void setAllRenderersVisible()
    {
        if (this.worldRenderers != null)
        {
            for (int i = 0; i < this.worldRenderers.length; ++i)
            {
                this.worldRenderers[i].isVisible = true;
            }
        }
    }

    public boolean isMoving(EntityLivingBase entityliving)
    {
        boolean moving = this.isMovingNow(entityliving);

        if (moving)
        {
            this.lastMovedTime = System.currentTimeMillis();
            return true;
        }
        else
        {
            return System.currentTimeMillis() - this.lastMovedTime < 2000L;
        }
    }

    private boolean isMovingNow(EntityLivingBase entityliving)
    {
        double maxDiff = 0.001D;
        return entityliving.isSneaking() ? true : ((double)entityliving.prevSwingProgress > maxDiff ? true : (this.mc.mouseHelper.deltaX != 0 ? true : (this.mc.mouseHelper.deltaY != 0 ? true : (Math.abs(entityliving.posX - entityliving.prevPosX) > maxDiff ? true : (Math.abs(entityliving.posY - entityliving.prevPosY) > maxDiff ? true : Math.abs(entityliving.posZ - entityliving.prevPosZ) > maxDiff)))));
    }

    public boolean isActing()
    {
        boolean acting = this.isActingNow();

        if (acting)
        {
            this.lastActionTime = System.currentTimeMillis();
            return true;
        }
        else
        {
            return System.currentTimeMillis() - this.lastActionTime < 500L;
        }
    }

    public boolean isActingNow()
    {
        return Mouse.isButtonDown(0) ? true : Mouse.isButtonDown(1);
    }

    public int renderAllSortedRenderers(int renderPass, double partialTicks)
    {
        return this.renderSortedRenderers(0, this.countSortedWorldRenderers, renderPass, partialTicks);
    }

    public void updateCapes() {}

    public AxisAlignedBB getTileEntityBoundingBox(TileEntity te)
    {
        if (!te.hasWorldObj())
        {
            return AABB_INFINITE;
        }
        else
        {
            Block blockType = te.getBlockType();

            if (blockType == Blocks.enchanting_table)
            {
                return AxisAlignedBB.getBoundingBox((double)te.xCoord, (double)te.yCoord, (double)te.zCoord, (double)(te.xCoord + 1), (double)(te.yCoord + 1), (double)(te.zCoord + 1));
            }
            else if (blockType != Blocks.chest && blockType != Blocks.trapped_chest)
            {
                AxisAlignedBB blockAabb;

                blockAabb = te.getRenderBoundingBox();

                if (blockAabb != null)
                {
                    return blockAabb;
                }

                if (blockType != null && blockType != Blocks.beacon)
                {
                    blockAabb = blockType.getCollisionBoundingBoxFromPool(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord);

                    if (blockAabb != null)
                    {
                        return blockAabb;
                    }
                }

                return AABB_INFINITE;
            }
            else
            {
                return AxisAlignedBB.getBoundingBox((double)(te.xCoord - 1), (double)te.yCoord, (double)(te.zCoord - 1), (double)(te.xCoord + 2), (double)(te.yCoord + 2), (double)(te.zCoord + 2));
            }
        }
    }

    public void addToSortedWorldRenderers(WorldRenderer wr)
    {
        if (!wr.inSortedWorldRenderers)
        {
            int pos = this.countSortedWorldRenderers;
            wr.updateDistanceToEntitySquared(this.renderViewEntity);
            float distSq = wr.sortDistanceToEntitySquared;
            int countGreater;

            if (this.countSortedWorldRenderers > 0)
            {
                countGreater = 0;
                int high = this.countSortedWorldRenderers - 1;
                int mid = (countGreater + high) / 2;

                while (countGreater <= high)
                {
                    mid = (countGreater + high) / 2;
                    WorldRenderer wrMid = this.sortedWorldRenderers[mid];

                    if (distSq < wrMid.sortDistanceToEntitySquared)
                    {
                        high = mid - 1;
                    }
                    else
                    {
                        countGreater = mid + 1;
                    }
                }

                if (countGreater > mid)
                {
                    pos = mid + 1;
                }
                else
                {
                    pos = mid;
                }
            }

            countGreater = this.countSortedWorldRenderers - pos;

            if (countGreater > 0)
            {
                System.arraycopy(this.sortedWorldRenderers, pos, this.sortedWorldRenderers, pos + 1, countGreater);
            }

            this.sortedWorldRenderers[pos] = wr;
            wr.inSortedWorldRenderers = true;
            ++this.countSortedWorldRenderers;
        }
    }

    public int getCountRenderers()
    {
        return this.renderersLoaded;
    }

    public int getCountActiveRenderers()
    {
        return this.renderersBeingRendered;
    }

    public int getCountEntitiesRendered()
    {
        return this.countEntitiesRendered;
    }

    public int getCountTileEntitiesRendered()
    {
        return this.countTileEntitiesRendered;
    }
}
