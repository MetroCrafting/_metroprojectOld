package net.minecraft.client.resources;

import com.google.common.collect.ImmutableSet;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class DefaultResourcePack implements IResourcePack
{
    public static final Set DEFAULT_RESOURCE_DOMAINS = ImmutableSet.of("minecraft", "realms");
    private final Map resources;
    private static final String __OBFID = "CL_00001073";

    public DefaultResourcePack(Map p_i1046_1_)
    {
        this.resources = p_i1046_1_;
    }

    public InputStream getInputStream(ResourceLocation p_110590_1_) throws IOException
    {
        InputStream inputstream = this.getResourceStream(p_110590_1_);

        if (inputstream != null)
        {
            return inputstream;
        }
        else
        {
            InputStream inputstream1 = this.getInputStreamAssets(p_110590_1_);

            if (inputstream1 != null)
            {
                return inputstream1;
            }
            else
            {
                throw new FileNotFoundException(p_110590_1_.getResourcePath());
            }
        }
    }

    public InputStream getInputStreamAssets(ResourceLocation p_152780_1_) throws IOException
    {
        File file1 = (File)this.resources.get(p_152780_1_.toString()); // field_152781_b is this.resources
        return file1 != null && file1.isFile() ? new FileInputStream(file1) : null;
    }

    private InputStream getResourceStream(ResourceLocation resLoc)
    {
        return DefaultResourcePack.class.getResourceAsStream("/assets/" + resLoc.getResourceDomain() + "/" + resLoc.getResourcePath());
    }

    public boolean resourceExists(ResourceLocation par1ResourceLocation) {
        return this.getResourceStream(par1ResourceLocation) != null || this.resources.containsKey(par1ResourceLocation.toString());
  }

    public Set getResourceDomains() 
    {
        return DEFAULT_RESOURCE_DOMAINS;
    }

    public IMetadataSection getPackMetadata(IMetadataSerializer par1MetadataSerializer, String par2Str) throws IOException {
        try {
              FileInputStream var3 = new FileInputStream((File)this.resources.get("pack.mcmeta"));
              return AbstractResourcePack.readMetadata(par1MetadataSerializer, var3, par2Str);
        } catch (RuntimeException var4) {
              return null;
        } catch (FileNotFoundException var5) {
              return null;
        }
  }

    public BufferedImage getPackImage() throws IOException {
        return ImageIO.read(DefaultResourcePack.class.getResourceAsStream("/" + (new ResourceLocation("pack.png")).getResourcePath()));
  }

    public String getPackName()
    {
        return "Default";
    }
}