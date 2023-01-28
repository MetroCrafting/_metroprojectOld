package net.minecraft.client.renderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.Proxy.Type;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.src.CapeImageBuffer;
import net.minecraft.src.HttpPipeline;
import net.minecraft.src.HttpRequest;
import net.minecraft.src.HttpResponse;
import net.minecraft.src.TextureUtils;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class ThreadDownloadImageData extends SimpleTexture
{
    private static final Logger logger = LogManager.getLogger();
    private static final AtomicInteger threadDownloadCounter = new AtomicInteger(0);
    private final File field_152434_e;
    private final String imageUrl;
    private final IImageBuffer imageBuffer;
    private BufferedImage bufferedImage;
    private Thread imageThread;
    private boolean textureUploaded;
    private static final String __OBFID = "CL_00001049";
    public Boolean imageFound = null;
    public boolean pipeline = false;

    public ThreadDownloadImageData(File p_i1049_1_, String p_i1049_2_, ResourceLocation p_i1049_3_, IImageBuffer p_i1049_4_)
    {
        super(p_i1049_3_);
        this.field_152434_e = p_i1049_1_;
        this.imageUrl = p_i1049_2_;
        this.imageBuffer = p_i1049_4_;
    }

    private void checkTextureUploaded() // func_147640_e
    {
        if (!this.textureUploaded && this.bufferedImage != null) {
            this.textureUploaded = true;
            if (super.textureLocation != null) {
                  this.deleteGlTexture(); // deleteGlTexture is func_147631_c
            }

            TextureUtil.uploadTextureImage(super.getGlTextureId(), this.bufferedImage);
      }
    }

    public int getGlTextureId()
    {
        this.checkTextureUploaded();
        return super.getGlTextureId();
    }

    public void setBufferedImage(BufferedImage p_147641_1_) // func_147641_a
    {
        this.bufferedImage = p_147641_1_;
        if (this.imageBuffer != null) {
              this.imageBuffer.func_152634_a();
        }
        this.imageFound = this.bufferedImage != null;
    }

      public void loadTexture(IResourceManager par1ResourceManager) throws IOException {
            if (this.bufferedImage == null && super.textureLocation != null) {
                  super.loadTexture(par1ResourceManager);
            }

            if (this.imageThread == null) {
                  if (this.field_152434_e != null && this.field_152434_e.isFile()) {
                        logger.debug("Loading http texture from local cache ({})", new Object[]{this.field_152434_e});

                        try {
                              this.bufferedImage = ImageIO.read(this.field_152434_e);
                              if (this.imageBuffer != null) {
                                    this.setBufferedImage(this.imageBuffer.parseUserSkin(this.bufferedImage)); // setBufferedImage is func_147641_a
                              }
                              this.loadingFinished();
                        } catch (IOException var3) {
                              logger.error("Couldn't load skin " + this.field_152434_e, var3);
                              this.func_152433_a();
                        }
                  } else {
                        this.func_152433_a();
                  }
            }

      }

      protected void func_152433_a()
      {
        this.imageThread = new Thread("Texture Downloader #" + threadDownloadCounter.incrementAndGet())
        {
            private static final String __OBFID = "CL_00001050";
            public void run()
            {
                HttpURLConnection httpurlconnection = null;
                ThreadDownloadImageData.logger.debug("Downloading http texture from {} to {}", new Object[] {ThreadDownloadImageData.this.imageUrl, ThreadDownloadImageData.this.field_152434_e});

                try
                {
                    httpurlconnection = (HttpURLConnection)(new URL(ThreadDownloadImageData.this.imageUrl)).openConnection(Minecraft.getMinecraft().getProxy());
                    httpurlconnection.setDoInput(true);
                    httpurlconnection.setDoOutput(false);
                    httpurlconnection.connect();

                    if (httpurlconnection.getResponseCode() / 100 == 2)
                    {
                        BufferedImage bufferedimage;

                        if (ThreadDownloadImageData.this.field_152434_e != null)
                        {
                            FileUtils.copyInputStreamToFile(httpurlconnection.getInputStream(), ThreadDownloadImageData.this.field_152434_e);
                            bufferedimage = ImageIO.read(ThreadDownloadImageData.this.field_152434_e);
                        }
                        else
                        {
                            bufferedimage = ImageIO.read(httpurlconnection.getInputStream());
                        }

                        if (ThreadDownloadImageData.this.imageBuffer != null)
                        {
                            bufferedimage = ThreadDownloadImageData.this.imageBuffer.parseUserSkin(bufferedimage);
                        }

                        ThreadDownloadImageData.this.setBufferedImage(bufferedimage);
                        return;
                    }
                }
                catch (Exception exception)
                {
                    ThreadDownloadImageData.logger.error("Couldn\'t download http texture", exception);
                    return;
                }
                finally
                {
                    if (httpurlconnection != null)
                    {
                        httpurlconnection.disconnect();
                    }
                }
            }
        };
        this.imageThread.setDaemon(true);
        this.imageThread.start();
    }
      
      private boolean shouldPipeline() {
          if (!this.pipeline) {
                return false;
          } else {
                Proxy proxy = Minecraft.getMinecraft().getProxy();
                if (proxy.type() != Type.DIRECT && proxy.type() != Type.SOCKS) {
                      return false;
                } else {
                      return this.imageUrl.startsWith("http://");
                }
          }
    }

      private void loadPipelined() {
          try {
                HttpRequest req = HttpPipeline.makeRequest(this.imageUrl, Minecraft.getMinecraft().getProxy());
                HttpResponse resp = HttpPipeline.executeRequest(req);
                if (resp.getStatus() / 100 == 2) {
                      byte[] body = resp.getBody();
                      ByteArrayInputStream bais = new ByteArrayInputStream(body);
                      BufferedImage var2;
                      if (this.field_152434_e != null) {
                            FileUtils.copyInputStreamToFile(bais, this.field_152434_e);
                            var2 = ImageIO.read(this.field_152434_e);
                      } else {
                            var2 = TextureUtils.readBufferedImage(bais);
                      }

                      if (this.imageBuffer != null) {
                            var2 = this.imageBuffer.parseUserSkin(var2);
                      }

                      this.setBufferedImage(var2);
                      return;
                }
          } catch (Exception var9) {
                logger.error("Couldn't download http texture: " + var9.getClass().getName() + ": " + var9.getMessage());
                return;
          } finally {
                this.loadingFinished();
          }

    }
      
      private void loadingFinished() {
          this.imageFound = this.bufferedImage != null;
          if (this.imageBuffer instanceof CapeImageBuffer) {
                CapeImageBuffer cib = (CapeImageBuffer)this.imageBuffer;
                cib.cleanup();
          }
     }
}