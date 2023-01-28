package net.minecraft.src;

import java.awt.image.BufferedImage;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.ImageBufferDownload;
import net.minecraft.util.ResourceLocation;

public class CapeImageBuffer extends ImageBufferDownload {
      private AbstractClientPlayer player;
      private ResourceLocation resourceLocation;

      public CapeImageBuffer(AbstractClientPlayer player, ResourceLocation resourceLocation) {
            this.player = player;
            this.resourceLocation = resourceLocation;
      }

      public BufferedImage parseUserSkin(BufferedImage var1) {
            return CapeUtils.parseCape(var1);
      }

      public void func_152634_a() {
            if (this.player != null) {
                  this.player.setLocationOfCape(this.resourceLocation);
            }

      }

      public void cleanup() {
            this.player = null;
      }
}
