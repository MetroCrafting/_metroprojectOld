package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.src.Config;
import net.minecraft.src.RandomMobs;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import shadersmod.client.ShadersTex;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@SideOnly(Side.CLIENT)
public class TextureManager implements ITickable, IResourceManagerReloadListener {

    private static final Logger logger = LogManager.getLogger();
    private final Map<ResourceLocation, ITextureObject> mapTextureObjects = Maps.newHashMap();
    private final TIntObjectMap<ResourceLocation> mapResourceLocations = new TIntObjectHashMap<>();
    private final List<ITickable> listTickables = Lists.newArrayList();
    private final Map<String, Integer> mapTextureCounters = Maps.newHashMap();
    private IResourceManager theResourceManager;

    public TextureManager(IResourceManager p_i1284_1_) {
        this.theResourceManager = p_i1284_1_;
    }

    public void bindTexture(ResourceLocation par1ResourceLocation) {
        if(par1ResourceLocation != null) {
            if (Config.isRandomMobs()) {
                par1ResourceLocation = RandomMobs.getTextureLocation(par1ResourceLocation);
            }
            ITextureObject var2 = this.mapTextureObjects.get(par1ResourceLocation);
            if (var2 == null) {
                var2 = new SimpleTexture(par1ResourceLocation);
                this.loadTexture(par1ResourceLocation, var2);
            }
            if (Config.isShaders()) {
                ShadersTex.bindTexture(var2);
            } else {
                TextureUtil.bindTexture(var2.getGlTextureId());
            }
        }
    }

    public ResourceLocation getResourceLocation(int p_130087_1_) {
        return this.mapResourceLocations.get(p_130087_1_);
    }

    public boolean loadTextureMap(ResourceLocation p_130088_1_, TextureMap p_130088_2_) {
        if (this.loadTickableTexture(p_130088_1_, p_130088_2_)) {
            this.mapResourceLocations.put(p_130088_2_.getTextureType(), p_130088_1_);
            return true;
        } else {
            return false;
        }
    }

    public boolean loadTickableTexture(ResourceLocation p_110580_1_, ITickableTextureObject p_110580_2_) {
        if (this.loadTexture(p_110580_1_, p_110580_2_)) {
            this.listTickables.add(p_110580_2_);
            return true;
        } else {
            return false;
        }
    }

    public boolean loadTexture(ResourceLocation p_110579_1_, final ITextureObject p_110579_2_) {
        boolean flag = true;
        ITextureObject p_110579_2_2 = p_110579_2_;
        try {
            p_110579_2_.loadTexture(this.theResourceManager);
        } catch (IOException ioexception) {
            logger.warn("Failed to load texture: " + p_110579_1_, ioexception);
            p_110579_2_2 = TextureUtil.missingTexture;
            this.mapTextureObjects.put(p_110579_1_, p_110579_2_2);
            flag = false;
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Registering texture");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Resource location being registered");
            crashreportcategory.addCrashSection("Resource location", p_110579_1_);
            crashreportcategory.addCrashSectionCallable("Texture object class", () -> p_110579_2_.getClass().getName());
            throw new ReportedException(crashreport);
        }
        this.mapTextureObjects.put(p_110579_1_, p_110579_2_2);
        return flag;
    }

    public ITextureObject getTexture(ResourceLocation p_110581_1_) {
        return this.mapTextureObjects.get(p_110581_1_);
    }

    public ResourceLocation getDynamicTextureLocation(String par1Str, DynamicTexture par2DynamicTexture) {
        if (par1Str.equals("logo")) {
            par2DynamicTexture = Config.getMojangLogoTexture(par2DynamicTexture);
        }
        Integer var3 = this.mapTextureCounters.get(par1Str);
        if (var3 == null) {
            var3 = 1;
        } else {
            var3 = var3 + 1;
        }
        this.mapTextureCounters.put(par1Str, var3);
        ResourceLocation var4 = new ResourceLocation(String.format("dynamic/%s_%d", par1Str, var3));
        this.loadTexture(var4, par2DynamicTexture);
        return var4;
    }

    public void tick() {
        for (ITickable itickable : this.listTickables) {
            itickable.tick();
        }
    }

    public void deleteTexture(ResourceLocation p_147645_1_) {
        ITextureObject itextureobject = this.getTexture(p_147645_1_);
        if (itextureobject != null) {
            this.mapTextureObjects.remove(p_147645_1_);
            TextureUtil.deleteTexture(itextureobject.getGlTextureId());
        }
    }

    public void onResourceManagerReload(IResourceManager p_110549_1_) {
        cpw.mods.fml.common.ProgressManager.ProgressBar bar = cpw.mods.fml.common.ProgressManager.push("Reloading Texture Manager", this.mapTextureObjects.keySet().size(), true);
        Config.dbg("*** Reloading textures ***");
        Config.log("Resource packs: " + Config.getResourcePackNames());
        Iterator<Entry<ResourceLocation, ITextureObject>> iterator = this.mapTextureObjects.entrySet().iterator();
        while (iterator.hasNext()) {
            Object obj = iterator.next();
            if (obj instanceof ResourceLocation) {
                ResourceLocation var2 = (ResourceLocation) obj;
                String var3 = var2.getResourcePath();

                if (var3.startsWith("mcpatcher/") || var3.startsWith("optifine/")) {
                    ITextureObject tex = this.mapTextureObjects.get(var2);
                    int glTexId = tex.getGlTextureId();
                    if (glTexId > 0) {
                        GL11.glDeleteTextures(glTexId);
                    }
                    iterator.remove();
                }
            }
        }
        for (Entry<ResourceLocation, ITextureObject> entry : this.mapTextureObjects.entrySet()) {
            bar.step(entry.getKey().toString());
            this.loadTexture(entry.getKey(), entry.getValue());
        }
        cpw.mods.fml.common.ProgressManager.pop(bar);
    }
}