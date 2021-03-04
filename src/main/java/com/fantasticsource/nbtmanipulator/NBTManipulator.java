package com.fantasticsource.nbtmanipulator;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.io.PrintWriter;
import java.io.StringWriter;
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
            StringWriter stacktrace = new StringWriter();
            e.printStackTrace(new PrintWriter(stacktrace));
            WRAPPER.sendTo(new Network.NBTResultPacket(stacktrace.toString().replaceAll("\t", "")), editor);
        }
    }


    /**
     * @param callback is a function which will run it's "test()" method when the edited nbt is received.  The original object and the new NBTBase can be accessed from the NBTEditingData.  It also contains an "error" string which can be set to anything besides empty to show an editing error
     */
    public static void startEditing(EntityPlayerMP editor, INBTSerializable object, Predicate<NBTEditingData> callback)
    {
        EDITING_TARGETS.put(editor, new NBTEditingData(object, callback));
        WRAPPER.sendTo(new Network.NBTGUIPacket(object), editor);
    }

    protected static void hand(EntityPlayerMP editor)
    {
        startEditing(editor, editor.getHeldItemMainhand(), data ->
        {
            editor.inventory.setInventorySlotContents(editor.inventory.currentItem, new ItemStack((NBTTagCompound) data.newObjectNBT));
            return true;
        });
    }

    protected static void self(EntityPlayerMP editor)
    {
        player(editor, editor);
    }

    protected static void nearest(EntityPlayerMP editor)
    {
        entity(editor, editor.world.findNearestEntityWithinAABB(Entity.class, editor.getEntityBoundingBox().grow(100), editor));
    }

    protected static void entity(EntityPlayerMP editor, Entity target)
    {
        if (target instanceof EntityPlayerMP && !(target instanceof FakePlayer))
        {
            player(editor, (EntityPlayerMP) target);
            return;
        }

        startEditing(editor, target, data ->
        {
            NBTTagCompound compound = (NBTTagCompound) data.newObjectNBT;
            if (!compound.hasNoTags())
            {
                World world = target.world;

                String id = compound.getString("id");
                if (id.equals("player") || id.equals("minecraft:player"))
                {
                    data.error = "Cannot create player entities!";
                    return true;
                }

                Entity entity = EntityList.createEntityFromNBT(compound, world);

                if (!compound.hasKey("Pos")) entity.setPosition(target.posX, target.posY, target.posZ);
                target.setDead();
                world.spawnEntity(entity);
            }

            return true;
        });
    }

    protected static void player(EntityPlayerMP editor, EntityPlayerMP target)
    {
        startEditing(editor, target, data ->
        {
            target.deserializeNBT((NBTTagCompound) data.newObjectNBT);
            return true;
        });
    }
}
