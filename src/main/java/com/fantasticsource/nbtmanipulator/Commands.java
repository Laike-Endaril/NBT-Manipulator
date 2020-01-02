package com.fantasticsource.nbtmanipulator;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
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
        return "Usage: /nbtman";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
    {
        if (args.length == 0)
        {
            if (sender instanceof EntityPlayerMP)
            {
                ItemStack stack = ((EntityPlayerMP) sender).getHeldItemMainhand();
                Network.WRAPPER.sendTo(new Network.NBTGUIPacket(stack), (EntityPlayerMP) sender);
            }
        }
    }
}
