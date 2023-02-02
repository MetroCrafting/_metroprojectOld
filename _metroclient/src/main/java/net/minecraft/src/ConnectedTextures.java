package net.minecraft.src;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockQuartz;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.AbstractResourcePack;
import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeGenBase;

public class ConnectedTextures {
      private static ConnectedProperties[][] blockProperties = (ConnectedProperties[][])null;
      private static ConnectedProperties[][] tileProperties = (ConnectedProperties[][])null;
      private static boolean multipass = false;
      private static final int BOTTOM = 0;
      private static final int TOP = 1;
      private static final int EAST = 2;
      private static final int WEST = 3;
      private static final int NORTH = 4;
      private static final int SOUTH = 5;
      private static final int Y_NEG = 0;
      private static final int Y_POS = 1;
      private static final int Z_NEG = 2;
      private static final int Z_POS = 3;
      private static final int X_NEG = 4;
      private static final int X_POS = 5;
      private static final int Y_AXIS = 0;
      private static final int Z_AXIS = 1;
      private static final int X_AXIS = 2;
      private static final String[] propSuffixes = new String[]{"", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
      private static final int[] ctmIndexes = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 0, 0, 0, 0, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 0, 0, 0, 0, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 0, 0, 0, 0, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 0, 0, 0, 0, 0};

      public static IIcon getConnectedTexture(IBlockAccess blockAccess, Block block, int x, int y, int z, int side, IIcon icon) {
            if (blockAccess == null) {
                  return icon;
            } else {
                  IIcon newIcon = getConnectedTextureSingle(blockAccess, block, x, y, z, side, icon, true);
                  if (!multipass) {
                        return newIcon;
                  } else if (newIcon == icon) {
                        return newIcon;
                  } else {
                        IIcon mpIcon = newIcon;

                        for(int i = 0; i < 3; ++i) {
                              IIcon newMpIcon = getConnectedTextureSingle(blockAccess, block, x, y, z, side, mpIcon, false);
                              if (newMpIcon == mpIcon) {
                                    break;
                              }

                              mpIcon = newMpIcon;
                        }

                        return mpIcon;
                  }
            }
      }

      public static IIcon getConnectedTextureSingle(IBlockAccess blockAccess, Block block, int x, int y, int z, int side, IIcon icon, boolean checkBlocks) {
            if (!(icon instanceof TextureAtlasSprite)) {
                  return icon;
            } else {
                  TextureAtlasSprite ts = (TextureAtlasSprite)icon;
                  int iconId = ts.getIndexInMap();
                  int metadata = -1;
                  if (tileProperties != null && Tessellator.instance.defaultTexture && iconId >= 0 && iconId < tileProperties.length) {
                        ConnectedProperties[] cps = tileProperties[iconId];
                        if (cps != null) {
                              if (metadata < 0) {
                                    metadata = blockAccess.getBlockMetadata(x, y, z);
                              }

                              IIcon newIcon = getConnectedTexture((ConnectedProperties[])cps, blockAccess, block, x, y, z, side, ts, metadata);
                              if (newIcon != null) {
                                    return newIcon;
                              }
                        }
                  }

                  if (blockProperties != null && checkBlocks) {
                        int blockId = Block.getIdFromBlock(block);
                        if (blockId >= 0 && blockId < blockProperties.length) {
                              ConnectedProperties[] cps = blockProperties[blockId];
                              if (cps != null) {
                                    if (metadata < 0) {
                                          metadata = blockAccess.getBlockMetadata(x, y, z);
                                    }

                                    IIcon newIcon = getConnectedTexture((ConnectedProperties[])cps, blockAccess, block, x, y, z, side, ts, metadata);
                                    if (newIcon != null) {
                                          return newIcon;
                                    }
                              }
                        }
                  }

                  return icon;
            }
      }

      public static ConnectedProperties getConnectedProperties(IBlockAccess blockAccess, Block block, int x, int y, int z, int side, IIcon icon) {
            if (blockAccess == null) {
                  return null;
            } else if (!(icon instanceof TextureAtlasSprite)) {
                  return null;
            } else {
                  TextureAtlasSprite ts = (TextureAtlasSprite)icon;
                  int iconId = ts.getIndexInMap();
                  int metadata = -1;
                  if (tileProperties != null && Tessellator.instance.defaultTexture && iconId >= 0 && iconId < tileProperties.length) {
                        ConnectedProperties[] cps = tileProperties[iconId];
                        if (cps != null) {
                              if (metadata < 0) {
                                    metadata = blockAccess.getBlockMetadata(x, y, z);
                              }

                              ConnectedProperties cp = getConnectedProperties(cps, blockAccess, block, x, y, z, side, ts, metadata);
                              if (cp != null) {
                                    return cp;
                              }
                        }
                  }

                  if (blockProperties != null) {
                        int blockId = Block.getIdFromBlock(block);
                        if (blockId >= 0 && blockId < blockProperties.length) {
                              ConnectedProperties[] cps = blockProperties[blockId];
                              if (cps != null) {
                                    if (metadata < 0) {
                                          metadata = blockAccess.getBlockMetadata(x, y, z);
                                    }

                                    ConnectedProperties cp = getConnectedProperties(cps, blockAccess, block, x, y, z, side, ts, metadata);
                                    if (cp != null) {
                                          return cp;
                                    }
                              }
                        }
                  }

                  return null;
            }
      }

      private static IIcon getConnectedTexture(ConnectedProperties[] cps, IBlockAccess blockAccess, Block block, int x, int y, int z, int side, IIcon icon, int metadata) {
            for(int i = 0; i < cps.length; ++i) {
                  ConnectedProperties cp = cps[i];
                  if (cp != null) {
                        IIcon newIcon = getConnectedTexture(cp, blockAccess, block, x, y, z, side, icon, metadata);
                        if (newIcon != null) {
                              return newIcon;
                        }
                  }
            }

            return null;
      }

      private static ConnectedProperties getConnectedProperties(ConnectedProperties[] cps, IBlockAccess blockAccess, Block block, int x, int y, int z, int side, IIcon icon, int metadata) {
            for(int i = 0; i < cps.length; ++i) {
                  ConnectedProperties cp = cps[i];
                  if (cp != null) {
                        IIcon newIcon = getConnectedTexture(cp, blockAccess, block, x, y, z, side, icon, metadata);
                        if (newIcon != null) {
                              return cp;
                        }
                  }
            }

            return null;
      }

      private static IIcon getConnectedTexture(ConnectedProperties cp, IBlockAccess blockAccess, Block block, int x, int y, int z, int side, IIcon icon, int metadata) {
            if (cp.heights != null && !cp.heights.isInRange(y)) {
                  return null;
            } else {
                  int sideCheck;
                  if (cp.biomes != null) {
                        BiomeGenBase blockBiome = blockAccess.getBiomeGenForCoords(x, z);
                        boolean biomeOk = false;

                        for(sideCheck = 0; sideCheck < cp.biomes.length; ++sideCheck) {
                              BiomeGenBase biome = cp.biomes[sideCheck];
                              if (blockBiome == biome) {
                                    biomeOk = true;
                                    break;
                              }
                        }

                        if (!biomeOk) {
                              return null;
                        }
                  }

                  int vertAxis = 0;
                  int metadataCheck = metadata;
                  if (block instanceof BlockRotatedPillar) {
                        vertAxis = getWoodAxis(side, metadata);
                        metadataCheck = metadata & 3;
                  }

                  if (block instanceof BlockQuartz) {
                        vertAxis = getQuartzAxis(side, metadata);
                        if (metadataCheck > 2) {
                              metadataCheck = 2;
                        }
                  }

                  if (side >= 0 && cp.faces != 63) {
                        sideCheck = side;
                        if (vertAxis != 0) {
                              sideCheck = fixSideByAxis(side, vertAxis);
                        }

                        if ((1 << sideCheck & cp.faces) == 0) {
                              return null;
                        }
                  }

                  if (cp.metadatas != null) {
                        int[] mds = cp.metadatas;
                        boolean metadataFound = false;

                        for(int i = 0; i < mds.length; ++i) {
                              if (mds[i] == metadataCheck) {
                                    metadataFound = true;
                                    break;
                              }
                        }

                        if (!metadataFound) {
                              return null;
                        }
                  }

                  switch(cp.method) {
                  case 1:
                        return getConnectedTextureCtm(cp, blockAccess, block, x, y, z, side, icon, metadata);
                  case 2:
                        return getConnectedTextureHorizontal(cp, blockAccess, block, x, y, z, vertAxis, side, icon, metadata);
                  case 3:
                        return getConnectedTextureTop(cp, blockAccess, block, x, y, z, vertAxis, side, icon, metadata);
                  case 4:
                        return getConnectedTextureRandom(cp, blockAccess, block, x, y, z, side);
                  case 5:
                        return getConnectedTextureRepeat(cp, x, y, z, side);
                  case 6:
                        return getConnectedTextureVertical(cp, blockAccess, block, x, y, z, vertAxis, side, icon, metadata);
                  case 7:
                        return getConnectedTextureFixed(cp);
                  case 8:
                        return getConnectedTextureHorizontalVertical(cp, blockAccess, block, x, y, z, vertAxis, side, icon, metadata);
                  case 9:
                        return getConnectedTextureVerticalHorizontal(cp, blockAccess, block, x, y, z, vertAxis, side, icon, metadata);
                  default:
                        return null;
                  }
            }
      }

      private static int fixSideByAxis(int side, int vertAxis) {
            switch(vertAxis) {
            case 0:
                  return side;
            case 1:
                  switch(side) {
                  case 0:
                        return 2;
                  case 1:
                        return 3;
                  case 2:
                        return 1;
                  case 3:
                        return 0;
                  default:
                        return side;
                  }
            case 2:
                  switch(side) {
                  case 0:
                        return 4;
                  case 1:
                        return 5;
                  case 2:
                  case 3:
                  default:
                        return side;
                  case 4:
                        return 1;
                  case 5:
                        return 0;
                  }
            default:
                  return side;
            }
      }

      private static int getWoodAxis(int side, int metadata) {
            int orient = (metadata & 12) >> 2;
            switch(orient) {
            case 1:
                  return 2;
            case 2:
                  return 1;
            default:
                  return 0;
            }
      }

      private static int getQuartzAxis(int side, int metadata) {
            switch(metadata) {
            case 3:
                  return 2;
            case 4:
                  return 1;
            default:
                  return 0;
            }
      }

      private static IIcon getConnectedTextureRandom(ConnectedProperties cp, IBlockAccess blockAccess, Block block, int x, int y, int z, int side) {
            if (cp.tileIcons.length == 1) {
                  return cp.tileIcons[0];
            } else {
                  int face = side / cp.symmetry * cp.symmetry;
                  int yDown;
                  if (cp.linked) {
                        yDown = y - 1;

                        for(Block blockDown = blockAccess.getBlock(x, yDown, z); blockDown == block; blockDown = blockAccess.getBlock(x, yDown, z)) {
                              y = yDown--;
                              if (yDown < 0) {
                                    break;
                              }
                        }
                  }

                  yDown = Config.getRandom(x, y, z, face) & Integer.MAX_VALUE;
                  int index = 0;
                  if (cp.weights == null) {
                        index = yDown % cp.tileIcons.length;
                  } else {
                        int randWeight = yDown % cp.sumAllWeights;
                        int[] sumWeights = cp.sumWeights;

                        for(int i = 0; i < sumWeights.length; ++i) {
                              if (randWeight < sumWeights[i]) {
                                    index = i;
                                    break;
                              }
                        }
                  }

                  return cp.tileIcons[index];
            }
      }

      private static IIcon getConnectedTextureFixed(ConnectedProperties cp) {
            return cp.tileIcons[0];
      }

      private static IIcon getConnectedTextureRepeat(ConnectedProperties cp, int x, int y, int z, int side) {
            if (cp.tileIcons.length == 1) {
                  return cp.tileIcons[0];
            } else {
                  int nx = 0;
                  int ny = 0;
                  switch(side) {
                  case 0:
                        nx = x;
                        ny = z;
                        break;
                  case 1:
                        nx = x;
                        ny = z;
                        break;
                  case 2:
                        nx = -x - 1;
                        ny = -y;
                        break;
                  case 3:
                        nx = x;
                        ny = -y;
                        break;
                  case 4:
                        nx = z;
                        ny = -y;
                        break;
                  case 5:
                        nx = -z - 1;
                        ny = -y;
                  }

                  nx %= cp.width;
                  ny %= cp.height;
                  if (nx < 0) {
                        nx += cp.width;
                  }

                  if (ny < 0) {
                        ny += cp.height;
                  }

                  int index = ny * cp.width + nx;
                  return cp.tileIcons[index];
            }
      }

      private static IIcon getConnectedTextureCtm(ConnectedProperties cp, IBlockAccess blockAccess, Block block, int x, int y, int z, int side, IIcon icon, int metadata) {
            boolean[] borders = new boolean[6];
            switch(side) {
            case 0:
            case 1:
                  borders[0] = isNeighbour(cp, blockAccess, block, x - 1, y, z, side, icon, metadata);
                  borders[1] = isNeighbour(cp, blockAccess, block, x + 1, y, z, side, icon, metadata);
                  borders[2] = isNeighbour(cp, blockAccess, block, x, y, z + 1, side, icon, metadata);
                  borders[3] = isNeighbour(cp, blockAccess, block, x, y, z - 1, side, icon, metadata);
                  break;
            case 2:
                  borders[0] = isNeighbour(cp, blockAccess, block, x + 1, y, z, side, icon, metadata);
                  borders[1] = isNeighbour(cp, blockAccess, block, x - 1, y, z, side, icon, metadata);
                  borders[2] = isNeighbour(cp, blockAccess, block, x, y - 1, z, side, icon, metadata);
                  borders[3] = isNeighbour(cp, blockAccess, block, x, y + 1, z, side, icon, metadata);
                  break;
            case 3:
                  borders[0] = isNeighbour(cp, blockAccess, block, x - 1, y, z, side, icon, metadata);
                  borders[1] = isNeighbour(cp, blockAccess, block, x + 1, y, z, side, icon, metadata);
                  borders[2] = isNeighbour(cp, blockAccess, block, x, y - 1, z, side, icon, metadata);
                  borders[3] = isNeighbour(cp, blockAccess, block, x, y + 1, z, side, icon, metadata);
                  break;
            case 4:
                  borders[0] = isNeighbour(cp, blockAccess, block, x, y, z - 1, side, icon, metadata);
                  borders[1] = isNeighbour(cp, blockAccess, block, x, y, z + 1, side, icon, metadata);
                  borders[2] = isNeighbour(cp, blockAccess, block, x, y - 1, z, side, icon, metadata);
                  borders[3] = isNeighbour(cp, blockAccess, block, x, y + 1, z, side, icon, metadata);
                  break;
            case 5:
                  borders[0] = isNeighbour(cp, blockAccess, block, x, y, z + 1, side, icon, metadata);
                  borders[1] = isNeighbour(cp, blockAccess, block, x, y, z - 1, side, icon, metadata);
                  borders[2] = isNeighbour(cp, blockAccess, block, x, y - 1, z, side, icon, metadata);
                  borders[3] = isNeighbour(cp, blockAccess, block, x, y + 1, z, side, icon, metadata);
            }

            int index = 0;
            if (borders[0] & !borders[1] & !borders[2] & !borders[3]) {
                  index = 3;
            } else if (!borders[0] & borders[1] & !borders[2] & !borders[3]) {
                  index = 1;
            } else if (!borders[0] & !borders[1] & borders[2] & !borders[3]) {
                  index = 12;
            } else if (!borders[0] & !borders[1] & !borders[2] & borders[3]) {
                  index = 36;
            } else if (borders[0] & borders[1] & !borders[2] & !borders[3]) {
                  index = 2;
            } else if (!borders[0] & !borders[1] & borders[2] & borders[3]) {
                  index = 24;
            } else if (borders[0] & !borders[1] & borders[2] & !borders[3]) {
                  index = 15;
            } else if (borders[0] & !borders[1] & !borders[2] & borders[3]) {
                  index = 39;
            } else if (!borders[0] & borders[1] & borders[2] & !borders[3]) {
                  index = 13;
            } else if (!borders[0] & borders[1] & !borders[2] & borders[3]) {
                  index = 37;
            } else if (!borders[0] & borders[1] & borders[2] & borders[3]) {
                  index = 25;
            } else if (borders[0] & !borders[1] & borders[2] & borders[3]) {
                  index = 27;
            } else if (borders[0] & borders[1] & !borders[2] & borders[3]) {
                  index = 38;
            } else if (borders[0] & borders[1] & borders[2] & !borders[3]) {
                  index = 14;
            } else if (borders[0] & borders[1] & borders[2] & borders[3]) {
                  index = 26;
            }

            if (index == 0) {
                  return cp.tileIcons[index];
            } else if (!Config.isConnectedTexturesFancy()) {
                  return cp.tileIcons[index];
            } else {
                  boolean[] edges = new boolean[6];
                  switch(side) {
                  case 0:
                  case 1:
                        edges[0] = !isNeighbour(cp, blockAccess, block, x + 1, y, z + 1, side, icon, metadata);
                        edges[1] = !isNeighbour(cp, blockAccess, block, x - 1, y, z + 1, side, icon, metadata);
                        edges[2] = !isNeighbour(cp, blockAccess, block, x + 1, y, z - 1, side, icon, metadata);
                        edges[3] = !isNeighbour(cp, blockAccess, block, x - 1, y, z - 1, side, icon, metadata);
                        break;
                  case 2:
                        edges[0] = !isNeighbour(cp, blockAccess, block, x - 1, y - 1, z, side, icon, metadata);
                        edges[1] = !isNeighbour(cp, blockAccess, block, x + 1, y - 1, z, side, icon, metadata);
                        edges[2] = !isNeighbour(cp, blockAccess, block, x - 1, y + 1, z, side, icon, metadata);
                        edges[3] = !isNeighbour(cp, blockAccess, block, x + 1, y + 1, z, side, icon, metadata);
                        break;
                  case 3:
                        edges[0] = !isNeighbour(cp, blockAccess, block, x + 1, y - 1, z, side, icon, metadata);
                        edges[1] = !isNeighbour(cp, blockAccess, block, x - 1, y - 1, z, side, icon, metadata);
                        edges[2] = !isNeighbour(cp, blockAccess, block, x + 1, y + 1, z, side, icon, metadata);
                        edges[3] = !isNeighbour(cp, blockAccess, block, x - 1, y + 1, z, side, icon, metadata);
                        break;
                  case 4:
                        edges[0] = !isNeighbour(cp, blockAccess, block, x, y - 1, z + 1, side, icon, metadata);
                        edges[1] = !isNeighbour(cp, blockAccess, block, x, y - 1, z - 1, side, icon, metadata);
                        edges[2] = !isNeighbour(cp, blockAccess, block, x, y + 1, z + 1, side, icon, metadata);
                        edges[3] = !isNeighbour(cp, blockAccess, block, x, y + 1, z - 1, side, icon, metadata);
                        break;
                  case 5:
                        edges[0] = !isNeighbour(cp, blockAccess, block, x, y - 1, z - 1, side, icon, metadata);
                        edges[1] = !isNeighbour(cp, blockAccess, block, x, y - 1, z + 1, side, icon, metadata);
                        edges[2] = !isNeighbour(cp, blockAccess, block, x, y + 1, z - 1, side, icon, metadata);
                        edges[3] = !isNeighbour(cp, blockAccess, block, x, y + 1, z + 1, side, icon, metadata);
                  }

                  if (index == 13 && edges[0]) {
                        index = 4;
                  } else if (index == 15 && edges[1]) {
                        index = 5;
                  } else if (index == 37 && edges[2]) {
                        index = 16;
                  } else if (index == 39 && edges[3]) {
                        index = 17;
                  } else if (index == 14 && edges[0] && edges[1]) {
                        index = 7;
                  } else if (index == 25 && edges[0] && edges[2]) {
                        index = 6;
                  } else if (index == 27 && edges[3] && edges[1]) {
                        index = 19;
                  } else if (index == 38 && edges[3] && edges[2]) {
                        index = 18;
                  } else if (index == 14 && !edges[0] && edges[1]) {
                        index = 31;
                  } else if (index == 25 && edges[0] && !edges[2]) {
                        index = 30;
                  } else if (index == 27 && !edges[3] && edges[1]) {
                        index = 41;
                  } else if (index == 38 && edges[3] && !edges[2]) {
                        index = 40;
                  } else if (index == 14 && edges[0] && !edges[1]) {
                        index = 29;
                  } else if (index == 25 && !edges[0] && edges[2]) {
                        index = 28;
                  } else if (index == 27 && edges[3] && !edges[1]) {
                        index = 43;
                  } else if (index == 38 && !edges[3] && edges[2]) {
                        index = 42;
                  } else if (index == 26 && edges[0] && edges[1] && edges[2] && edges[3]) {
                        index = 46;
                  } else if (index == 26 && !edges[0] && edges[1] && edges[2] && edges[3]) {
                        index = 9;
                  } else if (index == 26 && edges[0] && !edges[1] && edges[2] && edges[3]) {
                        index = 21;
                  } else if (index == 26 && edges[0] && edges[1] && !edges[2] && edges[3]) {
                        index = 8;
                  } else if (index == 26 && edges[0] && edges[1] && edges[2] && !edges[3]) {
                        index = 20;
                  } else if (index == 26 && edges[0] && edges[1] && !edges[2] && !edges[3]) {
                        index = 11;
                  } else if (index == 26 && !edges[0] && !edges[1] && edges[2] && edges[3]) {
                        index = 22;
                  } else if (index == 26 && !edges[0] && edges[1] && !edges[2] && edges[3]) {
                        index = 23;
                  } else if (index == 26 && edges[0] && !edges[1] && edges[2] && !edges[3]) {
                        index = 10;
                  } else if (index == 26 && edges[0] && !edges[1] && !edges[2] && edges[3]) {
                        index = 34;
                  } else if (index == 26 && !edges[0] && edges[1] && edges[2] && !edges[3]) {
                        index = 35;
                  } else if (index == 26 && edges[0] && !edges[1] && !edges[2] && !edges[3]) {
                        index = 32;
                  } else if (index == 26 && !edges[0] && edges[1] && !edges[2] && !edges[3]) {
                        index = 33;
                  } else if (index == 26 && !edges[0] && !edges[1] && edges[2] && !edges[3]) {
                        index = 44;
                  } else if (index == 26 && !edges[0] && !edges[1] && !edges[2] && edges[3]) {
                        index = 45;
                  }

                  return cp.tileIcons[index];
            }
      }

      private static boolean isNeighbour(ConnectedProperties cp, IBlockAccess iblockaccess, Block block, int x, int y, int z, int side, IIcon icon, int metadata) {
            Block neighbourBlock = iblockaccess.getBlock(x, y, z);
            if (cp.connect == 2) {
                  if (neighbourBlock == null) {
                        return false;
                  } else {
                        int neighbourMetadata = iblockaccess.getBlockMetadata(x, y, z);
                        IIcon neighbourIcon;
                        if (side >= 0) {
                              neighbourIcon = neighbourBlock.getIcon(side, neighbourMetadata);
                        } else {
                              neighbourIcon = neighbourBlock.getIcon(1, neighbourMetadata);
                        }

                        return neighbourIcon == icon;
                  }
            } else if (cp.connect == 3) {
                  if (neighbourBlock == null) {
                        return false;
                  } else {
                        return neighbourBlock.getMaterial() == block.getMaterial();
                  }
            } else {
                  return neighbourBlock == block && iblockaccess.getBlockMetadata(x, y, z) == metadata;
            }
      }

      private static IIcon getConnectedTextureHorizontal(ConnectedProperties cp, IBlockAccess blockAccess, Block block, int x, int y, int z, int vertAxis, int side, IIcon icon, int metadata) {
            boolean left;
            boolean right;
            left = false;
            right = false;
            label46:
            switch(vertAxis) {
            case 0:
                  switch(side) {
                  case 0:
                  case 1:
                        return null;
                  case 2:
                        left = isNeighbour(cp, blockAccess, block, x + 1, y, z, side, icon, metadata);
                        right = isNeighbour(cp, blockAccess, block, x - 1, y, z, side, icon, metadata);
                        break label46;
                  case 3:
                        left = isNeighbour(cp, blockAccess, block, x - 1, y, z, side, icon, metadata);
                        right = isNeighbour(cp, blockAccess, block, x + 1, y, z, side, icon, metadata);
                        break label46;
                  case 4:
                        left = isNeighbour(cp, blockAccess, block, x, y, z - 1, side, icon, metadata);
                        right = isNeighbour(cp, blockAccess, block, x, y, z + 1, side, icon, metadata);
                        break label46;
                  case 5:
                        left = isNeighbour(cp, blockAccess, block, x, y, z + 1, side, icon, metadata);
                        right = isNeighbour(cp, blockAccess, block, x, y, z - 1, side, icon, metadata);
                  default:
                        break label46;
                  }
            case 1:
                  switch(side) {
                  case 0:
                        left = isNeighbour(cp, blockAccess, block, x + 1, y, z, side, icon, metadata);
                        right = isNeighbour(cp, blockAccess, block, x - 1, y, z, side, icon, metadata);
                        break label46;
                  case 1:
                        left = isNeighbour(cp, blockAccess, block, x + 1, y, z, side, icon, metadata);
                        right = isNeighbour(cp, blockAccess, block, x - 1, y, z, side, icon, metadata);
                        break label46;
                  case 2:
                  case 3:
                        return null;
                  case 4:
                        left = isNeighbour(cp, blockAccess, block, x, y + 1, z, side, icon, metadata);
                        right = isNeighbour(cp, blockAccess, block, x, y - 1, z, side, icon, metadata);
                        break label46;
                  case 5:
                        left = isNeighbour(cp, blockAccess, block, x, y + 1, z, side, icon, metadata);
                        right = isNeighbour(cp, blockAccess, block, x, y - 1, z, side, icon, metadata);
                  default:
                        break label46;
                  }
            case 2:
                  switch(side) {
                  case 0:
                        left = isNeighbour(cp, blockAccess, block, x, y, z - 1, side, icon, metadata);
                        right = isNeighbour(cp, blockAccess, block, x, y, z + 1, side, icon, metadata);
                        break;
                  case 1:
                        left = isNeighbour(cp, blockAccess, block, x, y, z - 1, side, icon, metadata);
                        right = isNeighbour(cp, blockAccess, block, x, y, z + 1, side, icon, metadata);
                        break;
                  case 2:
                        left = isNeighbour(cp, blockAccess, block, x, y + 1, z, side, icon, metadata);
                        right = isNeighbour(cp, blockAccess, block, x, y - 1, z, side, icon, metadata);
                        break;
                  case 3:
                        left = isNeighbour(cp, blockAccess, block, x, y + 1, z, side, icon, metadata);
                        right = isNeighbour(cp, blockAccess, block, x, y - 1, z, side, icon, metadata);
                        break;
                  case 4:
                  case 5:
                        return null;
                  }
            }

            byte index;
            if (left) {
                  if (right) {
                        index = 1;
                  } else {
                        index = 2;
                  }
            } else if (right) {
                  index = 0;
            } else {
                  index = 3;
            }

            return cp.tileIcons[index];
      }

      private static IIcon getConnectedTextureVertical(ConnectedProperties cp, IBlockAccess blockAccess, Block block, int x, int y, int z, int vertAxis, int side, IIcon icon, int metadata) {
            boolean bottom = false;
            boolean top = false;
            switch(vertAxis) {
            case 0:
                  if (side == 1 || side == 0) {
                        return null;
                  }

                  bottom = isNeighbour(cp, blockAccess, block, x, y - 1, z, side, icon, metadata);
                  top = isNeighbour(cp, blockAccess, block, x, y + 1, z, side, icon, metadata);
                  break;
            case 1:
                  if (side == 3 || side == 2) {
                        return null;
                  }

                  bottom = isNeighbour(cp, blockAccess, block, x, y, z - 1, side, icon, metadata);
                  top = isNeighbour(cp, blockAccess, block, x, y, z + 1, side, icon, metadata);
                  break;
            case 2:
                  if (side == 5 || side == 4) {
                        return null;
                  }

                  bottom = isNeighbour(cp, blockAccess, block, x - 1, y, z, side, icon, metadata);
                  top = isNeighbour(cp, blockAccess, block, x + 1, y, z, side, icon, metadata);
            }

            byte index;
            if (bottom) {
                  if (top) {
                        index = 1;
                  } else {
                        index = 2;
                  }
            } else if (top) {
                  index = 0;
            } else {
                  index = 3;
            }

            return cp.tileIcons[index];
      }

      private static IIcon getConnectedTextureHorizontalVertical(ConnectedProperties cp, IBlockAccess blockAccess, Block block, int x, int y, int z, int vertAxis, int side, IIcon icon, int metadata) {
            IIcon[] tileIcons = cp.tileIcons;
            IIcon iconH = getConnectedTextureHorizontal(cp, blockAccess, block, x, y, z, vertAxis, side, icon, metadata);
            if (iconH != null && iconH != icon && iconH != tileIcons[3]) {
                  return iconH;
            } else {
                  IIcon iconV = getConnectedTextureVertical(cp, blockAccess, block, x, y, z, vertAxis, side, icon, metadata);
                  if (iconV == tileIcons[0]) {
                        return tileIcons[4];
                  } else if (iconV == tileIcons[1]) {
                        return tileIcons[5];
                  } else {
                        return iconV == tileIcons[2] ? tileIcons[6] : iconV;
                  }
            }
      }

      private static IIcon getConnectedTextureVerticalHorizontal(ConnectedProperties cp, IBlockAccess blockAccess, Block block, int x, int y, int z, int vertAxis, int side, IIcon icon, int metadata) {
            IIcon[] tileIcons = cp.tileIcons;
            IIcon iconV = getConnectedTextureVertical(cp, blockAccess, block, x, y, z, vertAxis, side, icon, metadata);
            if (iconV != null && iconV != icon && iconV != tileIcons[3]) {
                  return iconV;
            } else {
                  IIcon iconH = getConnectedTextureHorizontal(cp, blockAccess, block, x, y, z, vertAxis, side, icon, metadata);
                  if (iconH == tileIcons[0]) {
                        return tileIcons[4];
                  } else if (iconH == tileIcons[1]) {
                        return tileIcons[5];
                  } else {
                        return iconH == tileIcons[2] ? tileIcons[6] : iconH;
                  }
            }
      }

      private static IIcon getConnectedTextureTop(ConnectedProperties cp, IBlockAccess blockAccess, Block block, int x, int y, int z, int vertAxis, int side, IIcon icon, int metadata) {
            boolean top = false;
            switch(vertAxis) {
            case 0:
                  if (side == 1 || side == 0) {
                        return null;
                  }

                  top = isNeighbour(cp, blockAccess, block, x, y + 1, z, side, icon, metadata);
                  break;
            case 1:
                  if (side != 3 && side != 2) {
                        top = isNeighbour(cp, blockAccess, block, x, y, z + 1, side, icon, metadata);
                        break;
                  }

                  return null;
            case 2:
                  if (side == 5 || side == 4) {
                        return null;
                  }

                  top = isNeighbour(cp, blockAccess, block, x + 1, y, z, side, icon, metadata);
            }

            if (top) {
                  return cp.tileIcons[0];
            } else {
                  return null;
            }
      }

      public static void updateIcons(TextureMap textureMap) {
            blockProperties = (ConnectedProperties[][])null;
            tileProperties = (ConnectedProperties[][])null;
            IResourcePack[] rps = Config.getResourcePacks();

            for(int i = rps.length - 1; i >= 0; --i) {
                  IResourcePack rp = rps[i];
                  updateIcons(textureMap, rp);
            }

            updateIcons(textureMap, Config.getDefaultResourcePack());
      }

      public static void updateIcons(TextureMap textureMap, IResourcePack rp) {
            String[] names = collectFiles(rp, "mcpatcher/ctm/", ".properties");
            Arrays.sort(names);
            List tileList = makePropertyList(tileProperties);
            List blockList = makePropertyList(blockProperties);

            for(int i = 0; i < names.length; ++i) {
                  String name = names[i];
                  Config.dbg("ConnectedTextures: " + name);

                  try {
                        ResourceLocation locFile = new ResourceLocation(name);
                        InputStream in = rp.getInputStream(locFile);
                        if (in == null) {
                              Config.warn("ConnectedTextures file not found: " + name);
                        } else {
                              Properties props = new Properties();
                              props.load(in);
                              ConnectedProperties cp = new ConnectedProperties(props, name);
                              if (cp.isValid(name)) {
                                    cp.updateIcons(textureMap);
                                    addToTileList(cp, tileList);
                                    addToBlockList(cp, blockList);
                              }
                        }
                  } catch (FileNotFoundException var11) {
                        Config.warn("ConnectedTextures file not found: " + name);
                  } catch (IOException var12) {
                        var12.printStackTrace();
                  }
            }

            blockProperties = propertyListToArray(blockList);
            tileProperties = propertyListToArray(tileList);
            multipass = detectMultipass();
            Config.dbg("Multipass connected textures: " + multipass);
      }

      private static List makePropertyList(ConnectedProperties[][] propsArr) {
            List list = new ArrayList();
            if (propsArr != null) {
                  for(int i = 0; i < propsArr.length; ++i) {
                        ConnectedProperties[] props = propsArr[i];
                        List propList = null;
                        if (props != null) {
                              propList = new ArrayList(Arrays.asList(props));
                        }

                        list.add(propList);
                  }
            }

            return list;
      }

      private static boolean detectMultipass() {
            List propList = new ArrayList();

            int i;
            ConnectedProperties[] cps;
            for(i = 0; i < tileProperties.length; ++i) {
                  cps = tileProperties[i];
                  if (cps != null) {
                        propList.addAll(Arrays.asList(cps));
                  }
            }

            for(i = 0; i < blockProperties.length; ++i) {
                  cps = blockProperties[i];
                  if (cps != null) {
                        propList.addAll(Arrays.asList(cps));
                  }
            }

            ConnectedProperties[] props = (ConnectedProperties[])((ConnectedProperties[])propList.toArray(new ConnectedProperties[propList.size()]));
            Set matchIconSet = new HashSet();
            Set tileIconSet = new HashSet();

            for(int i1 = 0; i1 < props.length; ++i1) {
                  ConnectedProperties cp = props[i1];
                  if (cp.matchTileIcons != null) {
                        matchIconSet.addAll(Arrays.asList(cp.matchTileIcons));
                  }

                  if (cp.tileIcons != null) {
                        tileIconSet.addAll(Arrays.asList(cp.tileIcons));
                  }
            }

            matchIconSet.retainAll(tileIconSet);
            return !matchIconSet.isEmpty();
      }

      private static ConnectedProperties[][] propertyListToArray(List list) {
            ConnectedProperties[][] propArr = new ConnectedProperties[list.size()][];

            for(int i = 0; i < list.size(); ++i) {
                  List subList = (List)list.get(i);
                  if (subList != null) {
                        ConnectedProperties[] subArr = (ConnectedProperties[])((ConnectedProperties[])subList.toArray(new ConnectedProperties[subList.size()]));
                        propArr[i] = subArr;
                  }
            }

            return propArr;
      }

      private static void addToTileList(ConnectedProperties cp, List tileList) {
            if (cp.matchTileIcons != null) {
                  for(int i = 0; i < cp.matchTileIcons.length; ++i) {
                        IIcon icon = cp.matchTileIcons[i];
                        if (!(icon instanceof TextureAtlasSprite)) {
                              Config.warn("IIcon is not TextureAtlasSprite: " + icon + ", name: " + icon.getIconName());
                        } else {
                              TextureAtlasSprite ts = (TextureAtlasSprite)icon;
                              int tileId = ts.getIndexInMap();
                              if (tileId < 0) {
                                    Config.warn("Invalid tile ID: " + tileId + ", icon: " + ts.getIconName());
                              } else {
                                    addToList(cp, tileList, tileId);
                              }
                        }
                  }

            }
      }

      private static void addToBlockList(ConnectedProperties cp, List blockList) {
            if (cp.matchBlocks != null) {
                  for(int i = 0; i < cp.matchBlocks.length; ++i) {
                        int blockId = cp.matchBlocks[i];
                        if (blockId < 0) {
                              Config.warn("Invalid block ID: " + blockId);
                        } else {
                              addToList(cp, blockList, blockId);
                        }
                  }

            }
      }

      private static void addToList(ConnectedProperties cp, List list, int id) {
            while(id >= list.size()) {
                  list.add((Object)null);
            }

            List subList = (List)list.get(id);
            if (subList == null) {
                  subList = new ArrayList();
                  list.set(id, subList);
            }

            ((List)subList).add(cp);
      }

      private static String[] collectFiles(IResourcePack rp, String prefix, String suffix) {
            if (rp instanceof DefaultResourcePack) {
                  return collectFilesDefault(rp);
            } else if (!(rp instanceof AbstractResourcePack)) {
                  return new String[0];
            } else {
                  AbstractResourcePack arp = (AbstractResourcePack)rp;
                  File tpFile = ResourceUtils.getResourcePackFile(arp);
                  if (tpFile == null) {
                        return new String[0];
                  } else if (tpFile.isDirectory()) {
                        return collectFilesFolder(tpFile, "", prefix, suffix);
                  } else {
                        return tpFile.isFile() ? collectFilesZIP(tpFile, prefix, suffix) : new String[0];
                  }
            }
      }

      private static String[] collectFilesDefault(IResourcePack rp) {
            List list = new ArrayList();
            String[] names = getDefaultCtmPaths();

            for(int i = 0; i < names.length; ++i) {
                  String name = names[i];
                  ResourceLocation loc = new ResourceLocation(name);
                  if (rp.resourceExists(loc)) {
                        list.add(name);
                  }
            }

            String[] nameArr = (String[])((String[])list.toArray(new String[list.size()]));
            return nameArr;
      }

      private static String[] getDefaultCtmPaths() {
            List list = new ArrayList();
            String defPath = "mcpatcher/ctm/default/";
            if (Config.isFromDefaultResourcePack(new ResourceLocation("textures/blocks/glass.png"))) {
                  list.add(defPath + "glass.properties");
                  list.add(defPath + "glasspane.properties");
            }

            if (Config.isFromDefaultResourcePack(new ResourceLocation("textures/blocks/bookshelf.png"))) {
                  list.add(defPath + "bookshelf.properties");
            }

            if (Config.isFromDefaultResourcePack(new ResourceLocation("textures/blocks/sandstone_normal.png"))) {
                  list.add(defPath + "sandstone.properties");
            }
            
            if (Config.isFromDefaultResourcePack(new ResourceLocation("textures/blocks/bedrock.png"))) {
                list.add(defPath + "bedrock/bedrock.properties");
            }
            
            if (Config.isFromDefaultResourcePack(new ResourceLocation("textures/blocks/stone.png"))) {
                list.add(defPath + "concente_a/_block.properties");
            }

            String[] colors = new String[]{"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "silver", "cyan", "purple", "blue", "brown", "green", "red", "black"};

            for(int i = 0; i < colors.length; ++i) {
                  String color = colors[i];
                  if (Config.isFromDefaultResourcePack(new ResourceLocation("textures/blocks/glass_" + color + ".png"))) {
                        list.add(defPath + i + "_glass_" + color + "/glass_" + color + ".properties");
                        list.add(defPath + i + "_glass_" + color + "/glass_pane_" + color + ".properties");
                  }
            }

            String[] paths = (String[])((String[])list.toArray(new String[list.size()]));
            return paths;
      }

      private static String[] collectFilesFolder(File tpFile, String basePath, String prefix, String suffix) {
            List list = new ArrayList();
            String prefixAssets = "assets/minecraft/";
            File[] files = tpFile.listFiles();
            if (files == null) {
                  return new String[0];
            } else {
                  for(int i = 0; i < files.length; ++i) {
                        File file = files[i];
                        String name;
                        if (file.isFile()) {
                              name = basePath + file.getName();
                              if (name.startsWith(prefixAssets)) {
                                    name = name.substring(prefixAssets.length());
                                    if (name.startsWith(prefix) && name.endsWith(suffix)) {
                                          list.add(name);
                                    }
                              }
                        } else if (file.isDirectory()) {
                              name = basePath + file.getName() + "/";
                              String[] names = collectFilesFolder(file, name, prefix, suffix);

                              for(int n = 0; n < names.length; ++n) {
                                    String name1 = names[n];
                                    list.add(name1);
                              }
                        }
                  }

                  String[] names = (String[])((String[])list.toArray(new String[list.size()]));
                  return names;
            }
      }

      private static String[] collectFilesZIP(File tpFile, String prefix, String suffix) {
            List list = new ArrayList();
            String prefixAssets = "assets/minecraft/";

            try {
                  ZipFile zf = new ZipFile(tpFile);
                  Enumeration en = zf.entries();

                  while(en.hasMoreElements()) {
                        ZipEntry ze = (ZipEntry)en.nextElement();
                        String name = ze.getName();
                        if (name.startsWith(prefixAssets)) {
                              name = name.substring(prefixAssets.length());
                              if (name.startsWith(prefix) && name.endsWith(suffix)) {
                                    list.add(name);
                              }
                        }
                  }

                  zf.close();
                  String[] names = (String[])((String[])list.toArray(new String[list.size()]));
                  return names;
            } catch (IOException var9) {
                  var9.printStackTrace();
                  return new String[0];
            }
      }

      public static int getPaneTextureIndex(boolean linkP, boolean linkN, boolean linkYp, boolean linkYn) {
            if (linkN && linkP) {
                  if (linkYp) {
                        return linkYn ? 34 : 50;
                  } else {
                        return linkYn ? 18 : 2;
                  }
            } else if (linkN && !linkP) {
                  if (linkYp) {
                        return linkYn ? 35 : 51;
                  } else {
                        return linkYn ? 19 : 3;
                  }
            } else if (!linkN && linkP) {
                  if (linkYp) {
                        return linkYn ? 33 : 49;
                  } else {
                        return linkYn ? 17 : 1;
                  }
            } else if (linkYp) {
                  return linkYn ? 32 : 48;
            } else {
                  return linkYn ? 16 : 0;
            }
      }

      public static int getReversePaneTextureIndex(int texNum) {
            int col = texNum % 16;
            if (col == 1) {
                  return texNum + 2;
            } else {
                  return col == 3 ? texNum - 2 : texNum;
            }
      }

      public static IIcon getCtmTexture(ConnectedProperties cp, int ctmIndex, IIcon icon) {
            if (cp.method != 1) {
                  return icon;
            } else if (ctmIndex >= 0 && ctmIndex < ctmIndexes.length) {
                  int index = ctmIndexes[ctmIndex];
                  IIcon[] ctmIcons = cp.tileIcons;
                  return index >= 0 && index < ctmIcons.length ? ctmIcons[index] : icon;
            } else {
                  return icon;
            }
      }
}
