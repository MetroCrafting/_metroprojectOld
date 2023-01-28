package net.minecraft.src;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.ISaveHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldServerOF extends WorldServer {
      private NextTickHashSet pendingTickListEntriesHashSet;
      private TreeSet pendingTickListEntriesTreeSet;
      private List pendingTickListEntriesThisTick = new ArrayList();
      private int lastViewDistance = 0;
      private boolean allChunksTicked = false;
      public Set setChunkCoordsToTickOnce = new HashSet();
      private Set limitedChunkSet = new HashSet();
      private static final Logger logger = LogManager.getLogger();

      public WorldServerOF(MinecraftServer par1MinecraftServer, ISaveHandler par2iSaveHandler, String par3Str, int par4, WorldSettings par5WorldSettings, Profiler par6Profiler) {
            super(par1MinecraftServer, par2iSaveHandler, par3Str, par4, par5WorldSettings, par6Profiler);
            this.fixSetNextTicks();
      }

      protected void initialize(WorldSettings par1WorldSettings) {
            super.initialize(par1WorldSettings);
            this.fixSetNextTicks();
      }

      private void fixSetNextTicks() {
            try {
                  Field[] fields = WorldServer.class.getDeclaredFields();
                  int posSet = this.findField(fields, Set.class, 0);
                  int posTreeSet = this.findField(fields, TreeSet.class, posSet);
                  int posList = this.findField(fields, List.class, posTreeSet);
                  if (posSet >= 0 && posTreeSet >= 0 && posList >= 0) {
                        Field fieldSet = fields[posSet];
                        Field fieldTreeSet = fields[posTreeSet];
                        Field fieldList = fields[posList];
                        fieldSet.setAccessible(true);
                        fieldTreeSet.setAccessible(true);
                        fieldList.setAccessible(true);
                        this.pendingTickListEntriesTreeSet = (TreeSet)fieldTreeSet.get(this);
                        this.pendingTickListEntriesThisTick = (List)fieldList.get(this);
                        Set oldSet = (Set)fieldSet.get(this);
                        if (oldSet instanceof NextTickHashSet) {
                              return;
                        }

                        this.pendingTickListEntriesHashSet = new NextTickHashSet(oldSet);
                        fieldSet.set(this, this.pendingTickListEntriesHashSet);
                        Config.dbg("WorldServer.nextTickSet updated");
                        return;
                  }

                  Config.warn("Error updating WorldServer.nextTickSet");
            } catch (Exception var9) {
                  Config.warn("Error setting WorldServer.nextTickSet: " + var9.getMessage());
            }

      }

      private int findField(Field[] fields, Class cls, int startPos) {
            if (startPos < 0) {
                  return -1;
            } else {
                  for(int i = startPos; i < fields.length; ++i) {
                        Field field = fields[i];
                        if (field.getType() == cls) {
                              return i;
                        }
                  }

                  return -1;
            }
      }

      public List getPendingBlockUpdates(Chunk par1Chunk, boolean par2) {
            if (this.pendingTickListEntriesHashSet != null && this.pendingTickListEntriesTreeSet != null && this.pendingTickListEntriesThisTick != null) {
                  ArrayList var3 = null;
                  ChunkCoordIntPair var4 = par1Chunk.getChunkCoordIntPair();
                  int var5 = (var4.chunkXPos << 4) - 2;
                  int var6 = var5 + 16 + 2;
                  int var7 = (var4.chunkZPos << 4) - 2;
                  int var8 = var7 + 16 + 2;

                  for(int var9 = 0; var9 < 2; ++var9) {
                        Iterator var10;
                        if (var9 != 0) {
                              var10 = this.pendingTickListEntriesThisTick.iterator();
                              if (!this.pendingTickListEntriesThisTick.isEmpty()) {
                                    logger.debug("toBeTicked = " + this.pendingTickListEntriesThisTick.size());
                              }
                        } else {
                              Set setAll = new TreeSet();
                              int dx = -1;

                              while(true) {
                                    if (dx > 1) {
                                          var10 = setAll.iterator();
                                          break;
                                    }

                                    for(int dz = -1; dz <= 1; ++dz) {
                                          Set set = this.pendingTickListEntriesHashSet.getNextTickEntriesSet(var4.chunkXPos + dx, var4.chunkZPos + dz);
                                          setAll.addAll(set);
                                    }

                                    ++dx;
                              }
                        }

                        while(var10.hasNext()) {
                              NextTickListEntry var11 = (NextTickListEntry)var10.next();
                              if (var11.xCoord >= var5 && var11.xCoord < var6 && var11.zCoord >= var7 && var11.zCoord < var8) {
                                    if (par2) {
                                          this.pendingTickListEntriesHashSet.remove(var11);
                                          this.pendingTickListEntriesTreeSet.remove(var11);
                                          var10.remove();
                                    }

                                    if (var3 == null) {
                                          var3 = new ArrayList();
                                    }

                                    var3.add(var11);
                              }
                        }
                  }

                  return var3;
            } else {
                  return super.getPendingBlockUpdates(par1Chunk, par2);
            }
      }

      public void tick() {
            super.tick();
            if (!Config.isTimeDefault()) {
                  this.fixWorldTime();
            }

            if (Config.waterOpacityChanged) {
                  Config.waterOpacityChanged = false;
                  ClearWater.updateWaterOpacity(Config.getGameSettings(), this);
            }

      }

      protected void updateWeather() {
            if (!Config.isWeatherEnabled()) {
                  this.fixWorldWeather();
            }

            super.updateWeather();
      }

      private void fixWorldWeather() {
            if (super.worldInfo.isRaining() || super.worldInfo.isThundering()) {
                  super.worldInfo.setRainTime(0);
                  super.worldInfo.setRaining(false);
                  this.setRainStrength(0.0F);
                  super.worldInfo.setThunderTime(0);
                  super.worldInfo.setThundering(false);
                  this.setThunderStrength(0.0F);
                  this.func_73046_m().getConfigurationManager().sendPacketToAllPlayers(new S2BPacketChangeGameState(2, 0.0F));
                  this.func_73046_m().getConfigurationManager().sendPacketToAllPlayers(new S2BPacketChangeGameState(7, 0.0F));
                  this.func_73046_m().getConfigurationManager().sendPacketToAllPlayers(new S2BPacketChangeGameState(8, 0.0F));
            }

      }

      private void fixWorldTime() {
            if (super.worldInfo.getGameType().getID() == 1) {
                  long time = this.getWorldTime();
                  long timeOfDay = time % 24000L;
                  if (Config.isTimeDayOnly()) {
                        if (timeOfDay <= 1000L) {
                              this.setWorldTime(time - timeOfDay + 1001L);
                        }

                        if (timeOfDay >= 11000L) {
                              this.setWorldTime(time - timeOfDay + 24001L);
                        }
                  }

                  if (Config.isTimeNightOnly()) {
                        if (timeOfDay <= 14000L) {
                              this.setWorldTime(time - timeOfDay + 14001L);
                        }

                        if (timeOfDay >= 22000L) {
                              this.setWorldTime(time - timeOfDay + 24000L + 14001L);
                        }
                  }

            }
      }

      public void updateEntity(Entity par1Entity) {
            if (this.canSkipEntityUpdate(par1Entity) && par1Entity instanceof EntityLivingBase) {
                  EntityLivingBase elb = (EntityLivingBase)par1Entity;
                  int entityAge = EntityUtils.getEntityAge(elb);
                  ++entityAge;
                  if (elb instanceof EntityMob) {
                        float brightness = elb.getBrightness(1.0F);
                        if (brightness > 0.5F) {
                              entityAge += 2;
                        }
                  }

                  EntityUtils.setEntityAge(elb, entityAge);
                  if (elb instanceof EntityLiving) {
                        EntityLiving el = (EntityLiving)elb;
                        EntityUtils.despawnEntity(el);
                  }

            } else {
                  super.updateEntity(par1Entity);
                  if (Config.isSmoothWorld()) {
                        Thread.currentThread();
                        Thread.yield();
                  }

            }
      }

      private boolean canSkipEntityUpdate(Entity entity) {
            if (!(entity instanceof EntityLivingBase)) {
                  return false;
            } else {
                  EntityLivingBase entityLiving = (EntityLivingBase)entity;
                  if (entityLiving.isChild()) {
                        return false;
                  } else if (entityLiving.hurtTime > 0) {
                        return false;
                  } else if (entity.ticksExisted < 20) {
                        return false;
                  } else if (super.playerEntities.size() != 1) {
                        return false;
                  } else {
                        Entity player = (Entity)super.playerEntities.get(0);
                        double dx = Math.max(Math.abs(entity.posX - player.posX) - 16.0D, 0.0D);
                        double dz = Math.max(Math.abs(entity.posZ - player.posZ) - 16.0D, 0.0D);
                        double distSq = dx * dx + dz * dz;
                        return !entity.isInRangeToRenderDist(distSq);
                  }
            }
      }

      protected void setActivePlayerChunksAndCheckLight() {
            super.setActivePlayerChunksAndCheckLight();
            this.limitedChunkSet.clear();
            int viewDistance = this.func_152379_p();
            if (viewDistance > 10) {
                  if (viewDistance != this.lastViewDistance) {
                        this.lastViewDistance = viewDistance;
                        this.allChunksTicked = false;
                  } else if (!this.allChunksTicked) {
                        this.allChunksTicked = true;
                  } else {
                        for(int i = 0; i < super.playerEntities.size(); ++i) {
                              EntityPlayer player = (EntityPlayer)super.playerEntities.get(i);
                              int pcx = MathHelper.floor_double(player.posX / 16.0D);
                              int pcz = MathHelper.floor_double(player.posZ / 16.0D);
                              int dist = 10;

                              for(int cx = -dist; cx <= dist; ++cx) {
                                    for(int cz = -dist; cz <= dist; ++cz) {
                                          this.limitedChunkSet.add(new ChunkCoordIntPair(cx + pcx, cz + pcz));
                                    }
                              }
                        }

                        if (this.setChunkCoordsToTickOnce.size() > 0) {
                              this.limitedChunkSet.addAll(this.setChunkCoordsToTickOnce);
                              this.setChunkCoordsToTickOnce.clear();
                        }

                  }
            }
      }

      public void addChunkToTickOnce(int cx, int cz) {
            int viewDistance = this.func_152379_p();
            if (viewDistance > 10) {
                  this.setChunkCoordsToTickOnce.add(new ChunkCoordIntPair(cx, cz));
            }
      }

      protected void func_147456_g() {
            Set oldSet = super.activeChunkSet;
            if (this.limitedChunkSet.size() > 0) {
                  super.activeChunkSet = this.limitedChunkSet;
            }

            super.func_147456_g();
            super.activeChunkSet = oldSet;
      }
}
