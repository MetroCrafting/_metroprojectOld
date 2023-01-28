package shadersmod.client;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.src.Config;
import net.minecraft.src.StrUtils;
import net.optifine.entity.model.anim.ExpressionParser;
import net.optifine.entity.model.anim.ExpressionType;
import net.optifine.entity.model.anim.IExpression;
import net.optifine.entity.model.anim.ParseException;
import shadersmod.common.SMCLog;
import shadersmod.uniform.CustomUniform;
import shadersmod.uniform.CustomUniforms;
import shadersmod.uniform.ShaderExpressionResolver;
import shadersmod.uniform.UniformType;

public class ShaderPackParser {
      private static final Pattern PATTERN_VERSION = Pattern.compile("^\\s*#version\\s+.*$");
      private static final Pattern PATTERN_INCLUDE = Pattern.compile("^\\s*#include\\s+\"([A-Za-z0-9_/\\.]+)\".*$");
      private static final Set setConstNames = makeSetConstNames();

      public static ShaderOption[] parseShaderPackOptions(IShaderPack shaderPack, String[] programNames, List listDimensions) {
            if (shaderPack == null) {
                  return new ShaderOption[0];
            } else {
                  Map mapOptions = new HashMap();
                  collectShaderOptions(shaderPack, "/shaders", programNames, mapOptions);
                  Iterator it = listDimensions.iterator();

                  while(it.hasNext()) {
                        int dimId = (Integer)it.next();
                        String dirWorld = "/shaders/world" + dimId;
                        collectShaderOptions(shaderPack, dirWorld, programNames, mapOptions);
                  }

                  Collection options = mapOptions.values();
                  ShaderOption[] sos = (ShaderOption[])((ShaderOption[])options.toArray(new ShaderOption[options.size()]));
                  Comparator comp = new Comparator() {
                        public int compare(ShaderOption o1, ShaderOption o2) 
                        {
                              return o1.getName().compareToIgnoreCase(o2.getName());
                        }
                        public int compare(Object x0, Object x1)
                        {
                            return this.compare((ShaderOption)x0, (ShaderOption)x1);
                        }
                  };
                  Arrays.sort(sos, comp);
                  return sos;
            }
      }

      private static void collectShaderOptions(IShaderPack shaderPack, String dir, String[] programNames, Map mapOptions) {
            for(int i = 0; i < programNames.length; ++i) {
                  String programName = programNames[i];
                  if (!programName.equals("")) {
                        String vsh = dir + "/" + programName + ".vsh";
                        String fsh = dir + "/" + programName + ".fsh";
                        collectShaderOptions(shaderPack, vsh, mapOptions);
                        collectShaderOptions(shaderPack, fsh, mapOptions);
                  }
            }

      }

      private static void collectShaderOptions(IShaderPack sp, String path, Map mapOptions) {
            String[] lines = getLines(sp, path);

            for(int i = 0; i < lines.length; ++i) {
                  String line = lines[i];
                  ShaderOption so = getShaderOption(line, path);
                  if (so != null && !so.getName().startsWith(ShaderMacros.getPrefixMacro()) && (!so.checkUsed() || isOptionUsed(so, lines))) {
                        String key = so.getName();
                        ShaderOption so2 = (ShaderOption)mapOptions.get(key);
                        if (so2 != null) {
                              if (!Config.equals(so2.getValueDefault(), so.getValueDefault())) {
                                    Config.warn("Ambiguous shader option: " + so.getName());
                                    Config.warn(" - in " + Config.arrayToString((Object[])so2.getPaths()) + ": " + so2.getValueDefault());
                                    Config.warn(" - in " + Config.arrayToString((Object[])so.getPaths()) + ": " + so.getValueDefault());
                                    so2.setEnabled(false);
                              }

                              if (so2.getDescription() == null || so2.getDescription().length() <= 0) {
                                    so2.setDescription(so.getDescription());
                              }

                              so2.addPaths(so.getPaths());
                        } else {
                              mapOptions.put(key, so);
                        }
                  }
            }

      }

      private static boolean isOptionUsed(ShaderOption so, String[] lines) {
            for(int i = 0; i < lines.length; ++i) {
                  String line = lines[i];
                  if (so.isUsedInLine(line)) {
                        return true;
                  }
            }

            return false;
      }

      private static String[] getLines(IShaderPack sp, String path) {
            try {
                  List listFiles = new ArrayList();
                  String str = loadFile(path, sp, 0, listFiles, 0);
                  if (str == null) {
                        return new String[0];
                  } else {
                        ByteArrayInputStream is = new ByteArrayInputStream(str.getBytes());
                        String[] lines = Config.readLines((InputStream)is);
                        return lines;
                  }
            } catch (IOException var6) {
                  Config.dbg(var6.getClass().getName() + ": " + var6.getMessage());
                  return new String[0];
            }
      }

      private static ShaderOption getShaderOption(String line, String path) {
            ShaderOption so = null;
            if (so == null) {
                  so = ShaderOptionSwitch.parseOption(line, path);
            }

            if (so == null) {
                  so = ShaderOptionVariable.parseOption(line, path);
            }

            if (so != null) {
                  return so;
            } else {
                  if (so == null) {
                        so = ShaderOptionSwitchConst.parseOption(line, path);
                  }

                  if (so == null) {
                        so = ShaderOptionVariableConst.parseOption(line, path);
                  }

                  return so != null && setConstNames.contains(so.getName()) ? so : null;
            }
      }

      private static Set makeSetConstNames() {
            Set set = new HashSet();
            set.add("shadowMapResolution");
            set.add("shadowMapFov");
            set.add("shadowDistance");
            set.add("shadowDistanceRenderMul");
            set.add("shadowIntervalSize");
            set.add("generateShadowMipmap");
            set.add("generateShadowColorMipmap");
            set.add("shadowHardwareFiltering");
            set.add("shadowHardwareFiltering0");
            set.add("shadowHardwareFiltering1");
            set.add("shadowtex0Mipmap");
            set.add("shadowtexMipmap");
            set.add("shadowtex1Mipmap");
            set.add("shadowcolor0Mipmap");
            set.add("shadowColor0Mipmap");
            set.add("shadowcolor1Mipmap");
            set.add("shadowColor1Mipmap");
            set.add("shadowtex0Nearest");
            set.add("shadowtexNearest");
            set.add("shadow0MinMagNearest");
            set.add("shadowtex1Nearest");
            set.add("shadow1MinMagNearest");
            set.add("shadowcolor0Nearest");
            set.add("shadowColor0Nearest");
            set.add("shadowColor0MinMagNearest");
            set.add("shadowcolor1Nearest");
            set.add("shadowColor1Nearest");
            set.add("shadowColor1MinMagNearest");
            set.add("wetnessHalflife");
            set.add("drynessHalflife");
            set.add("eyeBrightnessHalflife");
            set.add("centerDepthHalflife");
            set.add("sunPathRotation");
            set.add("ambientOcclusionLevel");
            set.add("superSamplingLevel");
            set.add("noiseTextureResolution");
            return set;
      }

      public static ShaderProfile[] parseProfiles(Properties props, ShaderOption[] shaderOptions) {
            String PREFIX_PROFILE = "profile.";
            List list = new ArrayList();
            Set keys = props.keySet();
            Iterator it = keys.iterator();

            while(it.hasNext()) {
                  String key = (String)it.next();
                  if (key.startsWith(PREFIX_PROFILE)) {
                        String name = key.substring(PREFIX_PROFILE.length());
                        props.getProperty(key);
                        Set parsedProfiles = new HashSet();
                        ShaderProfile p = parseProfile(name, props, parsedProfiles, shaderOptions);
                        if (p != null) {
                              list.add(p);
                        }
                  }
            }

            if (list.size() <= 0) {
                  return null;
            } else {
                  ShaderProfile[] profs = (ShaderProfile[])((ShaderProfile[])list.toArray(new ShaderProfile[list.size()]));
                  return profs;
            }
      }

      public static Set parseOptionSliders(Properties props, ShaderOption[] shaderOptions) {
            Set sliders = new HashSet();
            String value = props.getProperty("sliders");
            if (value == null) {
                  return sliders;
            } else {
                  String[] names = Config.tokenize(value, " ");

                  for(int i = 0; i < names.length; ++i) {
                        String name = names[i];
                        ShaderOption so = ShaderUtils.getShaderOption(name, shaderOptions);
                        if (so == null) {
                              Config.warn("Invalid shader option: " + name);
                        } else {
                              sliders.add(name);
                        }
                  }

                  return sliders;
            }
      }

      private static ShaderProfile parseProfile(String name, Properties props, Set parsedProfiles, ShaderOption[] shaderOptions) {
            String PREFIX_PROFILE = "profile.";
            String key = PREFIX_PROFILE + name;
            if (parsedProfiles.contains(key)) {
                  Config.warn("[Shaders] Profile already parsed: " + name);
                  return null;
            } else {
                  parsedProfiles.add(name);
                  ShaderProfile prof = new ShaderProfile(name);
                  String val = props.getProperty(key);
                  String[] parts = Config.tokenize(val, " ");

                  for(int i = 0; i < parts.length; ++i) {
                        String part = parts[i];
                        if (part.startsWith(PREFIX_PROFILE)) {
                              String nameParent = part.substring(PREFIX_PROFILE.length());
                              ShaderProfile profParent = parseProfile(nameParent, props, parsedProfiles, shaderOptions);
                              if (prof != null) {
                                    prof.addOptionValues(profParent);
                                    prof.addDisabledPrograms(profParent.getDisabledPrograms());
                              }
                        } else {
                              String[] tokens = Config.tokenize(part, ":=");
                              String option;
                              if (tokens.length == 1) {
                                    option = tokens[0];
                                    boolean on = true;
                                    if (option.startsWith("!")) {
                                          on = false;
                                          option = option.substring(1);
                                    }

                                    String PREFIX_PROGRAM = "program.";
                                    if (option.startsWith(PREFIX_PROGRAM)) {
                                          String program = option.substring(PREFIX_PROGRAM.length());
                                          if (!Shaders.isProgramPath(program)) {
                                                Config.warn("Invalid program: " + program + " in profile: " + prof.getName());
                                          } else if (on) {
                                                prof.removeDisabledProgram(program);
                                          } else {
                                                prof.addDisabledProgram(program);
                                          }
                                    } else {
                                          ShaderOption so = ShaderUtils.getShaderOption(option, shaderOptions);
                                          if (!(so instanceof ShaderOptionSwitch)) {
                                                Config.warn("[Shaders] Invalid option: " + option);
                                          } else {
                                                prof.addOptionValue(option, String.valueOf(on));
                                                so.setVisible(true);
                                          }
                                    }
                              } else if (tokens.length != 2) {
                                    Config.warn("[Shaders] Invalid option value: " + part);
                              } else {
                                    option = tokens[0];
                                    String value = tokens[1];
                                    ShaderOption so = ShaderUtils.getShaderOption(option, shaderOptions);
                                    if (so == null) {
                                          Config.warn("[Shaders] Invalid option: " + part);
                                    } else if (!so.isValidValue(value)) {
                                          Config.warn("[Shaders] Invalid value: " + part);
                                    } else {
                                          so.setVisible(true);
                                          prof.addOptionValue(option, value);
                                    }
                              }
                        }
                  }

                  return prof;
            }
      }

      public static Map parseGuiScreens(Properties props, ShaderProfile[] shaderProfiles, ShaderOption[] shaderOptions) {
            Map map = new HashMap();
            parseGuiScreen("screen", props, map, shaderProfiles, shaderOptions);
            return map.isEmpty() ? null : map;
      }

      private static boolean parseGuiScreen(String key, Properties props, Map map, ShaderProfile[] shaderProfiles, ShaderOption[] shaderOptions) {
            String val = props.getProperty(key);
            if (val == null) {
                  return false;
            } else {
                  List list = new ArrayList();
                  Set setNames = new HashSet();
                  String[] opNames = Config.tokenize(val, " ");

                  String opName;
                  for(int i = 0; i < opNames.length; ++i) {
                        opName = opNames[i];
                        if (opName.equals("<empty>")) {
                              list.add((Object)null);
                        } else if (setNames.contains(opName)) {
                              Config.warn("[Shaders] Duplicate option: " + opName + ", key: " + key);
                        } else {
                              setNames.add(opName);
                              if (opName.equals("<profile>")) {
                                    if (shaderProfiles == null) {
                                          Config.warn("[Shaders] Option profile can not be used, no profiles defined: " + opName + ", key: " + key);
                                    } else {
                                          ShaderOptionProfile optionProfile = new ShaderOptionProfile(shaderProfiles, shaderOptions);
                                          list.add(optionProfile);
                                    }
                              } else if (opName.equals("*")) {
                                    ShaderOption soRest = new ShaderOptionRest("<rest>");
                                    list.add(soRest);
                              } else if (opName.startsWith("[") && opName.endsWith("]")) {
                                    String screen = StrUtils.removePrefixSuffix(opName, "[", "]");
                                    if (!screen.matches("^[a-zA-Z0-9_]+$")) {
                                          Config.warn("[Shaders] Invalid screen: " + opName + ", key: " + key);
                                    } else if (!parseGuiScreen("screen." + screen, props, map, shaderProfiles, shaderOptions)) {
                                          Config.warn("[Shaders] Invalid screen: " + opName + ", key: " + key);
                                    } else {
                                          ShaderOptionScreen optionScreen = new ShaderOptionScreen(screen);
                                          list.add(optionScreen);
                                    }
                              } else {
                                    ShaderOption so = ShaderUtils.getShaderOption(opName, shaderOptions);
                                    if (so == null) {
                                          Config.warn("[Shaders] Invalid option: " + opName + ", key: " + key);
                                          list.add((Object)null);
                                    } else {
                                          so.setVisible(true);
                                          list.add(so);
                                    }
                              }
                        }
                  }

                  ShaderOption[] scrOps = (ShaderOption[])((ShaderOption[])list.toArray(new ShaderOption[list.size()]));
                  opName = props.getProperty(key + ".columns");
                  int columns = Config.parseInt(opName, 2);
                  ScreenShaderOptions sso = new ScreenShaderOptions(key, scrOps, columns);
                  map.put(key, sso);
                  return true;
            }
      }

      public static BufferedReader resolveIncludes(BufferedReader reader, String filePath, IShaderPack shaderPack, int fileIndex, List listFiles, int includeLevel) throws IOException {
            String fileDir = "/";
            int pos = filePath.lastIndexOf("/");
            if (pos >= 0) {
                  fileDir = filePath.substring(0, pos);
            }

            CharArrayWriter caw = new CharArrayWriter();
            int macroInsertPosition = -1;
            Set setExtensions = new LinkedHashSet();
            int lineNumber = 1;

            while(true) {
                  String line = reader.readLine();
                  String fileInc;
                  String lineA;
                  String ext;
                  if (line == null) {
                        char[] chars = caw.toCharArray();
                        if (macroInsertPosition >= 0 && setExtensions.size() > 0) {
                              StringBuilder sbExt = new StringBuilder();
                              Iterator it = setExtensions.iterator();

                              while(it.hasNext()) {
                                    lineA = (String)it.next();
                                    sbExt.append("#define ");
                                    sbExt.append(lineA);
                                    sbExt.append("\n");
                              }

                              fileInc = sbExt.toString();
                              StringBuilder sbAll = new StringBuilder(new String(chars));
                              sbAll.insert(macroInsertPosition, fileInc);
                              ext = sbAll.toString();
                              chars = ext.toCharArray();
                        }

                        CharArrayReader car = new CharArrayReader(chars);
                        return new BufferedReader(car);
                  }

                  Matcher mi;
                  if (macroInsertPosition < 0) {
                        mi = PATTERN_VERSION.matcher(line);
                        if (mi.matches()) {
                              fileInc = ShaderMacros.getMacroLines();
                              lineA = line + "\n" + fileInc;
                              ext = "#line " + (lineNumber + 1) + " " + fileIndex;
                              line = lineA + ext;
                              macroInsertPosition = caw.size() + lineA.length();
                        }
                  }

                  mi = PATTERN_INCLUDE.matcher(line);
                  if (mi.matches()) {
                        fileInc = mi.group(1);
                        boolean absolute = fileInc.startsWith("/");
                        ext = absolute ? "/shaders" + fileInc : fileDir + "/" + fileInc;
                        if (!listFiles.contains(ext)) {
                              listFiles.add(ext);
                        }

                        int includeFileIndex = listFiles.indexOf(ext) + 1;
                        line = loadFile(ext, shaderPack, includeFileIndex, listFiles, includeLevel);
                        if (line == null) {
                              throw new IOException("Included file not found: " + filePath);
                        }

                        if (line.endsWith("\n")) {
                              line = line.substring(0, line.length() - 1);
                        }

                        line = "#line 1 " + includeFileIndex + "\n" + line + "\n" + "#line " + (lineNumber + 1) + " " + fileIndex;
                  }

                  if (macroInsertPosition >= 0 && line.contains(ShaderMacros.getPrefixMacro())) {
                        String[] lineExts = findExtensions(line, ShaderMacros.getExtensions());

                        for(int i = 0; i < lineExts.length; ++i) {
                              ext = lineExts[i];
                              setExtensions.add(ext);
                        }
                  }

                  caw.write(line);
                  caw.write("\n");
                  ++lineNumber;
            }
      }

      private static String[] findExtensions(String line, String[] extensions) {
            List list = new ArrayList();

            for(int i = 0; i < extensions.length; ++i) {
                  String ext = extensions[i];
                  if (line.contains(ext)) {
                        list.add(ext);
                  }
            }

            String[] exts = (String[])((String[])list.toArray(new String[list.size()]));
            return exts;
      }

      private static String loadFile(String filePath, IShaderPack shaderPack, int fileIndex, List listFiles, int includeLevel) throws IOException {
            if (includeLevel >= 10) {
                  throw new IOException("#include depth exceeded: " + includeLevel + ", file: " + filePath);
            } else {
                  ++includeLevel;
                  InputStream in = shaderPack.getResourceAsStream(filePath);
                  if (in == null) {
                        return null;
                  } else {
                        InputStreamReader isr = new InputStreamReader(in, "ASCII");
                        BufferedReader br = new BufferedReader(isr);
                        br = resolveIncludes(br, filePath, shaderPack, fileIndex, listFiles, includeLevel);
                        CharArrayWriter caw = new CharArrayWriter();

                        while(true) {
                              String line = br.readLine();
                              if (line == null) {
                                    return caw.toString();
                              }

                              caw.write(line);
                              caw.write("\n");
                        }
                  }
            }
      }

      public static CustomUniforms parseCustomUniforms(Properties props) {
            String UNIFORM = "uniform";
            String VARIABLE = "variable";
            String PREFIX_UNIFORM = UNIFORM + ".";
            String PREFIX_VARIABLE = VARIABLE + ".";
            Map mapExpressions = new HashMap();
            List listUniforms = new ArrayList();
            Set keys = props.keySet();
            Iterator it = keys.iterator();

            while(true) {
                  while(true) {
                        String key;
                        String[] keyParts;
                        do {
                              if (!it.hasNext()) {
                                    if (listUniforms.size() <= 0) {
                                          return null;
                                    }

                                    CustomUniform[] cusArr = (CustomUniform[])((CustomUniform[])listUniforms.toArray(new CustomUniform[listUniforms.size()]));
                                    CustomUniforms cus = new CustomUniforms(cusArr);
                                    return cus;
                              }

                              key = (String)it.next();
                              keyParts = Config.tokenize(key, ".");
                        } while(keyParts.length != 3);

                        String kind = keyParts[0];
                        String type = keyParts[1];
                        String name = keyParts[2];
                        String src = props.getProperty(key).trim();
                        if (mapExpressions.containsKey(name)) {
                              SMCLog.warning("Expression already defined: " + name);
                        } else if (kind.equals(UNIFORM) || kind.equals(VARIABLE)) {
                              SMCLog.info("Custom " + kind + ": " + name);
                              CustomUniform cu = parseCustomUniform(kind, name, type, src, mapExpressions);
                              if (cu != null) {
                                    mapExpressions.put(name, cu.getExpression());
                                    if (!kind.equals(VARIABLE)) {
                                          listUniforms.add(cu);
                                    }
                              }
                        }
                  }
            }
      }

      private static CustomUniform parseCustomUniform(String kind, String name, String type, String src, Map mapExpressions) {
            try {
                  UniformType uniformType = UniformType.parse(type);
                  if (uniformType == null) {
                        SMCLog.warning("Unknown " + kind + " type: " + uniformType);
                        return null;
                  } else {
                        ShaderExpressionResolver resolver = new ShaderExpressionResolver(mapExpressions);
                        ExpressionParser parser = new ExpressionParser(resolver);
                        IExpression expr = parser.parse(src);
                        ExpressionType expressionType = expr.getExpressionType();
                        if (!uniformType.matchesExpressionType(expressionType)) {
                              SMCLog.warning("Expression type does not match " + kind + " type, expression: " + expressionType + ", " + kind + ": " + uniformType + " " + name);
                              return null;
                        } else {
                              CustomUniform cu = new CustomUniform(name, uniformType, expr);
                              return cu;
                        }
                  }
            } catch (ParseException var11) {
                  SMCLog.warning(var11.getClass().getName() + ": " + var11.getMessage());
                  return null;
            }
      }
}
