package com.fantasticsource.nbtmanipulator;

import com.fantasticsource.mctools.MCTools;
import com.fantasticsource.mctools.gui.GUILeftClickEvent;
import com.fantasticsource.mctools.gui.GUIScreen;
import com.fantasticsource.mctools.gui.guielements.GUIElement;
import com.fantasticsource.mctools.gui.guielements.GUIVerticalScrollbar;
import com.fantasticsource.mctools.gui.guielements.rect.GUIGradientRect;
import com.fantasticsource.mctools.gui.guielements.rect.text.GUITextButton;
import com.fantasticsource.mctools.gui.guielements.rect.text.GUITextInputRect;
import com.fantasticsource.mctools.gui.guielements.rect.text.GUITextRect;
import com.fantasticsource.mctools.gui.guielements.rect.text.MultilineTextInput;
import com.fantasticsource.mctools.gui.guielements.rect.view.GUIRectScrollView;
import com.fantasticsource.tools.datastructures.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;

public class NBTGUI extends GUIScreen
{
    public static final int MODE_ITEM = 0;
    private static final Color BLACK = new Color(0xEE);

    public static final NBTGUI GUI = new NBTGUI();

    private static ArrayList<String> lines;
    private static GUITextButton saveButton, cancelButton;
    private static MultilineTextInput code;
    private static GUIRectScrollView log;

    static
    {
        MinecraftForge.EVENT_BUS.register(NBTGUI.class);
    }


    public static void show(int mode, String nbtString)
    {
        lines = MCTools.legibleNBT(nbtString);
        Minecraft.getMinecraft().displayGuiScreen(GUI);
        Keyboard.enableRepeatEvents(true);
    }

    @Override
    protected void init()
    {
        //Background
        guiElements.add(new GUIGradientRect(this, 0, 0, 1, 1, BLACK, BLACK, BLACK, BLACK));


        //Buttons
        saveButton = new GUITextButton(this, 0, 0, "Save", Color.GREEN.copy().setAF(0.7f));
        guiElements.add(saveButton);
        cancelButton = new GUITextButton(this, saveButton.x + saveButton.width, 0, "Close", Color.RED.copy().setAF(0.7f));
        guiElements.add(cancelButton);


        //Multiline Text Input
        double y = saveButton.height;
        code = new MultilineTextInput(this, 0, y, 1, 2d / 3 - y, lines.toArray(new String[0]));
        guiElements.add(code);
        code.get(0).setActive(true);
        GUIVerticalScrollbar scrollbar = new GUIVerticalScrollbar(this, 0.97, 0, 0.03, 2d / 3, Color.GRAY, Color.BLANK, Color.WHITE, Color.BLANK, code);
        guiElements.add(scrollbar);


        //Error log
        y = code.y + code.height;
        log = new GUIRectScrollView(this, 0, y, 1, 1 - y);
        guiElements.add(log);
        scrollbar = new GUIVerticalScrollbar(this, 0.97, 2d / 3, 0.03, 1d / 3, Color.GRAY, Color.BLANK, Color.WHITE, Color.BLANK, log);
        guiElements.add(scrollbar);
    }

    @Override
    public void onResize(Minecraft mcIn, int w, int h)
    {
        super.onResize(mcIn, w, h);
        cancelButton.x = saveButton.x + saveButton.width;

        code.y = saveButton.height;
        code.height = 2d / 3 - code.y;

        for (int i = 1; i < log.size(); i++)
        {
            GUIElement element = log.get(i);
            element.y = (double) i * FONT_RENDERER.FONT_HEIGHT / GUI.height / log.height;
        }
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
                s.append(((GUITextInputRect) code.get(i)).text.trim());
            }
            try
            {
                NBTTagCompound compound = JsonToNBT.getTagFromJson(s.toString());
                ItemStack stack = new ItemStack(compound);
            }
            catch (Exception e)
            {
                setLog(e.toString());
            }
        }
    }

    public static void setLog(String error)
    {
        while (log.size() > 0) log.remove(0);
        for (String err : error.replaceAll("\r", "").split("\n"))
        {
            log.add(new GUITextRect(GUI, 0, (double) log.size() * FONT_RENDERER.FONT_HEIGHT / GUI.height / log.height, err, Color.RED));
            int i = 0;
            log.add(new GUITextRect(GUI, 0, (double) log.size() * FONT_RENDERER.FONT_HEIGHT / GUI.height / log.height, "" + i++, Color.RED));
            log.add(new GUITextRect(GUI, 0, (double) log.size() * FONT_RENDERER.FONT_HEIGHT / GUI.height / log.height, "" + i++, Color.RED));
            log.add(new GUITextRect(GUI, 0, (double) log.size() * FONT_RENDERER.FONT_HEIGHT / GUI.height / log.height, "" + i++, Color.RED));
            log.add(new GUITextRect(GUI, 0, (double) log.size() * FONT_RENDERER.FONT_HEIGHT / GUI.height / log.height, "" + i++, Color.RED));
            log.add(new GUITextRect(GUI, 0, (double) log.size() * FONT_RENDERER.FONT_HEIGHT / GUI.height / log.height, "" + i++, Color.RED));
            log.add(new GUITextRect(GUI, 0, (double) log.size() * FONT_RENDERER.FONT_HEIGHT / GUI.height / log.height, "" + i++, Color.RED));
            log.add(new GUITextRect(GUI, 0, (double) log.size() * FONT_RENDERER.FONT_HEIGHT / GUI.height / log.height, "" + i++, Color.RED));
            log.add(new GUITextRect(GUI, 0, (double) log.size() * FONT_RENDERER.FONT_HEIGHT / GUI.height / log.height, "" + i++, Color.RED));
            log.add(new GUITextRect(GUI, 0, (double) log.size() * FONT_RENDERER.FONT_HEIGHT / GUI.height / log.height, "" + i++, Color.RED));
            log.add(new GUITextRect(GUI, 0, (double) log.size() * FONT_RENDERER.FONT_HEIGHT / GUI.height / log.height, "" + i++, Color.RED));
            log.add(new GUITextRect(GUI, 0, (double) log.size() * FONT_RENDERER.FONT_HEIGHT / GUI.height / log.height, "" + i++, Color.RED));
            log.add(new GUITextRect(GUI, 0, (double) log.size() * FONT_RENDERER.FONT_HEIGHT / GUI.height / log.height, "" + i++, Color.RED));
            log.add(new GUITextRect(GUI, 0, (double) log.size() * FONT_RENDERER.FONT_HEIGHT / GUI.height / log.height, "" + i++, Color.RED));
            log.add(new GUITextRect(GUI, 0, (double) log.size() * FONT_RENDERER.FONT_HEIGHT / GUI.height / log.height, "" + i++, Color.RED));
            log.add(new GUITextRect(GUI, 0, (double) log.size() * FONT_RENDERER.FONT_HEIGHT / GUI.height / log.height, "" + i++, Color.RED));
            log.add(new GUITextRect(GUI, 0, (double) log.size() * FONT_RENDERER.FONT_HEIGHT / GUI.height / log.height, "" + i++, Color.RED));
            log.add(new GUITextRect(GUI, 0, (double) log.size() * FONT_RENDERER.FONT_HEIGHT / GUI.height / log.height, "" + i++, Color.RED));
            log.add(new GUITextRect(GUI, 0, (double) log.size() * FONT_RENDERER.FONT_HEIGHT / GUI.height / log.height, "" + i++, Color.RED));
            log.add(new GUITextRect(GUI, 0, (double) log.size() * FONT_RENDERER.FONT_HEIGHT / GUI.height / log.height, "" + i++, Color.RED));
            log.add(new GUITextRect(GUI, 0, (double) log.size() * FONT_RENDERER.FONT_HEIGHT / GUI.height / log.height, "" + i++, Color.RED));
            log.add(new GUITextRect(GUI, 0, (double) log.size() * FONT_RENDERER.FONT_HEIGHT / GUI.height / log.height, "" + i++, Color.RED));
            log.add(new GUITextRect(GUI, 0, (double) log.size() * FONT_RENDERER.FONT_HEIGHT / GUI.height / log.height, "" + i++, Color.RED));
            log.add(new GUITextRect(GUI, 0, (double) log.size() * FONT_RENDERER.FONT_HEIGHT / GUI.height / log.height, "" + i++, Color.RED));
            log.add(new GUITextRect(GUI, 0, (double) log.size() * FONT_RENDERER.FONT_HEIGHT / GUI.height / log.height, "" + i++, Color.RED));
            log.add(new GUITextRect(GUI, 0, (double) log.size() * FONT_RENDERER.FONT_HEIGHT / GUI.height / log.height, "" + i++, Color.RED));
            log.add(new GUITextRect(GUI, 0, (double) log.size() * FONT_RENDERER.FONT_HEIGHT / GUI.height / log.height, "" + i++, Color.RED));
        }
        log.recalc();
    }
}
