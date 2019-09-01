package com.fantasticsource.nbtmanipulator;

import com.fantasticsource.mctools.MCTools;
import com.fantasticsource.mctools.gui.GUIScreen;
import com.fantasticsource.mctools.gui.guielements.rect.GUIGradientRect;
import com.fantasticsource.mctools.gui.guielements.rect.text.MultilineTextInput;
import com.fantasticsource.tools.datastructures.Color;

import java.util.ArrayList;

public class NBTGUI extends GUIScreen
{
    public static final int MODE_ITEM = 0;
    private static final Color BLACK = new Color(0xEE);

    private final ArrayList<String> lines;

    public NBTGUI(int mode, String nbtString)
    {
        lines = MCTools.legibleNBT(nbtString);
    }

    @Override
    public void initGui()
    {
        super.initGui();

        //Background
        guiElements.add(new GUIGradientRect(this, 0, 0, 1, 1, BLACK, BLACK, BLACK, BLACK));

        //Multiline Text Input
        guiElements.add(new MultilineTextInput(this, 0, 0, 1, 1, lines.toArray(new String[0])));
    }
}
