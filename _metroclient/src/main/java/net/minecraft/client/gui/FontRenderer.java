package net.minecraft.client.gui;

import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.src.Config;
import net.minecraft.src.CustomColorizer;
import net.minecraft.src.FontUtils;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class FontRenderer implements IResourceManagerReloadListener
{
    private static final ResourceLocation[] unicodePageLocations = new ResourceLocation[256];
    /** Array of width of all the characters in default.png */
    private float[] charWidth = new float[256];
    /** the height in pixels of default text */
    public int FONT_HEIGHT = 9;
    public Random fontRandom = new Random();
    /** Array of the start/end column (in upper/lower nibble) for every glyph in the /font directory. */
    private byte[] glyphWidth = new byte[65536];
    /**
     * Array of RGB triplets defining the 16 standard chat colors followed by 16 darker version of the same colors for
     * drop shadows.
     */
    private int[] colorCode = new int[32];
    protected ResourceLocation locationFontTexture;
    /** The RenderEngine used to load and setup glyph textures. */
    private final TextureManager renderEngine;
    /** Current X coordinate at which to draw the next character. */
    private float posX;
    /** Current Y coordinate at which to draw the next character. */
    private float posY;
    /** If true, strings should be rendered with Unicode fonts instead of the default.png font */
    private boolean unicodeFlag;
    /** If true, the Unicode Bidirectional Algorithm should be run before rendering any string. */
    private boolean bidiFlag;
    /** Used to specify new red value for the current color. */
    private float red;
    /** Used to specify new blue value for the current color. */
    private float blue;
    /** Used to specify new green value for the current color. */
    private float green;
    /** Used to speify new alpha value for the current color. */
    private float alpha;
    /** Text color of the currently rendering string. */
    private int textColor;
    /** Set if the "k" style (random) is active in currently rendering string */
    private boolean randomStyle;
    /** Set if the "l" style (bold) is active in currently rendering string */
    private boolean boldStyle;
    /** Set if the "o" style (italic) is active in currently rendering string */
    private boolean italicStyle;
    /** Set if the "n" style (underlined) is active in currently rendering string */
    private boolean underlineStyle;
    /** Set if the "m" style (strikethrough) is active in currently rendering string */
    private boolean strikethroughStyle;
    private static final String __OBFID = "CL_00000660";
    public GameSettings gameSettings;
    public ResourceLocation locationFontTextureBase;
    public boolean enabled = true;
    public float offsetBold = 1.0F;
    private float[] charWidthFloat = new float[256];
    private boolean blend = false;

    public FontRenderer(GameSettings par1GameSettings, ResourceLocation par2ResourceLocation, TextureManager par3TextureManager, boolean par4) {
            this.gameSettings = par1GameSettings;
            this.locationFontTextureBase = par2ResourceLocation;
            this.locationFontTexture = par2ResourceLocation;
            this.renderEngine = par3TextureManager;
            this.unicodeFlag = par4;
            this.locationFontTexture = FontUtils.getHdFontLocation(this.locationFontTextureBase);
            this.bindTexture(this.locationFontTexture);

            for(int var5 = 0; var5 < 32; ++var5) {
                  int var6 = (var5 >> 3 & 1) * 85;
                  int var7 = (var5 >> 2 & 1) * 170 + var6;
                  int var8 = (var5 >> 1 & 1) * 170 + var6;
                  int var9 = (var5 >> 0 & 1) * 170 + var6;
                  if (var5 == 6) {
                        var7 += 85;
                  }

                  if (par1GameSettings.anaglyph) {
                        int var10 = (var7 * 30 + var8 * 59 + var9 * 11) / 100;
                        int var11 = (var7 * 30 + var8 * 70) / 100;
                        int var12 = (var7 * 30 + var9 * 70) / 100;
                        var7 = var10;
                        var8 = var11;
                        var9 = var12;
                  }

                  if (var5 >= 16) {
                        var7 /= 4;
                        var8 /= 4;
                        var9 /= 4;
                  }

                  this.colorCode[var5] = (var7 & 255) << 16 | (var8 & 255) << 8 | var9 & 255;
            }

            this.readGlyphSizes();
      }

    public void onResourceManagerReload(IResourceManager par1ResourceManager) {
        this.locationFontTexture = FontUtils.getHdFontLocation(this.locationFontTextureBase);

        for(int i = 0; i < unicodePageLocations.length; ++i) {
              unicodePageLocations[i] = null;
        }

        this.readFontTexture();
        this.readGlyphSizes();
  }

    private void readFontTexture() {
            BufferedImage bufferedimage;
            try {
                  bufferedimage = ImageIO.read(this.getResourceInputStream(this.locationFontTexture));
            } catch (IOException var21) {
                  throw new RuntimeException(var21);
            }

            Properties props = FontUtils.readFontProperties(this.locationFontTexture);
            this.blend = FontUtils.readBoolean(props, "blend", false);
            int imgWidth = bufferedimage.getWidth();
            int imgHeight = bufferedimage.getHeight();
            int charW = imgWidth / 16;
            int charH = imgHeight / 16;
            float kx = (float)imgWidth / 128.0F;
            float boldScaleFactor = Config.limit(kx, 1.0F, 2.0F);
            this.offsetBold = 1.0F / boldScaleFactor;
            float offsetBoldConfig = FontUtils.readFloat(props, "offsetBold", -1.0F);
            if (offsetBoldConfig >= 0.0F) {
                  this.offsetBold = offsetBoldConfig;
            }

            int[] ai = new int[imgWidth * imgHeight];
            bufferedimage.getRGB(0, 0, imgWidth, imgHeight, ai, 0, imgWidth);

            for(int k = 0; k < 256; ++k) {
                  int cx = k % 16;
                  int cy = k / 16;

                  int px;
                  for(px = charW - 1; px >= 0; --px) {
                        int x = cx * charW + px;
                        boolean flag = true;

                        for(int py = 0; py < charH && flag; ++py) {
                              int ypos = (cy * charH + py) * imgWidth;
                              int col = ai[x + ypos];
                              int al = col >> 24 & 255;
                              if (al > 16) {
                                    flag = false;
                              }
                        }

                        if (!flag) {
                              break;
                        }
                  }

                  if (k == 65) {
                        k = k;
                  }

                  if (k == 32) {
                        if (charW <= 8) {
                              px = (int)(2.0F * kx);
                        } else {
                              px = (int)(1.5F * kx);
                        }
                  }

                  this.charWidth[k] = (float)(px + 1) / kx + 1.0F;
            }

            FontUtils.readCustomCharWidths(props, this.charWidth);
      }

    private void readGlyphSizes() {
        try {
              InputStream var1 = this.getResourceInputStream(new ResourceLocation("font/glyph_sizes.bin"));
              var1.read(this.glyphWidth);
        } catch (IOException var2) {
              throw new RuntimeException(var2);
        }
  }

    /**
     * Pick how to render a single character and return the width used.
     */
    private float renderCharAtPos(int par1, char par2, boolean par3) {
        if (par2 == ' ') {
              return !this.unicodeFlag ? this.charWidth[par2] : 4.0F;
        } else {
              return par2 == ' ' ? 4.0F : ("ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".indexOf(par2) != -1 && !this.unicodeFlag ? this.renderDefaultChar(par1, par3) : this.renderUnicodeChar(par2, par3));
        }
  }

    /**
     * Render a single character with the default.png font at current (posX,posY) location...
     */
    private float renderDefaultChar(int par1, boolean par2) {
        float var3 = (float)(par1 % 16 * 8);
        float var4 = (float)(par1 / 16 * 8);
        float var5 = par2 ? 1.0F : 0.0F;
        this.bindTexture(this.locationFontTexture);
        float var6 = 7.99F;
        GL11.glBegin(5);
        GL11.glTexCoord2f(var3 / 128.0F, var4 / 128.0F);
        GL11.glVertex3f(this.posX + var5, this.posY, 0.0F);
        GL11.glTexCoord2f(var3 / 128.0F, (var4 + 7.99F) / 128.0F);
        GL11.glVertex3f(this.posX - var5, this.posY + 7.99F, 0.0F);
        GL11.glTexCoord2f((var3 + var6 - 1.0F) / 128.0F, var4 / 128.0F);
        GL11.glVertex3f(this.posX + var6 - 1.0F + var5, this.posY, 0.0F);
        GL11.glTexCoord2f((var3 + var6 - 1.0F) / 128.0F, (var4 + 7.99F) / 128.0F);
        GL11.glVertex3f(this.posX + var6 - 1.0F - var5, this.posY + 7.99F, 0.0F);
        GL11.glEnd();
        return this.charWidth[par1];
  }

    private ResourceLocation getUnicodePageLocation(int par1) {
        if (unicodePageLocations[par1] == null) {
              unicodePageLocations[par1] = new ResourceLocation(String.format("textures/font/unicode_page_%02x.png", par1));
              unicodePageLocations[par1] = FontUtils.getHdFontLocation(unicodePageLocations[par1]);
        }

        return unicodePageLocations[par1];
  }

    /**
     * Load one of the /font/glyph_XX.png into a new GL texture and store the texture ID in glyphTextureName array.
     */
    private void loadGlyphTexture(int par1) {
        this.bindTexture(this.getUnicodePageLocation(par1));
  }

    /**
     * Render a single Unicode character at current (posX,posY) location using one of the /font/glyph_XX.png files...
     */
    private float renderUnicodeChar(char par1, boolean par2) {
            if (this.glyphWidth[par1] == 0) {
                  return 0.0F;
            } else {
                  int var3 = par1 / 256;
                  this.loadGlyphTexture(var3);
                  int var4 = this.glyphWidth[par1] >>> 4;
                  int var5 = this.glyphWidth[par1] & 15;
                  var4 &= 15;
                  float var6 = (float)var4;
                  float var7 = (float)(var5 + 1);
                  float var8 = (float)(par1 % 16 * 16) + var6;
                  float var9 = (float)((par1 & 255) / 16 * 16);
                  float var10 = var7 - var6 - 0.02F;
                  float var11 = par2 ? 1.0F : 0.0F;
                  GL11.glBegin(5);
                  GL11.glTexCoord2f(var8 / 256.0F, var9 / 256.0F);
                  GL11.glVertex3f(this.posX + var11, this.posY, 0.0F);
                  GL11.glTexCoord2f(var8 / 256.0F, (var9 + 15.98F) / 256.0F);
                  GL11.glVertex3f(this.posX - var11, this.posY + 7.99F, 0.0F);
                  GL11.glTexCoord2f((var8 + var10) / 256.0F, var9 / 256.0F);
                  GL11.glVertex3f(this.posX + var10 / 2.0F + var11, this.posY, 0.0F);
                  GL11.glTexCoord2f((var8 + var10) / 256.0F, (var9 + 15.98F) / 256.0F);
                  GL11.glVertex3f(this.posX + var10 / 2.0F - var11, this.posY + 7.99F, 0.0F);
                  GL11.glEnd();
                  return (var7 - var6) / 2.0F + 1.0F;
            }
      }

    /**
     * Draws the specified string with a shadow.
     */
    public int drawStringWithShadow(String string, float x, float y, int color)
    {
        return this.drawString(string, x, y, color, true);
    }

    /**
     * Draws the specified string.
     */
    public int drawString(String par1Str, int par2, int par3, int par4) {
        return !this.enabled ? 0 : this.drawString(par1Str, par2, par3, par4, false);
  }

    /**
     * Draws the specified string. Args: string, x, y, color, dropShadow
     */
    public int drawString(String string, float x, float y, int color, boolean dropShadow)
    {
        this.enableAlpha();

        if (this.blend)
        {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }

        this.resetStyles();
        int l;

        if (dropShadow)
        {
            l = this.renderString(string, x + 1, y + 1, color, true);
            l = Math.max(l, this.renderString(string, x, y, color, false));
        }
        else
        {
            l = this.renderString(string, x, y, color, false);
        }

        return l;
    }

    /**
     * Apply Unicode Bidirectional Algorithm to string and return a new possibly reordered string for visual rendering.
     */
    private String bidiReorder(String p_147647_1_)
    {
        try
        {
            Bidi bidi = new Bidi((new ArabicShaping(8)).shape(p_147647_1_), 127);
            bidi.setReorderingMode(0);
            return bidi.writeReordered(2);
        }
        catch (ArabicShapingException arabicshapingexception)
        {
            return p_147647_1_;
        }
    }

    /**
     * Reset all style flag fields in the class to false; called at the start of string rendering
     */
    private void resetStyles() {
        this.randomStyle = false;
        this.boldStyle = false;
        this.italicStyle = false;
        this.underlineStyle = false;
        this.strikethroughStyle = false;
  }

    /**
     * Render a single line string at the current (posX,posY) and update posX
     */
    private void renderStringAtPos(String par1Str, boolean par2) {
            for(int var3 = 0; var3 < par1Str.length(); ++var3) {
                  char var4 = par1Str.charAt(var3);
                  int var5;
                  int var6;
                  if (var4 == 167 && var3 + 1 < par1Str.length()) {
                        var5 = "0123456789abcdefklmnor".indexOf(par1Str.toLowerCase().charAt(var3 + 1));
                        if (var5 < 16) {
                              this.randomStyle = false;
                              this.boldStyle = false;
                              this.strikethroughStyle = false;
                              this.underlineStyle = false;
                              this.italicStyle = false;
                              if (var5 < 0 || var5 > 15) {
                                    var5 = 15;
                              }

                              if (par2) {
                                    var5 += 16;
                              }

                              var6 = this.colorCode[var5];
                              if (Config.isCustomColors()) {
                                    var6 = CustomColorizer.getTextColor(var5, var6);
                              }

                              this.textColor = var6;
                              this.setColor((float)(var6 >> 16) / 255.0F, (float)(var6 >> 8 & 255) / 255.0F, (float)(var6 & 255) / 255.0F, this.alpha);
                        } else if (var5 == 16) {
                              this.randomStyle = true;
                        } else if (var5 == 17) {
                              this.boldStyle = true;
                        } else if (var5 == 18) {
                              this.strikethroughStyle = true;
                        } else if (var5 == 19) {
                              this.underlineStyle = true;
                        } else if (var5 == 20) {
                              this.italicStyle = true;
                        } else if (var5 == 21) {
                              this.randomStyle = false;
                              this.boldStyle = false;
                              this.strikethroughStyle = false;
                              this.underlineStyle = false;
                              this.italicStyle = false;
                              this.setColor(this.red, this.blue, this.green, this.alpha);
                        }

                        ++var3;
                  } else {
                        var5 = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".indexOf(var4);
                        if (this.randomStyle && var5 != -1) {
                              do {
                                    var6 = this.fontRandom.nextInt(this.charWidth.length);
                              } while((int)this.charWidth[var5] != (int)this.charWidth[var6]);

                              var5 = var6;
                        }

                        float var11 = var5 != -1 && !this.unicodeFlag ? this.offsetBold : 0.5F;
                        boolean var7 = (var4 == 0 || var5 == -1 || this.unicodeFlag) && par2;
                        if (var7) {
                              this.posX -= var11;
                              this.posY -= var11;
                        }

                        float var8 = this.renderCharAtPos(var5, var4, this.italicStyle);
                        if (var7) {
                              this.posX += var11;
                              this.posY += var11;
                        }

                        if (this.boldStyle) {
                              this.posX += var11;
                              if (var7) {
                                    this.posX -= var11;
                                    this.posY -= var11;
                              }

                              this.renderCharAtPos(var5, var4, this.italicStyle);
                              this.posX -= var11;
                              if (var7) {
                                    this.posX += var11;
                                    this.posY += var11;
                              }

                              var8 += var11;
                        }

                        this.doDraw(var8);
                  }
            }

      }

    protected void doDraw(float var8) {
            Tessellator var9;
            if (this.strikethroughStyle) {
                  var9 = Tessellator.instance;
                  GL11.glDisable(3553);
                  var9.startDrawingQuads();
                  var9.addVertex((double)this.posX, (double)(this.posY + (float)(this.FONT_HEIGHT / 2)), 0.0D);
                  var9.addVertex((double)(this.posX + var8), (double)(this.posY + (float)(this.FONT_HEIGHT / 2)), 0.0D);
                  var9.addVertex((double)(this.posX + var8), (double)(this.posY + (float)(this.FONT_HEIGHT / 2) - 1.0F), 0.0D);
                  var9.addVertex((double)this.posX, (double)(this.posY + (float)(this.FONT_HEIGHT / 2) - 1.0F), 0.0D);
                  var9.draw();
                  GL11.glEnable(3553);
            }

            if (this.underlineStyle) {
                  var9 = Tessellator.instance;
                  GL11.glDisable(3553);
                  var9.startDrawingQuads();
                  int var10 = this.underlineStyle ? -1 : 0;
                  var9.addVertex((double)(this.posX + (float)var10), (double)(this.posY + (float)this.FONT_HEIGHT), 0.0D);
                  var9.addVertex((double)(this.posX + var8), (double)(this.posY + (float)this.FONT_HEIGHT), 0.0D);
                  var9.addVertex((double)(this.posX + var8), (double)(this.posY + (float)this.FONT_HEIGHT - 1.0F), 0.0D);
                  var9.addVertex((double)(this.posX + (float)var10), (double)(this.posY + (float)this.FONT_HEIGHT - 1.0F), 0.0D);
                  var9.draw();
                  GL11.glEnable(3553);
            }

            this.posX += var8;
      }

    /**
     * Render string either left or right aligned depending on bidiFlag
     */
    private int renderStringAligned(String par1Str, float x, float y, float width, int par5, boolean par6) {
        if (this.bidiFlag) {
              int var7 = this.getStringWidth(this.bidiReorder(par1Str));
              x = x + width - var7;
        }

        return this.renderString(par1Str, x, y, par5, par6);
  }


    /**
     * Render single line string by setting GL color, current (posX,posY), and calling renderStringAtPos()
     */
    private int renderString(String string, float x, float y, int color, boolean dropShadow)
    {
        if (string == null)
        {
            return 0;
        }
        else
        {
            if (this.bidiFlag)
            {
                string = this.bidiReorder(string);
            }

            if ((color & -67108864) == 0)
            {
                color |= -16777216;
            }

            if (dropShadow)
            {
                color = (color & 16579836) >> 2 | color & -16777216;
            }

            this.red = (float)(color >> 16 & 255) / 255.0F;
            this.blue = (float)(color >> 8 & 255) / 255.0F;
            this.green = (float)(color & 255) / 255.0F;
            this.alpha = (float)(color >> 24 & 255) / 255.0F;
            setColor(this.red, this.blue, this.green, this.alpha);
            this.posX = x;
            this.posY = y;
            this.renderStringAtPos(string, dropShadow);
            return (int)this.posX;
        }
    }

    /**
     * Returns the width of this string. Equivalent of FontMetrics.stringWidth(String s).
     */
      public int getStringWidth(String par1Str) {
            if (par1Str == null) {
                  return 0;
            } else {
                  float var2 = 0.0F;
                  boolean var3 = false;

                  for(int var4 = 0; var4 < par1Str.length(); ++var4) {
                        char var5 = par1Str.charAt(var4);
                        float var6 = this.getCharWidthFloat(var5);
                        if (var6 < 0.0F && var4 < par1Str.length() - 1) {
                              ++var4;
                              var5 = par1Str.charAt(var4);
                              if (var5 != 'l' && var5 != 'L') {
                                    if (var5 == 'r' || var5 == 'R') {
                                          var3 = false;
                                    }
                              } else {
                                    var3 = true;
                              }

                              var6 = 0.0F;
                        }

                        var2 += var6;
                        if (var3 && var6 > 0.0F) {
                              var2 += this.unicodeFlag ? 1.0F : this.offsetBold;
                        }
                  }

                  return Math.round(var2);
            }
      }

    /**
     * Returns the width of this character as rendered.
     */
      public int getCharWidth(char par1) {
            return Math.round(this.getCharWidthFloat(par1));
      }
      
      /**
       *  OPTIFINE CODE
       */
      
      private float getCharWidthFloat(char par1) {
          if (par1 == 167) {
                return -1.0F;
          } else if (par1 == ' ') {
                return this.charWidth[32];
          } else {
                int var2 = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".indexOf(par1);
                if (par1 > 0 && var2 != -1 && !this.unicodeFlag) {
                      return this.charWidth[var2];
                } else if (this.glyphWidth[par1] != 0) {
                      int var3 = this.glyphWidth[par1] >>> 4;
                      int var4 = this.glyphWidth[par1] & 15;
                      var3 &= 15;
                      ++var4;
                      return (float)((var4 - var3) / 2 + 1);
                } else {
                      return 0.0F;
                }
          }
    }

      
      
    /**
     * Trims a string to fit a specified Width.
     */
      public String trimStringToWidth(String par1Str, float f) {
          return this.trimStringToWidth(par1Str, f, false);
    }

    /**
     * Trims a string to a specified width, and will reverse it if par3 is set.
     */
      public String trimStringToWidth(String par1Str, float f, boolean par3) {
            StringBuilder var4 = new StringBuilder();
            float var5 = 0.0F;
            int var6 = par3 ? par1Str.length() - 1 : 0;
            int var7 = par3 ? -1 : 1;
            boolean var8 = false;
            boolean var9 = false;

            for(int var10 = var6; var10 >= 0 && var10 < par1Str.length() && var5 < (float)f; var10 += var7) {
                  char var11 = par1Str.charAt(var10);
                  float var12 = this.getCharWidthFloat(var11);
                  if (var8) {
                        var8 = false;
                        if (var11 != 'l' && var11 != 'L') {
                              if (var11 == 'r' || var11 == 'R') {
                                    var9 = false;
                              }
                        } else {
                              var9 = true;
                        }
                  } else if (var12 < 0.0F) {
                        var8 = true;
                  } else {
                        var5 += var12;
                        if (var9) {
                              ++var5;
                        }
                  }

                  if (var5 > (float)f) {
                        break;
                  }

                  if (par3) {
                        var4.insert(0, var11);
                  } else {
                        var4.append(var11);
                  }
            }

            return var4.toString();
      }

    /**
     * Remove all newline characters from the end of the string
     */
      private String trimStringNewline(String par1Str) {
          while(par1Str != null && par1Str.endsWith("\n")) {
                par1Str = par1Str.substring(0, par1Str.length() - 1);
          }

          return par1Str;
    }

    /**
     * Splits and draws a String with wordwrap (maximum length is parameter k)
     */
      public void drawSplitString(String string, float x, float y, float width, int color)
      {
          if (this.blend)
          {
              GL11.glEnable(GL11.GL_BLEND);
              GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
          }

          this.resetStyles();
          this.textColor = color;
          string = this.trimStringNewline(string);
          this.renderSplitString(string, x, y, width, false);
      }


    /**
     * Perform actual work of rendering a multi-line string with wordwrap and with darker drop shadow color if flag is
     * set
     */
      private void renderSplitString(String string, float x, float y, float width, boolean p_78268_5_)
      {
          List list = this.listFormattedStringToWidth(string, width);

          for (Iterator iterator = list.iterator(); iterator.hasNext(); y += this.FONT_HEIGHT)
          {
              String s1 = (String)iterator.next();
              this.renderStringAligned(s1, x, y, width, this.textColor, p_78268_5_);
          }
      }

    /**
     * Returns the width of the wordwrapped String (maximum length is parameter k)
     */
      public int splitStringWidth(String par1Str, int par2) {
          return this.FONT_HEIGHT * this.listFormattedStringToWidth(par1Str, par2).size();
    }

    /**
     * Set unicodeFlag controlling whether strings should be rendered with Unicode fonts instead of the default.png
     * font.
     */
      public void setUnicodeFlag(boolean par1) {
          this.unicodeFlag = par1;
    }

    /**
     * Get unicodeFlag controlling whether strings should be rendered with Unicode fonts instead of the default.png
     * font.
     */
      public boolean getUnicodeFlag() {
          return this.unicodeFlag;
    }

    /**
     * Set bidiFlag to control if the Unicode Bidirectional Algorithm should be run before rendering any string.
     */
      public void setBidiFlag(boolean par1) {
          this.bidiFlag = par1;
    }

    /**
     * Breaks a string into a list of pieces that will fit a specified width.
     */
      public List listFormattedStringToWidth(String string, float width)
      {
          return Arrays.asList(this.wrapFormattedStringToWidth(string, width).split("\n"));
      }

    /**
     * Inserts newline and formatting into a string to wrap it within the specified width.
     */
    String wrapFormattedStringToWidth(String sstring, float width)
    {
        if (sstring.length() <= 1)
        {
            return sstring;
        }
        else
        {
            int var3 = this.sizeStringToWidth(sstring, width);

            if (sstring.length() <= var3)
            {
                return sstring;
            }
            else
            {
                String var4 = sstring.substring(0, var3);
                char var5 = sstring.charAt(var3);
                boolean var6 = var5 == 32 || var5 == 10;
                String var7 = getFormatFromString(var4) + sstring.substring(var3 + (var6 ? 1 : 0));
                return var4 + "\n" + this.wrapFormattedStringToWidth(var7, width);
            }
        }
    }

    /**
     * Determines how many characters from the string will fit into the specified width.
     */
      private int sizeStringToWidth(String par1Str, float width) {
            int var3 = par1Str.length();
            float var4 = 0.0F;
            int var5 = 0;
            int var6 = -1;

            for(boolean var7 = false; var5 < var3; ++var5) {
                  char var8 = par1Str.charAt(var5);
                  switch(var8) {
                  case '\n':
                        --var5;
                        break;
                  case ' ':
                        var6 = var5;
                  default:
                        var4 += this.getCharWidthFloat(var8);
                        if (var7) {
                              ++var4;
                        }
                        break;
                  case '§':
                        if (var5 < var3 - 1) {
                              ++var5;
                              char var9 = par1Str.charAt(var5);
                              if (var9 != 'l' && var9 != 'L') {
                                    if (var9 == 'r' || var9 == 'R' || isFormatColor(var9)) {
                                          var7 = false;
                                    }
                              } else {
                                    var7 = true;
                              }
                        }
                  }

                  if (var8 == '\n') {
                        ++var5;
                        var6 = var5;
                        break;
                  }

                  if (Math.round(var4) > width) {
                        break;
                  }
            }

            return var5 != var3 && var6 != -1 && var6 < var5 ? var6 : var5;
      }

    /**
     * Checks if the char code is a hexadecimal character, used to set colour.
     */
      private static boolean isFormatColor(char par0) {
          return par0 >= '0' && par0 <= '9' || par0 >= 'a' && par0 <= 'f' || par0 >= 'A' && par0 <= 'F';
    }

    /**
     * Checks if the char code is O-K...lLrRk-o... used to set special formatting.
     */
      private static boolean isFormatSpecial(char par0) {
          return par0 >= 'k' && par0 <= 'o' || par0 >= 'K' && par0 <= 'O' || par0 == 'r' || par0 == 'R';
    }

    /**
     * Digests a string for nonprinting formatting characters then returns a string containing only that formatting.
     */
      private static String getFormatFromString(String par0Str) {
            String var1 = "";
            int var2 = -1;
            int var3 = par0Str.length();

            while((var2 = par0Str.indexOf(167, var2 + 1)) != -1) {
                  if (var2 < var3 - 1) {
                        char var4 = par0Str.charAt(var2 + 1);
                        if (isFormatColor(var4)) {
                              var1 = "§" + var4;
                        } else if (isFormatSpecial(var4)) {
                              var1 = var1 + "§" + var4;
                        }
                  }
            }

            return var1;
      }
    /**
     * Get bidiFlag that controls if the Unicode Bidirectional Algorithm should be run before rendering any string
     */
      public boolean getBidiFlag() {
          return this.bidiFlag;
    }

      protected void setColor(float r, float g, float b, float a) {
          GL11.glColor4f(r, g, b, a);
    }

      protected void enableAlpha() {
          GL11.glEnable(3008);
    }

      protected void bindTexture(ResourceLocation location) {
          this.renderEngine.bindTexture(location);
    }

      protected InputStream getResourceInputStream(ResourceLocation location) throws IOException {
          return Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream();
    }
}