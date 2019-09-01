package com.fantasticsource.nbtmanipulator;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
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
    }


    public static class NBTGUIPacket implements IMessage
    {
        ItemStack copy;
        String serializedNBT;

        public NBTGUIPacket()
        {
            //Required
        }

        public NBTGUIPacket(ItemStack stack)
        {
            copy = stack.copy();
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            ByteBufUtils.writeUTF8String(buf, copy.serializeNBT().toString());
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            serializedNBT = ByteBufUtils.readUTF8String(buf);
        }
    }

    public static class NBTGUIPacketHandler implements IMessageHandler<NBTGUIPacket, IMessage>
    {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(NBTGUIPacket packet, MessageContext ctx)
        {
            Minecraft mc = Minecraft.getMinecraft();
            mc.addScheduledTask(() -> mc.displayGuiScreen(new NBTGUI(NBTGUI.MODE_ITEM, packet.serializedNBT)));
            return null;
        }
    }
}
