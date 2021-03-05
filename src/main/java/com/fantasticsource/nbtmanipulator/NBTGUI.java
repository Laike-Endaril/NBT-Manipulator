package com.fantasticsource.nbtmanipulator;

import com.fantasticsource.mctools.MCTools;
import com.fantasticsource.mctools.gui.GUIScreen;
import com.fantasticsource.mctools.gui.element.GUIElement;
import com.fantasticsource.mctools.gui.element.other.GUIDarkenedBackground;
import com.fantasticsource.mctools.gui.element.other.GUIVerticalScrollbar;
import com.fantasticsource.mctools.gui.element.text.CodeInput;
import com.fantasticsource.mctools.gui.element.text.GUIText;
import com.fantasticsource.mctools.gui.element.text.GUITextButton;
import com.fantasticsource.mctools.gui.element.text.GUITextInput;
import com.fantasticsource.mctools.gui.element.view.GUIScrollView;
import com.fantasticsource.tools.datastructures.Color;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;

public class NBTGUI extends GUIScreen
{
    protected ArrayList<String> lines;
    protected GUITextButton saveButton, closeButton;
    protected CodeInput code;
    protected GUIVerticalScrollbar codeScroll;
    protected GUIScrollView log;

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


        //Buttons
        saveButton = new GUITextButton(this, "Save", Color.GREEN);
        saveButton.addClickActions(() ->
        {
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < code.size(); i++)
            {
                s.append(((GUITextInput) code.get(i)).getText().trim());
            }
            try
            {
                if (clientSide) ClientCommands.save(s.toString());
                else Network.WRAPPER.sendToServer(new Network.NBTSavePacket(s.toString()));
            }
            catch (Exception e)
            {
                setError(e.toString());
            }
        });
        root.add(saveButton);

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
        closeButton.x = saveButton.x + saveButton.width;

        code.y = saveButton.height;
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
