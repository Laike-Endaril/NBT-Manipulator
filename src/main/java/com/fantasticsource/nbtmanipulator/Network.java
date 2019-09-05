package com.fantasticsource.nbtmanipulator;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static com.fantasticsource.nbtmanipulator.NBTGUI.MODE_ITEM;

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
            Minecraft.getMinecraft().addScheduledTask(() -> NBTGUI.show(MODE_ITEM, packet.serializedNBT));
            return null;
        }
    }

    public static class NBTSavePacket implements IMessage
    {
        String compoundString;

        public NBTSavePacket()
        {
            //Required
        }

        public NBTSavePacket(String compoundString)
        {
            this.compoundString = compoundString;
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            ByteBufUtils.writeUTF8String(buf, compoundString);
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            compoundString = ByteBufUtils.readUTF8String(buf);
        }
    }

    public static class NBTSavePacketHandler implements IMessageHandler<NBTSavePacket, IMessage>
    {
        @Override
        public IMessage onMessage(NBTSavePacket packet, MessageContext ctx)
        {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() ->
            {
                try
                {
                    NBTTagCompound compound = JsonToNBT.getTagFromJson(packet.compoundString);
                    if (Item.getByNameOrId(compound.getString("id")) == null) throw new IllegalArgumentException("Item ID not found: " + compound.getString("id"));

                    InventoryPlayer inv = ctx.getServerHandler().player.inventory;
                    inv.setInventorySlotContents(inv.currentItem, new ItemStack(compound));

                    //Success
                    WRAPPER.sendTo(new NBTResultPacket(""), ctx.getServerHandler().player);
                }
                catch (Exception e)
                {
                    WRAPPER.sendTo(new NBTResultPacket(e.toString()), ctx.getServerHandler().player);
                }
            });
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
