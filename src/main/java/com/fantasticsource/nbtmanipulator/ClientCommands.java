package com.fantasticsource.nbtmanipulator;

import com.fantasticsource.mctools.ClientTickTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.IClientCommand;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

public class ClientCommands extends CommandBase implements IClientCommand
{
    public static final ClientCommands INSTANCE;
    protected static final ArrayList<String> subcommands = new ArrayList<>();

    protected static NBTEditingData CLIENT_DATA = null;

    static
    {
        INSTANCE = new ClientCommands();
        subcommands.add("hand");
        subcommands.add("self");
        subcommands.add("nearestentity");
        subcommands.add("nearesttileentity");
    }

    private ClientCommands()
    {
    }

    @Override
    public String getName()
    {
        return "cnbtman";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return true;
    }

    @Override
    public boolean allowUsageWithoutPrefix(ICommandSender sender, String message)
    {
        return false;
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "Usage: /cnbtman [hand|self|nearestentity|nearesttileentity]";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
    {
        if (args.length == 0) hand();
        else
        {
            switch (args[0])
            {
                case "hand":
                    hand();
                    break;

                case "self":
                    self();
                    break;

                case "nearestentity":
                    nearestEntity();
                    break;

                case "nearesttileentity":
                    nearestTileEntity();
            }
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        return getListOfStringsMatchingLastWord(args, subcommands);
    }


    public static void save(String nbtString)
    {
        try
        {
            CLIENT_DATA.run(nbtString);
            NBTGUI.setError(CLIENT_DATA.error);
        }
        catch (Exception e)
        {
            StringWriter stacktrace = new StringWriter();
            e.printStackTrace(new PrintWriter(stacktrace));

            NBTGUI.setError(stacktrace.toString().replaceAll("\t", ""));
        }
    }


    /**
     * @param callback is a function which will run it's "test()" method when the edited nbt is received.  The original object and the new NBTBase can be accessed from the NBTEditingData.  It also contains an "error" string which can be set to anything besides empty to show an editing error
     */
    public static void startEditing(INBTSerializable object, Predicate<NBTEditingData> callback)
    {
        CLIENT_DATA = new NBTEditingData(object, callback);
        Class<? extends INBTSerializable> category = CNBTTemplate.getCategory(object);
        HashMap<String, CNBTTemplate> map = CNBTTemplate.TEMPLATES.getOrDefault(category, new HashMap<>());
        ClientTickTimer.schedule(1, () -> new NBTGUI(category, map, CNBTTemplate.getNBT(object).toString(), true));
    }

    protected static void hand()
    {
        EntityPlayerSP editor = Minecraft.getMinecraft().player;

        startEditing(editor.getHeldItemMainhand(), data ->
        {
            editor.inventory.setInventorySlotContents(editor.inventory.currentItem, new ItemStack((NBTTagCompound) data.newObjectNBT));
            return true;
        });
    }

    protected static void self()
    {
        player(Minecraft.getMinecraft().player);
    }

    protected static void nearestEntity()
    {
        EntityPlayerSP editor = Minecraft.getMinecraft().player;
        entity(Minecraft.getMinecraft().world.findNearestEntityWithinAABB(Entity.class, editor.getEntityBoundingBox().grow(100), editor));
    }

    protected static void nearestTileEntity()
    {
        EntityPlayerSP editor = Minecraft.getMinecraft().player;

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

        if (nearest != null) tileEntity(nearest);
    }

    protected static void entity(Entity target)
    {
        if (target instanceof EntityPlayerSP)
        {
            player((EntityPlayerSP) target);
            return;
        }

        startEditing(target, data ->
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

    protected static void player(EntityPlayerSP target)
    {
        startEditing(target, data ->
        {
            target.deserializeNBT((NBTTagCompound) data.newObjectNBT);
            return true;
        });
    }

    protected static void tileEntity(TileEntity target)
    {
        startEditing(target, data ->
        {
            target.deserializeNBT((NBTTagCompound) data.newObjectNBT);
            return true;
        });
    }
}
