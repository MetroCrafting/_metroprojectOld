package net.minecraft.src;

import java.util.List;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

public class WrUpdaterSmooth implements IWrUpdater {
      private long lastUpdateStartTimeNs = 0L;
      private long updateStartTimeNs = 0L;
      private long updateTimeNs = 10000000L;
      private WorldRendererSmooth currentUpdateRenderer = null;
      private int renderersUpdated = 0;
      private int renderersFound = 0;

      public void initialize() {
      }

      public void terminate() {
      }

      public WorldRenderer makeWorldRenderer(World worldObj, List tileEntities, int x, int y, int z, int glRenderListBase) {
            return new WorldRendererSmooth(worldObj, tileEntities, x, y, z, glRenderListBase);
      }

      public boolean updateRenderers(RenderGlobal rg, EntityLivingBase entityliving, boolean flag) {
            this.lastUpdateStartTimeNs = this.updateStartTimeNs;
            this.updateStartTimeNs = System.nanoTime();
            long finishTimeNs = this.updateStartTimeNs + this.updateTimeNs;
            int maxNum = Config.getUpdatesPerFrame();
            if (Config.isDynamicUpdates() && !rg.isMoving(entityliving)) {
                  maxNum *= 3;
            }

            this.renderersUpdated = 0;

            do {
                  this.renderersFound = 0;
                  this.updateRenderersImpl(rg, entityliving, flag);
            } while(this.renderersFound > 0 && System.nanoTime() - finishTimeNs < 0L);

            if (this.renderersFound > 0) {
                  maxNum = Math.min(maxNum, this.renderersFound);
                  long diff = 400000L;
                  if (this.renderersUpdated > maxNum) {
                        this.updateTimeNs -= 2L * diff;
                  }

                  if (this.renderersUpdated < maxNum) {
                        this.updateTimeNs += diff;
                  }
            } else {
                  this.updateTimeNs = 0L;
                  this.updateTimeNs -= 200000L;
            }

            if (this.updateTimeNs < 0L) {
                  this.updateTimeNs = 0L;
            }

            return this.renderersUpdated > 0;
      }

      private void updateRenderersImpl(RenderGlobal rg, EntityLivingBase entityliving, boolean flag) {
            this.renderersFound = 0;
            boolean currentUpdateFinished = true;
            if (this.currentUpdateRenderer != null) {
                  ++this.renderersFound;
                  currentUpdateFinished = this.updateRenderer(this.currentUpdateRenderer);
                  if (currentUpdateFinished) {
                        ++this.renderersUpdated;
                  }
            }

            if (rg.worldRenderersToUpdate.size() > 0) {
                  int NOT_IN_FRUSTRUM_MUL = 4;
                  WorldRendererSmooth wrBest = null;
                  float distSqBest = Float.MAX_VALUE;
                  int indexBest = -1;

                  int dstIndex;
                  for(dstIndex = 0; dstIndex < rg.worldRenderersToUpdate.size(); ++dstIndex) {
                        WorldRendererSmooth wr = (WorldRendererSmooth)rg.worldRenderersToUpdate.get(dstIndex);
                        if (wr != null) {
                              ++this.renderersFound;
                              if (!wr.needsUpdate) {
                                    rg.worldRenderersToUpdate.set(dstIndex, (Object)null);
                              } else {
                                    float distSq = wr.distanceToEntitySquared(entityliving);
                                    if (distSq <= 256.0F && rg.isActingNow()) {
                                          wr.updateRenderer();
                                          wr.needsUpdate = false;
                                          rg.worldRenderersToUpdate.set(dstIndex, (Object)null);
                                          ++this.renderersUpdated;
                                    } else {
                                          if (!wr.isInFrustum) {
                                                distSq *= (float)NOT_IN_FRUSTRUM_MUL;
                                          }

                                          if (wrBest == null) {
                                                wrBest = wr;
                                                distSqBest = distSq;
                                                indexBest = dstIndex;
                                          } else if (distSq < distSqBest) {
                                                wrBest = wr;
                                                distSqBest = distSq;
                                                indexBest = dstIndex;
                                          }
                                    }
                              }
                        }
                  }

                  if (this.currentUpdateRenderer == null || currentUpdateFinished) {
                        int i;
                        if (wrBest != null) {
                              rg.worldRenderersToUpdate.set(indexBest, (Object)null);
                              if (!this.updateRenderer(wrBest)) {
                                    return;
                              }

                              ++this.renderersUpdated;
                              if (System.nanoTime() > this.updateStartTimeNs + this.updateTimeNs) {
                                    return;
                              }

                              float maxDiffDistSq = distSqBest / 5.0F;

                              for(i = 0; i < rg.worldRenderersToUpdate.size(); ++i) {
                                    WorldRendererSmooth wr = (WorldRendererSmooth)rg.worldRenderersToUpdate.get(i);
                                    if (wr != null) {
                                          float distSq = wr.distanceToEntitySquared(entityliving);
                                          if (!wr.isInFrustum) {
                                                distSq *= (float)NOT_IN_FRUSTRUM_MUL;
                                          }

                                          float diffDistSq = Math.abs(distSq - distSqBest);
                                          if (diffDistSq < maxDiffDistSq) {
                                                rg.worldRenderersToUpdate.set(i, (Object)null);
                                                if (!this.updateRenderer(wr)) {
                                                      return;
                                                }

                                                ++this.renderersUpdated;
                                                if (System.nanoTime() > this.updateStartTimeNs + this.updateTimeNs) {
                                                      break;
                                                }
                                          }
                                    }
                              }
                        }

                        if (this.renderersFound == 0) {
                              rg.worldRenderersToUpdate.clear();
                        }

                        if (rg.worldRenderersToUpdate.size() > 100 && this.renderersFound < rg.worldRenderersToUpdate.size() * 4 / 5) {
                              dstIndex = 0;

                              for(i = 0; i < rg.worldRenderersToUpdate.size(); ++i) {
                                    Object wr = rg.worldRenderersToUpdate.get(i);
                                    if (wr != null) {
                                          if (i != dstIndex) {
                                                rg.worldRenderersToUpdate.set(dstIndex, wr);
                                          }

                                          ++dstIndex;
                                    }
                              }

                              for(i = rg.worldRenderersToUpdate.size() - 1; i >= dstIndex; --i) {
                                    rg.worldRenderersToUpdate.remove(i);
                              }
                        }

                  }
            }
      }

      private boolean updateRenderer(WorldRendererSmooth wr) {
            long finishTime = this.updateStartTimeNs + this.updateTimeNs;
            wr.needsUpdate = false;
            boolean ready = wr.updateRenderer(finishTime);
            if (!ready) {
                  this.currentUpdateRenderer = wr;
                  return false;
            } else {
                  wr.finishUpdate();
                  this.currentUpdateRenderer = null;
                  return true;
            }
      }

      public void finishCurrentUpdate() {
            if (this.currentUpdateRenderer != null) {
                  this.currentUpdateRenderer.updateRenderer();
                  this.currentUpdateRenderer = null;
            }
      }

      public void resumeBackgroundUpdates() {
      }

      public void pauseBackgroundUpdates() {
      }

      public void preRender(RenderGlobal rg, EntityLivingBase player) {
      }

      public void postRender() {
      }

      public void clearAllUpdates() {
            this.finishCurrentUpdate();
      }
}
