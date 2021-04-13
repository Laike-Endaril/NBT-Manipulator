package com.fantasticsource.nbtmanipulator;

import com.fantasticsource.tools.ReflectionTool;
import com.fantasticsource.tools.component.CStringUTF8;
import com.fantasticsource.tools.component.Component;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

public class CNBTTemplate extends Component
{
    public static final HashMap<Class<? extends INBTSerializable>, HashMap<String, CNBTTemplate>> TEMPLATES = new HashMap<>();

    public String name, description;
    protected Class<? extends INBTSerializable> category;
    public NBTBase objectNBT;


    public CNBTTemplate()
    {
        //Only to be used with component functions!
    }

    public CNBTTemplate(String name, String description, INBTSerializable nbtObject)
    {
        this(name, description, getCategory(nbtObject), getNBT(nbtObject));
    }

    public CNBTTemplate(String name, String description, Class<? extends INBTSerializable> category, String objectNBTString) throws NBTException
    {
        this(name, description, category, JsonToNBT.getTagFromJson(objectNBTString));
    }

    public CNBTTemplate(String name, String description, Class<? extends INBTSerializable> category, NBTBase objectNBT)
    {
        this.name = name;
        this.description = description;
        setObject(category, objectNBT);
    }


    public static Class<? extends INBTSerializable> getCategory(INBTSerializable object)
    {
        if (object instanceof ItemStack) return ItemStack.class;

        if (object instanceof EntityPlayer) return EntityPlayer.class;
        if (object instanceof Entity) return Entity.class;

        if (object instanceof TileEntity) return TileEntity.class;

        return object.getClass();
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


    public CNBTTemplate setObject(INBTSerializable nbtObject)
    {
        return setObject(nbtObject.getClass(), getNBT(nbtObject));
    }

    public CNBTTemplate setObject(Class<? extends INBTSerializable> objectClass, String objectNBTString) throws NBTException
    {
        return setObject(objectClass, JsonToNBT.getTagFromJson(objectNBTString));
    }

    public CNBTTemplate setObject(Class<? extends INBTSerializable> objectClass, NBTBase objectNBT)
    {
        this.category = objectClass;
        this.objectNBT = objectNBT;
        return this;
    }


    public INBTSerializable createObject(World world)
    {
        if (category == ItemStack.class) return new ItemStack((NBTTagCompound) objectNBT);

        if (category == EntityPlayer.class)
        {
            System.err.println(TextFormatting.RED + "Cannot create player entities, but can edit them (see CNBTTemplate.editExisting())");
            return null;
        }
        if (category == Entity.class) return EntityList.createEntityFromNBT((NBTTagCompound) objectNBT, world);

        System.err.println(TextFormatting.RED + "Creation for class not implemented: " + category + " (but maybe try CNBTTemplate.editExisting())");
        return null;
    }

    public INBTSerializable editExisting(INBTSerializable existingObject)
    {
        if (category == ItemStack.class)
        {
            System.err.println(TextFormatting.RED + "Cannot edit existing itemstacks, but can create new ones (and replace the old one with it if wanted) (see CNBTTemplate.createObject())");
            return null;
        }

        if (category == Entity.class)
        {
            System.err.println(TextFormatting.RED + "Cannot edit existing entities (except players), but can create new ones (and replace the old one with it if wanted) (see CNBTTemplate.createObject())");
            return null;
        }

        existingObject.deserializeNBT(objectNBT); //TileEntity, EntityPlayer
        return existingObject;
    }


    @Override
    public CNBTTemplate write(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, name);
        ByteBufUtils.writeUTF8String(buf, description);
        ByteBufUtils.writeUTF8String(buf, category.getName());

        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag("nbt", objectNBT);
        ByteBufUtils.writeUTF8String(buf, compound.toString());

        return this;
    }

    @Override
    public CNBTTemplate read(ByteBuf buf)
    {
        name = ByteBufUtils.readUTF8String(buf);
        description = ByteBufUtils.readUTF8String(buf);
        category = ReflectionTool.getClassByName(ByteBufUtils.readUTF8String(buf));

        try
        {
            NBTTagCompound compound = JsonToNBT.getTagFromJson(ByteBufUtils.readUTF8String(buf));
            objectNBT = compound.getTag("nbt");
        }
        catch (NBTException e)
        {
            e.printStackTrace();
        }

        return this;
    }

    @Override
    public CNBTTemplate save(OutputStream stream)
    {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag("nbt", objectNBT);
        new CStringUTF8().set(name).save(stream).set(description).save(stream).set(category.getName()).save(stream).set(compound.toString()).save(stream);

        return this;
    }

    @Override
    public CNBTTemplate load(InputStream stream)
    {
        CStringUTF8 cs = new CStringUTF8();

        name = cs.load(stream).value;
        description = cs.load(stream).value;
        category = ReflectionTool.getClassByName(cs.load(stream).value);
        try
        {
            NBTTagCompound compound = JsonToNBT.getTagFromJson(cs.load(stream).value);
            objectNBT = compound.getTag("nbt");
        }
        catch (NBTException e)
        {
            e.printStackTrace();
        }

        return this;
    }
}
