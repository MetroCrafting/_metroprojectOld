package net.minecraft.client.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.File;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ImageBufferDownload;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.client.resources.SkinManager.SkinAvailableCallback;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.src.CapeUtils;
import net.minecraft.src.Config;
import net.minecraft.src.PlayerConfigurations;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public abstract class AbstractClientPlayer extends EntityPlayer implements SkinManager.SkinAvailableCallback
{
    public static final ResourceLocation locationStevePng = new ResourceLocation("textures/entity/steve.png");
    private ResourceLocation locationSkin;
    private ResourceLocation locationCape;
    private static final String __OBFID = "CL_00000935";
    private ResourceLocation locationOfCape = null;
    private String nameClear = null;

    public AbstractClientPlayer(World p_i45074_1_, GameProfile p_i45074_2_) {
        super(p_i45074_1_, p_i45074_2_);
        String var3 = this.getCommandSenderName();
        if (!var3.isEmpty()) {
              SkinManager var4 = Minecraft.getMinecraft().func_152342_ad();
              var4.func_152790_a(p_i45074_2_, this, true);
        }

        this.nameClear = p_i45074_2_.getName();
        if (this.nameClear != null && !this.nameClear.isEmpty()) {
              this.nameClear = StringUtils.stripControlCodes(this.nameClear);
        }

        CapeUtils.downloadCape(this);
        PlayerConfigurations.getPlayerConfiguration(this);
  }

    public boolean func_152122_n() {
        if (!Config.isShowCapes()) {
              return false;
        } else if (this.locationOfCape != null) {
              return true;
        } else {
              return this.locationCape != null;
        }
  }

    public boolean func_152123_o()
    {
        return this.locationSkin != null;
    }

    public ResourceLocation getLocationSkin()
    {
        return this.locationSkin == null ? locationStevePng : this.locationSkin;
    }

    public ResourceLocation getLocationCape() {
        if (!Config.isShowCapes()) {
              return null;
        } else {
              return this.locationOfCape != null ? this.locationOfCape : this.locationCape;
        }
  }
    public static ThreadDownloadImageData getDownloadImageSkin(ResourceLocation par0ResourceLocation, String par1Str) {
        TextureManager var2 = Minecraft.getMinecraft().getTextureManager();
        Object var3 = var2.getTexture(par0ResourceLocation);
        if (var3 == null) {
              var3 = new ThreadDownloadImageData((File)null, String.format("http://skins.minecraft.net/MinecraftSkins/%s.png", StringUtils.stripControlCodes(par1Str)), locationStevePng, new ImageBufferDownload());
              var2.loadTexture(par0ResourceLocation, (ITextureObject)var3);
        }

        return (ThreadDownloadImageData)var3;
  }

    public static ResourceLocation getLocationSkin(String par0Str) {
        return new ResourceLocation("skins/" + StringUtils.stripControlCodes(par0Str));
  }

    public void func_152121_a(Type p_152121_1_, ResourceLocation p_152121_2_) {
        switch(p_152121_1_) {
        case SKIN:
              this.locationSkin = p_152121_2_;
              break;
        case CAPE:
              this.locationCape = p_152121_2_;
        }

  }

    public String getNameClear() {
        return this.nameClear;
    }

    public ResourceLocation getLocationOfCape() {
        return this.locationOfCape;
    }

    public void setLocationOfCape(ResourceLocation locationOfCape) {
        this.locationOfCape = locationOfCape;
    }
    
    @SideOnly(Side.CLIENT)

    static final class SwitchType
        {
            static final int[] field_152630_a = new int[Type.values().length];
            private static final String __OBFID = "CL_00001832";

            static
            {
                try
                {
                    field_152630_a[Type.SKIN.ordinal()] = 1;
                }
                catch (NoSuchFieldError var2)
                {
                    ;
                }

                try
                {
                    field_152630_a[Type.CAPE.ordinal()] = 2;
                }
                catch (NoSuchFieldError var1)
                {
                    ;
                }
            }
        }
}