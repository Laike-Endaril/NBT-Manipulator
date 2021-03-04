package com.fantasticsource.nbtmanipulator;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class Commands extends CommandBase
{
    public static final Commands INSTANCE;

    static
    {
        INSTANCE = new Commands();
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
        return "Usage: /nbtman [hand|self|nearestentity]";
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
                    NBTManipulator.nearest((EntityPlayerMP) sender);
                    break;
            }
        }
    }


}
