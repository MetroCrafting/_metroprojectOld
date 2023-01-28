package net.minecraft.src;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunkProvider;

public class ClearWater {
      public static void updateWaterOpacity(GameSettings settings, World world) {
            if (settings != null) {
                  int opacity = 3;
                  if (settings.ofClearWater) {
                        opacity = 1;
                  }

                  BlockUtils.setLightOpacity(Blocks.water, opacity);
                  BlockUtils.setLightOpacity(Blocks.flowing_water, opacity);
            }

            if (world != null) {
                  IChunkProvider cp = world.getChunkProvider();
                  if (cp != null) {
                        Entity rve = Config.getMinecraft().renderViewEntity;
                        if (rve != null) {
                              int cViewX = (int)rve.posX / 16;
                              int cViewZ = (int)rve.posZ / 16;
                              int cXMin = cViewX - 512;
                              int cXMax = cViewX + 512;
                              int cZMin = cViewZ - 512;
                              int cZMax = cViewZ + 512;
                              int countUpdated = 0;

                              for(int cx = cXMin; cx < cXMax; ++cx) {
                                    for(int cz = cZMin; cz < cZMax; ++cz) {
                                          if (cp.chunkExists(cx, cz)) {
                                                Chunk c = cp.provideChunk(cx, cz);
                                                if (c != null && !(c instanceof EmptyChunk)) {
                                                      int x0 = cx << 4;
                                                      int z0 = cz << 4;
                                                      int x1 = x0 + 16;
                                                      int z1 = z0 + 16;

                                                      for(int x = x0; x < x1; ++x) {
                                                            for(int z = z0; z < z1; ++z) {
                                                                  int posH = world.getPrecipitationHeight(x, z);

                                                                  for(int y = 0; y < posH; ++y) {
                                                                        Block block = world.getBlock(x, y, z);
                                                                        if (block.getMaterial() == Material.water) {
                                                                              world.markBlocksDirtyVertical(x, z, y, posH);
                                                                              ++countUpdated;
                                                                              break;
                                                                        }
                                                                  }
                                                            }
                                                      }
                                                }
                                          }
                                    }
                              }

                              if (countUpdated > 0) {
                                    String threadName = "server";
                                    if (Config.isMinecraftThread()) {
                                          threadName = "client";
                                    }

                                    Config.dbg("ClearWater (" + threadName + ") relighted " + countUpdated + " chunks");
                              }

                        }
                  }
            }
      }
}
