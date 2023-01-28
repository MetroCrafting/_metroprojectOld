package net.minecraft.src;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.world.biome.BiomeGenBase;

public class ConnectedParser {
      private String context = null;
      public static final VillagerProfession[] PROFESSIONS_INVALID = new VillagerProfession[0];

      public ConnectedParser(String context) {
            this.context = context;
      }

      public String parseName(String path) {
            String str = path;
            int pos = path.lastIndexOf(47);
            if (pos >= 0) {
                  str = path.substring(pos + 1);
            }

            int pos2 = str.lastIndexOf(46);
            if (pos2 >= 0) {
                  str = str.substring(0, pos2);
            }

            return str;
      }

      public String parseBasePath(String path) {
            int pos = path.lastIndexOf(47);
            return pos < 0 ? "" : path.substring(0, pos);
      }

      public MatchBlock[] parseMatchBlocks(String propMatchBlocks) {
            if (propMatchBlocks == null) {
                  return null;
            } else {
                  List list = new ArrayList();
                  String[] blockStrs = Config.tokenize(propMatchBlocks, " ");

                  for(int i = 0; i < blockStrs.length; ++i) {
                        String blockStr = blockStrs[i];
                        MatchBlock[] mbs = this.parseMatchBlock(blockStr);
                        if (mbs != null) {
                              list.addAll(Arrays.asList(mbs));
                        }
                  }

                  MatchBlock[] mbs = (MatchBlock[])((MatchBlock[])list.toArray(new MatchBlock[list.size()]));
                  return mbs;
            }
      }

      public MatchBlock[] parseMatchBlock(String blockStr) {
            if (blockStr == null) {
                  return null;
            } else {
                  blockStr = blockStr.trim();
                  if (blockStr.length() <= 0) {
                        return null;
                  } else {
                        String[] parts = Config.tokenize(blockStr, ":");
                        String domain = "minecraft";
                        byte blockIndex;
                        if (parts.length > 1 && this.isFullBlockName(parts)) {
                              domain = parts[0];
                              blockIndex = 1;
                        } else {
                              domain = "minecraft";
                              blockIndex = 0;
                        }

                        String blockPart = parts[blockIndex];
                        String[] params = (String[])Arrays.copyOfRange(parts, blockIndex + 1, parts.length);
                        Block[] blocks = this.parseBlockPart(domain, blockPart);
                        if (blocks == null) {
                              return null;
                        } else {
                              MatchBlock[] datas = new MatchBlock[blocks.length];

                              for(int i = 0; i < blocks.length; ++i) {
                                    Block block = blocks[i];
                                    int blockId = Block.getIdFromBlock(block);
                                    int[] metadatas = null;
                                    if (params.length > 0) {
                                          metadatas = this.parseBlockMetadatas(block, params);
                                          if (metadatas == null) {
                                                return null;
                                          }
                                    }

                                    MatchBlock bd = new MatchBlock(blockId, metadatas);
                                    datas[i] = bd;
                              }

                              return datas;
                        }
                  }
            }
      }

      public boolean isFullBlockName(String[] parts) {
            if (parts.length < 2) {
                  return false;
            } else {
                  String part1 = parts[1];
                  if (part1.length() < 1) {
                        return false;
                  } else if (this.startsWithDigit(part1)) {
                        return false;
                  } else {
                        return !part1.contains("=");
                  }
            }
      }

      public boolean startsWithDigit(String str) {
            if (str == null) {
                  return false;
            } else if (str.length() < 1) {
                  return false;
            } else {
                  char ch = str.charAt(0);
                  return Character.isDigit(ch);
            }
      }

      public Block[] parseBlockPart(String domain, String blockPart) {
            if (this.startsWithDigit(blockPart)) {
                  int[] ids = this.parseIntList(blockPart);
                  if (ids == null) {
                        return null;
                  } else {
                        Block[] blocks = new Block[ids.length];

                        for(int i = 0; i < ids.length; ++i) {
                              int id = ids[i];
                              Block block = Block.getBlockById(id);
                              if (block == null) {
                                    this.warn("Block not found for id: " + id);
                                    return null;
                              }

                              blocks[i] = block;
                        }

                        return blocks;
                  }
            } else {
                  String fullName = domain + ":" + blockPart;
                  Block block = Block.getBlockFromName(fullName);
                  if (block == null) {
                        this.warn("Block not found for name: " + fullName);
                        return null;
                  } else {
                        Block[] blocks = new Block[]{block};
                        return blocks;
                  }
            }
      }

      public int[] parseBlockMetadatas(Block block, String[] params) {
            if (params.length <= 0) {
                  return null;
            } else {
                  String param0 = params[0];
                  if (this.startsWithDigit(param0)) {
                        int[] mds = this.parseIntList(param0);
                        return mds;
                  } else {
                        this.warn("Invalid block metadata: " + param0);
                        return null;
                  }
            }
      }

      public BiomeGenBase[] parseBiomes(String str) {
            if (str == null) {
                  return null;
            } else {
                  str = str.trim();
                  boolean negative = false;
                  if (str.startsWith("!")) {
                        negative = true;
                        str = str.substring(1);
                  }

                  String[] biomeNames = Config.tokenize(str, " ");
                  List list = new ArrayList();

                  for(int i = 0; i < biomeNames.length; ++i) {
                        String biomeName = biomeNames[i];
                        BiomeGenBase biome = this.findBiome(biomeName);
                        if (biome == null) {
                              this.warn("Biome not found: " + biomeName);
                        } else {
                              list.add(biome);
                        }
                  }

                  if (negative) {
                        List listAllBiomes = new ArrayList(Arrays.asList(BiomeGenBase.getBiomeGenArray()));
                        listAllBiomes.removeAll(list);
                        list = listAllBiomes;
                  }

                  BiomeGenBase[] biomeArr = (BiomeGenBase[])((BiomeGenBase[])list.toArray(new BiomeGenBase[list.size()]));
                  return biomeArr;
            }
      }

      public BiomeGenBase findBiome(String biomeName) {
            biomeName = biomeName.toLowerCase();
            if (biomeName.equals("nether")) {
                  return BiomeGenBase.hell;
            } else {
                  BiomeGenBase[] biomeList = BiomeGenBase.getBiomeGenArray();

                  for(int i = 0; i < biomeList.length; ++i) {
                        BiomeGenBase biome = biomeList[i];
                        if (biome != null) {
                              String name = biome.biomeName.replace(" ", "").toLowerCase();
                              if (name.equals(biomeName)) {
                                    return biome;
                              }
                        }
                  }

                  return null;
            }
      }

      public int parseInt(String str) {
            if (str == null) {
                  return -1;
            } else {
                  str = str.trim();
                  int num = Config.parseInt(str, -1);
                  if (num < 0) {
                        this.warn("Invalid number: " + str);
                  }

                  return num;
            }
      }

      public int parseInt(String str, int defVal) {
            if (str == null) {
                  return defVal;
            } else {
                  str = str.trim();
                  int num = Config.parseInt(str, -1);
                  if (num < 0) {
                        this.warn("Invalid number: " + str);
                        return defVal;
                  } else {
                        return num;
                  }
            }
      }

      public int[] parseIntList(String str) {
            if (str == null) {
                  return null;
            } else {
                  List list = new ArrayList();
                  String[] intStrs = Config.tokenize(str, " ,");

                  for(int i = 0; i < intStrs.length; ++i) {
                        String intStr = intStrs[i];
                        if (intStr.contains("-")) {
                              String[] subStrs = Config.tokenize(intStr, "-");
                              if (subStrs.length != 2) {
                                    this.warn("Invalid interval: " + intStr + ", when parsing: " + str);
                              } else {
                                    int min = Config.parseInt(subStrs[0], -1);
                                    int max = Config.parseInt(subStrs[1], -1);
                                    if (min >= 0 && max >= 0 && min <= max) {
                                          for(int n = min; n <= max; ++n) {
                                                list.add(n);
                                          }
                                    } else {
                                          this.warn("Invalid interval: " + intStr + ", when parsing: " + str);
                                    }
                              }
                        } else {
                              int val = Config.parseInt(intStr, -1);
                              if (val < 0) {
                                    this.warn("Invalid number: " + intStr + ", when parsing: " + str);
                              } else {
                                    list.add(val);
                              }
                        }
                  }

                  int[] ints = new int[list.size()];

                  for(int i = 0; i < ints.length; ++i) {
                        ints[i] = (Integer)list.get(i);
                  }

                  return ints;
            }
      }

      public void dbg(String str) {
            Config.dbg("" + this.context + ": " + str);
      }

      public void warn(String str) {
            Config.warn("" + this.context + ": " + str);
      }

      public RangeListInt parseRangeListInt(String str) {
            if (str == null) {
                  return null;
            } else {
                  RangeListInt list = new RangeListInt();
                  String[] parts = Config.tokenize(str, " ,");

                  for(int i = 0; i < parts.length; ++i) {
                        String part = parts[i];
                        RangeInt ri = this.parseRangeInt(part);
                        if (ri == null) {
                              return null;
                        }

                        list.addRange(ri);
                  }

                  return list;
            }
      }

      private RangeInt parseRangeInt(String str) {
            if (str == null) {
                  return null;
            } else if (str.indexOf(45) >= 0) {
                  String[] parts = Config.tokenize(str, "-");
                  if (parts.length != 2) {
                        this.warn("Invalid range: " + str);
                        return null;
                  } else {
                        int min = Config.parseInt(parts[0], -1);
                        int max = Config.parseInt(parts[1], -1);
                        if (min >= 0 && max >= 0) {
                              return new RangeInt(min, max);
                        } else {
                              this.warn("Invalid range: " + str);
                              return null;
                        }
                  }
            } else {
                  int val = Config.parseInt(str, -1);
                  if (val < 0) {
                        this.warn("Invalid integer: " + str);
                        return null;
                  } else {
                        return new RangeInt(val, val);
                  }
            }
      }

      public boolean parseBoolean(String str) {
            if (str == null) {
                  return false;
            } else {
                  String strLower = str.toLowerCase().trim();
                  return strLower.equals("true");
            }
      }

      public Boolean parseBooleanObject(String str) {
            if (str == null) {
                  return null;
            } else {
                  String strLower = str.toLowerCase().trim();
                  if (strLower.equals("true")) {
                        return Boolean.TRUE;
                  } else if (strLower.equals("false")) {
                        return Boolean.FALSE;
                  } else {
                        this.warn("Invalid boolean: " + str);
                        return null;
                  }
            }
      }

      public static int parseColor(String str, int defVal) {
            if (str == null) {
                  return defVal;
            } else {
                  str = str.trim();

                  try {
                        int val = Integer.parseInt(str, 16) & 16777215;
                        return val;
                  } catch (NumberFormatException var3) {
                        return defVal;
                  }
            }
      }

      public NbtTagValue parseNbtTagValue(String path, String value) {
            return path != null && value != null ? new NbtTagValue(path, value) : null;
      }

      public VillagerProfession[] parseProfessions(String profStr) {
            if (profStr == null) {
                  return null;
            } else {
                  List list = new ArrayList();
                  String[] tokens = Config.tokenize(profStr, " ");

                  for(int i = 0; i < tokens.length; ++i) {
                        String str = tokens[i];
                        VillagerProfession prof = this.parseProfession(str);
                        if (prof == null) {
                              this.warn("Invalid profession: " + str);
                              return PROFESSIONS_INVALID;
                        }

                        list.add(prof);
                  }

                  if (list.isEmpty()) {
                        return null;
                  } else {
                        VillagerProfession[] arr = (VillagerProfession[])((VillagerProfession[])list.toArray(new VillagerProfession[list.size()]));
                        return arr;
                  }
            }
      }

      private VillagerProfession parseProfession(String str) {
            str = str.toLowerCase();
            String[] parts = Config.tokenize(str, ":");
            if (parts.length > 2) {
                  return null;
            } else {
                  String profStr = parts[0];
                  String carStr = null;
                  if (parts.length > 1) {
                        carStr = parts[1];
                  }

                  int prof = parseProfessionId(profStr);
                  if (prof < 0) {
                        return null;
                  } else {
                        int[] cars = null;
                        if (carStr != null) {
                              cars = parseCareerIds(prof, carStr);
                              if (cars == null) {
                                    return null;
                              }
                        }

                        return new VillagerProfession(prof, cars);
                  }
            }
      }

      private static int parseProfessionId(String str) {
            int id = Config.parseInt(str, -1);
            if (id >= 0) {
                  return id;
            } else if (str.equals("farmer")) {
                  return 0;
            } else if (str.equals("librarian")) {
                  return 1;
            } else if (str.equals("priest")) {
                  return 2;
            } else if (str.equals("blacksmith")) {
                  return 3;
            } else if (str.equals("butcher")) {
                  return 4;
            } else {
                  return str.equals("nitwit") ? 5 : -1;
            }
      }

      private static int[] parseCareerIds(int prof, String str) {
            Set set = new HashSet();
            String[] parts = Config.tokenize(str, ",");

            int i;
            for(int i1 = 0; i1 < parts.length; ++i1) {
                  String part = parts[i1];
                  i1 = parseCareerId(prof, part);
                  if (i1 < 0) {
                        return null;
                  }

                  set.add(i1);
            }

            Integer[] integerArr = (Integer[])((Integer[])set.toArray(new Integer[set.size()]));
            int[] arr = new int[integerArr.length];

            for(i = 0; i < arr.length; ++i) {
                  arr[i] = integerArr[i];
            }

            return arr;
      }

      private static int parseCareerId(int prof, String str) {
            int id = Config.parseInt(str, -1);
            if (id >= 0) {
                  return id;
            } else {
                  if (prof == 0) {
                        if (str.equals("farmer")) {
                              return 1;
                        }

                        if (str.equals("fisherman")) {
                              return 2;
                        }

                        if (str.equals("shepherd")) {
                              return 3;
                        }

                        if (str.equals("fletcher")) {
                              return 4;
                        }
                  }

                  if (prof == 1) {
                        if (str.equals("librarian")) {
                              return 1;
                        }

                        if (str.equals("cartographer")) {
                              return 2;
                        }
                  }

                  if (prof == 2 && str.equals("cleric")) {
                        return 1;
                  } else {
                        if (prof == 3) {
                              if (str.equals("armor")) {
                                    return 1;
                              }

                              if (str.equals("weapon")) {
                                    return 2;
                              }

                              if (str.equals("tool")) {
                                    return 3;
                              }
                        }

                        if (prof == 4) {
                              if (str.equals("butcher")) {
                                    return 1;
                              }

                              if (str.equals("leather")) {
                                    return 2;
                              }
                        }

                        return prof == 5 && str.equals("nitwit") ? 1 : -1;
                  }
            }
      }
}
