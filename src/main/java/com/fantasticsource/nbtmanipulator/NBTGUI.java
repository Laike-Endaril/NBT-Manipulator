package com.fantasticsource.nbtmanipulator;

import com.fantasticsource.mctools.MCTools;
import com.fantasticsource.mctools.gui.GUIScreen;
import com.fantasticsource.mctools.gui.guielements.rect.GUIGradientRect;
import com.fantasticsource.mctools.gui.guielements.rect.text.MultilineTextInput;
import com.fantasticsource.tools.datastructures.Color;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;

public class NBTGUI extends GUIScreen
{
    public static final int MODE_ITEM = 0;
    private static final Color BLACK = new Color(0xEE);

    public static final NBTGUI GUI = new NBTGUI();

    private static ArrayList<String> lines;

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

        //Multiline Text Input
        MultilineTextInput multi = new MultilineTextInput(this, 0, 0, 1, 1, lines.toArray(new String[0]));
        guiElements.add(multi);
        multi.get(0).setActive(true);
    }
}
