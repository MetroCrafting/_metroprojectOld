package com.creativemd.littletiles.common.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.util.Vec3;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.utils.PlacementHelper;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

public class LittlePlacePacket extends CreativeCorePacket {

    public LittlePlacePacket() {

    }

    public LittlePlacePacket(ItemStack stack, Vec3 playerPos, Vec3 hitVec, int x, int y, int z, int side,
            boolean customPlacement) // , int direction, int direction2)
    {
        this.stack = stack;
        this.playerPos = playerPos;
        this.hitVec = hitVec;
        this.x = x;
        this.y = y;
        this.z = z;
        this.side = side;
        this.customPlacement = customPlacement;
        // this.direction = direction;
        // this.direction2 = direction2;
    }

    public ItemStack stack;
    public Vec3 hitVec;
    public Vec3 playerPos;
    public int x;
    public int y;
    public int z;
    public int side;
    public boolean customPlacement;
    // public int direction;
    // public int direction2;

    @Override
    public void writeBytes(ByteBuf buf) {
        ByteBufUtils.writeItemStack(buf, stack);
        writeVec3(playerPos, buf);
        writeVec3(hitVec, buf);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(side);
        buf.writeBoolean(customPlacement);
        // buf.writeInt(direction);
        // buf.writeInt(direction2);
    }

    @Override
    public void readBytes(ByteBuf buf) {
        stack = ByteBufUtils.readItemStack(buf);
        playerPos = readVec3(buf);
        hitVec = readVec3(buf);
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.side = buf.readInt();
        this.customPlacement = buf.readBoolean();
        // this.direction = buf.readInt();
        // this.direction2 = buf.readInt();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void executeClient(EntityPlayer player) {

    }

    @Override
    public void executeServer(EntityPlayer player) {
        if (PlacementHelper.isLittleBlock(stack)) {
            PlacementHelper helper = PlacementHelper.getInstance(player); // new PlacementHelper(player, x, y, z);
            // helper.side = side;

            ((ItemBlockTiles) Item.getItemFromBlock(LittleTiles.blockTile)).placeBlockAt(
                    player,
                    stack,
                    player.worldObj,
                    playerPos,
                    hitVec,
                    helper,
                    x,
                    y,
                    z,
                    side,
                    customPlacement); // , ForgeDirection.getOrientation(direction),
                                      // ForgeDirection.getOrientation(direction2));

            EntityPlayerMP playerMP = (EntityPlayerMP) player;
            Slot slot = playerMP.openContainer.getSlotFromInventory(playerMP.inventory, playerMP.inventory.currentItem);
            playerMP.playerNetServerHandler.sendPacket(
                    new S2FPacketSetSlot(
                            playerMP.openContainer.windowId,
                            slot.slotNumber,
                            playerMP.inventory.getCurrentItem()));

        }
    }

}
