package com.creativemd.creativecore.common.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class CreativeMessageHandler implements IMessage {

    public CreativeMessageHandler() {

    }

    public CreativeCorePacket packet = null;

    public CreativeMessageHandler(CreativeCorePacket packet) {
        this.packet = packet;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        String id = ByteBufUtils.readUTF8String(buf);
        Class PacketClass = CreativeCorePacket.getClassByID(id);
        packet = null;
        try {
            packet = (CreativeCorePacket) PacketClass.getConstructor().newInstance();
        } catch (Exception e) {
            System.out.println("Invalid packet id=" + id);
        }
        if (packet != null) packet.readBytes(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, CreativeCorePacket.getIDByClass(packet));
        packet.writeBytes(buf);
    }

}
