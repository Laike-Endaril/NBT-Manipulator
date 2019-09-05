package com.fantasticsource.nbtmanipulator;

import com.fantasticsource.mctools.MCTools;
import com.fantasticsource.mctools.gui.GUIScreen;
import com.fantasticsource.mctools.gui.guielements.rect.GUIGradientRect;
import com.fantasticsource.mctools.gui.guielements.rect.text.GUITextButton;
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
    private static GUITextButton saveButton, cancelButton;
    private static MultilineTextInput code;


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
        saveButton = new GUITextButton(this, 0, 0, "Save", Color.GREEN);
        guiElements.add(saveButton);
        cancelButton = new GUITextButton(this, saveButton.x + saveButton.width, 0, "Cancel", Color.RED);
        guiElements.add(cancelButton);


        //Multiline Text Input
        double y = saveButton.height;
        code = new MultilineTextInput(this, 0, y, 1, 1 - y, lines.toArray(new String[0]));
        guiElements.add(code);
        code.get(0).setActive(true);
    }

    @Override
    public void onResize(Minecraft mcIn, int w, int h)
    {
        super.onResize(mcIn, w, h);
        cancelButton.x = saveButton.x + saveButton.width;
        code.y = saveButton.y + saveButton.height;
        code.height = 1 - code.y;
    }
}
