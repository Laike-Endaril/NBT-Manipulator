package com.fantasticsource.nbtmanipulator;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import java.util.ArrayList;
import java.util.List;

public class Commands extends CommandBase
{
    @Override
    public String getName()
    {
        return "nbtman";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "Usage: /nbtman hand";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
    {
        if (args.length == 0)
        {
            sender.sendMessage(new TextComponentString(getUsage(sender)));
            return;
        }

        switch (args[0])
        {
            case "hand":
                if (sender instanceof EntityPlayerMP)
                {
                    ItemStack stack = ((EntityPlayerMP) sender).getHeldItemMainhand();
                    Network.WRAPPER.sendTo(new Network.NBTGUIPacket(stack), (EntityPlayerMP) sender);
                }
                break;

            default:
                sender.sendMessage(new TextComponentString(getUsage(sender)));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        if (args.length == 1) return getListOfStringsMatchingLastWord(args, "hand");
        return new ArrayList<>();
    }
}
