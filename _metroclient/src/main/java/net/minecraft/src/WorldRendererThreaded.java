package net.minecraft.src;

import java.util.HashSet;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.shader.TesselatorVertexState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.ForgeHooksClient;

import org.lwjgl.opengl.GL11;

public class WorldRendererThreaded extends WorldRenderer {
      private int glRenderListWork;
      private int glRenderListBoundingBox;
      public boolean[] tempSkipRenderPass = new boolean[2];
      public TesselatorVertexState tempVertexState;
      private Tessellator tessellatorWork = null;

      public WorldRendererThreaded(World par1World, List par2List, int par3, int par4, int par5, int par6) {
            super(par1World, par2List, par3, par4, par5, par6);
            RenderGlobal renderGlobal = Minecraft.getMinecraft().renderGlobal;
            this.glRenderListWork = renderGlobal.displayListAllocator.allocateDisplayLists(2);
            this.glRenderListBoundingBox = super.glRenderList + 2;
      }

      public void setPosition(int px, int py, int pz) {
            if (super.isUpdating) {
                  WrUpdates.finishCurrentUpdate();
            }

            super.setPosition(px, py, pz);
      }

      public void updateRenderer() {
            if (super.worldObj != null) {
                  this.updateRenderer((IWrUpdateListener)null);
                  this.finishUpdate();
            }
      }

      public void updateRenderer(IWrUpdateListener updateListener) {
            if (super.worldObj != null) {
                  super.needsUpdate = false;
                  int xMin = super.posX;
                  int yMin = super.posY;
                  int zMin = super.posZ;
                  int xMax = super.posX + 16;
                  int yMax = super.posY + 16;
                  int zMax = super.posZ + 16;

                  for(int i = 0; i < this.tempSkipRenderPass.length; ++i) {
                        this.tempSkipRenderPass[i] = true;
                  }

                  Chunk.isLit = false;
                  HashSet hashset = new HashSet();
                  hashset.addAll(super.tileEntityRenderers);
                  super.tileEntityRenderers.clear();
                  Minecraft var9 = Minecraft.getMinecraft();
                  EntityLivingBase var10 = var9.renderViewEntity;
                  int viewEntityPosX = MathHelper.floor_double(var10.posX);
                  int viewEntityPosY = MathHelper.floor_double(var10.posY);
                  int viewEntityPosZ = MathHelper.floor_double(var10.posZ);
                  int one = 1;
                  ChunkCacheOF chunkcache = new ChunkCacheOF(super.worldObj, xMin - one, yMin - one, zMin - one, xMax + one, yMax + one, zMax + one, one);
                  if (!chunkcache.extendedLevelsInChunkCache()) {
                        ++WorldRenderer.chunksUpdated;
                        chunkcache.renderStart();
                        RenderBlocks renderblocks = new RenderBlocks(chunkcache);
                        ForgeHooksClient.setWorldRendererRB(renderblocks);
                        super.bytesDrawn = 0;
                        this.tempVertexState = null;
                        super.tessellator = Tessellator.instance;
                        WrUpdateControl uc = new WrUpdateControl();

                        for(int renderPass = 0; renderPass < 2; ++renderPass) {
                              uc.setRenderPass(renderPass);
                              boolean renderNextPass = false;
                              boolean hasRenderedBlocks = false;
                              boolean hasGlList = false;

                              for(int y = yMin; y < yMax; ++y) {
                                    if (hasRenderedBlocks && updateListener != null) {
                                          updateListener.updating(uc);
                                          super.tessellator = Tessellator.instance;
                                    }

                                    for(int z = zMin; z < zMax; ++z) {
                                          for(int x = xMin; x < xMax; ++x) {
                                                Block block = chunkcache.getBlock(x, y, z);
                                                if (block.getMaterial() != Material.air) {
                                                      if (!hasGlList) {
                                                            hasGlList = true;
                                                            this.preRenderBlocksThreaded(renderPass);
                                                      }

                                                      if (renderPass == 0 && block.hasTileEntity(chunkcache.getBlockMetadata(x, y, z))) {
                                                            TileEntity var25 = chunkcache.getTileEntity(x, y, z);
                                                            if (TileEntityRendererDispatcher.instance.hasSpecialRenderer(var25)) {
                                                                  super.tileEntityRenderers.add(var25);
                                                            }
                                                      }

                                                      int blockPass = block.getRenderBlockPass();
                                                      if (blockPass > renderPass) {
                                                            renderNextPass = true;
                                                      }

                                                      if (block.canRenderInPass(renderPass)) {
                                                            hasRenderedBlocks |= renderblocks.renderBlockByRenderType(block, x, y, z);
                                                            if (block.getRenderType() == 0 && x == viewEntityPosX && y == viewEntityPosY && z == viewEntityPosZ) {
                                                                  renderblocks.setRenderFromInside(true);
                                                                  renderblocks.setRenderAllFaces(true);
                                                                  renderblocks.renderBlockByRenderType(block, x, y, z);
                                                                  renderblocks.setRenderFromInside(false);
                                                                  renderblocks.setRenderAllFaces(false);
                                                            }
                                                      }
                                                }
                                          }
                                    }
                              }

                              if (hasRenderedBlocks) {
                                    this.tempSkipRenderPass[renderPass] = false;
                              }

                              if (hasGlList) {
                                    if (updateListener != null) {
                                          updateListener.updating(uc);
                                    }

                                    super.tessellator = Tessellator.instance;
                                    this.postRenderBlocksThreaded(renderPass, super.renderGlobal.renderViewEntity);
                              } else {
                                    hasRenderedBlocks = false;
                              }

                              if (!renderNextPass) {
                                    break;
                              }
                        }

                        ForgeHooksClient.setWorldRendererRB(null);
                        chunkcache.renderFinish();
                  }

                  HashSet hashset1 = new HashSet();
                  hashset1.addAll(super.tileEntityRenderers);
                  hashset1.removeAll(hashset);
                  super.tileEntities.addAll(hashset1);
                  hashset.removeAll(super.tileEntityRenderers);
                  super.tileEntities.removeAll(hashset);
                  super.isChunkLit = Chunk.isLit;
                  super.isInitialized = true;
            }
      }

      protected void preRenderBlocksThreaded(int renderpass) {
            GL11.glNewList(this.glRenderListWork + renderpass, 4864);
            super.tessellator.setRenderingChunk(true);
            if (Config.isFastRender()) {
                  ForgeHooksClient.onPreRenderWorld(this, renderpass);
                  super.tessellator.startDrawingQuads();
                  super.tessellator.setTranslation((double)(-WorldRenderer.globalChunkOffsetX), 0.0D, (double)(-WorldRenderer.globalChunkOffsetZ));
            } else {
                  GL11.glPushMatrix();
                  this.setupGLTranslation();
                  float var2 = 1.000001F;
                  GL11.glTranslatef(-8.0F, -8.0F, -8.0F);
                  GL11.glScalef(var2, var2, var2);
                  GL11.glTranslatef(8.0F, 8.0F, 8.0F);
                  ForgeHooksClient.onPreRenderWorld(this, renderpass);
                  super.tessellator.startDrawingQuads();
                  super.tessellator.setTranslation((double)(-super.posX), (double)(-super.posY), (double)(-super.posZ));
            }
      }

      protected void postRenderBlocksThreaded(int renderpass, EntityLivingBase entityLiving) {
            if (Config.isTranslucentBlocksFancy() && renderpass == 1 && !this.tempSkipRenderPass[renderpass]) {
                  this.tempVertexState = super.tessellator.getVertexState((float)entityLiving.posX, (float)entityLiving.posY, (float)entityLiving.posZ);
            }

            super.bytesDrawn += super.tessellator.draw();
            ForgeHooksClient.onPostRenderWorld(this, renderpass);
            super.tessellator.setRenderingChunk(false);
            if (!Config.isFastRender()) {
                  GL11.glPopMatrix();
            }

            GL11.glEndList();
            super.tessellator.setTranslation(0.0D, 0.0D, 0.0D);
      }

      public void finishUpdate() {
            int temp = super.glRenderList;
            super.glRenderList = this.glRenderListWork;
            this.glRenderListWork = temp;

            int i;
            for(i = 0; i < 2; ++i) {
                  if (!super.skipRenderPass[i]) {
                        GL11.glNewList(this.glRenderListWork + i, 4864);
                        GL11.glEndList();
                  }
            }

            for(i = 0; i < 2; ++i) {
                  super.skipRenderPass[i] = this.tempSkipRenderPass[i];
            }

            super.skipAllRenderPasses = super.skipRenderPass[0] && super.skipRenderPass[1];
            if (super.needsBoxUpdate && !this.skipAllRenderPasses()) {
                  GL11.glNewList(this.glRenderListBoundingBox, 4864);
                  RenderItem.renderAABB(AxisAlignedBB.getBoundingBox((double)super.posXClip, (double)super.posYClip, (double)super.posZClip, (double)(super.posXClip + 16), (double)(super.posYClip + 16), (double)(super.posZClip + 16)));
                  GL11.glEndList();
                  super.needsBoxUpdate = false;
            }

            super.vertexState = this.tempVertexState;
            super.isVisible = true;
            super.isVisibleFromPosition = false;

            this.updateFinished();
      }

      public void callOcclusionQueryList() {
            GL11.glCallList(this.glRenderListBoundingBox);
      }
}
