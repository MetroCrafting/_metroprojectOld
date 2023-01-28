package net.minecraft.src;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javax.imageio.ImageIO;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;

public class TextureAnimations {
      private static TextureAnimation[] textureAnimations = null;

      public static void reset() {
            textureAnimations = null;
      }

      public static void update() {
            textureAnimations = null;
            IResourcePack[] rps = Config.getResourcePacks();
            textureAnimations = getTextureAnimations(rps);
            if (Config.isAnimatedTextures()) {
                  updateAnimations();
            }

      }

      public static void updateCustomAnimations() {
            if (textureAnimations != null) {
                  if (Config.isAnimatedTextures()) {
                        updateAnimations();
                  }
            }
      }

      public static void updateAnimations() {
            if (textureAnimations != null) {
                  for(int i = 0; i < textureAnimations.length; ++i) {
                        TextureAnimation anim = textureAnimations[i];
                        anim.updateTexture();
                  }

            }
      }

      public static TextureAnimation[] getTextureAnimations(IResourcePack[] rps) {
            List list = new ArrayList();

            for(int i = 0; i < rps.length; ++i) {
                  IResourcePack rp = rps[i];
                  TextureAnimation[] tas = getTextureAnimations(rp);
                  if (tas != null) {
                        list.addAll(Arrays.asList(tas));
                  }
            }

            TextureAnimation[] anims = (TextureAnimation[])((TextureAnimation[])list.toArray(new TextureAnimation[list.size()]));
            return anims;
      }

      public static TextureAnimation[] getTextureAnimations(IResourcePack rp) {
            String[] animPropNames = ResUtils.collectFiles(rp, (String)"mcpatcher/anim/", (String)".properties", (String[])null);
            if (animPropNames.length <= 0) {
                  return null;
            } else {
                  List list = new ArrayList();

                  for(int i = 0; i < animPropNames.length; ++i) {
                        String propName = animPropNames[i];
                        Config.dbg("Texture animation: " + propName);

                        try {
                              ResourceLocation propLoc = new ResourceLocation(propName);
                              InputStream in = rp.getInputStream(propLoc);
                              Properties props = new Properties();
                              props.load(in);
                              TextureAnimation anim = makeTextureAnimation(props, propLoc);
                              if (anim != null) {
                                    ResourceLocation locDstTex = new ResourceLocation(anim.getDstTex());
                                    if (Config.getDefiningResourcePack(locDstTex) != rp) {
                                          Config.dbg("Skipped: " + propName + ", target texture not loaded from same resource pack");
                                    } else {
                                          list.add(anim);
                                    }
                              }
                        } catch (FileNotFoundException var10) {
                              Config.warn("File not found: " + var10.getMessage());
                        } catch (IOException var11) {
                              var11.printStackTrace();
                        }
                  }

                  TextureAnimation[] anims = (TextureAnimation[])((TextureAnimation[])list.toArray(new TextureAnimation[list.size()]));
                  return anims;
            }
      }

      public static TextureAnimation makeTextureAnimation(Properties props, ResourceLocation propLoc) {
            String texFrom = props.getProperty("from");
            String texTo = props.getProperty("to");
            int x = Config.parseInt(props.getProperty("x"), -1);
            int y = Config.parseInt(props.getProperty("y"), -1);
            int width = Config.parseInt(props.getProperty("w"), -1);
            int height = Config.parseInt(props.getProperty("h"), -1);
            if (texFrom != null && texTo != null) {
                  if (x >= 0 && y >= 0 && width >= 0 && height >= 0) {
                        texFrom = texFrom.trim();
                        texTo = texTo.trim();
                        String basePath = TextureUtils.getBasePath(propLoc.getResourcePath());
                        texFrom = TextureUtils.fixResourcePath(texFrom, basePath);
                        texTo = TextureUtils.fixResourcePath(texTo, basePath);
                        byte[] imageBytes = getCustomTextureData(texFrom, width);
                        if (imageBytes == null) {
                              Config.warn("TextureAnimation: Source texture not found: " + texTo);
                              return null;
                        } else {
                              int countPixels = imageBytes.length / 4;
                              int countFrames = countPixels / (width * height);
                              int countPixelsAllFrames = countFrames * width * height;
                              if (countPixels != countPixelsAllFrames) {
                                    Config.warn("TextureAnimation: Source texture has invalid number of frames: " + texFrom + ", frames: " + (float)countPixels / (float)(width * height));
                                    return null;
                              } else {
                                    ResourceLocation locTexTo = new ResourceLocation(texTo);

                                    try {
                                          InputStream inTexTo = Config.getResourceStream(locTexTo);
                                          if (inTexTo == null) {
                                                Config.warn("TextureAnimation: Target texture not found: " + texTo);
                                                return null;
                                          } else {
                                                BufferedImage imgTexTo = readTextureImage(inTexTo);
                                                if (x + width <= imgTexTo.getWidth() && y + height <= imgTexTo.getHeight()) {
                                                      TextureAnimation anim = new TextureAnimation(texFrom, imageBytes, texTo, locTexTo, x, y, width, height, props, 1);
                                                      return anim;
                                                } else {
                                                      Config.warn("TextureAnimation: Animation coordinates are outside the target texture: " + texTo);
                                                      return null;
                                                }
                                          }
                                    } catch (IOException var17) {
                                          Config.warn("TextureAnimation: Target texture not found: " + texTo);
                                          return null;
                                    }
                              }
                        }
                  } else {
                        Config.warn("TextureAnimation: Invalid coordinates");
                        return null;
                  }
            } else {
                  Config.warn("TextureAnimation: Source or target texture not specified");
                  return null;
            }
      }

      public static byte[] getCustomTextureData(String imagePath, int tileWidth) {
            byte[] imageBytes = loadImage(imagePath, tileWidth);
            if (imageBytes == null) {
                  imageBytes = loadImage("/anim" + imagePath, tileWidth);
            }

            return imageBytes;
      }

      private static byte[] loadImage(String name, int targetWidth) {
            GameSettings options = Config.getGameSettings();

            try {
                  ResourceLocation locRes = new ResourceLocation(name);
                  InputStream in = Config.getResourceStream(locRes);
                  if (in == null) {
                        return null;
                  } else {
                        BufferedImage image = readTextureImage(in);
                        in.close();
                        if (image == null) {
                              return null;
                        } else {
                              if (targetWidth > 0 && image.getWidth() != targetWidth) {
                                    double aspectHW = (double)(image.getHeight() / image.getWidth());
                                    int targetHeight = (int)((double)targetWidth * aspectHW);
                                    image = scaleBufferedImage(image, targetWidth, targetHeight);
                              }

                              int width = image.getWidth();
                              int height = image.getHeight();
                              int[] ai = new int[width * height];
                              byte[] byteBuf = new byte[width * height * 4];
                              image.getRGB(0, 0, width, height, ai, 0, width);

                              for(int l = 0; l < ai.length; ++l) {
                                    int alpha = ai[l] >> 24 & 255;
                                    int red = ai[l] >> 16 & 255;
                                    int green = ai[l] >> 8 & 255;
                                    int blue = ai[l] & 255;
                                    if (options != null && options.anaglyph) {
                                          int j3 = (red * 30 + green * 59 + blue * 11) / 100;
                                          int l3 = (red * 30 + green * 70) / 100;
                                          int j4 = (red * 30 + blue * 70) / 100;
                                          red = j3;
                                          green = l3;
                                          blue = j4;
                                    }

                                    byteBuf[l * 4 + 0] = (byte)red;
                                    byteBuf[l * 4 + 1] = (byte)green;
                                    byteBuf[l * 4 + 2] = (byte)blue;
                                    byteBuf[l * 4 + 3] = (byte)alpha;
                              }

                              return byteBuf;
                        }
                  }
            } catch (FileNotFoundException var18) {
                  return null;
            } catch (Exception var19) {
                  var19.printStackTrace();
                  return null;
            }
      }

      private static BufferedImage readTextureImage(InputStream par1InputStream) throws IOException {
            BufferedImage var2 = ImageIO.read(par1InputStream);
            par1InputStream.close();
            return var2;
      }

      public static BufferedImage scaleBufferedImage(BufferedImage image, int width, int height) {
            BufferedImage scaledImage = new BufferedImage(width, height, 2);
            Graphics2D gr = scaledImage.createGraphics();
            gr.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            gr.drawImage(image, 0, 0, width, height, (ImageObserver)null);
            return scaledImage;
      }
}
