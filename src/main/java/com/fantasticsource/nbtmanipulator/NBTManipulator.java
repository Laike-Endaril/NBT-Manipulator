package com.fantasticsource.nbtmanipulator;

import com.fantasticsource.mctools.MCTools;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Predicate;

import static com.fantasticsource.nbtmanipulator.Network.WRAPPER;

@Mod(modid = NBTManipulator.MODID, name = NBTManipulator.NAME, version = NBTManipulator.VERSION, dependencies = "required-after:fantasticlib@[1.12.2.044zzh,)", acceptableRemoteVersions = "*")
public class NBTManipulator
{
    public static final String MODID = "nbtmanipulator";
    public static final String NAME = "NBT Manipulator";
    public static final String VERSION = "1.12.2.006";

    protected static final HashMap<UUID, NBTEditingData> EDITING_TARGETS = new HashMap<>();

    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent event)
    {
        Network.init();
        MinecraftForge.EVENT_BUS.register(NBTManipulator.class);

        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            ClientCommandHandler.instance.registerCommand(ClientCommands.INSTANCE);
        }

        CNBTTemplate.loadAll(event);
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
        EDITING_TARGETS.remove(event.player.getUniqueID());
    }


    public static void save(EntityPlayerMP editor, String nbtString)
    {
        try
        {
            NBTEditingData data = EDITING_TARGETS.get(editor.getUniqueID());
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
        EDITING_TARGETS.put(editor.getUniqueID(), new NBTEditingData(object, callback));
        WRAPPER.sendTo(new Network.NBTGUIPacket(object), editor);
    }

    public static void hand(EntityPlayerMP editor)
    {
        startEditing(editor, editor.getHeldItemMainhand(), data ->
        {
            ItemStack stack = new ItemStack((NBTTagCompound) data.newObjectNBT);
            editor.inventory.setInventorySlotContents(editor.inventory.currentItem, stack);
            data.oldObject = stack;
            return true;
        });
    }

    public static void self(EntityPlayerMP editor)
    {
        player(editor, editor);
    }

    public static void nearestEntity(EntityPlayerMP editor)
    {
        entity(editor, editor.world.findNearestEntityWithinAABB(Entity.class, editor.getEntityBoundingBox().grow(100), editor));
    }

    public static void nearestTileEntity(EntityPlayerMP editor)
    {
        TileEntity nearest = null;
        double minDistSqr = Double.MAX_VALUE;
        double x = editor.posX, y = editor.posY, z = editor.posZ;
        for (TileEntity te : editor.world.loadedTileEntityList)
        {
            double distSqr = te.getDistanceSq(x, y, z);
            if (distSqr < minDistSqr)
            {
                minDistSqr = distSqr;
                nearest = te;
            }
        }

        if (nearest != null) tileEntity(editor, nearest);
    }

    public static void entity(EntityPlayerMP editor, Entity target)
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
                Entity oldEntity = (Entity) data.oldObject;
                WorldServer world = (WorldServer) oldEntity.world;

                String id = compound.getString("id");
                if (id.equals("player") || id.equals("minecraft:player"))
                {
                    data.error = "Cannot create player entities!";
                    return true;
                }

                Entity entity = EntityList.createEntityFromNBT(compound, world);

                if (!compound.hasKey("Pos")) entity.setPosition(oldEntity.posX, oldEntity.posY, oldEntity.posZ);

                MCTools.removeEntityImmediate(oldEntity);
                world.spawnEntity(entity);
                data.oldObject = entity;
            }

            return true;
        });
    }

    public static void player(EntityPlayerMP editor, EntityPlayerMP target)
    {
        startEditing(editor, target, data ->
        {
            target.deserializeNBT((NBTTagCompound) data.newObjectNBT);
            return true;
        });
    }

    public static void tileEntity(EntityPlayerMP editor, TileEntity target)
    {
        startEditing(editor, target, data ->
        {
            target.deserializeNBT((NBTTagCompound) data.newObjectNBT);
            return true;
        });
    }
}
