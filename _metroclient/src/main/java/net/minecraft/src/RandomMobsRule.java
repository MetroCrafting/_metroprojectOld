package net.minecraft.src;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.BiomeGenBase;

public class RandomMobsRule {
      private ResourceLocation baseResLoc = null;
      private int index;
      private int[] skins = null;
      private ResourceLocation[] resourceLocations = null;
      private int[] weights = null;
      private BiomeGenBase[] biomes = null;
      private RangeListInt heights = null;
      private NbtTagValue nbtName = null;
      public int[] sumWeights = null;
      public int sumAllWeights = 1;
      private VillagerProfession[] professions = null;

      public RandomMobsRule(ResourceLocation baseResLoc, int index, int[] skins, int[] weights, BiomeGenBase[] biomes, RangeListInt heights, NbtTagValue nbtName, VillagerProfession[] professions) {
            this.baseResLoc = baseResLoc;
            this.index = index;
            this.skins = skins;
            this.weights = weights;
            this.biomes = biomes;
            this.heights = heights;
            this.nbtName = nbtName;
            this.professions = professions;
      }

      public boolean isValid(String path) {
            if (this.skins != null && this.skins.length != 0) {
                  if (this.resourceLocations != null) {
                        return true;
                  } else {
                        this.resourceLocations = new ResourceLocation[this.skins.length];
                        ResourceLocation locMcp = RandomMobs.getMcpatcherLocation(this.baseResLoc);
                        if (locMcp == null) {
                              Config.warn("Invalid path: " + this.baseResLoc.getResourcePath());
                              return false;
                        } else {
                              int sum;
                              int i;
                              for(sum = 0; sum < this.resourceLocations.length; ++sum) {
                                    i = this.skins[sum];
                                    if (i <= 1) {
                                          this.resourceLocations[sum] = this.baseResLoc;
                                    } else {
                                          ResourceLocation locNew = RandomMobs.getLocationIndexed(locMcp, i);
                                          if (locNew == null) {
                                                Config.warn("Invalid path: " + this.baseResLoc.getResourcePath());
                                                return false;
                                          }

                                          if (!Config.hasResource(locNew)) {
                                                Config.warn("Texture not found: " + locNew.getResourcePath());
                                                return false;
                                          }

                                          this.resourceLocations[sum] = locNew;
                                    }
                              }

                              if (this.weights != null) {
                                    int[] weights2;
                                    if (this.weights.length > this.resourceLocations.length) {
                                          Config.warn("More weights defined than skins, trimming weights: " + path);
                                          weights2 = new int[this.resourceLocations.length];
                                          System.arraycopy(this.weights, 0, weights2, 0, weights2.length);
                                          this.weights = weights2;
                                    }

                                    if (this.weights.length < this.resourceLocations.length) {
                                          Config.warn("Less weights defined than skins, expanding weights: " + path);
                                          weights2 = new int[this.resourceLocations.length];
                                          System.arraycopy(this.weights, 0, weights2, 0, this.weights.length);
                                          i = MathUtils.getAverage(this.weights);

                                          for(int i1 = this.weights.length; i1 < weights2.length; ++i1) {
                                                weights2[i1] = i1;
                                          }

                                          this.weights = weights2;
                                    }

                                    this.sumWeights = new int[this.weights.length];
                                    sum = 0;

                                    for(i = 0; i < this.weights.length; ++i) {
                                          if (this.weights[i] < 0) {
                                                Config.warn("Invalid weight: " + this.weights[i]);
                                                return false;
                                          }

                                          sum += this.weights[i];
                                          this.sumWeights[i] = sum;
                                    }

                                    this.sumAllWeights = sum;
                                    if (this.sumAllWeights <= 0) {
                                          Config.warn("Invalid sum of all weights: " + sum);
                                          this.sumAllWeights = 1;
                                    }
                              }

                              if (this.professions == ConnectedParser.PROFESSIONS_INVALID) {
                                    Config.warn("Invalid professions or careers: " + path);
                                    return false;
                              } else {
                                    return true;
                              }
                        }
                  }
            } else {
                  Config.warn("Invalid skins for rule: " + this.index);
                  return false;
            }
      }

      public boolean matches(EntityLiving el) {
            if (this.biomes != null) {
                  BiomeGenBase spawnBiome = el.spawnBiome;
                  boolean matchBiome = false;

                  for(int i = 0; i < this.biomes.length; ++i) {
                        BiomeGenBase biome = this.biomes[i];
                        if (biome == spawnBiome) {
                              matchBiome = true;
                              break;
                        }
                  }

                  if (!matchBiome) {
                        return false;
                  }
            }

            if (this.heights != null && el.spawnPosition != null) {
                  return this.heights.isInRange(el.spawnPosition.getY());
            } else {
                  if (this.nbtName != null) {
                        String name = el.hasCustomNameTag() ? el.getCustomNameTag() : null;
                        if (!this.nbtName.matchesValue(name)) {
                              return false;
                        }
                  }

                  if (this.professions != null && el instanceof EntityVillager) {
                        EntityVillager entityVillager = (EntityVillager)el;
                        int profInt = entityVillager.getProfession();
                        int careerInt = 0;
                        if (profInt < 0 || careerInt < 0) {
                              return false;
                        }

                        boolean matchProfession = false;

                        for(int i = 0; i < this.professions.length; ++i) {
                              VillagerProfession prof = this.professions[i];
                              if (prof.matches(profInt, careerInt)) {
                                    matchProfession = true;
                                    break;
                              }
                        }

                        if (!matchProfession) {
                              return false;
                        }
                  }

                  return true;
            }
      }

      public ResourceLocation getTextureLocation(ResourceLocation loc, int randomId) {
            if (this.resourceLocations != null && this.resourceLocations.length != 0) {
                  int index = 0;
                  if (this.weights == null) {
                        index = randomId % this.resourceLocations.length;
                  } else {
                        int randWeight = randomId % this.sumAllWeights;

                        for(int i = 0; i < this.sumWeights.length; ++i) {
                              if (this.sumWeights[i] > randWeight) {
                                    index = i;
                                    break;
                              }
                        }
                  }

                  return this.resourceLocations[index];
            } else {
                  return loc;
            }
      }
}
