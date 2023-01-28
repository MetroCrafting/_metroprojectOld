package net.minecraft.src;

import java.io.File;
import net.minecraft.client.resources.AbstractResourcePack;

public class ResourceUtils {
    public static File getResourcePackFile(AbstractResourcePack arp)
    {
        return arp.resourcePackFile;
    }
}
