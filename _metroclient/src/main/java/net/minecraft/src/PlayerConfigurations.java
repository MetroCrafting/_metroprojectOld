package net.minecraft.src;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;

public class PlayerConfigurations {
      private static Map mapConfigurations = null;
      private static boolean reloadPlayerItems = Boolean.getBoolean("player.models.reload");
      private static long timeReloadPlayerItemsMs = System.currentTimeMillis();

      public static void renderPlayerItems(ModelBiped modelBiped, AbstractClientPlayer player, float scale, float partialTicks) {
            PlayerConfiguration cfg = getPlayerConfiguration(player);
            if (cfg != null) {
                  cfg.renderPlayerItems(modelBiped, player, scale, partialTicks);
            }

      }

      public static synchronized PlayerConfiguration getPlayerConfiguration(AbstractClientPlayer player) {
            if (reloadPlayerItems && System.currentTimeMillis() > timeReloadPlayerItemsMs + 5000L) {
                  AbstractClientPlayer currentPlayer = Minecraft.getMinecraft().thePlayer;
                  if (currentPlayer != null) {
                        setPlayerConfiguration(currentPlayer.getNameClear(), (PlayerConfiguration)null);
                        timeReloadPlayerItemsMs = System.currentTimeMillis();
                  }
            }

            String name = player.getNameClear();
            if (name == null) {
                  return null;
            } else {
                  PlayerConfiguration pc = (PlayerConfiguration)getMapConfigurations().get(name);
                  if (pc == null) {
                        pc = new PlayerConfiguration();
                        getMapConfigurations().put(name, pc);
                        PlayerConfigurationReceiver pcl = new PlayerConfigurationReceiver(name);
                        String url = HttpUtils.getPlayerItemsUrl() + "/users/" + name + ".cfg";
                        FileDownloadThread fdt = new FileDownloadThread(url, pcl);
                        fdt.start();
                  }

                  return pc;
            }
      }

      public static synchronized void setPlayerConfiguration(String player, PlayerConfiguration pc) {
            getMapConfigurations().put(player, pc);
      }

      private static Map getMapConfigurations() {
            if (mapConfigurations == null) {
                  mapConfigurations = new HashMap();
            }

            return mapConfigurations;
      }
}
