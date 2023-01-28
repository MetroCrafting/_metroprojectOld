package net.minecraft.server.management;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.network.play.server.S26PacketMapChunkBulk;
import net.minecraft.src.CompactArrayList;
import net.minecraft.src.WorldServerOF;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.LongHashMap;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PlayerManager
{
    private static final Logger field_152627_a = LogManager.getLogger();
    private final WorldServer theWorldServer;
    /** players in the current instance */
    private final List players = new ArrayList();
    /** A map of chunk position (two ints concatenated into a long) to PlayerInstance */
    private final LongHashMap playerInstances = new LongHashMap();
    /**
     * contains a PlayerInstance for every chunk they can see. the "player instance" cotains a list of all players who
     * can also that chunk
     */
    private final List chunkWatcherWithPlayers = new ArrayList();
    /** This field is using when chunk should be processed (every 8000 ticks) */
    private final List playerInstanceList = new ArrayList();
    /** Number of chunks the server sends to the client. Valid 3<=x<=15. In server.properties. */
    private int playerViewRadius;
    /** time what is using to check if InhabitedTime should be calculated */
    private long previousTotalWorldTime;
    /** x, z direction vectors: east, south, west, north */
    private final int[][] xzDirectionsConst = new int[][] {{1, 0}, {0, 1}, { -1, 0}, {0, -1}};
    public CompactArrayList chunkCoordsNotLoaded = new CompactArrayList(100, 0.8F);
    private static final String __OBFID = "CL_00001434";
    

    public PlayerManager(WorldServer par1Minecraft)
    {
        this.theWorldServer = par1Minecraft;
        this.func_152622_a(par1Minecraft.func_73046_m().getConfigurationManager().getViewDistance());
    }

    public WorldServer getWorldServer()
    {
        return this.theWorldServer;
    }

    /**
     * updates all the player instances that need to be updated
     */
    public void updatePlayerInstances()
    {
        long i = this.theWorldServer.getTotalWorldTime();
        int j;
        PlayerManager.PlayerInstance playerinstance;

        if (i - this.previousTotalWorldTime > 8000L)
        {
            this.previousTotalWorldTime = i;

            for (j = 0; j < this.playerInstanceList.size(); ++j)
            {
                playerinstance = (PlayerManager.PlayerInstance)this.playerInstanceList.get(j);
                playerinstance.sendChunkUpdate();
                playerinstance.processChunk();
            }
        }
        else
        {
            for (j = 0; j < this.chunkWatcherWithPlayers.size(); ++j)
            {
                playerinstance = (PlayerManager.PlayerInstance)this.chunkWatcherWithPlayers.get(j);
                playerinstance.sendChunkUpdate();
            }
        }

        this.chunkWatcherWithPlayers.clear();

        if (this.players.isEmpty())
        {
            WorldProvider worldprovider = this.theWorldServer.provider;

            if (!worldprovider.canRespawnHere())
            {
                this.theWorldServer.theChunkProviderServer.unloadAllChunks();
            }
        }

        if (this.chunkCoordsNotLoaded.size() > 0)
        {
            for (int var22 = 0; var22 < this.players.size(); ++var22)
            {
                EntityPlayerMP player = (EntityPlayerMP)this.players.get(var22);
                int px = player.chunkCoordX;
                int pz = player.chunkCoordZ;
                int maxRadius = this.playerViewRadius + 1;
                int maxRadius2 = maxRadius / 2;
                int maxDistSq = maxRadius * maxRadius + maxRadius2 * maxRadius2;
                int bestDistSq = maxDistSq;
                int bestIndex = -1;
                PlayerManager.PlayerInstance bestCw = null;
                ChunkCoordIntPair bestCoords = null;

                for (int chunk = 0; chunk < this.chunkCoordsNotLoaded.size(); ++chunk)
                {
                    ChunkCoordIntPair coords = (ChunkCoordIntPair)this.chunkCoordsNotLoaded.get(chunk);

                    if (coords != null)
                    {
                        PlayerManager.PlayerInstance cw = this.getOrCreateChunkWatcher(coords.chunkXPos, coords.chunkZPos, false);

                        if (cw != null && !cw.chunkLoaded)
                        {
                            int dx = px - coords.chunkXPos;
                            int dz = pz - coords.chunkZPos;
                            int distSq = dx * dx + dz * dz;

                            if (distSq < bestDistSq)
                            {
                                bestDistSq = distSq;
                                bestIndex = chunk;
                                bestCw = cw;
                                bestCoords = coords;
                            }
                        }
                        else
                        {
                            this.chunkCoordsNotLoaded.set(chunk, (Object)null);
                        }
                    }
                }

                if (bestIndex >= 0)
                {
                    this.chunkCoordsNotLoaded.set(bestIndex, (Object)null);
                }

                if (bestCw != null)
                {
                    bestCw.chunkLoaded = true;
                    this.getWorldServer().theChunkProviderServer.loadChunk(bestCoords.chunkXPos, bestCoords.chunkZPos);
                    bestCw.sendThisChunkToAllPlayers();
                    break;
                }
            }

            this.chunkCoordsNotLoaded.compact();
        }
    }

    public boolean func_152621_a(int p_152621_1_, int p_152621_2_)
    {
        long var3 = (long)p_152621_1_ + 2147483647L | (long)p_152621_2_ + 2147483647L << 32;
        return this.playerInstances.getValueByKey(var3) != null;
    }

    public PlayerManager.PlayerInstance getOrCreateChunkWatcher(int par1, int par2, boolean par3)
    {
        return this.getOrCreateChunkWatcher(par1, par2, par3, false);
    }

    public PlayerManager.PlayerInstance getOrCreateChunkWatcher(int par1, int par2, boolean par3, boolean lazy)
    {
        long var4 = (long)par1 + 2147483647L | (long)par2 + 2147483647L << 32;
        PlayerManager.PlayerInstance var6 = (PlayerManager.PlayerInstance)this.playerInstances.getValueByKey(var4);

        if (var6 == null && par3)
        {
            var6 = new PlayerManager.PlayerInstance(par1, par2, lazy);
            this.playerInstances.add(var4, var6);
            this.playerInstanceList.add(var6);
        }

        return var6;
    }
    
    public void markBlockForUpdate(int p_151250_1_, int p_151250_2_, int p_151250_3_)
    {
        int l = p_151250_1_ >> 4;
        int i1 = p_151250_3_ >> 4;
        PlayerManager.PlayerInstance playerinstance = this.getOrCreateChunkWatcher(l, i1, false);

        if (playerinstance != null)
        {
            playerinstance.flagChunkForUpdate(p_151250_1_ & 15, p_151250_2_, p_151250_3_ & 15);
        }
    }

    /**
     * Adds an EntityPlayerMP to the PlayerManager.
     */
    public void addPlayer(EntityPlayerMP par1EntityPlayerMP)
    {
        int var2 = (int)par1EntityPlayerMP.posX >> 4;
        int var3 = (int)par1EntityPlayerMP.posZ >> 4;
        par1EntityPlayerMP.managedPosX = par1EntityPlayerMP.posX;
        par1EntityPlayerMP.managedPosZ = par1EntityPlayerMP.posZ;
        ArrayList spawnList = new ArrayList(1);

        for (int var4 = var2 - this.playerViewRadius; var4 <= var2 + this.playerViewRadius; ++var4)
        {
            for (int var5 = var3 - this.playerViewRadius; var5 <= var3 + this.playerViewRadius; ++var5)
            {
                this.getOrCreateChunkWatcher(var4, var5, true).addPlayer(par1EntityPlayerMP);

                if (var4 >= var2 - 1 && var4 <= var2 + 1 && var5 >= var3 - 1 && var5 <= var3 + 1)
                {
                    Chunk spawnChunk = this.getWorldServer().theChunkProviderServer.loadChunk(var4, var5);
                    spawnList.add(spawnChunk);
                }
            }
        }

        par1EntityPlayerMP.playerNetServerHandler.sendPacket(new S26PacketMapChunkBulk(spawnList));
        this.players.add(par1EntityPlayerMP);
        this.filterChunkLoadQueue(par1EntityPlayerMP);
    }

    /**
     * Removes all chunks from the given player's chunk load queue that are not in viewing range of the player.
     */
    public void filterChunkLoadQueue(EntityPlayerMP p_72691_1_)
    {
        ArrayList arraylist = new ArrayList(p_72691_1_.loadedChunks);
        int i = 0;
        int j = this.playerViewRadius;
        int k = (int)p_72691_1_.posX >> 4;
        int l = (int)p_72691_1_.posZ >> 4;
        int i1 = 0;
        int j1 = 0;
        ChunkCoordIntPair chunkcoordintpair = this.getOrCreateChunkWatcher(k, l, true).chunkLocation;
        p_72691_1_.loadedChunks.clear();

        if (arraylist.contains(chunkcoordintpair))
        {
            p_72691_1_.loadedChunks.add(chunkcoordintpair);
        }

        int k1;

        for (k1 = 1; k1 <= j * 2; ++k1)
        {
            for (int l1 = 0; l1 < 2; ++l1)
            {
                int[] aint = this.xzDirectionsConst[i++ % 4];

                for (int i2 = 0; i2 < k1; ++i2)
                {
                    i1 += aint[0];
                    j1 += aint[1];
                    chunkcoordintpair = this.getOrCreateChunkWatcher(k + i1, l + j1, true).chunkLocation;

                    if (arraylist.contains(chunkcoordintpair))
                    {
                        p_72691_1_.loadedChunks.add(chunkcoordintpair);
                    }
                }
            }
        }

        i %= 4;

        for (k1 = 0; k1 < j * 2; ++k1)
        {
            i1 += this.xzDirectionsConst[i][0];
            j1 += this.xzDirectionsConst[i][1];
            chunkcoordintpair = this.getOrCreateChunkWatcher(k + i1, l + j1, true).chunkLocation;

            if (arraylist.contains(chunkcoordintpair))
            {
                p_72691_1_.loadedChunks.add(chunkcoordintpair);
            }
        }
    }

    /**
     * Removes an EntityPlayerMP from the PlayerManager.
     */
    public void removePlayer(EntityPlayerMP p_72695_1_)
    {
        int i = (int)p_72695_1_.managedPosX >> 4;
        int j = (int)p_72695_1_.managedPosZ >> 4;

        for (int k = i - this.playerViewRadius; k <= i + this.playerViewRadius; ++k)
        {
            for (int l = j - this.playerViewRadius; l <= j + this.playerViewRadius; ++l)
            {
                PlayerManager.PlayerInstance playerinstance = this.getOrCreateChunkWatcher(k, l, false);

                if (playerinstance != null)
                {
                    playerinstance.removePlayer(p_72695_1_);
                }
            }
        }

        this.players.remove(p_72695_1_);
    }

    /**
     * Determine if two rectangles centered at the given points overlap for the provided radius. Arguments: x1, z1, x2,
     * z2, radius.
     */
    private boolean overlaps(int p_72684_1_, int p_72684_2_, int p_72684_3_, int p_72684_4_, int p_72684_5_)
    {
        int j1 = p_72684_1_ - p_72684_3_;
        int k1 = p_72684_2_ - p_72684_4_;
        return j1 >= -p_72684_5_ && j1 <= p_72684_5_ ? k1 >= -p_72684_5_ && k1 <= p_72684_5_ : false;
    }

    /**
     * Update which chunks the player needs info on.
     */
    public void updatePlayerPertinentChunks(EntityPlayerMP par1EntityPlayerMP)
    {
        int var2 = (int)par1EntityPlayerMP.posX >> 4;
        int var3 = (int)par1EntityPlayerMP.posZ >> 4;
        double var4 = par1EntityPlayerMP.managedPosX - par1EntityPlayerMP.posX;
        double var6 = par1EntityPlayerMP.managedPosZ - par1EntityPlayerMP.posZ;
        double var8 = var4 * var4 + var6 * var6;

        if (var8 >= 64.0D)
        {
            int var10 = (int)par1EntityPlayerMP.managedPosX >> 4;
            int var11 = (int)par1EntityPlayerMP.managedPosZ >> 4;
            int var12 = this.playerViewRadius;
            int var13 = var2 - var10;
            int var14 = var3 - var11;

            if (var13 != 0 || var14 != 0)
            {
                WorldServerOF worldServerOf = null;

                if (this.theWorldServer instanceof WorldServerOF)
                {
                    worldServerOf = (WorldServerOF)this.theWorldServer;
                }

                for (int var15 = var2 - var12; var15 <= var2 + var12; ++var15)
                {
                    for (int var16 = var3 - var12; var16 <= var3 + var12; ++var16)
                    {
                        if (!this.overlaps(var15, var16, var10, var11, var12))
                        {
                            this.getOrCreateChunkWatcher(var15, var16, true, true).addPlayer(par1EntityPlayerMP);

                            if (worldServerOf != null)
                            {
                                worldServerOf.addChunkToTickOnce(var15, var16);
                            }
                        }

                        if (!this.overlaps(var15 - var13, var16 - var14, var2, var3, var12))
                        {
                            PlayerManager.PlayerInstance var17 = this.getOrCreateChunkWatcher(var15 - var13, var16 - var14, false);

                            if (var17 != null)
                            {
                                var17.removePlayer(par1EntityPlayerMP);
                            }
                        }
                    }
                }

                this.filterChunkLoadQueue(par1EntityPlayerMP);
                par1EntityPlayerMP.managedPosX = par1EntityPlayerMP.posX;
                par1EntityPlayerMP.managedPosZ = par1EntityPlayerMP.posZ;
            }
        }
    }

    public boolean isPlayerWatchingChunk(EntityPlayerMP par1EntityPlayerMP, int par2, int par3)
    {
        PlayerManager.PlayerInstance var4 = this.getOrCreateChunkWatcher(par2, par3, false);
        return var4 != null && var4.playersWatchingChunk.contains(par1EntityPlayerMP) && !par1EntityPlayerMP.loadedChunks.contains(var4.chunkLocation);
    }

    public void func_152622_a(int p_152622_1_)
    {
        p_152622_1_ = MathHelper.clamp_int(p_152622_1_, 3, 32);

        if (p_152622_1_ != this.playerViewRadius)
        {
            int j = p_152622_1_ - this.playerViewRadius;
            Iterator iterator = this.players.iterator();

            while (iterator.hasNext())
            {
                EntityPlayerMP entityplayermp = (EntityPlayerMP)iterator.next();
                int k = (int)entityplayermp.posX >> 4;
                int l = (int)entityplayermp.posZ >> 4;
                int i1;
                int j1;

                if (j > 0)
                {
                    for (i1 = k - p_152622_1_; i1 <= k + p_152622_1_; ++i1)
                    {
                        for (j1 = l - p_152622_1_; j1 <= l + p_152622_1_; ++j1)
                        {
                            PlayerManager.PlayerInstance playerinstance = this.getOrCreateChunkWatcher(i1, j1, true);

                            if (!playerinstance.playersWatchingChunk.contains(entityplayermp))
                            {
                                playerinstance.addPlayer(entityplayermp);
                            }
                        }
                    }
                }
                else
                {
                    for (i1 = k - this.playerViewRadius; i1 <= k + this.playerViewRadius; ++i1)
                    {
                        for (j1 = l - this.playerViewRadius; j1 <= l + this.playerViewRadius; ++j1)
                        {
                            if (!this.overlaps(i1, j1, k, l, p_152622_1_))
                            {
                                this.getOrCreateChunkWatcher(i1, j1, true).removePlayer(entityplayermp);
                            }
                        }
                    }
                }
            }

            this.playerViewRadius = p_152622_1_;
        }
    }

    /**
     * Get the furthest viewable block given player's view distance
     */
    public static int getFurthestViewableBlock(int par0)
    {
        return par0 * 16 - 16;
    }

    public class PlayerInstance
    {
        private final List playersWatchingChunk;
        /** note: this is final */
        private final ChunkCoordIntPair chunkLocation;
        private short[] locationOfBlockChange;
        private int numberOfTilesToUpdate;
        /** Integer field where each bit means to make update 16x16x16 division of chunk (from bottom). */
        private int flagsYAreasToUpdate;
        /** time what is using when chunk InhabitedTime is being calculated */
        private long previousWorldTime;
        public boolean chunkLoaded;

        public PlayerInstance(int par2, int par3)
        {
            this(par2, par3, false);
        }

        public PlayerInstance(int par2, int par3, boolean lazy)
        {
            this.playersWatchingChunk = new ArrayList();
            this.locationOfBlockChange = new short[64];
            this.chunkLoaded = false;
            this.chunkLocation = new ChunkCoordIntPair(par2, par3);
           // boolean useLazy = lazy && Config.isLazyChunkLoading();

//            if (!PlayerManager.this.getWorldServer().theChunkProviderServer.chunkExists(par2, par3))
//            {
//                PlayerManager.this.chunkCoordsNotLoaded.add(this.chunkLocation);
//                this.chunkLoaded = false;
//            }
//            else
//            {
                PlayerManager.this.getWorldServer().theChunkProviderServer.loadChunk(par2, par3);
                this.chunkLoaded = true;
//            }
        }

        public void addPlayer(EntityPlayerMP par1EntityPlayerMP)
        {
            if (this.playersWatchingChunk.contains(par1EntityPlayerMP))
            {
                PlayerManager.field_152627_a.debug("Failed to add player. {} already is in chunk {}, {}", par1EntityPlayerMP, this.chunkLocation.chunkXPos, this.chunkLocation.chunkZPos);
            }
            else
            {
                if (this.playersWatchingChunk.isEmpty())
                {
                    this.previousWorldTime = PlayerManager.this.theWorldServer.getTotalWorldTime();
                }

                this.playersWatchingChunk.add(par1EntityPlayerMP);
                par1EntityPlayerMP.loadedChunks.add(this.chunkLocation);
            }
        }

        public void removePlayer(EntityPlayerMP par1EntityPlayerMP)
        {
            this.removePlayer(par1EntityPlayerMP, true);
        }

        public void removePlayer(EntityPlayerMP par1EntityPlayerMP, boolean sendData)
        {
            if (this.playersWatchingChunk.contains(par1EntityPlayerMP))
            {
                Chunk var2 = PlayerManager.this.theWorldServer.getChunkFromChunkCoords(this.chunkLocation.chunkXPos, this.chunkLocation.chunkZPos);

                if (sendData && var2.func_150802_k())
                {
                    par1EntityPlayerMP.playerNetServerHandler.sendPacket(new S21PacketChunkData(var2, true, 0));
                }

                this.playersWatchingChunk.remove(par1EntityPlayerMP);
                par1EntityPlayerMP.loadedChunks.remove(this.chunkLocation);

                net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.ChunkWatchEvent.UnWatch(chunkLocation, par1EntityPlayerMP));

                if (this.playersWatchingChunk.isEmpty())
                {
                    long var3 = (long)this.chunkLocation.chunkXPos + 2147483647L | (long)this.chunkLocation.chunkZPos + 2147483647L << 32;
                    this.increaseInhabitedTime(var2);
                    PlayerManager.this.playerInstances.remove(var3);
                    PlayerManager.this.playerInstanceList.remove(this);

                    if (this.numberOfTilesToUpdate > 0)
                    {
                        PlayerManager.this.chunkWatcherWithPlayers.remove(this);
                    }

                    if (this.chunkLoaded)
                    {
                        PlayerManager.this.getWorldServer().theChunkProviderServer.unloadChunksIfNotNearSpawn(this.chunkLocation.chunkXPos, this.chunkLocation.chunkZPos);
                    }
                }
            }
        }

        /**
         * This method currently only increases chunk inhabited time. Extension is possible in next versions
         */
        public void processChunk()
        {
            this.increaseInhabitedTime(PlayerManager.this.theWorldServer.getChunkFromChunkCoords(this.chunkLocation.chunkXPos, this.chunkLocation.chunkZPos));
        }

        /**
         * Increases chunk inhabited time every 8000 ticks
         */
        private void increaseInhabitedTime(Chunk p_111196_1_)
        {
            p_111196_1_.inhabitedTime += PlayerManager.this.theWorldServer.getTotalWorldTime() - this.previousWorldTime;
            this.previousWorldTime = PlayerManager.this.theWorldServer.getTotalWorldTime();
        }

        public void flagChunkForUpdate(int p_151253_1_, int p_151253_2_, int p_151253_3_)
        {
            if (this.numberOfTilesToUpdate == 0)
            {
                PlayerManager.this.chunkWatcherWithPlayers.add(this);
            }

            this.flagsYAreasToUpdate |= 1 << (p_151253_2_ >> 4);

            if (this.numberOfTilesToUpdate < 64)
            {
                short var4 = (short)(p_151253_1_ << 12 | p_151253_3_ << 8 | p_151253_2_);

                for (int var5 = 0; var5 < this.numberOfTilesToUpdate; ++var5)
                {
                    if (this.locationOfBlockChange[var5] == var4)
                    {
                        return;
                    }
                }

                this.locationOfBlockChange[this.numberOfTilesToUpdate++] = var4;
            }
        }

        public void sendToAllPlayersWatchingChunk(Packet p_151251_1_)
        {
            for (int i = 0; i < this.playersWatchingChunk.size(); ++i)
            {
                EntityPlayerMP entityplayermp = (EntityPlayerMP)this.playersWatchingChunk.get(i);

                if (!entityplayermp.loadedChunks.contains(this.chunkLocation))
                {
                    entityplayermp.playerNetServerHandler.sendPacket(p_151251_1_);
                }
            }
        }

        //TODO: [APULAZ]: Чужой код
        @SuppressWarnings("unused")
        public void sendChunkUpdate()
        {
            if (this.numberOfTilesToUpdate != 0)
            {
                int i;
                int j;
                int k;

                if (this.numberOfTilesToUpdate == 1)
                {
                    i = this.chunkLocation.chunkXPos * 16 + (this.locationOfBlockChange[0] >> 12 & 15);
                    j = this.locationOfBlockChange[0] & 255;
                    k = this.chunkLocation.chunkZPos * 16 + (this.locationOfBlockChange[0] >> 8 & 15);
                    this.sendToAllPlayersWatchingChunk(new S23PacketBlockChange(i, j, k, PlayerManager.this.theWorldServer));

                    if (PlayerManager.this.theWorldServer.getBlock(i, j, k).hasTileEntity(PlayerManager.this.theWorldServer.getBlockMetadata(i, j, k)))
                    {
                        this.sendTileToAllPlayersWatchingChunk(PlayerManager.this.theWorldServer.getTileEntity(i, j, k));
                    }
                }
                else
                {
                    int l;

                    if (this.numberOfTilesToUpdate >= net.minecraftforge.common.ForgeModContainer.clumpingThreshold)
//                    if(this.numberOfTilesToUpdate == 64)
                    {
                        i = this.chunkLocation.chunkXPos * 16;
                        j = this.chunkLocation.chunkZPos * 16;
                        this.sendToAllPlayersWatchingChunk(new S21PacketChunkData(PlayerManager.this.theWorldServer.getChunkFromChunkCoords(this.chunkLocation.chunkXPos, this.chunkLocation.chunkZPos), false, this.flagsYAreasToUpdate));

                        // Forge: Grabs ALL tile entities is costly on a modded server, only send needed ones
                        for (k = 0; false && k < 16; ++k)
//                        for (k = 0; k < 16; ++k)
                        {
                            if ((this.flagsYAreasToUpdate & 1 << k) != 0)
                            {
                                l = k << 4;
                                List list = PlayerManager.this.theWorldServer.func_147486_a(i, l, j, i + 16, l + 16, j + 16);

                                for (int i1 = 0; i1 < list.size(); ++i1)
                                {
                                    this.sendTileToAllPlayersWatchingChunk((TileEntity)list.get(i1));
                                }
                            }
                        }
                    }
                    else
                    {
                        this.sendToAllPlayersWatchingChunk(new S22PacketMultiBlockChange(this.numberOfTilesToUpdate, this.locationOfBlockChange, PlayerManager.this.theWorldServer.getChunkFromChunkCoords(this.chunkLocation.chunkXPos, this.chunkLocation.chunkZPos)));
                    }

                    { //Forge: Send only the tile entities that are updated, Adding this brace lets us keep the indent and the patch small
                        WorldServer world = PlayerManager.this.theWorldServer;
                        for (i = 0; i < this.numberOfTilesToUpdate; ++i)
                        {
                            j = this.chunkLocation.chunkXPos * 16 + (this.locationOfBlockChange[i] >> 12 & 15);
                            k = this.locationOfBlockChange[i] & 255;
                            l = this.chunkLocation.chunkZPos * 16 + (this.locationOfBlockChange[i] >> 8 & 15);

                            if (world.getBlock(j, k, l).hasTileEntity(world.getBlockMetadata(j, k, l)))
                            {
                                this.sendTileToAllPlayersWatchingChunk(PlayerManager.this.theWorldServer.getTileEntity(j, k, l));
                            }
                        }
                    }
                }

                this.numberOfTilesToUpdate = 0;
                this.flagsYAreasToUpdate = 0;
            }
        }

        private void sendTileToAllPlayersWatchingChunk(TileEntity p_151252_1_)
        {
            if (p_151252_1_ != null)
            {
                Packet packet = p_151252_1_.getDescriptionPacket();

                if (packet != null)
                {
                    this.sendToAllPlayersWatchingChunk(packet);
                }
            }
        }

        public void sendThisChunkToAllPlayers()
        {
            for (int i = 0; i < this.playersWatchingChunk.size(); ++i)
            {
                EntityPlayerMP player = (EntityPlayerMP)this.playersWatchingChunk.get(i);
                Chunk chunk = PlayerManager.this.getWorldServer().getChunkFromChunkCoords(this.chunkLocation.chunkXPos, this.chunkLocation.chunkZPos);
                ArrayList list = new ArrayList(1);
                list.add(chunk);
                player.playerNetServerHandler.sendPacket(new S26PacketMapChunkBulk(list));
            }
        }
    }
}