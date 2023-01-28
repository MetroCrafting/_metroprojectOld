package net.minecraft.src;

import java.util.List;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.Drawable;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;

public class WrUpdaterThreaded implements IWrUpdater {
      private WrUpdateThread updateThread = null;
      private float timePerUpdateMs = 10.0F;
      private long updateStartTimeNs = 0L;
      private boolean firstUpdate = true;
      private int updateTargetNum = 0;

      public void terminate() {
            if (this.updateThread != null) {
                  this.updateThread.terminate();
                  this.updateThread.unpauseToEndOfUpdate();
            }
      }

      public void initialize() {
      }

      private void delayedInit() {
            if (this.updateThread == null) {
                  this.createUpdateThread(Display.getDrawable());
            }
      }

      public WorldRenderer makeWorldRenderer(World worldObj, List tileEntities, int x, int y, int z, int glRenderListBase) {
            return new WorldRendererThreaded(worldObj, tileEntities, x, y, z, glRenderListBase);
      }

      public WrUpdateThread createUpdateThread(Drawable displayDrawable) {
            if (this.updateThread != null) {
                  throw new IllegalStateException("UpdateThread is already existing");
            } else {
                  try {
                        Pbuffer pbuffer = new Pbuffer(1, 1, new PixelFormat(), displayDrawable);
                        this.updateThread = new WrUpdateThread(pbuffer);
                        this.updateThread.setPriority(1);
                        this.updateThread.start();
                        this.updateThread.pause();
                        return this.updateThread;
                  } catch (Exception var3) {
                        throw new RuntimeException(var3);
                  }
            }
      }

      public boolean isUpdateThread() {
            return Thread.currentThread() == this.updateThread;
      }

      public static boolean isBackgroundChunkLoading() {
            return true;
      }

      public void preRender(RenderGlobal rg, EntityLivingBase player) {
            this.updateTargetNum = 0;
            if (this.updateThread != null) {
                  if (this.updateStartTimeNs == 0L) {
                        this.updateStartTimeNs = System.nanoTime();
                  }

                  if (this.updateThread.hasWorkToDo()) {
                        this.updateTargetNum = Config.getUpdatesPerFrame();
                        if (Config.isDynamicUpdates() && !rg.isMoving(player)) {
                              this.updateTargetNum *= 3;
                        }

                        this.updateTargetNum = Math.min(this.updateTargetNum, this.updateThread.getPendingUpdatesCount());
                        if (this.updateTargetNum > 0) {
                              this.updateThread.unpause();
                        }
                  }
            }

      }

      public void postRender() {
            if (this.updateThread != null) {
                  float sleepTimeMs = 0.0F;
                  if (this.updateTargetNum > 0) {
                        long renderTimeNs = System.nanoTime() - this.updateStartTimeNs;
                        float targetRunTime = this.timePerUpdateMs * (1.0F + (float)(this.updateTargetNum - 1) / 2.0F);
                        if (targetRunTime > 0.0F) {
                              int sleepTimeMsInt = (int)targetRunTime;
                              Config.sleep((long)sleepTimeMsInt);
                        }

                        this.updateThread.pause();
                  }

                  float deltaTime = 0.2F;
                  if (this.updateTargetNum > 0) {
                        int updateCount = this.updateThread.resetUpdateCount();
                        if (updateCount < this.updateTargetNum) {
                              this.timePerUpdateMs += deltaTime;
                        }

                        if (updateCount > this.updateTargetNum) {
                              this.timePerUpdateMs -= deltaTime;
                        }

                        if (updateCount == this.updateTargetNum) {
                              this.timePerUpdateMs -= deltaTime;
                        }
                  } else {
                        this.timePerUpdateMs -= deltaTime / 5.0F;
                  }

                  if (this.timePerUpdateMs < 0.0F) {
                        this.timePerUpdateMs = 0.0F;
                  }

                  this.updateStartTimeNs = System.nanoTime();
            }

      }

      public boolean updateRenderers(RenderGlobal rg, EntityLivingBase entityliving, boolean flag) {
            this.delayedInit();
            if (rg.worldRenderersToUpdate.size() <= 0) {
                  return true;
            } else {
                  int num = 0;
                  int NOT_IN_FRUSTRUM_MUL = 4;
                  int numValid = 0;
                  WorldRenderer wrBest = null;
                  float distSqBest = Float.MAX_VALUE;
                  int indexBest = -1;

                  int i;
                  float maxDiffDistSq;
                  for(i = 0; i < rg.worldRenderersToUpdate.size(); ++i) {
                        WorldRenderer wr = (WorldRenderer)rg.worldRenderersToUpdate.get(i);
                        if (wr != null) {
                              ++numValid;
                              if (!wr.isUpdating) {
                                    if (!wr.needsUpdate) {
                                          rg.worldRenderersToUpdate.set(i, (Object)null);
                                    } else {
                                          maxDiffDistSq = wr.distanceToEntitySquared(entityliving);
                                          if (maxDiffDistSq < 512.0F) {
                                                if (maxDiffDistSq < 256.0F && rg.isActingNow() && wr.isInFrustum || this.firstUpdate) {
                                                      if (this.updateThread != null) {
                                                            this.updateThread.unpauseToEndOfUpdate();
                                                      }

                                                      wr.updateRenderer(entityliving);
                                                      wr.needsUpdate = false;
                                                      rg.worldRenderersToUpdate.set(i, (Object)null);
                                                      ++num;
                                                      continue;
                                                }

                                                if (this.updateThread != null) {
                                                      this.updateThread.addRendererToUpdate(wr, true);
                                                      wr.needsUpdate = false;
                                                      rg.worldRenderersToUpdate.set(i, (Object)null);
                                                      ++num;
                                                      continue;
                                                }
                                          }

                                          if (!wr.isInFrustum) {
                                                maxDiffDistSq *= (float)NOT_IN_FRUSTRUM_MUL;
                                          }

                                          if (wrBest == null) {
                                                wrBest = wr;
                                                distSqBest = maxDiffDistSq;
                                                indexBest = i;
                                          } else if (maxDiffDistSq < distSqBest) {
                                                wrBest = wr;
                                                distSqBest = maxDiffDistSq;
                                                indexBest = i;
                                          }
                                    }
                              }
                        }
                  }

                  i = Config.getUpdatesPerFrame();
                  boolean turboMode = false;
                  if (Config.isDynamicUpdates() && !rg.isMoving(entityliving)) {
                        i *= 3;
                        turboMode = true;
                  }

                  if (this.updateThread != null) {
                        i = this.updateThread.getUpdateCapacity();
                        if (i <= 0) {
                              return true;
                        }
                  }

                  int srcIndex;
                  if (wrBest != null) {
                        this.updateRenderer(wrBest, entityliving);
                        rg.worldRenderersToUpdate.set(indexBest, (Object)null);
                        ++num;
                        maxDiffDistSq = distSqBest / 5.0F;

                        for(srcIndex = 0; srcIndex < rg.worldRenderersToUpdate.size() && num < i; ++srcIndex) {
                              WorldRenderer wr = (WorldRenderer)rg.worldRenderersToUpdate.get(srcIndex);
                              if (wr != null && !wr.isUpdating) {
                                    float distSq = wr.distanceToEntitySquared(entityliving);
                                    if (!wr.isInFrustum) {
                                          distSq *= (float)NOT_IN_FRUSTRUM_MUL;
                                    }

                                    float diffDistSq = Math.abs(distSq - distSqBest);
                                    if (diffDistSq < maxDiffDistSq) {
                                          this.updateRenderer(wr, entityliving);
                                          rg.worldRenderersToUpdate.set(srcIndex, (Object)null);
                                          ++num;
                                    }
                              }
                        }
                  }

                  if (numValid == 0) {
                        rg.worldRenderersToUpdate.clear();
                  }

                  if (rg.worldRenderersToUpdate.size() > 100 && numValid < rg.worldRenderersToUpdate.size() * 4 / 5) {
                        int dstIndex = 0;

                        for(srcIndex = 0; srcIndex < rg.worldRenderersToUpdate.size(); ++srcIndex) {
                              Object wr = rg.worldRenderersToUpdate.get(srcIndex);
                              if (wr != null) {
                                    if (srcIndex != dstIndex) {
                                          rg.worldRenderersToUpdate.set(dstIndex, wr);
                                    }

                                    ++dstIndex;
                              }
                        }

                        for(srcIndex = rg.worldRenderersToUpdate.size() - 1; srcIndex >= dstIndex; --srcIndex) {
                              rg.worldRenderersToUpdate.remove(srcIndex);
                        }
                  }

                  this.firstUpdate = false;
                  return true;
            }
      }

      private void updateRenderer(WorldRenderer wr, EntityLivingBase entityLiving) {
            WrUpdateThread ut = this.updateThread;
            if (ut != null) {
                  ut.addRendererToUpdate(wr, false);
                  wr.needsUpdate = false;
            } else {
                  wr.updateRenderer(entityLiving);
                  wr.needsUpdate = false;
                  wr.isUpdating = false;
            }
      }

      public void finishCurrentUpdate() {
            if (this.updateThread != null) {
                  this.updateThread.unpauseToEndOfUpdate();
            }

      }

      public void resumeBackgroundUpdates() {
            if (this.updateThread != null) {
                  this.updateThread.unpause();
            }

      }

      public void pauseBackgroundUpdates() {
            if (this.updateThread != null) {
                  this.updateThread.pause();
            }

      }

      public void clearAllUpdates() {
            if (this.updateThread != null) {
                  this.updateThread.clearAllUpdates();
            }

            this.firstUpdate = true;
      }
}
