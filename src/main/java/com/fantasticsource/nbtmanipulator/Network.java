package com.fantasticsource.nbtmanipulator;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Network
{
    public static final SimpleNetworkWrapper WRAPPER = new SimpleNetworkWrapper(NBTManipulator.MODID);
    private static int discriminator = 0;

    public static void init()
    {
        WRAPPER.registerMessage(NBTGUIPacketHandler.class, NBTGUIPacket.class, discriminator++, Side.CLIENT);
        WRAPPER.registerMessage(NBTSavePacketHandler.class, NBTSavePacket.class, discriminator++, Side.SERVER);
        WRAPPER.registerMessage(NBTResultPacketHandler.class, NBTResultPacket.class, discriminator++, Side.CLIENT);
    }


    public static class NBTGUIPacket implements IMessage
    {
        String nbtString;

        public NBTGUIPacket()
        {
            //Required
        }

        public NBTGUIPacket(INBTSerializable object)
        {
            nbtString = object.serializeNBT().toString();
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            ByteBufUtils.writeUTF8String(buf, nbtString);
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            nbtString = ByteBufUtils.readUTF8String(buf);
        }
    }

    public static class NBTGUIPacketHandler implements IMessageHandler<NBTGUIPacket, IMessage>
    {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(NBTGUIPacket packet, MessageContext ctx)
        {
            Minecraft.getMinecraft().addScheduledTask(() -> new NBTGUI(packet.nbtString));
            return null;
        }
    }

    public static class NBTSavePacket implements IMessage
    {
        String nbtString;

        public NBTSavePacket()
        {
            //Required
        }

        public NBTSavePacket(String nbtString)
        {
            this.nbtString = nbtString;
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            ByteBufUtils.writeUTF8String(buf, nbtString);
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            nbtString = ByteBufUtils.readUTF8String(buf);
        }
    }

    public static class NBTSavePacketHandler implements IMessageHandler<NBTSavePacket, IMessage>
    {
        @Override
        public IMessage onMessage(NBTSavePacket packet, MessageContext ctx)
        {
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            EntityPlayerMP player = ctx.getServerHandler().player;
            if (!Commands.INSTANCE.checkPermission(server, player)) return null;

            server.addScheduledTask(() -> NBTManipulator.save(player, packet.nbtString));
            return null;
        }
    }


    public static class NBTResultPacket implements IMessage
    {
        String errorMessage;

        public NBTResultPacket()
        {
            //Required
        }

        public NBTResultPacket(String errorMessage)
        {
            this.errorMessage = errorMessage;
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            ByteBufUtils.writeUTF8String(buf, errorMessage);
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            errorMessage = ByteBufUtils.readUTF8String(buf);
        }
    }

    public static class NBTResultPacketHandler implements IMessageHandler<NBTResultPacket, IMessage>
    {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(NBTResultPacket packet, MessageContext ctx)
        {
            String s = packet.errorMessage;
            if (s.equals("")) NBTGUI.setSuccess();
            else NBTGUI.setError(s);
            return null;
        }
    }
}
