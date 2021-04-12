package com.fantasticsource.nbtmanipulator;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;

public class SavedNBT<T extends INBTSerializable>
{
    public static final HashMap<Class<? extends INBTSerializable>, HashMap<String, SavedNBT>> SAVED_NBT = new HashMap<>();

    public String name, description;
    protected Class<T> objectClass;
    protected NBTBase objectNBT;


    public SavedNBT(String name, String description, T nbtObject)
    {
        this(name, description, (Class<T>) nbtObject.getClass(), getNBT(nbtObject));
    }

    public SavedNBT(String name, String description, Class<T> objectClass, String objectNBTString) throws NBTException
    {
        this(name, description, objectClass, JsonToNBT.getTagFromJson(objectNBTString));
    }

    public SavedNBT(String name, String description, Class<T> objectClass, NBTBase objectNBT)
    {
        this.name = name;
        this.description = description;
        setObject(objectClass, objectNBT);
    }


    public static NBTBase getNBT(INBTSerializable nbtObject)
    {
        if (nbtObject instanceof EntityPlayer)
        {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setString("id", "minecraft:player");
            ((EntityPlayer) nbtObject).writeToNBT(compound);
            return compound;
        }

        return nbtObject.serializeNBT();
    }


    public SavedNBT<T> setObject(T nbtObject)
    {
        return setObject((Class<T>) nbtObject.getClass(), getNBT(nbtObject));
    }

    public SavedNBT<T> setObject(Class<T> objectClass, String objectNBTString) throws NBTException
    {
        return setObject(objectClass, JsonToNBT.getTagFromJson(objectNBTString));
    }

    public SavedNBT<T> setObject(Class<T> objectClass, NBTBase objectNBT)
    {
        this.objectClass = objectClass;
        this.objectNBT = objectNBT;
        return this;
    }


    public T createObject(World world)
    {
        if (ItemStack.class.isAssignableFrom(objectClass)) return (T) new ItemStack((NBTTagCompound) objectNBT);

        if (EntityPlayer.class.isAssignableFrom(objectClass))
        {
            System.err.println(TextFormatting.RED + "Cannot create player entities, but can edit them (see SavedNBT.editExisting())");
            return null;
        }
        if (Entity.class.isAssignableFrom(objectClass)) return (T) EntityList.createEntityFromNBT((NBTTagCompound) objectNBT, world);

        System.err.println(TextFormatting.RED + "Creation for class not implemented: " + objectClass);
        return null;
    }

    public T editExisting(T existingObject)
    {
        if (ItemStack.class.isAssignableFrom(objectClass))
        {
            System.err.println(TextFormatting.RED + "Cannot edit existing itemstacks, but can create new ones (and replace the old one with it if wanted) (see SavedNBT.createObject())");
            return null;
        }

        if (Entity.class.isAssignableFrom(objectClass) && !EntityPlayer.class.isAssignableFrom(objectClass))
        {
            System.err.println(TextFormatting.RED + "Cannot edit existing entities (except players), but can create new ones (and replace the old one with it if wanted) (see SavedNBT.createObject())");
            return null;
        }

        existingObject.deserializeNBT(objectNBT); //TileEntity, EntityPlayer
        return existingObject;
    }
}
