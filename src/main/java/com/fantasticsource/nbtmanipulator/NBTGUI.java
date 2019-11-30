package com.fantasticsource.nbtmanipulator;

import com.fantasticsource.mctools.MCTools;
import com.fantasticsource.mctools.gui.GUILeftClickEvent;
import com.fantasticsource.mctools.gui.GUIScreen;
import com.fantasticsource.mctools.gui.element.GUIElement;
import com.fantasticsource.mctools.gui.element.other.GUIGradient;
import com.fantasticsource.mctools.gui.element.other.GUIVerticalScrollbar;
import com.fantasticsource.mctools.gui.element.text.CodeInput;
import com.fantasticsource.mctools.gui.element.text.GUIText;
import com.fantasticsource.mctools.gui.element.text.GUITextButton;
import com.fantasticsource.mctools.gui.element.text.GUITextInput;
import com.fantasticsource.mctools.gui.element.view.GUIScrollView;
import com.fantasticsource.tools.datastructures.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;

public class NBTGUI extends GUIScreen
{
    public static final int MODE_ITEM = 0;
    private static final Color BLACK = new Color(0xCC);

    public static final NBTGUI GUI = new NBTGUI();

    private static ArrayList<String> lines;
    private static GUITextButton saveButton, cancelButton;
    private static CodeInput code;
    private static GUIVerticalScrollbar codeScroll;
    private static GUIScrollView log;

    static
    {
        MinecraftForge.EVENT_BUS.register(NBTGUI.class);
    }


    public static void show(int mode, String nbtString)
    {
        lines = MCTools.legibleNBT(nbtString);
        Minecraft.getMinecraft().displayGuiScreen(GUI);


        GUI.root.clear();

        //Background
        GUI.root.add(new GUIGradient(GUI, 0, 0, 1, 1, BLACK, BLACK, BLACK, BLACK));


        //Buttons
        saveButton = new GUITextButton(GUI, 0, 0, "Save", Color.GREEN.copy().setAF(0.7f));
        GUI.root.add(saveButton);
        cancelButton = new GUITextButton(GUI, saveButton.x + saveButton.width, 0, "Close", Color.RED.copy().setAF(0.7f));
        GUI.root.add(cancelButton);


        //Multiline Text Input
        double y = saveButton.height;
        code = new CodeInput(GUI, 0, y, 0.98, 2d / 3 - y, lines.toArray(new String[0]));
        GUI.root.add(code);
        code.get(0).setActive(true);
        codeScroll = new GUIVerticalScrollbar(GUI, 0.98, 0, 0.02, 2d / 3, Color.GRAY, Color.BLANK, Color.WHITE, Color.BLANK, code);
        GUI.root.add(codeScroll);


        //Error log
        y = code.y + code.height;
        log = new GUIScrollView(GUI, 0, y, 0.98, 1 - y);
        GUI.root.add(log);
        GUIVerticalScrollbar scrollbar = new GUIVerticalScrollbar(GUI, 0.98, 2d / 3, 0.02, 1d / 3, Color.GRAY, Color.BLANK, Color.WHITE, Color.BLANK, log);
        GUI.root.add(scrollbar);


        //Multiline Text Input
        double yy = saveButton.height;
        GUI.root.remove(codeScroll);
        GUI.root.remove(code);
        code = new CodeInput(GUI, 0, yy, 0.98, 2d / 3 - yy, lines.toArray(new String[0]));
        GUI.root.add(code);
        code.get(0).setActive(true);
        codeScroll = new GUIVerticalScrollbar(GUI, 0.98, 0, 0.02, 2d / 3, Color.GRAY, Color.BLANK, Color.WHITE, Color.BLANK, code);
        GUI.root.add(codeScroll);
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
        cancelButton.x = saveButton.x + saveButton.width;

        code.y = saveButton.height;
        code.height = 2d / 3 - code.y;
        code.recalc(0);

        for (int i = 1; i < log.size(); i++)
        {
            GUIElement element = log.get(i);
            element.y = (double) i * FONT_RENDERER.FONT_HEIGHT / GUI.height / log.height;
        }
        log.recalc(0);
    }

    @SubscribeEvent
    public static void click(GUILeftClickEvent event)
    {
        GUIElement element = event.getElement();
        if (element == cancelButton) GUI.close();
        else if (element == saveButton)
        {
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < code.size(); i++)
            {
                s.append(((GUITextInput) code.get(i)).text.trim());
            }
            try
            {
                NBTTagCompound compound = JsonToNBT.getTagFromJson(s.toString());
                new ItemStack(compound);
                Network.WRAPPER.sendToServer(new Network.NBTSavePacket(s.toString()));
            }
            catch (Exception e)
            {
                setError(e.toString());
            }
        }
    }

    public static void setError(String error)
    {
        GUI.root.remove(log);
        double y = code.y + code.height;
        log = new GUIScrollView(GUI, 0, y, 1, 1 - y);
        GUI.root.add(log);
        for (String err : error.replaceAll("\r", "").split("\n"))
        {
            log.add(new GUIText(GUI, 0, (double) log.size() * FONT_RENDERER.FONT_HEIGHT / GUI.height / log.height, err, Color.RED));
        }
        log.recalc(0);
    }

    public static void setSuccess()
    {
        GUI.root.remove(log);
        double y = code.y + code.height;
        log = new GUIScrollView(GUI, 0, y, 1, 1 - y);
        GUI.root.add(log);
        log.add(new GUIText(GUI, 0, (double) log.size() * FONT_RENDERER.FONT_HEIGHT / GUI.height / log.height, "Item successfully saved!", Color.GREEN));
        log.recalc(0);
    }
}
