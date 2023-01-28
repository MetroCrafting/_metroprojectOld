package net.minecraft.client.renderer.texture;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.src.Mipmaps;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.IntBuffer;

@SideOnly(Side.CLIENT)
public class TextureUtil
{
    private static final Logger logger = LogManager.getLogger();
    private static final IntBuffer dataBuffer = GLAllocation.createDirectIntBuffer(4194304);
    public static final DynamicTexture missingTexture = new DynamicTexture(16, 16);
    public static final int[] missingTextureData = missingTexture.getTextureData();
    private static int field_147958_e = -1;
    private static int field_147956_f = -1;
    private static float field_152779_g = -1.0F;
    private static final int[] field_147957_g;

    public static int glGenTextures()
    {
        return GL11.glGenTextures();
    }

    public static void deleteTexture(int p_147942_0_)
    {
        GL11.glDeleteTextures(p_147942_0_);
    }

    public static int uploadTextureImage(int p_110987_0_, BufferedImage bufferedImage)
    {
        return uploadTextureImageAllocate(p_110987_0_, bufferedImage, false, false);
    }

    public static void uploadTexture(int texture, int[] pixels, int width, int height)
    {
        bindTexture(texture);
        uploadTextureSub(0, pixels, width, height, 0, 0, false, false, false);
    }

    public static int[][] generateMipmapData(int p_147949_0_, int p_147949_1_, int[][] p_147949_2_)
    {
        int[][] aint1 = new int[p_147949_0_ + 1][];
        aint1[0] = p_147949_2_[0];

        if (p_147949_0_ > 0)
        {
            boolean flag = false;
            int k;

            for (k = 0; k < p_147949_2_.length; ++k)
            {
                if (p_147949_2_[0][k] >> 24 == 0)
                {
                    flag = true;
                    break;
                }
            }

            for (k = 1; k <= p_147949_0_; ++k)
            {
                if (p_147949_2_[k] != null)
                {
                    aint1[k] = p_147949_2_[k];
                }
                else
                {
                    int[] aint2 = aint1[k - 1];
                    int[] aint3 = new int[aint2.length >> 2];
                    int l = p_147949_1_ >> k;
                    int i1 = aint3.length / l;
                    int j1 = l << 1;

                    for (int k1 = 0; k1 < l; ++k1)
                    {
                        for (int l1 = 0; l1 < i1; ++l1)
                        {
                            int i2 = 2 * (k1 + l1 * j1);
                            aint3[k1 + l1 * l] = func_147943_a(aint2[i2], aint2[i2 + 1], aint2[i2 + j1], aint2[i2 + 1 + j1], flag);
                        }
                    }

                    aint1[k] = aint3;
                }
            }
        }

        return aint1;
    }

    private static int func_147943_a(int p_147943_0_, int p_147943_1_, int p_147943_2_, int p_147943_3_, boolean p_147943_4_)
    {
        return Mipmaps.alphaBlend(p_147943_0_, p_147943_1_, p_147943_2_, p_147943_3_);
    }

    private static int func_147944_a(int p_147944_0_, int p_147944_1_, int p_147944_2_, int p_147944_3_, int p_147944_4_)
    {
        float f = (float)Math.pow((float)(p_147944_0_ >> p_147944_4_ & 255) / 255.0F, 2.2D);
        float f1 = (float)Math.pow((float)(p_147944_1_ >> p_147944_4_ & 255) / 255.0F, 2.2D);
        float f2 = (float)Math.pow((float)(p_147944_2_ >> p_147944_4_ & 255) / 255.0F, 2.2D);
        float f3 = (float)Math.pow((float)(p_147944_3_ >> p_147944_4_ & 255) / 255.0F, 2.2D);
        float f4 = (float)Math.pow((double)(f + f1 + f2 + f3) * 0.25D, 0.45454545454545453D);
        return (int)((double)f4 * 255.0D);
    }

    public static void uploadTextureMipmap(int[][] textureData, int width, int height, int xOffset, int yOffset, boolean linearFiltering, boolean clamped)
    {
        for (int i1 = 0; i1 < textureData.length; ++i1)
        {
            int[] aint1 = textureData[i1];
            uploadTextureSub(i1, aint1, width >> i1, height >> i1, xOffset >> i1, yOffset >> i1, linearFiltering, clamped, textureData.length > 1);
        }
    }

    private static void uploadTextureSub(int level, int[] textureData, int width, int height, int xOffset, int yOffset, boolean linearFiltering, boolean clamped, boolean hasMiMaps)
    {
        int j1 = 4194304 / width;
        setTextureFiltering(linearFiltering, hasMiMaps);
        setTextureClamped(clamped);
        int finalHeight;

        for (int k1 = 0; k1 < width * height; k1 += width * finalHeight)
        {
            int l1 = k1 / width;
            finalHeight = Math.min(j1, height - l1);
            int j2 = width * finalHeight;
            copyToBufferPos(textureData, k1, j2);
            GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, level, xOffset, yOffset + l1, width, finalHeight, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, dataBuffer);
        }
    }

    public static int uploadTextureImageAllocate(int texture, BufferedImage bufferedImage, boolean p_110989_2_, boolean p_110989_3_)
    {
        allocateTexture(texture, bufferedImage.getWidth(), bufferedImage.getHeight());
        return uploadTextureImageSub(texture, bufferedImage, 0, 0, p_110989_2_, p_110989_3_);
    }

    public static void allocateTexture(int texture, int width, int height)
    {
        allocateTextureImpl(texture, 0, width, height, 1.0F);
    }

    public static void allocateTextureImpl(int texture, int mipmapCount, int width, int height, float anisotropicValue)
    {
        synchronized(cpw.mods.fml.client.SplashProgress.class)
        {
            deleteTexture(texture);
            bindTexture(texture);
        }

        if (OpenGlHelper.anisotropicFilteringSupported)
        {
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, 34046, anisotropicValue);
        }

        if (mipmapCount > 0)
        {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, mipmapCount);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MIN_LOD, 0.0F);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LOD, (float)mipmapCount);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, 0.0F);
        }

        for (int i1 = 0; i1 <= mipmapCount; ++i1)
        {
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, i1, GL11.GL_RGBA, width >> i1, height >> i1, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, (IntBuffer)null);
        }
    }

    public static int uploadTextureImageSub(int p_110995_0_, BufferedImage bufferedImage, int width, int height, boolean linearFiltering, boolean clamped)
    {
        bindTexture(p_110995_0_);
        uploadTextureImageSubImpl(bufferedImage, width, height, linearFiltering, clamped);
        return p_110995_0_;
    }

    private static void uploadTextureImageSubImpl(BufferedImage bufferedImage, int width, int height, boolean linearFiltering, boolean clamped)
    {
        int k = bufferedImage.getWidth();
        int l = bufferedImage.getHeight();
        int i1 = 4194304 / k;
        int[] aint = new int[i1 * k];
        setTextureBlurred(linearFiltering);
        setTextureClamped(clamped);

        for (int j1 = 0; j1 < k * l; j1 += k * i1)
        {
            int k1 = j1 / k;
            int l1 = Math.min(i1, l - k1);
            int i2 = k * l1;
            bufferedImage.getRGB(0, k1, k, l1, aint, 0, k);
            copyToBuffer(aint, i2);
            GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, width, height + k1, k, l1, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, dataBuffer);
        }
    }

    public static void setTextureClamped(boolean clamped)
    {
        if (clamped)
        {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
        }
        else
        {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        }
    }

    private static void setTextureBlurred(boolean linearFiltering)
    {
        setTextureFiltering(linearFiltering, false);
    }

    public static void func_152777_a(boolean p_152777_0_, boolean p_152777_1_, float p_152777_2_)
    {
        field_147958_e = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER);
        field_147956_f = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER);
        field_152779_g = GL11.glGetTexParameterf(GL11.GL_TEXTURE_2D, 34046);
        setTextureFiltering(p_152777_0_, p_152777_1_);
        func_152778_a(p_152777_2_);
    }

    public static void func_147945_b()
    {
        if (field_147958_e >= 0 && field_147956_f >= 0 && field_152779_g >= 0.0F)
        {
            func_147952_b(field_147958_e, field_147956_f);
            func_152778_a(field_152779_g);
            field_152779_g = -1.0F;
            field_147958_e = -1;
            field_147956_f = -1;
        }
    }

    private static void func_147952_b(int p_147952_0_, int p_147952_1_)
    {
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, p_147952_0_);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, p_147952_1_);
    }

    private static void func_152778_a(float p_152778_0_)
    {
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, 34046, p_152778_0_);
    }

    public static void setTextureFiltering(boolean linearFiltering, boolean hasMipmaps)
    {
        if (linearFiltering)
        {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, hasMipmaps ? GL11.GL_LINEAR_MIPMAP_LINEAR : GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        }
        else
        {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, hasMipmaps ? GL11.GL_NEAREST_MIPMAP_LINEAR : GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        }
    }

    private static void copyToBuffer(int[] p_110990_0_, int p_110990_1_)
    {
        copyToBufferPos(p_110990_0_, 0, p_110990_1_);
    }

    private static void copyToBufferPos(int[] p_110994_0_, int p_110994_1_, int p_110994_2_)
    {
        int[] aint1 = p_110994_0_;

        if (Minecraft.getMinecraft().gameSettings.anaglyph)
        {
            aint1 = updateAnaglyph(p_110994_0_);
        }

        dataBuffer.clear();
        dataBuffer.put(aint1, p_110994_1_, p_110994_2_);
        dataBuffer.position(0).limit(p_110994_2_);
    }

    static void bindTexture(int p_94277_0_)
    {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, p_94277_0_);
    }

    public static int[] readImageData(IResourceManager par0ResourceManager, ResourceLocation par1ResourceLocation) throws IOException
    {
        BufferedImage var2 = ImageIO.read(par0ResourceManager.getResource(par1ResourceLocation).getInputStream());

        if (var2 == null)
        {
            return null;
        }
        else
        {
            int var3 = var2.getWidth();
            int var4 = var2.getHeight();
            int[] var5 = new int[var3 * var4];
            var2.getRGB(0, 0, var3, var4, var5, 0, var3);
            return var5;
        }
    }

    public static int[] updateAnaglyph(int[] p_110985_0_)
    {
        int[] aint1 = new int[p_110985_0_.length];

        for (int i = 0; i < p_110985_0_.length; ++i)
        {
            int j = p_110985_0_[i] >> 24 & 255;
            int k = p_110985_0_[i] >> 16 & 255;
            int l = p_110985_0_[i] >> 8 & 255;
            int i1 = p_110985_0_[i] & 255;
            int j1 = (k * 30 + l * 59 + i1 * 11) / 100;
            int k1 = (k * 30 + l * 70) / 100;
            int l1 = (k * 30 + i1 * 70) / 100;
            aint1[i] = j << 24 | j1 << 16 | k1 << 8 | l1;
        }

        return aint1;
    }

    public static int[] prepareAnisotropicData(int[] textureData, int p_147948_1_, int p_147948_2_, int p_147948_3_)
    {
        int l = p_147948_1_ + 2 * p_147948_3_;
        int i1;
        int j1;

        for (i1 = p_147948_2_ - 1; i1 >= 0; --i1)
        {
            j1 = i1 * p_147948_1_;
            int k1 = p_147948_3_ + (i1 + p_147948_3_) * l;
            int l1;

            for (l1 = 0; l1 < p_147948_3_; l1 += p_147948_1_)
            {
                int i2 = Math.min(p_147948_1_, p_147948_3_ - l1);
                System.arraycopy(textureData, j1 + p_147948_1_ - i2, textureData, k1 - l1 - i2, i2);
            }

            System.arraycopy(textureData, j1, textureData, k1, p_147948_1_);

            for (l1 = 0; l1 < p_147948_3_; l1 += p_147948_1_)
            {
                System.arraycopy(textureData, j1, textureData, k1 + p_147948_1_ + l1, Math.min(p_147948_1_, p_147948_3_ - l1));
            }
        }

        for (i1 = 0; i1 < p_147948_3_; i1 += p_147948_2_)
        {
            j1 = Math.min(p_147948_2_, p_147948_3_ - i1);
            System.arraycopy(textureData, (p_147948_3_ + p_147948_2_ - j1) * l, textureData, (p_147948_3_ - i1 - j1) * l, l * j1);
        }

        for (i1 = 0; i1 < p_147948_3_; i1 += p_147948_2_)
        {
            j1 = Math.min(p_147948_2_, p_147948_3_ - i1);
            System.arraycopy(textureData, p_147948_3_ * l, textureData, (p_147948_2_ + p_147948_3_ + i1) * l, l * j1);
        }

        return textureData;
    }

    public static void func_147953_a(int[] p_147953_0_, int p_147953_1_, int p_147953_2_)
    {
        int[] aint1 = new int[p_147953_1_];
        int k = p_147953_2_ / 2;

        for (int l = 0; l < k; ++l)
        {
            System.arraycopy(p_147953_0_, l * p_147953_1_, aint1, 0, p_147953_1_);
            System.arraycopy(p_147953_0_, (p_147953_2_ - 1 - l) * p_147953_1_, p_147953_0_, l * p_147953_1_, p_147953_1_);
            System.arraycopy(aint1, 0, p_147953_0_, (p_147953_2_ - 1 - l) * p_147953_1_, p_147953_1_);
        }
    }

    public static void allocateTextureImpl(int p_147946_0_, int p_147946_1_, int p_147946_2_, int p_147946_3_)
    {
        allocateTextureImpl(p_147946_0_, p_147946_1_, p_147946_2_, p_147946_3_, 1.0F);
    }

    static
    {
        int color1 = 16777216;
        int color2 = 524040;
        int[] colors1 = new int[] { -color2, -color2, -color2, -color2, -color2, -color2, -color2, -color2};
        int[] colors2 = new int[] { -color1, -color1, -color1, -color1, -color1, -color1, -color1, -color1};
        int length = colors1.length;

        for (int i = 0; i < 16; ++i)
        {
            System.arraycopy(i < length ? colors1 : colors2, 0, missingTextureData, 16 * i, length);
            System.arraycopy(i < length ? colors2 : colors1, 0, missingTextureData, 16 * i + length, length);
        }

        missingTexture.updateDynamicTexture();
        field_147957_g = new int[4];
    }
}