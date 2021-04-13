package com.fantasticsource.nbtmanipulator;

import com.fantasticsource.mctools.MCTools;
import com.fantasticsource.mctools.gui.GUIScreen;
import com.fantasticsource.mctools.gui.element.GUIElement;
import com.fantasticsource.mctools.gui.element.other.GUIDarkenedBackground;
import com.fantasticsource.mctools.gui.element.other.GUIVerticalScrollbar;
import com.fantasticsource.mctools.gui.element.text.CodeInput;
import com.fantasticsource.mctools.gui.element.text.GUIText;
import com.fantasticsource.mctools.gui.element.text.GUITextButton;
import com.fantasticsource.mctools.gui.element.text.filter.FilterNotEmpty;
import com.fantasticsource.mctools.gui.element.view.GUIScrollView;
import com.fantasticsource.mctools.gui.screen.TextInputGUI;
import com.fantasticsource.tools.datastructures.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTException;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;

public class NBTGUI extends GUIScreen
{
    public ArrayList<String> lines;
    public GUITextButton saveToObject, saveToServerTemplate, loadFromServerTemplate, closeButton;
    public CodeInput code;
    public GUIVerticalScrollbar codeScroll;
    public GUIScrollView log;

    static
    {
        MinecraftForge.EVENT_BUS.register(NBTGUI.class);
    }


    public NBTGUI(String nbtString)
    {
        this(nbtString, false);
    }

    public NBTGUI(String nbtString, boolean clientSide)
    {
        lines = MCTools.legibleNBT(nbtString);
        show();


        //Background
        root.add(new GUIDarkenedBackground(this));


        //TODO tabview; code (all current elements), templates (server), templates (local)

        //Buttons
        saveToObject = new GUITextButton(this, "Save to Object", Color.YELLOW);
        saveToObject.addClickActions(() ->
        {
            String codeString = code.getCodeAsCompressedString();
            try
            {
                if (clientSide) ClientCommands.save(codeString);
                else Network.WRAPPER.sendToServer(new Network.SaveToObjectPacket(codeString));
            }
            catch (Exception e)
            {
                setError(e.toString());
            }
        });
        root.add(saveToObject);

        saveToServerTemplate = new GUITextButton(this, "Save to Server Template", Color.GREEN);
        saveToServerTemplate.addClickActions(() ->
        {
            TextInputGUI gui = new TextInputGUI("Save Server Template", "Name: ");
            gui.input.input.filter = FilterNotEmpty.INSTANCE;
            gui.doneButton.onClickActions.clear();
            gui.doneButton.addClickActions(() ->
            {
                if (!gui.input.valid()) return;

                try
                {
                    Network.WRAPPER.sendToServer(new Network.SaveTemplatePacket(new CNBTTemplate(gui.input.getText(), Network.MAGIC_STRING, INBTSerializable.class, code.getCodeAsCompressedString())));
                }
                catch (NBTException e)
                {
                    setError(e.toString());
                }
                gui.close();
            });
        });
        root.add(saveToServerTemplate);

        loadFromServerTemplate = new GUITextButton(this, "Load from Server Template", Color.WHITE);
        loadFromServerTemplate.addClickActions(() -> Network.WRAPPER.sendToServer(new Network.RequestTemplateListPacket()));
        root.add(loadFromServerTemplate);

        //TODO save to local template button
        //TODO load from local template button

        closeButton = new GUITextButton(this, "Close", Color.RED);
        closeButton.addClickActions(this::close);
        root.add(closeButton);


        //Code input
        code = new CodeInput(this, 0.98, 2d / 3, lines.toArray(new String[0]));
        root.add(code);
        code.get(0).setActive(true);
        codeScroll = new GUIVerticalScrollbar(this, 0.02, 2d / 3, Color.GRAY, Color.BLANK, Color.WHITE, Color.BLANK, code);
        root.add(codeScroll);


        //Error log
        log = new GUIScrollView(this, 0.98, 1 - (code.y + code.height));
        root.add(log);
        GUIVerticalScrollbar scrollbar = new GUIVerticalScrollbar(this, 0.02, 1 - (code.y + code.height), Color.GRAY, Color.BLANK, Color.WHITE, Color.BLANK, log);
        root.add(scrollbar);


        //Warning
        setError("WARNING!!!\nLooking at the NBT should never cause an issue, but editing and saving can cause crashes and world corruption if you don't know what you're doing!  Use at your own risk!  World backup suggested!");
    }

    @Override
    public String title()
    {
        return "NBT Manipulator";
    }

    @Override
    protected void init()
    {
    }

    @Override
    public void onResize(Minecraft mcIn, int w, int h)
    {
        super.onResize(mcIn, w, h);

        code.y = closeButton.y + closeButton.height;
        code.height = 2d / 3 - code.y;
        code.recalc(0);

        for (int i = 1; i < log.size(); i++)
        {
            GUIElement element = log.get(i);
            element.y = (double) i * FONT_RENDERER.FONT_HEIGHT / height / log.height;
        }
        log.recalc(0);
    }

    public static void setError(String error)
    {
        if (error.equals(""))
        {
            setSuccess();
            return;
        }


        if (!(Minecraft.getMinecraft().currentScreen instanceof NBTGUI)) return;


        NBTGUI gui = (NBTGUI) Minecraft.getMinecraft().currentScreen;
        gui.log.clear();
        for (String err : error.replaceAll("\r", "").split("\n"))
        {
            gui.log.add(new GUIText(gui, 0, (double) gui.log.size() * FONT_RENDERER.FONT_HEIGHT / gui.height / gui.log.height, err, Color.RED));
        }
        gui.log.recalc(0);
    }

    public static void setSuccess()
    {
        if (!(Minecraft.getMinecraft().currentScreen instanceof NBTGUI)) return;


        NBTGUI gui = (NBTGUI) Minecraft.getMinecraft().currentScreen;
        gui.log.clear();
        gui.log.add(new GUIText(gui, 0, (double) gui.log.size() * FONT_RENDERER.FONT_HEIGHT / gui.height / gui.log.height, "Object successfully saved!", Color.GREEN));
        gui.log.recalc(0);
    }
}
