package com.fantasticsource.nbtmanipulator;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = NBTManipulator.MODID, name = NBTManipulator.NAME, version = NBTManipulator.VERSION, dependencies = "required-after:fantasticlib@[1.12.2.034f,)", acceptableRemoteVersions = "*")
public class NBTManipulator
{
    public static final String MODID = "nbtmanipulator";
    public static final String NAME = "NBT Manipulator";
    public static final String VERSION = "1.12.2.003a";

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
    public void serverStarting(FMLServerStartingEvent event)
    {
        event.registerServerCommand(Commands.INSTANCE);
    }
}
