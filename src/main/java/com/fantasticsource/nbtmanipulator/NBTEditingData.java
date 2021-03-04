package com.fantasticsource.nbtmanipulator;

import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.function.Predicate;

public class NBTEditingData
{
    protected final INBTSerializable oldObject;
    protected final Predicate<NBTEditingData> callback;
    protected NBTBase newObjectNBT = null;
    public String error = "";

    public NBTEditingData(INBTSerializable oldObject, Predicate<NBTEditingData> callback)
    {
        this.oldObject = oldObject;
        this.callback = callback;
    }

    public boolean run(String nbtString) throws NBTException
    {
        newObjectNBT = JsonToNBT.getTagFromJson(nbtString);
        return run();
    }

    public boolean run()
    {
        return callback.test(this);
    }
}
