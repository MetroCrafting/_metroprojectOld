package net.minecraft.src;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class DynamicLight {
      private Entity entity = null;
      private double offsetY = 0.0D;
      private double lastPosX = -2.147483648E9D;
      private double lastPosY = -2.147483648E9D;
      private double lastPosZ = -2.147483648E9D;
      private int lastLightLevel = 0;
      private boolean underwater = false;
      private long timeCheckMs = 0L;
      private Set setLitChunkPos = new HashSet();

      public DynamicLight(Entity entity) {
            this.entity = entity;
            this.offsetY = (double)entity.getEyeHeight();
      }

      public void update(RenderGlobal renderGlobal) {
            if (Config.isDynamicLightsFast()) {
                  long timeNowMs = System.currentTimeMillis();
                  if (timeNowMs < this.timeCheckMs + 500L) {
                        return;
                  }

                  this.timeCheckMs = timeNowMs;
            }

            double posX = this.entity.posX - 0.5D;
            double posY = this.entity.posY - 0.5D + this.offsetY;
            double posZ = this.entity.posZ - 0.5D;
            int lightLevel = DynamicLights.getLightLevel(this.entity);
            double dx = posX - this.lastPosX;
            double dy = posY - this.lastPosY;
            double dz = posZ - this.lastPosZ;
            double delta = 0.1D;
            if (Math.abs(dx) > delta || Math.abs(dy) > delta || Math.abs(dz) > delta || this.lastLightLevel != lightLevel) {
                  this.lastPosX = posX;
                  this.lastPosY = posY;
                  this.lastPosZ = posZ;
                  this.lastLightLevel = lightLevel;
                  this.underwater = false;
                  World world = renderGlobal.theWorld;
                  if (world != null) {
                        Block block = world.getBlock(MathHelper.floor_double(posX), MathHelper.floor_double(posY), MathHelper.floor_double(posZ));
                        this.underwater = block == Blocks.water;
                  }

                  Set setNewPos = new HashSet();
                  if (lightLevel > 0) {
                        EnumFacing dirX = (MathHelper.floor_double(posX) & 15) >= 8 ? EnumFacing.EAST : EnumFacing.WEST;
                        EnumFacing dirY = (MathHelper.floor_double(posY) & 15) >= 8 ? EnumFacing.UP : EnumFacing.DOWN;
                        EnumFacing dirZ = (MathHelper.floor_double(posZ) & 15) >= 8 ? EnumFacing.SOUTH : EnumFacing.NORTH;
                        dirX = this.getOpposite(dirX);
                        BlockPos pos = new BlockPos(MathHelper.floor_double(posX), MathHelper.floor_double(posY), MathHelper.floor_double(posZ));
                        BlockPos chunkView = new BlockPos(MathHelper.bucketInt(pos.getX(), 16) * 16, MathHelper.bucketInt(pos.getY(), 16) * 16, MathHelper.bucketInt(pos.getZ(), 16) * 16);
                        BlockPos chunkX = getRenderChunk(chunkView, dirX);
                        BlockPos chunkZ = getRenderChunk(chunkView, dirZ);
                        BlockPos chunkXZ = getRenderChunk(chunkX, dirZ);
                        BlockPos chunkY = getRenderChunk(chunkView, dirY);
                        BlockPos chunkYX = getRenderChunk(chunkY, dirX);
                        BlockPos chunkYZ = getRenderChunk(chunkY, dirZ);
                        BlockPos chunkYXZ = getRenderChunk(chunkYX, dirZ);
                        this.updateChunkLight(renderGlobal, chunkView, this.setLitChunkPos, setNewPos);
                        this.updateChunkLight(renderGlobal, chunkX, this.setLitChunkPos, setNewPos);
                        this.updateChunkLight(renderGlobal, chunkZ, this.setLitChunkPos, setNewPos);
                        this.updateChunkLight(renderGlobal, chunkXZ, this.setLitChunkPos, setNewPos);
                        this.updateChunkLight(renderGlobal, chunkY, this.setLitChunkPos, setNewPos);
                        this.updateChunkLight(renderGlobal, chunkYX, this.setLitChunkPos, setNewPos);
                        this.updateChunkLight(renderGlobal, chunkYZ, this.setLitChunkPos, setNewPos);
                        this.updateChunkLight(renderGlobal, chunkYXZ, this.setLitChunkPos, setNewPos);
                  }

                  this.updateLitChunks(renderGlobal);
                  this.setLitChunkPos = setNewPos;
            }
      }

      private EnumFacing getOpposite(EnumFacing facing) {
            switch(facing) {
            case DOWN:
                  return EnumFacing.UP;
            case UP:
                  return EnumFacing.DOWN;
            case NORTH:
                  return EnumFacing.SOUTH;
            case SOUTH:
                  return EnumFacing.NORTH;
            case EAST:
                  return EnumFacing.WEST;
            case WEST:
                  return EnumFacing.EAST;
            default:
                  return EnumFacing.DOWN;
            }
      }

      private static BlockPos getRenderChunk(BlockPos pos, EnumFacing facing) {
            return new BlockPos(pos.getX() + facing.getFrontOffsetX() * 16, pos.getY() + facing.getFrontOffsetY() * 16, pos.getZ() + facing.getFrontOffsetZ() * 16);
      }

      private void updateChunkLight(RenderGlobal renderGlobal, BlockPos pos, Set setPrevPos, Set setNewPos) {
            if (pos != null) {
                  renderGlobal.markBlockForUpdate(pos.getX() + 8, pos.getY() + 8, pos.getZ() + 8);
                  if (setPrevPos != null) {
                        setPrevPos.remove(pos);
                  }

                  if (setNewPos != null) {
                        setNewPos.add(pos);
                  }

            }
      }

      public void updateLitChunks(RenderGlobal renderGlobal) {
            Iterator it = this.setLitChunkPos.iterator();

            while(it.hasNext()) {
                  BlockPos posOld = (BlockPos)it.next();
                  this.updateChunkLight(renderGlobal, posOld, (Set)null, (Set)null);
            }

      }

      public Entity getEntity() {
            return this.entity;
      }

      public double getLastPosX() {
            return this.lastPosX;
      }

      public double getLastPosY() {
            return this.lastPosY;
      }

      public double getLastPosZ() {
            return this.lastPosZ;
      }

      public int getLastLightLevel() {
            return this.lastLightLevel;
      }

      public boolean isUnderwater() {
            return this.underwater;
      }

      public double getOffsetY() {
            return this.offsetY;
      }

      public String toString() {
            return "Entity: " + this.entity + ", offsetY: " + this.offsetY;
      }
}
