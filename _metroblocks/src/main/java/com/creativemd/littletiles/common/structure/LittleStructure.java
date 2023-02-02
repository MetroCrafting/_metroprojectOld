package com.creativemd.littletiles.common.structure;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraftforge.common.util.ForgeDirection;

import com.creativemd.creativecore.common.gui.SubGui;
import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTile.LittleTilePosition;
import com.creativemd.littletiles.common.utils.small.LittleTileCoord;
import com.creativemd.littletiles.utils.PreviewTile;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class LittleStructure {

    private static final HashMap<String, LittleStructureEntry> structures = new HashMap<>();

    public static ArrayList<String> getStructureNames() {
        return new ArrayList<>(structures.keySet());
    }

    public static void registerLittleStructure(String id, LittleStructure structure) {
        registerLittleStructure(id, new LittleStructureEntry(structure.getClass(), structure));
    }

    public static void registerLittleStructure(String id, LittleStructureEntry entry) {
        if (structures.containsKey(id)) System.out.println("ID is already taken! id=" + id);
        else if (structures.containsValue(entry)) System.out.println("Already registered class=" + entry);
        else {
            structures.put(id, entry);
        }
    }

    public static String getIDByClass(Class<? extends LittleStructure> classStructure) {
        for (String id : structures.keySet()) {
            if (classStructure.equals(structures.get(id).structureClass)) return id;
        }
        return "";
    }

    public static Class<? extends LittleStructure> getClassByID(String id) {
        LittleStructureEntry entry = structures.get(id);
        if (entry != null) return entry.structureClass;
        return null;
    }

    public static LittleStructureEntry getEntryByID(String id) {
        return structures.get(id);
    }

    public static void initStructures() {
        registerLittleStructure("fixed", new LittleFixedStructure());
        registerLittleStructure("chair", new LittleChair());
        registerLittleStructure("door", new LittleDoor());
        registerLittleStructure("ladder", new LittleLadder());
        registerLittleStructure("bed", new LittleBed());
    }

    public static LittleStructure createAndLoadStructure(NBTTagCompound nbt, LittleTile mainTile) {
        if (nbt == null) return null;
        String id = nbt.getString("id");
        LittleStructureEntry entry = getEntryByID(id);
        if (entry != null) {
            Class<? extends LittleStructure> classStructure = entry.structureClass;
            if (classStructure != null) {
                LittleStructure structure = null;
                try {
                    structure = classStructure.getConstructor().newInstance();
                } catch (Exception e) {
                    System.out.println("Found invalid structureID=" + id);
                }
                structure.mainTile = mainTile;
                structure.loadFromNBT(nbt);
                return structure;
            }
        }
        return null;
    }

    public LittleTile mainTile;
    private ArrayList<LittleTile> tiles = null;

    public void setTiles(ArrayList<LittleTile> tiles) {
        this.tiles = tiles;
    }

    public ArrayList<LittleTile> getTiles() {
        if (tiles == null) if (!loadTiles()) {
            return new ArrayList<>();
        }
        return tiles;
    }

    public boolean hasLoaded() {
        loadTiles();
        return tilesToLoad == null || tilesToLoad.size() == 0;
    }

    public boolean loadTiles() {
        if (mainTile != null) {
            if (tiles == null) {
                tiles = new ArrayList<>();
                tiles.add(mainTile);
            }

            if (tilesToLoad == null) return true;

            int i = 0;
            while (i < tilesToLoad.size()) {
                if (checkForTile(mainTile.te.getWorldObj(), tilesToLoad.get(i))) tilesToLoad.remove(i);
                else i++;
            }

            if (tilesToLoad.size() == 0) tilesToLoad = null;
            return true;
        }
        return false;
    }

    // public ArrayList<LittleTilePosition> tilesToLoad = null;
    public ArrayList<LittleTileCoord> tilesToLoad = null;

    public ItemStack dropStack;

    public LittleStructure() {

    }

    public void loadFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("stack")) dropStack = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("stack"));

        // LoadTiles
        if (nbt.hasKey("count")) {
            tilesToLoad = new ArrayList<>();
            int count = nbt.getInteger("count");
            for (int i = 0; i < count; i++) {
                LittleTileCoord coord;
                if (nbt.hasKey("i" + i + "coX")) {
                    LittleTilePosition pos = new LittleTilePosition("i" + i, nbt);
                    coord = new LittleTileCoord(mainTile.te, pos.coord, pos.position);
                } else {
                    coord = new LittleTileCoord("i" + i, nbt);
                }

                tilesToLoad.add(coord);
            }
        }

        loadFromNBTExtra(nbt);
    }

    protected abstract void loadFromNBTExtra(NBTTagCompound nbt);

    public void writeToNBT(NBTTagCompound nbt) {
        if (dropStack != null) {
            NBTTagCompound nbtStack = new NBTTagCompound();
            dropStack.writeToNBT(nbtStack);
            nbt.setTag("stack", nbtStack);
        }

        nbt.setString("id", getIDOfStructure());

        // SaveTiles
        if (tiles != null) {
            nbt.setInteger("count", tiles.size());
            for (int i = 0; i < tiles.size(); i++) {
                if (tiles.get(i).isStructureBlock) {
                    tiles.get(i).updateCorner();
                    new LittleTileCoord(mainTile.te, tiles.get(i).te.getCoord(), tiles.get(i).cornerVec.copy())
                            .writeToNBT("i" + i, nbt);
                    // new LittleTilePosition().writeToNBT("i" + i, nbt);
                }
                // tiles.get(i).pos.writeToNBT("i" + i, nbt);
            }
        }

        if (tilesToLoad != null) {
            int start = nbt.getInteger("count");
            nbt.setInteger("count", start + tilesToLoad.size());
            for (int i = 0; i < tilesToLoad.size(); i++) {
                tilesToLoad.get(i).writeToNBT("i" + (i + start), nbt);
            }
        }

        // if(mainTile == null)
        // System.out.println("Couldn't save tiles!!!" + mainTile.te.getCoord());

        writeToNBTExtra(nbt);
    }

    protected abstract void writeToNBTExtra(NBTTagCompound nbt);

    public boolean checkForTile(World world, LittleTileCoord pos) {
        ChunkCoordinates coord = pos.getAbsolutePosition(mainTile.te);
        Chunk chunk = world.getChunkFromBlockCoords(coord.posX, coord.posZ);
        if (!(chunk instanceof EmptyChunk)) {
            // chunk.isChunkLoaded
            TileEntity tileEntity = world.getTileEntity(coord.posX, coord.posY, coord.posZ);
            if (tileEntity instanceof TileEntityLittleTiles) {
                LittleTile tile = ((TileEntityLittleTiles) tileEntity).getTile(pos.position);
                if (tile != null && tile.isStructureBlock) {
                    if (!tiles.contains(tile)) tiles.add(tile);
                    tile.structure = this;
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * @Deprecated public boolean checkForTile(World world, LittleTilePosition pos) { Chunk chunk =
     * world.getChunkFromBlockCoords(pos.coord.posX, pos.coord.posZ); if(!(chunk instanceof EmptyChunk)) {
     * //chunk.isChunkLoaded TileEntity tileEntity = world.getTileEntity(pos.coord.posX, pos.coord.posY,
     * pos.coord.posZ); if(tileEntity instanceof TileEntityLittleTiles) { LittleTile tile = ((TileEntityLittleTiles)
     * tileEntity).getTile(pos.position); if(tile != null && tile.isStructureBlock) { if(!tiles.contains(tile))
     * tiles.add(tile); tile.structure = this; return true; } } } return false; }
     */

    // ====================Rendering====================

    public ArrayList<PreviewTile> getSpecialTiles() {
        return new ArrayList<>();
    }

    // ====================LittleTile-Stuff====================

    public void onLittleTileDestory() {
        ArrayList<LittleTile> tiles = getTiles();
        for (LittleTile tile : tiles) {
            tile.te.removeTile(tile);
            // tiles.get(i).destroy();
        }
    }

    public ItemStack getStructureDrop() {
        return dropStack;
    }

    public boolean onBlockActivated(World world, LittleTile tile, int x, int y, int z, EntityPlayer player, int side,
            float moveX, float moveY, float moveZ) {
        return false;
    }

    // ====================SORTING====================

    public HashMapList<ChunkCoordinates, LittleTile> getTilesSortedPerBlock() {
        HashMapList<ChunkCoordinates, LittleTile> coords = new HashMapList<>();
        ArrayList<LittleTile> tiles = getTiles();
        for (LittleTile tile : tiles) {
            coords.add(tile.te.getCoord(), tile);
        }
        return coords;
    }

    public void onFlip(World world, EntityPlayer player, ItemStack stack, ForgeDirection direction) {}

    public void onRotate(World world, EntityPlayer player, ItemStack stack, ForgeDirection direction) {}

    // ====================GUI STUFF====================
    @SideOnly(Side.CLIENT)
    public abstract void createControls(SubGui gui, LittleStructure structure);

    @SideOnly(Side.CLIENT)
    public abstract LittleStructure parseStructure(SubGui gui);

    // ====================LittleStructure ID====================

    public String getIDOfStructure() {
        return getIDByClass(this.getClass());
    }

    public static class LittleStructureEntry {

        public Class<? extends LittleStructure> structureClass;
        public LittleStructure parser;

        public LittleStructureEntry(Class<? extends LittleStructure> structureClass, LittleStructure parser) {
            this.structureClass = structureClass;
            this.parser = parser;
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof LittleStructureEntry
                    && ((LittleStructureEntry) object).structureClass == this.structureClass;
        }

        @Override
        public String toString() {
            return structureClass.toString();
        }
    }

    public boolean isBed(IBlockAccess world, int x, int y, int z, EntityLivingBase player) {
        return false;
    }

    public boolean isLadder() {
        return false;
    }

}
