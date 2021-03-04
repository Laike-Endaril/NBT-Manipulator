package com.fantasticsource.nbtmanipulator;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.HashMap;
import java.util.function.Predicate;

import static com.fantasticsource.nbtmanipulator.Network.WRAPPER;

@Mod(modid = NBTManipulator.MODID, name = NBTManipulator.NAME, version = NBTManipulator.VERSION, dependencies = "required-after:fantasticlib@[1.12.2.044zj,)", acceptableRemoteVersions = "*")
public class NBTManipulator
{
    public static final String MODID = "nbtmanipulator";
    public static final String NAME = "NBT Manipulator";
    public static final String VERSION = "1.12.2.004";

    protected static final HashMap<EntityPlayerMP, NBTEditingData> EDITING_TARGETS = new HashMap<>();

    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent event)
    {
        Network.init();
        MinecraftForge.EVENT_BUS.register(NBTManipulator.class);
    }

    @SubscribeEvent
    public static void saveConfig(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if (event.getModID().equals(MODID)) ConfigManager.sync(MODID, Config.Type.INSTANCE);
    }

    @Mod.EventHandler
    public static void serverStarting(FMLServerStartingEvent event)
    {
        event.registerServerCommand(Commands.INSTANCE);
    }


    @SubscribeEvent
    public static void playerLogoff(PlayerEvent.PlayerLoggedOutEvent event)
    {
        EDITING_TARGETS.remove(event.player);
    }

    /**
     * @param callback is a function which will run it's "test()" method when the edited nbt is received.  The original object and the new NBTBase can be accessed from the NBTEditingData.  It also contains an "error" string which can be set to anything besides empty to show an editing error
     */
    public static void startEditing(EntityPlayerMP editor, INBTSerializable object, Predicate<NBTEditingData> callback)
    {
        EDITING_TARGETS.put(editor, new NBTEditingData(object, callback));
        WRAPPER.sendTo(new Network.NBTGUIPacket(object), editor);
    }

    public static void save(EntityPlayerMP editor, String nbtString)
    {
        try
        {
            NBTEditingData data = EDITING_TARGETS.get(editor);
            data.run(nbtString);
            WRAPPER.sendTo(new Network.NBTResultPacket(data.error), editor);
        }
        catch (Exception e)
        {
            WRAPPER.sendTo(new Network.NBTResultPacket(e.toString()), editor);
        }
    }
}
