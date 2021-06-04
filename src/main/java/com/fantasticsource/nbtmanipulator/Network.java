package com.fantasticsource.nbtmanipulator;

import com.fantasticsource.mctools.MCTools;
import com.fantasticsource.mctools.gui.element.GUIElement;
import com.fantasticsource.mctools.gui.element.text.GUIText;
import com.fantasticsource.mctools.gui.element.view.GUIList;
import com.fantasticsource.mctools.gui.screen.TextSelectionGUI;
import com.fantasticsource.mctools.gui.screen.YesNoGUI;
import com.fantasticsource.tools.ReflectionTool;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Network
{
    public static final SimpleNetworkWrapper WRAPPER = new SimpleNetworkWrapper(NBTManipulator.MODID);
    public static final String MAGIC_STRING = "xbcoihsf*&*&Y*@";
    private static int discriminator = 0;

    public static void init()
    {
        WRAPPER.registerMessage(NBTGUIPacketHandler.class, NBTGUIPacket.class, discriminator++, Side.CLIENT);
        WRAPPER.registerMessage(SaveToObjectPacketHandler.class, SaveToObjectPacket.class, discriminator++, Side.SERVER);
        WRAPPER.registerMessage(NBTResultPacketHandler.class, NBTResultPacket.class, discriminator++, Side.CLIENT);
        WRAPPER.registerMessage(RequestTemplateListPacketHandler.class, RequestTemplateListPacket.class, discriminator++, Side.SERVER);
        WRAPPER.registerMessage(TemplateListPacketHandler.class, TemplateListPacket.class, discriminator++, Side.CLIENT);
        WRAPPER.registerMessage(SaveTemplatePacketHandler.class, SaveTemplatePacket.class, discriminator++, Side.SERVER);
        WRAPPER.registerMessage(CheckOverwriteTemplatePacketHandler.class, CheckOverwriteTemplatePacket.class, discriminator++, Side.CLIENT);
        WRAPPER.registerMessage(ConfirmOverwriteTemplatePacketHandler.class, ConfirmOverwriteTemplatePacket.class, discriminator++, Side.SERVER);
    }


    public static class NBTGUIPacket implements IMessage
    {
        Class<? extends INBTSerializable> category;
        HashMap<String, CNBTTemplate> map;
        String nbtString;

        public NBTGUIPacket()
        {
            //Required
        }

        public NBTGUIPacket(INBTSerializable object)
        {
            category = CNBTTemplate.getCategory(object);
            map = CNBTTemplate.TEMPLATES.computeIfAbsent(category, o -> new HashMap<>());
            nbtString = CNBTTemplate.getNBT(object).toString();
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            ByteBufUtils.writeUTF8String(buf, category.getName());
            buf.writeInt(map.size());
            for (Map.Entry<String, CNBTTemplate> entry : map.entrySet())
            {
                ByteBufUtils.writeUTF8String(buf, entry.getKey());
                entry.getValue().write(buf);
            }
            ByteBufUtils.writeUTF8String(buf, nbtString);
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            category = ReflectionTool.getClassByName(ByteBufUtils.readUTF8String(buf));
            map = new HashMap<>();
            for (int i = buf.readInt(); i > 0; i--)
            {
                map.put(ByteBufUtils.readUTF8String(buf), new CNBTTemplate().read(buf));
            }
            nbtString = ByteBufUtils.readUTF8String(buf);
        }
    }

    public static class NBTGUIPacketHandler implements IMessageHandler<NBTGUIPacket, IMessage>
    {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(NBTGUIPacket packet, MessageContext ctx)
        {
            Minecraft.getMinecraft().addScheduledTask(() -> new NBTGUI(packet.category, packet.map, packet.nbtString));
            return null;
        }
    }


    public static class SaveToObjectPacket implements IMessage
    {
        String nbtString;

        public SaveToObjectPacket()
        {
            //Required
        }

        public SaveToObjectPacket(String nbtString)
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

    public static class SaveToObjectPacketHandler implements IMessageHandler<SaveToObjectPacket, IMessage>
    {
        @Override
        public IMessage onMessage(SaveToObjectPacket packet, MessageContext ctx)
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
            NBTGUI.setError(packet.errorMessage);
            return null;
        }
    }


    public static class RequestTemplateListPacket implements IMessage
    {
        public RequestTemplateListPacket()
        {
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
        }
    }

    public static class RequestTemplateListPacketHandler implements IMessageHandler<RequestTemplateListPacket, IMessage>
    {
        @Override
        public IMessage onMessage(RequestTemplateListPacket packet, MessageContext ctx)
        {
            EntityPlayerMP player = ctx.getServerHandler().player;
            if (!MCTools.isOP(player)) return null;

            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            server.addScheduledTask(() ->
            {
                NBTEditingData data = NBTManipulator.EDITING_TARGETS.get(player.getUniqueID());
                if (data == null) return;

                INBTSerializable object = data.oldObject;
                if (object == null) return;

                Class<? extends INBTSerializable> category = CNBTTemplate.getCategory(object);
                WRAPPER.sendTo(new TemplateListPacket(category.getSimpleName(), CNBTTemplate.TEMPLATES.computeIfAbsent(category, o -> new HashMap<>())), player);
            });

            return null;
        }
    }


    public static class TemplateListPacket implements IMessage
    {
        String category;
        HashMap<String, CNBTTemplate> map;

        public TemplateListPacket()
        {
            //Required
        }

        public TemplateListPacket(String category, HashMap<String, CNBTTemplate> map)
        {
            this.category = category;
            this.map = map;
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            ByteBufUtils.writeUTF8String(buf, category);
            buf.writeInt(map.size());
            for (Map.Entry<String, CNBTTemplate> entry : map.entrySet())
            {
                ByteBufUtils.writeUTF8String(buf, entry.getKey());
                entry.getValue().write(buf);
            }
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            category = ByteBufUtils.readUTF8String(buf);
            map = new HashMap<>();
            for (int i = buf.readInt(); i > 0; i--) map.put(ByteBufUtils.readUTF8String(buf), new CNBTTemplate().read(buf));
        }
    }

    public static class TemplateListPacketHandler implements IMessageHandler<TemplateListPacket, IMessage>
    {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(TemplateListPacket packet, MessageContext ctx)
        {
            Minecraft mc = Minecraft.getMinecraft();
            mc.addScheduledTask(() ->
            {
                if (!(mc.currentScreen instanceof NBTGUI)) return;


                NBTGUI gui = (NBTGUI) mc.currentScreen;

                GUIText fake = new GUIText(gui, "");
                ArrayList<String> list = new ArrayList<>(packet.map.keySet());
                Collections.sort(list);
                TextSelectionGUI gui2 = new TextSelectionGUI(fake, "Load " + packet.category + " Template", list.toArray(new String[0]));
                for (GUIElement element : gui2.root.children)
                {
                    if (element instanceof GUIList)
                    {
                        for (GUIList.Line line : ((GUIList) element).getLines())
                        {
                            GUIText text = (GUIText) line.getLineElement(0);
                            text.setTooltip(packet.map.get(text.getText()).description);
                        }
                    }
                }

                gui2.addOnClosedActions(() ->
                {
                    CNBTTemplate template = packet.map.get(fake.getText());
                    if (template != null) gui.code.setCode(MCTools.legibleNBT(template.objectNBT));
                });
            });
            return null;
        }
    }


    public static class SaveTemplatePacket implements IMessage
    {
        CNBTTemplate template;

        public SaveTemplatePacket()
        {
        }

        public SaveTemplatePacket(CNBTTemplate template)
        {
            this.template = template;
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            template.write(buf);
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            template = new CNBTTemplate().read(buf);
        }
    }

    public static class SaveTemplatePacketHandler implements IMessageHandler<SaveTemplatePacket, IMessage>
    {
        @Override
        public IMessage onMessage(SaveTemplatePacket packet, MessageContext ctx)
        {
            EntityPlayerMP player = ctx.getServerHandler().player;
            if (!MCTools.isOP(player)) return null;

            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            server.addScheduledTask(() ->
            {
                CNBTTemplate template = packet.template;
                if (template.category.equals(INBTSerializable.class)) template.category = CNBTTemplate.getCategory(NBTManipulator.EDITING_TARGETS.get(player.getUniqueID()).oldObject);
                HashMap<String, CNBTTemplate> map = CNBTTemplate.TEMPLATES.computeIfAbsent(template.category, o -> new HashMap<>());
                if (!map.containsKey(template.name))
                {
                    if (template.description.equals(MAGIC_STRING)) template.description = "";
                    map.put(template.name, template);
                    template.saveToFile();
                }
                else WRAPPER.sendTo(new CheckOverwriteTemplatePacket(template), player);
            });

            return null;
        }
    }


    public static class CheckOverwriteTemplatePacket implements IMessage
    {
        CNBTTemplate template;

        public CheckOverwriteTemplatePacket()
        {
            //Required
        }

        public CheckOverwriteTemplatePacket(CNBTTemplate template)
        {
            this.template = template;
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            template.write(buf);
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            template = new CNBTTemplate().read(buf);
        }
    }

    public static class CheckOverwriteTemplatePacketHandler implements IMessageHandler<CheckOverwriteTemplatePacket, IMessage>
    {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(CheckOverwriteTemplatePacket packet, MessageContext ctx)
        {
            Minecraft mc = Minecraft.getMinecraft();
            mc.addScheduledTask(() ->
            {
                if (!(mc.currentScreen instanceof NBTGUI)) return;


                YesNoGUI gui = new YesNoGUI("Overwrite Template?", "Overwrite template " + '"' + packet.template.name + '"' + " (" + packet.template.category.getSimpleName() + ")?");
                gui.addOnClosedActions(() ->
                {
                    if (gui.pressedYes) WRAPPER.sendToServer(new ConfirmOverwriteTemplatePacket(packet.template));
                });
            });
            return null;
        }
    }


    public static class ConfirmOverwriteTemplatePacket implements IMessage
    {
        CNBTTemplate template;

        public ConfirmOverwriteTemplatePacket()
        {
        }

        public ConfirmOverwriteTemplatePacket(CNBTTemplate template)
        {
            this.template = template;
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            template.write(buf);
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            template = new CNBTTemplate().read(buf);
        }
    }

    public static class ConfirmOverwriteTemplatePacketHandler implements IMessageHandler<ConfirmOverwriteTemplatePacket, IMessage>
    {
        @Override
        public IMessage onMessage(ConfirmOverwriteTemplatePacket packet, MessageContext ctx)
        {
            EntityPlayerMP player = ctx.getServerHandler().player;
            if (!MCTools.isOP(player)) return null;

            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            server.addScheduledTask(() ->
            {
                CNBTTemplate template = packet.template;
                HashMap<String, CNBTTemplate> map = CNBTTemplate.TEMPLATES.computeIfAbsent(template.category, o -> new HashMap<>());
                if (template.description.equals(MAGIC_STRING))
                {
                    CNBTTemplate oldTemplate = map.get(template.name);
                    template.description = oldTemplate != null ? oldTemplate.description : "";
                }
                map.put(template.name, template);
                template.saveToFile();
            });

            return null;
        }
    }
}
