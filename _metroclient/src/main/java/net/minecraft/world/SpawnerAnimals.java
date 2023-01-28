package net.minecraft.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;

import cpw.mods.fml.common.eventhandler.Event.Result;
import net.minecraftforge.event.ForgeEventFactory;

public final class SpawnerAnimals
{
    /** The 17x17 area around the player where mobs can spawn */
    private HashMap eligibleChunksForSpawning = new HashMap();
    private static final String __OBFID = "CL_00000152";
    private Map mapSampleEntitiesByClass = new HashMap();
    private int lastPlayerChunkX = Integer.MAX_VALUE;
    private int lastPlayerChunkZ = Integer.MAX_VALUE;

    protected static ChunkPosition func_151350_a(World p_151350_0_, int p_151350_1_, int p_151350_2_) {
        Chunk var3 = p_151350_0_.getChunkFromChunkCoords(p_151350_1_, p_151350_2_);
        int var4 = p_151350_1_ * 16 + p_151350_0_.rand.nextInt(16);
        int var5 = p_151350_2_ * 16 + p_151350_0_.rand.nextInt(16);
        int var6 = p_151350_0_.rand.nextInt(var3 == null ? p_151350_0_.getActualHeight() : var3.getTopFilledSegment() + 16 - 1);
        return new ChunkPosition(var4, var6, var5);
  }

    /**
     * adds all chunks within the spawn radius of the players to eligibleChunksForSpawning. pars: the world,
     * hostileCreatures, passiveCreatures. returns number of eligible chunks.
     */
    public int findChunksForSpawning(WorldServer par1WorldServer, boolean par2, boolean par3, boolean par4)
    {
        if (!par2 && !par3)
        {
            return 0;
        }
        else
        {
            EntityPlayer player = null;

            if (par1WorldServer.playerEntities.size() == 1)
            {
                player = (EntityPlayer)par1WorldServer.playerEntities.get(0);
            }

            int var5;
            int var8;
            int countEntities;
            ChunkCoordIntPair var39;

            if (player == null || player.chunkCoordX != this.lastPlayerChunkX || player.chunkCoordZ != this.lastPlayerChunkZ || this.eligibleChunksForSpawning.size() <= 0)
            {
                this.eligibleChunksForSpawning.clear();

                for (var5 = 0; var5 < par1WorldServer.playerEntities.size(); ++var5)
                {
                    EntityPlayer var34 = (EntityPlayer)par1WorldServer.playerEntities.get(var5);
                    int var35 = MathHelper.floor_double(var34.posX / 16.0D);
                    var8 = MathHelper.floor_double(var34.posZ / 16.0D);
                    byte var36 = 8;

                    for (int var37 = -var36; var37 <= var36; ++var37)
                    {
                        for (countEntities = -var36; countEntities <= var36; ++countEntities)
                        {
                            boolean var38 = var37 == -var36 || var37 == var36 || countEntities == -var36 || countEntities == var36;
                            var39 = new ChunkCoordIntPair(var37 + var35, countEntities + var8);

                            if (!var38)
                            {
                                this.eligibleChunksForSpawning.put(var39, Boolean.valueOf(false));
                            }
                            else if (!this.eligibleChunksForSpawning.containsKey(var39))
                            {
                                this.eligibleChunksForSpawning.put(var39, Boolean.valueOf(true));
                            }
                        }
                    }
                }

                if (player != null)
                {
                    this.lastPlayerChunkX = player.chunkCoordX;
                    this.lastPlayerChunkZ = player.chunkCoordZ;
                }
            }

            var5 = 0;
            ChunkCoordinates var411 = par1WorldServer.getSpawnPoint();
            EnumCreatureType[] var42 = EnumCreatureType.values();
            var8 = var42.length;

            for (int var43 = 0; var43 < var8; ++var43)
            {
                EnumCreatureType var44 = var42[var43];

                countEntities = par1WorldServer.countEntities(var44, true);

                if ((!var44.getPeacefulCreature() || par3) && (var44.getPeacefulCreature() || par2) && (!var44.getAnimal() || par4) && countEntities <= var44.getMaxNumberOfCreature() * this.eligibleChunksForSpawning.size() / 256)
                {
                    Iterator var46 = this.eligibleChunksForSpawning.keySet().iterator();
                    label143:

                    while (var46.hasNext())
                    {
                        var39 = (ChunkCoordIntPair)var46.next();

                        if (!((Boolean)this.eligibleChunksForSpawning.get(var39)).booleanValue())
                        {
                            Chunk chunk = par1WorldServer.getChunkFromChunkCoords(var39.chunkXPos, var39.chunkZPos);
                            int var14 = var39.chunkXPos * 16 + par1WorldServer.rand.nextInt(16);
                            int var16 = var39.chunkZPos * 16 + par1WorldServer.rand.nextInt(16);
                            int var15 = par1WorldServer.rand.nextInt(chunk == null ? par1WorldServer.getActualHeight() : chunk.getTopFilledSegment() + 16 - 1);

                            if (!par1WorldServer.getBlock(var14, var15, var16).isNormalCube() && par1WorldServer.getBlock(var14, var15, var16).getMaterial() == var44.getCreatureMaterial())
                            {
                                int var17 = 0;
                                int var18 = 0;

                                while (var18 < 3)
                                {
                                    int var19 = var14;
                                    int var20 = var15;
                                    int var21 = var16;
                                    byte var22 = 6;
                                    BiomeGenBase.SpawnListEntry var23 = null;
                                    IEntityLivingData var24 = null;
                                    int var25 = 0;

                                    while (true)
                                    {
                                        if (var25 < 4)
                                        {
                                            label136:
                                            {
                                                var19 += par1WorldServer.rand.nextInt(var22) - par1WorldServer.rand.nextInt(var22);
                                                var20 += par1WorldServer.rand.nextInt(1) - par1WorldServer.rand.nextInt(1);
                                                var21 += par1WorldServer.rand.nextInt(var22) - par1WorldServer.rand.nextInt(var22);

                                                if (canCreatureTypeSpawnAtLocation(var44, par1WorldServer, var19, var20, var21))
                                                {
                                                    float var26 = (float)var19 + 0.5F;
                                                    float var27 = (float)var20;
                                                    float var28 = (float)var21 + 0.5F;

                                                    if (par1WorldServer.getClosestPlayer((double)var26, (double)var27, (double)var28, 24.0D) == null)
                                                    {
                                                        float var29 = var26 - (float)var411.posX;
                                                        float var30 = var27 - (float)var411.posY;
                                                        float var31 = var28 - (float)var411.posZ;
                                                        float var32 = var29 * var29 + var30 * var30 + var31 * var31;

                                                        if (var32 >= 576.0F)
                                                        {
                                                            if (var23 == null)
                                                            {
                                                                var23 = par1WorldServer.spawnRandomCreature(var44, var19, var20, var21);

                                                                if (var23 == null)
                                                                {
                                                                    break label136;
                                                                }
                                                            }

                                                            EntityLiving var41;

                                                            try
                                                            {
                                                                var41 = (EntityLiving)this.mapSampleEntitiesByClass.get(var23.entityClass);

                                                                if (var41 == null)
                                                                {
                                                                    var41 = (EntityLiving)var23.entityClass.getConstructor(new Class[] {World.class}).newInstance(new Object[] {par1WorldServer});
                                                                    this.mapSampleEntitiesByClass.put(var23.entityClass, var41);
                                                                }
                                                            }
                                                            catch (Exception var40)
                                                            {
                                                                var40.printStackTrace();
                                                                return var5;
                                                            }

                                                            var41.setLocationAndAngles((double)var26, (double)var27, (double)var28, par1WorldServer.rand.nextFloat() * 360.0F, 0.0F);

                                                            Result canSpawn = ForgeEventFactory.canEntitySpawn(var41, par1WorldServer, var26, var27, var28);
                                                            if (canSpawn == Result.ALLOW || (canSpawn == Result.DEFAULT && var41.getCanSpawnHere()))
                                                            {
                                                                this.mapSampleEntitiesByClass.put(var23.entityClass, (Object)null);
                                                                ++var17;
                                                                par1WorldServer.spawnEntityInWorld(var41);
                                                                if (!ForgeEventFactory.doSpecialSpawn(var41, par1WorldServer, var26, var27, var28))
                                                                {
                                                                    var24 = var41.onSpawnWithEgg(var24);
                                                                }

                                                                if (var17 >= ForgeEventFactory.getMaxSpawnPackSize(var41))
                                                                {
                                                                    continue label143;
                                                                }
                                                            }

                                                            var5 += var17;
                                                        }
                                                    }
                                                }

                                                ++var25;
                                                continue;
                                            }
                                        }

                                        ++var18;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return var5;
        }
    }

    /**
     * Returns whether or not the specified creature type can spawn at the specified location.
     */
    public static boolean canCreatureTypeSpawnAtLocation(EnumCreatureType p_77190_0_, World p_77190_1_, int p_77190_2_, int p_77190_3_, int p_77190_4_)
    {
        if (p_77190_0_.getCreatureMaterial() == Material.water)
        {
            return p_77190_1_.getBlock(p_77190_2_, p_77190_3_, p_77190_4_).getMaterial().isLiquid() && p_77190_1_.getBlock(p_77190_2_, p_77190_3_ - 1, p_77190_4_).getMaterial().isLiquid() && !p_77190_1_.getBlock(p_77190_2_, p_77190_3_ + 1, p_77190_4_).isNormalCube();
        }
        else if (!World.doesBlockHaveSolidTopSurface(p_77190_1_, p_77190_2_, p_77190_3_ - 1, p_77190_4_))
        {
            return false;
        }
        else
        {
            Block block = p_77190_1_.getBlock(p_77190_2_, p_77190_3_ - 1, p_77190_4_);
            boolean spawnBlock = block.canCreatureSpawn(p_77190_0_, p_77190_1_, p_77190_2_, p_77190_3_ - 1, p_77190_4_);
            return spawnBlock && block != Blocks.bedrock && !p_77190_1_.getBlock(p_77190_2_, p_77190_3_, p_77190_4_).isNormalCube() && !p_77190_1_.getBlock(p_77190_2_, p_77190_3_, p_77190_4_).getMaterial().isLiquid() && !p_77190_1_.getBlock(p_77190_2_, p_77190_3_ + 1, p_77190_4_).isNormalCube();
        }
    }

    /**
     * Called during chunk generation to spawn initial creatures.
     */
    public static void performWorldGenSpawning(World p_77191_0_, BiomeGenBase p_77191_1_, int p_77191_2_, int p_77191_3_, int p_77191_4_, int p_77191_5_, Random p_77191_6_)
    {
        List list = p_77191_1_.getSpawnableList(EnumCreatureType.creature);

        if (!list.isEmpty())
        {
            while (p_77191_6_.nextFloat() < p_77191_1_.getSpawningChance())
            {
                BiomeGenBase.SpawnListEntry spawnlistentry = (BiomeGenBase.SpawnListEntry)WeightedRandom.getRandomItem(p_77191_0_.rand, list);
                IEntityLivingData ientitylivingdata = null;
                int i1 = spawnlistentry.minGroupCount + p_77191_6_.nextInt(1 + spawnlistentry.maxGroupCount - spawnlistentry.minGroupCount);
                int j1 = p_77191_2_ + p_77191_6_.nextInt(p_77191_4_);
                int k1 = p_77191_3_ + p_77191_6_.nextInt(p_77191_5_);
                int l1 = j1;
                int i2 = k1;

                for (int j2 = 0; j2 < i1; ++j2)
                {
                    boolean flag = false;

                    for (int k2 = 0; !flag && k2 < 4; ++k2)
                    {
                        int l2 = p_77191_0_.getTopSolidOrLiquidBlock(j1, k1);

                        if (canCreatureTypeSpawnAtLocation(EnumCreatureType.creature, p_77191_0_, j1, l2, k1))
                        {
                            float f = (float)j1 + 0.5F;
                            float f1 = (float)l2;
                            float f2 = (float)k1 + 0.5F;
                            EntityLiving entityliving;

                            try
                            {
                                entityliving = (EntityLiving)spawnlistentry.entityClass.getConstructor(new Class[] {World.class}).newInstance(new Object[] {p_77191_0_});
                            }
                            catch (Exception exception)
                            {
                                exception.printStackTrace();
                                continue;
                            }

                            entityliving.setLocationAndAngles((double)f, (double)f1, (double)f2, p_77191_6_.nextFloat() * 360.0F, 0.0F);
                            p_77191_0_.spawnEntityInWorld(entityliving);
                            ientitylivingdata = entityliving.onSpawnWithEgg(ientitylivingdata);
                            flag = true;
                        }

                        j1 += p_77191_6_.nextInt(5) - p_77191_6_.nextInt(5);

                        for (k1 += p_77191_6_.nextInt(5) - p_77191_6_.nextInt(5); j1 < p_77191_2_ || j1 >= p_77191_2_ + p_77191_4_ || k1 < p_77191_3_ || k1 >= p_77191_3_ + p_77191_4_; k1 = i2 + p_77191_6_.nextInt(5) - p_77191_6_.nextInt(5))
                        {
                            j1 = l1 + p_77191_6_.nextInt(5) - p_77191_6_.nextInt(5);
                        }
                    }
                }
            }
        }
    }
}