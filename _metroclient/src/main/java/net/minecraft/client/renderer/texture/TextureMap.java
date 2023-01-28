package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.item.Item;
import net.minecraft.src.*;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shadersmod.client.ShadersTex;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;

@SideOnly(Side.CLIENT)
public class TextureMap extends AbstractTexture implements ITickableTextureObject, IIconRegister
{
    private static final boolean ENABLE_SKIP = Boolean.parseBoolean(System.getProperty("fml.skipFirstTextureLoad", "true"));
    private static final Logger logger = LogManager.getLogger();
    public static final ResourceLocation locationBlocksTexture = new ResourceLocation("textures/atlas/blocks.png");
    public static final ResourceLocation locationItemsTexture = new ResourceLocation("textures/atlas/items.png");
    private final List<TextureAtlasSprite> listAnimatedSprites;
    private final Map<String, TextureAtlasSprite> mapRegisteredSprites;
    private final Map<String, TextureAtlasSprite> mapUploadedSprites;

    /** 0 = terrain.png, 1 = items.png */
    public final int textureType;
    public final String basePath;
    private int mipmapLevels;
    private int anisotropicFiltering;
    private final TextureAtlasSprite missingImage;
    private boolean skipFirst;
    public static TextureMap textureMapBlocks = null;
    public static TextureMap textureMapItems = null;
    private TextureAtlasSprite[] iconGrid;
    private int iconGridSize;
    private int iconGridCountX;
    private int iconGridCountY;
    private double iconGridSizeU;
    private double iconGridSizeV;
    private int counterIndexInMap;
    public int atlasWidth;
    public int atlasHeight;

    public TextureMap(int par1, String par2Str)
    {
        this(par1, par2Str, false);
    }

    public TextureMap(int par1, String par2Str, boolean skipFirst)
    {
        this.listAnimatedSprites = Lists.newArrayList();
        this.mapRegisteredSprites = Maps.newHashMap();
        this.mapUploadedSprites = Maps.newHashMap();
        this.anisotropicFiltering = 1;
        this.missingImage = new TextureAtlasSprite("missingno");
        this.skipFirst = false;
        this.iconGrid = null;
        this.iconGridSize = -1;
        this.iconGridCountX = -1;
        this.iconGridCountY = -1;
        this.iconGridSizeU = -1.0D;
        this.iconGridSizeV = -1.0D;
        this.counterIndexInMap = 0;
        this.atlasWidth = 0;
        this.atlasHeight = 0;
        this.textureType = par1;
        this.basePath = par2Str;

        if (this.textureType == 0)
        {
            textureMapBlocks = this;
        }

        if (this.textureType == 1)
        {
            textureMapItems = this;
        }

        this.registerIcons();
        this.skipFirst = skipFirst && ENABLE_SKIP;
    }

    private void initMissingImage()
    {
        int[] var1;

        if ((float)this.anisotropicFiltering > 1.0F)
        {
            this.missingImage.setIconWidth(32);
            this.missingImage.setIconHeight(32);
            var1 = new int[1024];
            System.arraycopy(TextureUtil.missingTextureData, 0, var1, 0, TextureUtil.missingTextureData.length);
            TextureUtil.prepareAnisotropicData(var1, 16, 16, 8);
        }
        else
        {
            var1 = TextureUtil.missingTextureData;
            this.missingImage.setIconWidth(16);
            this.missingImage.setIconHeight(16);
        }

        int[][] var51 = new int[this.mipmapLevels + 1][];
        var51[0] = var1;
        this.missingImage.setFramesTextureData(Lists.newArrayList(new int[][][] {var51}));
        this.missingImage.setIndexInMap(this.counterIndexInMap++);
    }

    public void loadTexture(IResourceManager par1ResourceManager) throws IOException
    {
        ShadersTex.resManager = par1ResourceManager;
        this.counterIndexInMap = 0;
        this.initMissingImage();
        this.deleteGlTexture();
        this.loadTextureAtlas(par1ResourceManager);
    }

    public void loadTextureAtlas(IResourceManager p_110571_1_)
    {
        ShadersTex.resManager = p_110571_1_;
        Config.dbg("Loading texture map: " + this.basePath);
        WrUpdates.finishCurrentUpdate();
        registerIcons(); //Re-gather list of Icons, allows for addition/removal of blocks/items after this map was initially constructed.
        int i = Minecraft.getGLMaximumTextureSize();
        Stitcher stitcher = new Stitcher(i, i, true, 0, this.mipmapLevels);
        this.mapUploadedSprites.clear();
        this.listAnimatedSprites.clear();
        int j = Integer.MAX_VALUE;
        ForgeHooksClient.onTextureStitchedPre(this);
        cpw.mods.fml.common.ProgressManager.ProgressBar bar = cpw.mods.fml.common.ProgressManager.push("Texture Loading", skipFirst ? 0 : this.mapRegisteredSprites.size());
        Iterator iterator = this.mapRegisteredSprites.entrySet().iterator();
        TextureAtlasSprite textureatlassprite;

        while (!skipFirst && iterator.hasNext())
        {
            Entry entry = (Entry)iterator.next();
            ResourceLocation resourcelocation = new ResourceLocation((String)entry.getKey());
            textureatlassprite = (TextureAtlasSprite)entry.getValue();
            ResourceLocation resourcelocation1 = this.completeResourceLocation(resourcelocation, 0);
            bar.step(resourcelocation1.getResourcePath());

            if (textureatlassprite.getIndexInMap() < 0)
            {
                textureatlassprite.setIndexInMap(this.counterIndexInMap++);
            }

            if (textureatlassprite.hasCustomLoader(p_110571_1_, resourcelocation))
            {
                if (!textureatlassprite.load(p_110571_1_, resourcelocation))
                {
                    j = Math.min(j, Math.min(textureatlassprite.getIconWidth(), textureatlassprite.getIconHeight()));
                    stitcher.addSprite(textureatlassprite);
                }

                Config.dbg("Custom loader: " + textureatlassprite);
                continue;
            }

            try
            {
                IResource iresource = p_110571_1_.getResource(resourcelocation1);
                BufferedImage[] abufferedimage = new BufferedImage[1 + this.mipmapLevels];
                abufferedimage[0] = ImageIO.read(iresource.getInputStream());
                TextureMetadataSection texturemetadatasection = (TextureMetadataSection)iresource.getMetadata("texture");

                if (texturemetadatasection != null)
                {
                    List list = texturemetadatasection.getListMipmaps();
                    int l;

                    if (!list.isEmpty())
                    {
                        int k = abufferedimage[0].getWidth();
                        l = abufferedimage[0].getHeight();

                        if (MathHelper.roundUpToPowerOfTwo(k) != k || MathHelper.roundUpToPowerOfTwo(l) != l)
                        {
                            throw new RuntimeException("Unable to load extra miplevels, source-texture is not power of two");
                        }
                    }

                    for (Object o : list) {
                        l = (Integer) o;

                        if (l > 0 && l < abufferedimage.length - 1 && abufferedimage[l] == null) {
                            ResourceLocation resourcelocation2 = this.completeResourceLocation(resourcelocation, l);

                            try {
                                abufferedimage[l] = ImageIO.read(p_110571_1_.getResource(resourcelocation2).getInputStream());
                            } catch (IOException ioexception) {
                                logger.error("Unable to load miplevel {} from: {}", l, resourcelocation2, ioexception);
                            }
                        }
                    }
                }

                AnimationMetadataSection animationmetadatasection = (AnimationMetadataSection)iresource.getMetadata("animation");
                textureatlassprite.loadSprite(abufferedimage, animationmetadatasection, false);//(float)this.anisotropicFiltering > 1.0F);
            }
            catch (RuntimeException runtimeexception)
            {
                logger.error("Unable to parse metadata from " + resourcelocation1, runtimeexception);
                cpw.mods.fml.client.FMLClientHandler.instance().trackBrokenTexture(resourcelocation1, runtimeexception.getMessage());
                continue;
            }
            catch (IOException ioexception1)
            {
                logger.error("Using missing texture, unable to load " + resourcelocation1, ioexception1);
                cpw.mods.fml.client.FMLClientHandler.instance().trackMissingTexture(resourcelocation1);
                continue;
            }

            j = Math.min(j, Math.min(textureatlassprite.getIconWidth(), textureatlassprite.getIconHeight()));
            stitcher.addSprite(textureatlassprite);
        }

        cpw.mods.fml.common.ProgressManager.pop(bar);
        int i1 = MathHelper.calculateLogBaseTwo(j);

        if (i1 < 0)
        {
            i1 = 0;
        }

        if (i1 < this.mipmapLevels)
        {
            logger.debug("{}: dropping miplevel from {} to {}, because of minTexel: {}", this.basePath, this.mipmapLevels, i1, j);
            this.mipmapLevels = i1;
        }

        Iterator iterator1 = this.mapRegisteredSprites.values().iterator();
        bar = cpw.mods.fml.common.ProgressManager.push("Mipmap generation", skipFirst ? 0 : this.mapRegisteredSprites.size());

        while (!skipFirst && iterator1.hasNext())
        {
            final TextureAtlasSprite textureatlassprite1 = (TextureAtlasSprite)iterator1.next();
            bar.step(textureatlassprite1.getIconName());

            try
            {
                textureatlassprite1.generateMipmaps(this.mipmapLevels);
            }
            catch (Throwable throwable1)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable1, "Applying mipmap");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Sprite being mipmapped");
                crashreportcategory.addCrashSectionCallable("Sprite name", textureatlassprite1::getIconName);
                crashreportcategory.addCrashSectionCallable("Sprite size", () -> textureatlassprite1.getIconWidth() + " x " + textureatlassprite1.getIconHeight());
                crashreportcategory.addCrashSectionCallable("Sprite frames", () -> textureatlassprite1.getFrameCount() + " frames");
                crashreportcategory.addCrashSection("Mipmap levels", this.mipmapLevels);
                throw new ReportedException(crashreport);
            }
        }

        this.missingImage.generateMipmaps(this.mipmapLevels);
        stitcher.addSprite(this.missingImage);
        cpw.mods.fml.common.ProgressManager.pop(bar);
        skipFirst = false;
        bar = cpw.mods.fml.common.ProgressManager.push("Texture creation", 3);

        bar.step("Stitching");
        stitcher.doStitch();

        Config.dbg("Texture size: " + this.basePath + ", " + stitcher.getCurrentWidth() + "x" + stitcher.getCurrentHeight());
        int sheetWidth2 = stitcher.getCurrentWidth();
        int sheetHeight1 = stitcher.getCurrentHeight();
        BufferedImage debugImage2 = null;

        if (System.getProperty("saveTextureMap", "false").equalsIgnoreCase("true"))
        {
            debugImage2 = this.makeDebugImage(sheetWidth2, sheetHeight1);
        }

        logger.info("Created: {}x{} {}-atlas", stitcher.getCurrentWidth(), stitcher.getCurrentHeight(), this.basePath);
        bar.step("Allocating GL texture");

        if (Config.isShaders())
        {
            ShadersTex.allocateTextureMap(this.getGlTextureId(), this.mipmapLevels, stitcher.getCurrentWidth(), stitcher.getCurrentHeight(), stitcher, this);
        }
        else
        {
            TextureUtil.allocateTextureImpl(this.getGlTextureId(), this.mipmapLevels, stitcher.getCurrentWidth(), stitcher.getCurrentHeight(), 1f);//(float)this.anisotropicFiltering);
        }

        HashMap hashmap = Maps.newHashMap(this.mapRegisteredSprites);
        Iterator iterator2 = stitcher.getStichSlots().iterator();

        bar.step("Uploading GL texture");
        while (iterator2.hasNext())
        {
            textureatlassprite = (TextureAtlasSprite)iterator2.next();

            if (Config.isShaders())
            {
                ShadersTex.setIconName(ShadersTex.setSprite(textureatlassprite).getIconName());
            }

            String s = textureatlassprite.getIconName();
            hashmap.remove(s);
            this.mapUploadedSprites.put(s, textureatlassprite);

            try
            {
                if (Config.isShaders())
                {
                    ShadersTex.uploadTexSubForLoadAtlas(textureatlassprite.getFrameTextureData(0), textureatlassprite.getIconWidth(), textureatlassprite.getIconHeight(), textureatlassprite.getOriginX(), textureatlassprite.getOriginY(), false, false);
                }
                else
                {
                    TextureUtil.uploadTextureMipmap(textureatlassprite.getFrameTextureData(0), textureatlassprite.getIconWidth(), textureatlassprite.getIconHeight(), textureatlassprite.getOriginX(), textureatlassprite.getOriginY(), false, false);
                }

                if (debugImage2 != null)
                {
                    this.addDebugSprite(textureatlassprite, debugImage2);
                }
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport1 = CrashReport.makeCrashReport(throwable, "Stitching texture atlas");
                CrashReportCategory crashreportcategory1 = crashreport1.makeCategory("Texture being stitched together");
                crashreportcategory1.addCrashSection("Atlas path", this.basePath);
                crashreportcategory1.addCrashSection("Sprite", textureatlassprite);
                throw new ReportedException(crashreport1);
            }

            if (textureatlassprite.hasAnimationMetadata())
            {
                this.listAnimatedSprites.add(textureatlassprite);
            }
            else
            {
                textureatlassprite.clearFramesTextureData();
            }
        }

        iterator2 = hashmap.values().iterator();

        while (iterator2.hasNext())
        {
            textureatlassprite = (TextureAtlasSprite)iterator2.next();
            textureatlassprite.copyFrom(this.missingImage);
        }

        if (debugImage2 != null)
        {
            this.writeDebugImage(debugImage2, "debug_" + this.basePath.replace('/', '_') + ".png");
        }

        ForgeHooksClient.onTextureStitchedPost(this);
        cpw.mods.fml.common.ProgressManager.pop(bar);
    }

    public ResourceLocation completeResourceLocation(ResourceLocation p_147634_1_, int p_147634_2_)
    {
        return this.isAbsoluteLocation(p_147634_1_) ? (p_147634_2_ == 0 ? new ResourceLocation(p_147634_1_.getResourceDomain(), p_147634_1_.getResourcePath() + ".png") : new ResourceLocation(p_147634_1_.getResourceDomain(), p_147634_1_.getResourcePath() + "mipmap" + p_147634_2_ + ".png")) : (p_147634_2_ == 0 ? new ResourceLocation(p_147634_1_.getResourceDomain(), String.format("%s/%s%s", new Object[] {this.basePath, p_147634_1_.getResourcePath(), ".png"})): new ResourceLocation(p_147634_1_.getResourceDomain(), String.format("%s/mipmaps/%s.%d%s", new Object[] {this.basePath, p_147634_1_.getResourcePath(), Integer.valueOf(p_147634_2_), ".png"})));
    }

    private void registerIcons()
    {
        this.mapRegisteredSprites.clear();
        Iterator iterator;

        if (this.textureType == 0)
        {
            iterator = Block.blockRegistry.iterator();

            while (iterator.hasNext())
            {
                Block block = (Block)iterator.next();

                if (block.getMaterial() != Material.air)
                {
                    block.registerBlockIcons(this);
                }
            }

            Minecraft.getMinecraft().renderGlobal.registerDestroyBlockIcons(this);
            RenderManager.instance.updateIcons(this);
            ConnectedTextures.updateIcons(this);
        }

        if (this.textureType == 1)
        {
            CustomItems.updateIcons(this);
        }

        iterator = Item.itemRegistry.iterator();

        while (iterator.hasNext())
        {
            Item item = (Item)iterator.next();

            if (item != null && item.getSpriteNumber() == this.textureType)
            {
                item.registerIcons(this);
            }
        }
    }

    public TextureAtlasSprite getAtlasSprite(String p_110572_1_)
    {
        TextureAtlasSprite textureatlassprite = this.mapUploadedSprites.get(p_110572_1_);

        if (textureatlassprite == null)
        {
            textureatlassprite = this.missingImage;
        }

        return textureatlassprite;
    }

    public void updateAnimations()
    {
        if (Config.isShaders())
        {
            ShadersTex.updatingTex = this.getMultiTexID();
        }

        boolean hasNormal = false;
        boolean hasSpecular = false;
        TextureUtil.bindTexture(this.getGlTextureId());
        Iterator var1 = this.listAnimatedSprites.iterator();

        while (var1.hasNext())
        {
            TextureAtlasSprite i$ = (TextureAtlasSprite)var1.next();

            if (this.textureType == 0)
            {
                if (!this.isTerrainAnimationActive(i$))
                {
                    continue;
                }
            }
            else if (this.textureType == 1 && !this.isItemAnimationActive(i$))
            {
                continue;
            }

            i$.updateAnimation();

            if (i$.spriteNormal != null)
            {
                hasNormal = true;
            }

            if (i$.spriteSpecular != null)
            {
                hasSpecular = true;
            }
        }

        if (Config.isShaders())
        {
            TextureAtlasSprite textureatlassprite;
            Iterator i$1;

            if (hasNormal)
            {
                TextureUtil.bindTexture(this.getMultiTexID().norm);
                i$1 = this.listAnimatedSprites.iterator();

                while (i$1.hasNext())
                {
                    textureatlassprite = (TextureAtlasSprite)i$1.next();

                    if (textureatlassprite.spriteNormal != null && this.isTerrainAnimationActive(textureatlassprite))
                    {
                        if (textureatlassprite == TextureUtils.iconClock || textureatlassprite == TextureUtils.iconCompass)
                        {
                            textureatlassprite.spriteNormal.frameCounter = textureatlassprite.frameCounter;
                        }

                        textureatlassprite.spriteNormal.updateAnimation();
                    }
                }
            }

            if (hasSpecular)
            {
                TextureUtil.bindTexture(this.getMultiTexID().spec);
                i$1 = this.listAnimatedSprites.iterator();

                while (i$1.hasNext())
                {
                    textureatlassprite = (TextureAtlasSprite)i$1.next();

                    if (textureatlassprite.spriteSpecular != null && this.isTerrainAnimationActive(textureatlassprite))
                    {
                        if (textureatlassprite == TextureUtils.iconClock || textureatlassprite == TextureUtils.iconCompass)
                        {
                            textureatlassprite.spriteNormal.frameCounter = textureatlassprite.frameCounter;
                        }

                        textureatlassprite.spriteSpecular.updateAnimation();
                    }
                }
            }

            if (hasNormal || hasSpecular)
            {
                TextureUtil.bindTexture(this.getGlTextureId());
            }
        }

        if (Config.isShaders())
        {
            ShadersTex.updatingTex = null;
        }
    }

    private boolean isItemAnimationActive(TextureAtlasSprite ts)
    {
        return ts == TextureUtils.iconClock || ts == TextureUtils.iconCompass || Config.isAnimatedItems();
    }

    public IIcon registerIcon(String par1Str)
    {
        if (par1Str == null)
        {
            throw new IllegalArgumentException("Name cannot be null!");
        }
        else if (par1Str.indexOf(92) != -1 && !this.isAbsoluteLocationPath(par1Str))
        {
            throw new IllegalArgumentException("Name cannot contain slashes!");
        }
        else
        {
            TextureAtlasSprite var2 = this.mapRegisteredSprites.get(par1Str);

            if (var2 == null)
            {
                if (this.textureType == 1)
                {
                    if ("clock".equals(par1Str))
                    {
                        var2 = new TextureClock(par1Str);
                    }
                    else if ("compass".equals(par1Str))
                    {
                        var2 = new TextureCompass(par1Str);
                    }
                    else
                    {
                        var2 = new TextureAtlasSprite(par1Str);
                    }
                }
                else
                {
                    var2 = new TextureAtlasSprite(par1Str);
                }

                this.mapRegisteredSprites.put(par1Str, var2);

                TextureAtlasSprite tas = var2;

                if (tas.getIndexInMap() < 0)
                {
                    tas.setIndexInMap(this.counterIndexInMap++);
                }
            }

            return var2;
        }
    }

    public int getTextureType()
    {
        return this.textureType;
    }

    public void tick()
    {
        this.updateAnimations();
    }

    public void setMipmapLevels(int p_147633_1_)
    {
        this.mipmapLevels = p_147633_1_;
    }

    public void setAnisotropicFiltering(int p_147632_1_)
    {
        this.anisotropicFiltering = p_147632_1_;
    }

    //===================================================================================================
    //                                           Forge Start
    //===================================================================================================
    /**
     * Grabs the registered entry for the specified name, returning null if there was not a entry.
     * Opposed to registerIcon, this will not instantiate the entry, useful to test if a mapping exists.
     *
     * @param name The name of the entry to find
     * @return The registered entry, null if nothing was registered.
     */
    public TextureAtlasSprite getTextureExtry(String name)
    {
        return mapRegisteredSprites.get(name);
    }

    /**
     * Adds a texture registry entry to this map for the specified name if one does not already exist.
     * Returns false if the map already contains a entry for the specified name.
     *
     * @param name Entry name
     * @param entry Entry instance
     * @return True if the entry was added to the map, false otherwise.
     */
    public boolean setTextureEntry(String name, TextureAtlasSprite entry)
    {
        if (!mapRegisteredSprites.containsKey(name))
        {
            this.mapRegisteredSprites.put(name, entry);

            if (entry.getIndexInMap() < 0)
            {
                entry.setIndexInMap(this.counterIndexInMap++);
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean setTextureEntry(TextureAtlasSprite entry)
    {
        return this.setTextureEntry(entry.getIconName(), entry);
    }

    public String getBasePath()
    {
        return this.basePath;
    }

    public int getMipmapLevels()
    {
        return this.mipmapLevels;
    }

    private boolean isAbsoluteLocation(ResourceLocation loc)
    {
        String path = loc.getResourcePath();
        return this.isAbsoluteLocationPath(path);
    }

    private boolean isAbsoluteLocationPath(String resPath)
    {
        String path = resPath.toLowerCase();
        return path.startsWith("mcpatcher/") || path.startsWith("optifine/");
    }

    public TextureAtlasSprite getIconSafe(String name)
    {
        return this.mapRegisteredSprites.get(name);
    }

    private int getStandardTileSize(Collection icons)
    {
        int[] sizeCounts = new int[16];
        Iterator mostUsedPo2 = icons.iterator();
        int value;
        int count;

        while (mostUsedPo2.hasNext())
        {
            TextureAtlasSprite mostUsedCount = (TextureAtlasSprite)mostUsedPo2.next();

            if (mostUsedCount != null)
            {
                value = TextureUtils.getPowerOfTwo(mostUsedCount.getWidth());
                count = TextureUtils.getPowerOfTwo(mostUsedCount.getHeight());
                int po2 = Math.max(value, count);

                if (po2 < sizeCounts.length)
                {
                    ++sizeCounts[po2];
                }
            }
        }

        int var8 = 4;
        int var9 = 0;

        for (value = 0; value < sizeCounts.length; ++value)
        {
            count = sizeCounts[value];

            if (count > var9)
            {
                var8 = value;
                var9 = count;
            }
        }

        if (var8 < 4)
        {
            var8 = 4;
        }

        value = TextureUtils.twoToPower(var8);
        return value;
    }

    private void updateIconGrid(int sheetWidth, int sheetHeight)
    {
        this.iconGridCountX = -1;
        this.iconGridCountY = -1;
        this.iconGrid = null;

        if (this.iconGridSize > 0)
        {
            this.iconGridCountX = sheetWidth / this.iconGridSize;
            this.iconGridCountY = sheetHeight / this.iconGridSize;
            this.iconGrid = new TextureAtlasSprite[this.iconGridCountX * this.iconGridCountY];
            this.iconGridSizeU = 1.0D / (double)this.iconGridCountX;
            this.iconGridSizeV = 1.0D / (double)this.iconGridCountY;
            Iterator it = this.mapUploadedSprites.values().iterator();

            while (it.hasNext())
            {
                TextureAtlasSprite ts = (TextureAtlasSprite)it.next();
                double deltaU = 0.5D / (double)sheetWidth;
                double deltaV = 0.5D / (double)sheetHeight;
                double uMin = (double)Math.min(ts.getMinU(), ts.getMaxU()) + deltaU;
                double vMin = (double)Math.min(ts.getMinV(), ts.getMaxV()) + deltaV;
                double uMax = (double)Math.max(ts.getMinU(), ts.getMaxU()) - deltaU;
                double vMax = (double)Math.max(ts.getMinV(), ts.getMaxV()) - deltaV;
                int iuMin = (int)(uMin / this.iconGridSizeU);
                int ivMin = (int)(vMin / this.iconGridSizeV);
                int iuMax = (int)(uMax / this.iconGridSizeU);
                int ivMax = (int)(vMax / this.iconGridSizeV);

                for (int iu = iuMin; iu <= iuMax; ++iu)
                {
                    if (iu >= 0 && iu < this.iconGridCountX)
                    {
                        for (int iv = ivMin; iv <= ivMax; ++iv)
                        {
                            if (iv >= 0 && iv < this.iconGridCountX)
                            {
                                int index = iv * this.iconGridCountX + iu;
                                this.iconGrid[index] = ts;
                            }
                            else
                            {
                                Config.warn("Invalid grid V: " + iv + ", icon: " + ts.getIconName());
                            }
                        }
                    }
                    else
                    {
                        Config.warn("Invalid grid U: " + iu + ", icon: " + ts.getIconName());
                    }
                }
            }
        }
    }

    public TextureAtlasSprite getIconByUV(double u, double v)
    {
        if (this.iconGrid == null)
        {
            return null;
        }
        else
        {
            int iu = (int)(u / this.iconGridSizeU);
            int iv = (int)(v / this.iconGridSizeV);
            int index = iv * this.iconGridCountX + iu;
            return index >= 0 && index <= this.iconGrid.length ? this.iconGrid[index] : null;
        }
    }

    public TextureAtlasSprite getMissingSprite()
    {
        return this.missingImage;
    }

    public int getMaxTextureIndex()
    {
        return this.mapRegisteredSprites.size();
    }

    private boolean isTerrainAnimationActive(TextureAtlasSprite ts)
    {
        return ts != TextureUtils.iconWaterStill && ts != TextureUtils.iconWaterFlow ? (ts != TextureUtils.iconLavaStill && ts != TextureUtils.iconLavaFlow ? (ts != TextureUtils.iconFireLayer0 && ts != TextureUtils.iconFireLayer1 ? (ts == TextureUtils.iconPortal ? Config.isAnimatedPortal() : (ts != TextureUtils.iconClock && ts != TextureUtils.iconCompass ? Config.isAnimatedTerrain() : true)) : Config.isAnimatedFire()) : Config.isAnimatedLava()) : Config.isAnimatedWater();
    }

    public int getCountRegisteredSprites()
    {
        return this.counterIndexInMap;
    }

    public void loadTextureSafe(IResourceManager rm)
    {
        try
        {
            this.loadTexture(rm);
        }
        catch (IOException var3)
        {
            Config.warn("Error loading texture map: " + this.basePath);
            var3.printStackTrace();
        }
    }

    private BufferedImage makeDebugImage(int sheetWidth, int sheetHeight)
    {
        BufferedImage image = new BufferedImage(sheetWidth, sheetHeight, 2);
        Graphics2D g = image.createGraphics();
        g.setPaint(new Color(255, 255, 0));
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        return image;
    }

    private void addDebugSprite(TextureAtlasSprite ts, BufferedImage image)
    {
        if (ts.getFrameCount() < 1)
        {
            Config.warn("Debug sprite has no data: " + ts.getIconName());
        }
        else
        {
            int[] data = ts.getFrameTextureData(0)[0];
            image.setRGB(ts.getOriginX(), ts.getOriginY(), ts.getIconWidth(), ts.getIconHeight(), data, 0, ts.getIconWidth());
        }
    }

    private void writeDebugImage(BufferedImage image, String pngPath)
    {
        try
        {
            ImageIO.write(image, "png", new File(Config.getMinecraft().mcDataDir, pngPath));
        }
        catch (Exception var4)
        {
            var4.printStackTrace();
        }
    }
}
