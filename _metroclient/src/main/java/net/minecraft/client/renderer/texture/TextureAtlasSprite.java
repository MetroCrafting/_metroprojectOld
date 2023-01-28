package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.AnimationFrame;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.src.Config;
import net.minecraft.src.Mipmaps;
import net.minecraft.src.TextureUtils;
import net.minecraft.util.IIcon;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import shadersmod.client.Shaders;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

@SideOnly(Side.CLIENT)
public class TextureAtlasSprite implements IIcon
{
    private final String iconName;
    protected List framesTextureData = Lists.newArrayList();
    private AnimationMetadataSection animationMetadata;
    protected boolean rotated;
    private boolean useAnisotropicFiltering;
    protected int originX;
    protected int originY;
    protected int width;
    protected int height;
    private float minU;
    private float maxU;
    private float minV;
    private float maxV;
    protected int frameCounter;
    protected int tickCounter;
    //private static final String __OBFID = "CL_00001062";
    private int indexInMap = -1;
    public float baseU;
    public float baseV;
    public int sheetWidth;
    public int sheetHeight;
    private boolean mipmapActive = false;
    public int glOwnTextureId = -1;
    private int uploadedFrameIndex = -1;
    private int uploadedOwnFrameIndex = -1;
    public IntBuffer[] frameBuffers;
    public Mipmaps[] frameMipmaps;
    public int mipmapLevels = 0;
    public TextureAtlasSprite spriteNormal = null;
    public TextureAtlasSprite spriteSpecular = null;
    public boolean isShadersSprite = false;

    protected TextureAtlasSprite(String p_i1282_1_)
    {
        this.iconName = p_i1282_1_;
    }

    public void initSprite(int par1, int par2, int par3, int par4, boolean par5)
    {
        this.originX = par3;
        this.originY = par4;
        this.rotated = par5;
        float var6 = (float)(0.009999999776482582D / (double)par1);
        float var7 = (float)(0.009999999776482582D / (double)par2);
        this.minU = (float)par3 / (float)((double)par1) + var6;
        this.maxU = (float)(par3 + this.width) / (float)((double)par1) - var6;
        this.minV = (float)par4 / (float)par2 + var7;
        this.maxV = (float)(par4 + this.height) / (float)par2 - var7;

//        if (this.useAnisotropicFiltering)
//        {
//            float var8 = 8.0F / (float)par1;
//            float var9 = 8.0F / (float)par2;
//            this.minU += var8;
//            this.maxU -= var8;
//            this.minV += var9;
//            this.maxV -= var9;
//        }

        this.baseU = Math.min(this.minU, this.maxU);
        this.baseV = Math.min(this.minV, this.maxV);

        if (this.spriteNormal != null)
        {
            this.spriteNormal.initSprite(par1, par2, par3, par4, par5);
        }

        if (this.spriteSpecular != null)
        {
            this.spriteSpecular.initSprite(par1, par2, par3, par4, par5);
        }
    }

    public void copyFrom(TextureAtlasSprite par1TextureAtlasSprite)
    {
        this.originX = par1TextureAtlasSprite.originX;
        this.originY = par1TextureAtlasSprite.originY;
        this.width = par1TextureAtlasSprite.width;
        this.height = par1TextureAtlasSprite.height;
        this.rotated = par1TextureAtlasSprite.rotated;
        this.minU = par1TextureAtlasSprite.minU;
        this.maxU = par1TextureAtlasSprite.maxU;
        this.minV = par1TextureAtlasSprite.minV;
        this.maxV = par1TextureAtlasSprite.maxV;
        this.baseU = Math.min(this.minU, this.maxU);
        this.baseV = Math.min(this.minV, this.maxV);
    }

    /**
     * Returns the X position of this icon on its texture sheet, in pixels.
     */
    public int getOriginX()
    {
        return this.originX;
    }

    /**
     * Returns the Y position of this icon on its texture sheet, in pixels.
     */
    public int getOriginY()
    {
        return this.originY;
    }

    /**
     * Returns the width of the icon, in pixels.
     */
    public int getIconWidth()
    {
        return this.width;
    }

    /**
     * Returns the height of the icon, in pixels.
     */
    public int getIconHeight()
    {
        return this.height;
    }

    /**
     * Returns the minimum U coordinate to use when rendering with this icon.
     */
    public float getMinU()
    {
        return this.minU;
    }

    /**
     * Returns the maximum U coordinate to use when rendering with this icon.
     */
    public float getMaxU()
    {
        return this.maxU;
    }

    /**
     * Gets a U coordinate on the icon. 0 returns uMin and 16 returns uMax. Other arguments return in-between values.
     */
    public float getInterpolatedU(double p_94214_1_)
    {
        float f = this.maxU - this.minU;
        return this.minU + f * (float)p_94214_1_ / 16.0F;
    }

    /**
     * Returns the minimum V coordinate to use when rendering with this icon.
     */
    public float getMinV()
    {
        return this.minV;
    }

    /**
     * Returns the maximum V coordinate to use when rendering with this icon.
     */
    public float getMaxV()
    {
        return this.maxV;
    }

    /**
     * Gets a V coordinate on the icon. 0 returns vMin and 16 returns vMax. Other arguments return in-between values.
     */
    public float getInterpolatedV(double p_94207_1_)
    {
        float f = this.maxV - this.minV;
        return this.minV + f * ((float)p_94207_1_ / 16.0F);
    }

    public String getIconName()
    {
        return this.iconName;
    }

    public void updateAnimation()
    {
        if (this.animationMetadata != null)
        {
            ++this.tickCounter;

            if (this.tickCounter >= this.animationMetadata.getFrameTimeSingle(this.frameCounter))
            {
                int var1 = this.animationMetadata.getFrameIndex(this.frameCounter);
                int var2 = this.animationMetadata.getFrameCount() == 0 ? this.framesTextureData.size() : this.animationMetadata.getFrameCount();
                this.frameCounter = (this.frameCounter + 1) % var2;
                this.tickCounter = 0;
                int var3 = this.animationMetadata.getFrameIndex(this.frameCounter);
                boolean texBlur = false;
                boolean texClamp = false;

                if (var1 != var3 && var3 >= 0 && var3 < this.framesTextureData.size())
                {
                    TextureUtil.uploadTextureMipmap((int[][])this.framesTextureData.get(var3), this.width, this.height, this.originX, this.originY, texBlur, texClamp);
                    this.uploadedFrameIndex = var3;
                }
            }
        }
    }

    public int[][] getFrameTextureData(int p_147965_1_)
    {
        return (int[][])this.framesTextureData.get(p_147965_1_);
    }

    public int getFrameCount()
    {
        return this.framesTextureData.size();
    }

    public void setIconWidth(int p_110966_1_)
    {
        this.width = p_110966_1_;
    }

    public void setIconHeight(int p_110969_1_)
    {
        this.height = p_110969_1_;
    }

    public void loadSprite(BufferedImage[] p_147964_1_, AnimationMetadataSection p_147964_2_, boolean anisotropFiltering)
    {
        this.resetSprite();
        this.useAnisotropicFiltering = anisotropFiltering;
        int i = p_147964_1_[0].getWidth();
        int j = p_147964_1_[0].getHeight();
        this.width = i;
        this.height = j;

//        if (anisotropFiltering)
//        {
//            this.width += 16;
//            this.height += 16;
//        }

        int[][] aint = new int[p_147964_1_.length][];
        int k;

        for (k = 0; k < p_147964_1_.length; ++k)
        {
            BufferedImage bufferedimage = p_147964_1_[k];

            if (bufferedimage != null)
            {
                if (k > 0 && (bufferedimage.getWidth() != i >> k || bufferedimage.getHeight() != j >> k))
                {
                    throw new RuntimeException(String.format("Unable to load miplevel: %d, image is size: %dx%d, expected %dx%d", k, bufferedimage.getWidth(), bufferedimage.getHeight(), i >> k, j >> k));
                }

                aint[k] = new int[bufferedimage.getWidth() * bufferedimage.getHeight()];
                bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), aint[k], 0, bufferedimage.getWidth());
            }
        }

        if (p_147964_2_ == null)
        {
            if (j != i)
            {
                throw new RuntimeException("broken aspect ratio and not an animation");
            }

            this.fixTransparentPixels(aint);
            this.framesTextureData.add(this.prepareAnisotropicFiltering(aint, i, j));
        }
        else
        {
            k = j / i;
            int j1 = i;
            int l = i;
            this.height = this.width;
            int i1;

            if (p_147964_2_.getFrameCount() > 0)
            {
                Iterator iterator = p_147964_2_.getFrameIndexSet().iterator();

                while (iterator.hasNext())
                {
                    i1 = ((Integer)iterator.next()).intValue();

                    if (i1 >= k)
                    {
                        throw new RuntimeException("invalid frameindex " + i1);
                    }

                    this.allocateFrameTextureData(i1);
                    this.framesTextureData.set(i1, this.prepareAnisotropicFiltering(getFrameTextureData(aint, j1, l, i1), j1, l));
                }

                this.animationMetadata = p_147964_2_;
            }
            else
            {
                ArrayList arraylist = Lists.newArrayList();

                for (i1 = 0; i1 < k; ++i1)
                {
                    this.framesTextureData.add(this.prepareAnisotropicFiltering(getFrameTextureData(aint, j1, l, i1), j1, l));
                    arraylist.add(new AnimationFrame(i1, -1));
                }

                this.animationMetadata = new AnimationMetadataSection(arraylist, this.width, this.height, p_147964_2_.getFrameTime());
            }
        }
    }

    public void generateMipmaps(int p_147963_1_)
    {
        ArrayList arraylist = Lists.newArrayList();

        for (int j = 0; j < this.framesTextureData.size(); ++j)
        {
            final int[][] aint = (int[][])this.framesTextureData.get(j);

            if (aint != null)
            {
                try
                {
                    arraylist.add(TextureUtil.generateMipmapData(p_147963_1_, this.width, aint));
                }
                catch (Throwable throwable)
                {
                    CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Generating mipmaps for frame");
                    CrashReportCategory crashreportcategory = crashreport.makeCategory("Frame being iterated");
                    crashreportcategory.addCrashSection("Frame index", Integer.valueOf(j));
                    crashreportcategory.addCrashSectionCallable("Frame sizes", new Callable()
                    {
                        //private static final String __OBFID = "CL_00001063";
                        public String call()
                        {
                            StringBuilder stringbuilder = new StringBuilder();
                            int[][] aint1 = aint;
                            int k = aint1.length;

                            for (int l = 0; l < k; ++l)
                            {
                                int[] aint2 = aint1[l];

                                if (stringbuilder.length() > 0)
                                {
                                    stringbuilder.append(", ");
                                }

                                stringbuilder.append(aint2 == null ? "null" : Integer.valueOf(aint2.length));
                            }

                            return stringbuilder.toString();
                        }
                    });
                    throw new ReportedException(crashreport);
                }
            }
        }

        this.setFramesTextureData(arraylist);
    }

    private void fixTransparentPixels(int[][] p_147961_1_)
    {
        int[] aint1 = p_147961_1_[0];
        int i = 0;
        int j = 0;
        int k = 0;
        int l = 0;
        int i1;

        for (i1 = 0; i1 < aint1.length; ++i1)
        {
            if ((aint1[i1] & -16777216) != 0)
            {
                j += aint1[i1] >> 16 & 255;
                k += aint1[i1] >> 8 & 255;
                l += aint1[i1] >> 0 & 255;
                ++i;
            }
        }

        if (i != 0)
        {
            j /= i;
            k /= i;
            l /= i;

            for (i1 = 0; i1 < aint1.length; ++i1)
            {
                if ((aint1[i1] & -16777216) == 0)
                {
                    aint1[i1] = j << 16 | k << 8 | l;
                }
            }
        }
    }

    private int[][] prepareAnisotropicFiltering(int[][] p_147960_1_, int p_147960_2_, int p_147960_3_)
    {
//        if (!this.useAnisotropicFiltering)
//        {
            return p_147960_1_;
//        }
//        else
//        {
//            int[][] aint1 = new int[p_147960_1_.length][];
//
//            for (int k = 0; k < p_147960_1_.length; ++k)
//            {
//                int[] aint2 = p_147960_1_[k];
//
//                if (aint2 != null)
//                {
//                    int[] aint3 = new int[(p_147960_2_ + 16 >> k) * (p_147960_3_ + 16 >> k)];
//                    System.arraycopy(aint2, 0, aint3, 0, aint2.length);
//                    aint1[k] = TextureUtil.prepareAnisotropicData(aint3, p_147960_2_ >> k, p_147960_3_ >> k, 8 >> k);
//                }
//            }
//
//            return aint1;
//        }
    }

    private void allocateFrameTextureData(int p_130099_1_)
    {
        if (this.framesTextureData.size() <= p_130099_1_)
        {
            for (int j = this.framesTextureData.size(); j <= p_130099_1_; ++j)
            {
                this.framesTextureData.add((Object)null);
            }
        }
    }

    private static int[][] getFrameTextureData(int[][] p_147962_0_, int p_147962_1_, int p_147962_2_, int p_147962_3_)
    {
        int[][] aint1 = new int[p_147962_0_.length][];

        for (int l = 0; l < p_147962_0_.length; ++l)
        {
            int[] aint2 = p_147962_0_[l];

            if (aint2 != null)
            {
                aint1[l] = new int[(p_147962_1_ >> l) * (p_147962_2_ >> l)];
                System.arraycopy(aint2, p_147962_3_ * aint1[l].length, aint1[l], 0, aint1[l].length);
            }
        }

        return aint1;
    }

    public void clearFramesTextureData()
    {
        this.framesTextureData.clear();
    }

    public boolean hasAnimationMetadata()
    {
        return this.animationMetadata != null;
    }

    public void setFramesTextureData(List par1List)
    {
        this.framesTextureData = par1List;

        for (int i = 0; i < this.framesTextureData.size(); ++i)
        {
            int[][] datas = (int[][])((int[][])this.framesTextureData.get(i));

            if (datas != null && !this.iconName.startsWith("leaves_"))
            {
                for (int di = 0; di < datas.length; ++di)
                {
                    int[] data = datas[di];

                    if (data != null)
                    {
                        this.fixTransparentColor(data);
                    }
                }
            }
        }
    }

    private void resetSprite()
    {
        this.animationMetadata = null;
        this.setFramesTextureData(Lists.newArrayList());
        this.frameCounter = 0;
        this.tickCounter = 0;
        this.deleteOwnTexture();
        this.uploadedFrameIndex = -1;
        this.uploadedOwnFrameIndex = -1;
        this.frameBuffers = null;
        this.frameMipmaps = null;
    }

    public String toString()
    {
        return "TextureAtlasSprite{name=\'" + this.iconName + '\'' + ", frameCount=" + this.framesTextureData.size() + ", rotated=" + this.rotated + ", x=" + this.originX + ", y=" + this.originY + ", height=" + this.height + ", width=" + this.width + ", u0=" + this.minU + ", u1=" + this.maxU + ", v0=" + this.minV + ", v1=" + this.maxV + '}';
    }

    public int getWidth()
    {
        return this.width;
    }

    public int getHeight()
    {
        return this.height;
    }

    public int getIndexInMap()
    {
        return this.indexInMap;
    }

    public void setIndexInMap(int indexInMap)
    {
        this.indexInMap = indexInMap;
    }

    public void setMipmapActive(boolean mipmapActive)
    {
        this.mipmapActive = mipmapActive;
        this.frameMipmaps = null;
    }

    public void uploadFrameTexture()
    {
        this.uploadFrameTexture(this.frameCounter, this.originX, this.originY);
    }

    public void uploadFrameTexture(int frameIndex, int xPos, int yPos) {}

    private void uploadFrameMipmaps(int frameIndex, int xPos, int yPos) {}

    public void bindOwnTexture() {}

    public void bindUploadOwnTexture()
    {
        this.bindOwnTexture();
        this.uploadFrameTexture(this.frameCounter, 0, 0);
    }

    public void uploadOwnAnimation()
    {
        if (this.uploadedFrameIndex != this.uploadedOwnFrameIndex)
        {
            TextureUtil.bindTexture(this.glOwnTextureId);
            this.uploadFrameTexture(this.uploadedFrameIndex, 0, 0);
            this.uploadedOwnFrameIndex = this.uploadedFrameIndex;
        }
    }

    public void deleteOwnTexture()
    {
        if (this.glOwnTextureId >= 0)
        {
            GL11.glDeleteTextures(this.glOwnTextureId);
            this.glOwnTextureId = -1;
        }
    }

    private void fixTransparentColor(int[] data)
    {
        if (!this.isShadersSprite)
        {
            long redSum = 0L;
            long greenSum = 0L;
            long blueSum = 0L;
            long count = 0L;
            int redAvg;
            int greenAvg;
            int blueAvg;
            int colAvg;
            int i;
            int col;

            for (redAvg = 0; redAvg < data.length; ++redAvg)
            {
                greenAvg = data[redAvg];
                blueAvg = greenAvg >> 24 & 255;

                if (blueAvg >= 16)
                {
                    colAvg = greenAvg >> 16 & 255;
                    i = greenAvg >> 8 & 255;
                    col = greenAvg & 255;
                    redSum += (long)colAvg;
                    greenSum += (long)i;
                    blueSum += (long)col;
                    ++count;
                }
            }

            if (count > 0L)
            {
                redAvg = (int)(redSum / count);
                greenAvg = (int)(greenSum / count);
                blueAvg = (int)(blueSum / count);
                colAvg = redAvg << 16 | greenAvg << 8 | blueAvg;

                for (i = 0; i < data.length; ++i)
                {
                    col = data[i];
                    int alpha = col >> 24 & 255;

                    if (alpha <= 16)
                    {
                        data[i] = colAvg;
                    }
                }
            }
        }
    }

    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location)
    {
        return false;
    }

    public boolean load(IResourceManager manager, ResourceLocation location)
    {
        return true;
    }

    private void loadShadersSprites()
    {
        this.mipmapLevels = Config.getTextureMap().getMipmapLevels();
        String nameSpecular;
        ResourceLocation locSpecular;
        TextureAtlasSprite e;

        if (Shaders.configNormalMap)
        {
            nameSpecular = this.iconName + "_n";
            locSpecular = new ResourceLocation(nameSpecular);
            locSpecular = Config.getTextureMap().completeResourceLocation(locSpecular, 0);

            if (Config.hasResource(locSpecular))
            {
                try
                {
                    e = new TextureAtlasSprite(nameSpecular);
                    e.isShadersSprite = true;
                    e.copyFrom(this);
                    e.loadShaderSpriteFrames(locSpecular, this.mipmapLevels + 1);
                    e.generateMipmaps(this.mipmapLevels);
                    this.spriteNormal = e;
                }
                catch (IOException var5)
                {
                    Config.warn("Error loading normal texture: " + nameSpecular);
                    Config.warn(var5.getClass().getName() + ": " + var5.getMessage());
                }
            }
        }

        if (Shaders.configSpecularMap)
        {
            nameSpecular = this.iconName + "_s";
            locSpecular = new ResourceLocation(nameSpecular);
            locSpecular = Config.getTextureMap().completeResourceLocation(locSpecular, 0);

            if (Config.hasResource(locSpecular))
            {
                try
                {
                    e = new TextureAtlasSprite(nameSpecular);
                    e.isShadersSprite = true;
                    e.copyFrom(this);
                    e.loadShaderSpriteFrames(locSpecular, this.mipmapLevels + 1);
                    e.generateMipmaps(this.mipmapLevels);
                    this.spriteSpecular = e;
                }
                catch (IOException var4)
                {
                    Config.warn("Error loading specular texture: " + nameSpecular);
                    Config.warn(var4.getClass().getName() + ": " + var4.getMessage());
                }
            }
        }
    }

    public void loadShaderSpriteFrames(ResourceLocation loc, int mipmaplevels) throws IOException
    {
        IResource resource = Config.getResource(loc);
        BufferedImage bufferedimage = TextureUtils.readBufferedImage(resource.getInputStream());

        if (this.width != bufferedimage.getWidth())
        {
            bufferedimage = TextureUtils.scaleImage(bufferedimage, this.width);
        }

        AnimationMetadataSection animationmetadatasection = (AnimationMetadataSection)resource.getMetadata("animation");
        int[][] aint = new int[mipmaplevels][];
        aint[0] = new int[bufferedimage.getWidth() * bufferedimage.getHeight()];
        bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), aint[0], 0, bufferedimage.getWidth());

        if (animationmetadatasection == null)
        {
            this.framesTextureData.add(aint);
        }
        else
        {
            int i = bufferedimage.getHeight() / this.width;
            int k;

            if (animationmetadatasection.getFrameCount() > 0)
            {
                Iterator list = animationmetadatasection.getFrameIndexSet().iterator();

                while (list.hasNext())
                {
                    k = ((Integer)list.next()).intValue();

                    if (k >= i)
                    {
                        throw new RuntimeException("invalid frameindex " + k);
                    }

                    this.allocateFrameTextureData(k);
                    this.framesTextureData.set(k, getFrameTextureData(aint, this.width, this.width, k));
                }

                this.animationMetadata = animationmetadatasection;
            }
            else
            {
                ArrayList var10 = Lists.newArrayList();

                for (k = 0; k < i; ++k)
                {
                    this.framesTextureData.add(getFrameTextureData(aint, this.width, this.width, k));
                    var10.add(new AnimationFrame(k, -1));
                }

                this.animationMetadata = new AnimationMetadataSection(var10, this.width, this.height, animationmetadatasection.getFrameTime());
            }
        }
    }
}
