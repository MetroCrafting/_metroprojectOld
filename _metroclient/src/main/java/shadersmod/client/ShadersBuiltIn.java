package shadersmod.client;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.src.Config;
import shadersmod.common.SMCLog;

public class ShadersBuiltIn {
      public static Reader getShaderReader(String filename) {
            if (filename.endsWith("/deferred_last.vsh")) {
                  return getCompositeShaderReader(true, true);
            } else if (filename.endsWith("/composite_last.vsh")) {
                  return getCompositeShaderReader(false, true);
            } else if (filename.endsWith("/deferred_last.fsh")) {
                  return getCompositeShaderReader(true, false);
            } else {
                  return filename.endsWith("/composite_last.fsh") ? getCompositeShaderReader(false, false) : null;
            }
      }

      private static Reader getCompositeShaderReader(boolean deferred, boolean vertex) {
            if (!hasDeferredPrograms() && !hasSkipClear()) {
                  return null;
            } else {
                  int[] flipBuffers = getLastFlipBuffers(deferred);
                  if (flipBuffers == null) {
                        return null;
                  } else {
                        String shader;
                        if (!vertex) {
                              shader = deferred ? "deferred" : "composite";
                              SMCLog.info("flipped buffers after " + shader + ": " + Config.arrayToString(flipBuffers));
                        }

                        if (vertex) {
                              shader = getCompositeVertexShader(flipBuffers);
                        } else {
                              shader = getCompositeFragmentShader(flipBuffers);
                        }

                        return new StringReader(shader);
                  }
            }
      }

      private static Reader getCompositeFragmentShaderReader(boolean deferred) {
            if (!hasDeferredPrograms() && !hasSkipClear()) {
                  return null;
            } else {
                  int[] flipBuffers = getLastFlipBuffers(deferred);
                  if (flipBuffers == null) {
                        return null;
                  } else {
                        String shader = getCompositeFragmentShader(flipBuffers);
                        return new StringReader(shader);
                  }
            }
      }

      private static boolean hasDeferredPrograms() {
            for(int i = 33; i < 41; ++i) {
                  if (Shaders.programsID[i] != 0) {
                        return true;
                  }
            }

            return false;
      }

      private static boolean hasSkipClear() {
            for(int i = 0; i < Shaders.gbuffersClear.length; ++i) {
                  if (!Shaders.gbuffersClear[i]) {
                        return true;
                  }
            }

            return false;
      }

      private static String getCompositeVertexShader(int[] buffers) {
            List list = new ArrayList();
            list.add("#version 120                        ");
            list.add("varying vec2 texcoord;              ");
            list.add("void main()                         ");
            list.add("{                                   ");
            list.add("  gl_Position = ftransform();       ");
            list.add("  texcoord = gl_MultiTexCoord0.xy;  ");
            list.add("}                                   ");
            return Config.listToString(list, "\n");
      }

      private static String getCompositeFragmentShader(int[] buffers) {
            List list = new ArrayList();
            String drawBuffers = Config.arrayToString(buffers, "");
            list.add("#version 120                                           ");

            int i;
            for(i = 0; i < buffers.length; ++i) {
                  list.add("uniform sampler2D colortex" + buffers[i] + ";        ");
            }

            list.add("varying vec2 texcoord;                                 ");
            list.add("/* DRAWBUFFERS:" + drawBuffers + " */                  ");
            list.add("void main()                                            ");
            list.add("{                                                      ");

            for(i = 0; i < buffers.length; ++i) {
                  list.add("  gl_FragData[" + i + "] = texture2D(colortex" + buffers[i] + ", texcoord);     ");
            }

            list.add("}                                                      ");
            return Config.listToString(list, "\n");
      }

      private static int[] getLastFlipBuffers(boolean deferred) {
            return deferred ? getLastFlipBuffers(33, 8) : getLastFlipBuffers(21, 8);
      }

      private static int[] getLastFlipBuffers(int programStart, int programCount) {
            List list = new ArrayList();
            boolean[] toggled = new boolean[8];

            int t;
            for(t = programStart; t < programStart + programCount; ++t) {
                  if (Shaders.programsID[t] != 0) {
                        boolean[] togglesTexture = getProgramTogglesTexture(t);

                        for(int t1 = 0; t1 < togglesTexture.length; ++t1) {
                              boolean toggle = togglesTexture[t1];
                              if (toggle) {
                                    toggled[t1] = !toggled[t1];
                              }
                        }
                  }
            }

            for(t = 0; t < toggled.length; ++t) {
                  boolean toggle = toggled[t];
                  if (toggle) {
                        list.add(new Integer(t));
                  }
            }

            if (list.isEmpty()) {
                  return null;
            } else {
                  Integer[] arr = (Integer[])list.toArray(new Integer[list.size()]);
                  return Config.toPrimitive(arr);
            }
      }

      private static boolean[] getProgramTogglesTexture(int program) {
            boolean[] toggles = new boolean[8];
            String drawBufStr = Shaders.programsDrawBufSettings[program];
            if (drawBufStr == null) {
                  return toggles;
            } else {
                  for(int i = 0; i < drawBufStr.length(); ++i) {
                        char ch = drawBufStr.charAt(i);
                        int buf = ch - 48;
                        if (buf >= 0 && buf < toggles.length) {
                              toggles[buf] = true;
                        }
                  }

                  return toggles;
            }
      }
}
