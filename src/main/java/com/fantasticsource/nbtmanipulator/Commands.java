package com.fantasticsource.nbtmanipulator;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Commands extends CommandBase
{
    public static final Commands INSTANCE;
    protected static final ArrayList<String> subcommands = new ArrayList<>();

    static
    {
        INSTANCE = new Commands();
        subcommands.add("hand");
        subcommands.add("self");
        subcommands.add("nearestentity");
        subcommands.add("nearesttileentity");
    }

    private Commands()
    {
    }

    @Override
    public String getName()
    {
        return "nbtman";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "Usage: /nbtman [hand|self|nearestentity|nearesttileentity]";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
    {
        if (!(sender instanceof EntityPlayerMP)) return;
        if (args.length == 0) NBTManipulator.hand((EntityPlayerMP) sender);
        else
        {
            switch (args[0])
            {
                case "hand":
                    NBTManipulator.hand((EntityPlayerMP) sender);
                    break;

                case "self":
                    NBTManipulator.self((EntityPlayerMP) sender);
                    break;

                case "nearestentity":
                    NBTManipulator.nearestEntity((EntityPlayerMP) sender);
                    break;

                case "nearesttileentity":
                    NBTManipulator.nearestTileEntity((EntityPlayerMP) sender);
            }
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        return getListOfStringsMatchingLastWord(args, subcommands);
    }
}
