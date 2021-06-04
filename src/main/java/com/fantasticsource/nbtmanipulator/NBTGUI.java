package com.fantasticsource.nbtmanipulator;

import com.fantasticsource.mctools.MCTools;
import com.fantasticsource.mctools.gui.GUIScreen;
import com.fantasticsource.mctools.gui.element.GUIElement;
import com.fantasticsource.mctools.gui.element.other.GUIDarkenedBackground;
import com.fantasticsource.mctools.gui.element.other.GUIVerticalScrollbar;
import com.fantasticsource.mctools.gui.element.text.CodeInput;
import com.fantasticsource.mctools.gui.element.text.GUINavbar;
import com.fantasticsource.mctools.gui.element.text.GUIText;
import com.fantasticsource.mctools.gui.element.text.GUITextButton;
import com.fantasticsource.mctools.gui.element.text.filter.FilterNotEmpty;
import com.fantasticsource.mctools.gui.element.view.GUIList;
import com.fantasticsource.mctools.gui.element.view.GUIScrollView;
import com.fantasticsource.mctools.gui.element.view.GUITabView;
import com.fantasticsource.mctools.gui.screen.TextInputGUI;
import com.fantasticsource.mctools.gui.screen.TextSelectionGUI;
import com.fantasticsource.mctools.gui.screen.YesNoGUI;
import com.fantasticsource.tools.datastructures.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTException;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class NBTGUI extends GUIScreen
{
    public String title;
    public ArrayList<String> lines;
    public GUITabView tabView;
    public GUITextButton saveToObject, saveToServerTemplate, loadFromServerTemplate, saveToLocalTemplate, loadFromLocalTemplate;
    public CodeInput code;
    public GUIVerticalScrollbar codeScroll;
    public GUIScrollView log;

    static
    {
        MinecraftForge.EVENT_BUS.register(NBTGUI.class);
    }


    public NBTGUI(Class<? extends INBTSerializable> category, HashMap<String, CNBTTemplate> map, String nbtString)
    {
        this(category, map, nbtString, false);
    }

    public NBTGUI(Class<? extends INBTSerializable> category, HashMap<String, CNBTTemplate> serverMap, String nbtString, boolean clientSide)
    {
        title = "NBT Manipulator (" + category.getSimpleName() + ")";
        lines = MCTools.legibleNBT(nbtString);
        show();


        //Background and navbar
        GUINavbar navbar = new GUINavbar(this);
        root.addAll(
                new GUIDarkenedBackground(this),
                navbar
        );

        //Tabview
        tabView = new GUITabView(this, 1, 1, "Code", "Server Template List", "Local Template List");
        root.add(tabView);
        navbar.addRecalcActions(() -> tabView.height = 1 - tabView.y);


        //Code Tab

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
        tabView.tabViews.get(0).add(saveToObject);

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
        tabView.tabViews.get(0).add(saveToServerTemplate);

        loadFromServerTemplate = new GUITextButton(this, "Load from Server Template", Color.WHITE);
        loadFromServerTemplate.addClickActions(() -> Network.WRAPPER.sendToServer(new Network.RequestTemplateListPacket()));
        tabView.tabViews.get(0).add(loadFromServerTemplate);

        saveToLocalTemplate = new GUITextButton(this, "Save to Local Template", Color.GREEN);
        saveToLocalTemplate.addClickActions(() ->
        {
            TextInputGUI gui = new TextInputGUI("Save Server Template", "Name: ");
            gui.input.input.filter = FilterNotEmpty.INSTANCE;
            gui.doneButton.onClickActions.clear();
            gui.doneButton.addClickActions(() ->
            {
                if (!gui.input.valid()) return;

                try
                {
                    CNBTTemplate template = new CNBTTemplate(gui.input.getText(), "", category, code.getCodeAsCompressedString());
                    HashMap<String, CNBTTemplate> map2 = CNBTTemplate.TEMPLATES.computeIfAbsent(category, o -> new HashMap<>());
                    CNBTTemplate oldTemplate = map2.get(template.name);
                    if (oldTemplate == null)
                    {
                        map2.put(template.name, template);
                        gui.close();
                    }
                    else
                    {
                        template.description = oldTemplate.description;
                        YesNoGUI gui2 = new YesNoGUI("Overwrite Template?", "Overwrite template " + '"' + template.name + '"' + " (" + template.category.getSimpleName() + ")?");
                        gui2.addOnClosedActions(() ->
                        {
                            if (gui2.pressedYes)
                            {
                                map2.put(template.name, template);
                                gui.close();
                            }
                        });
                    }
                    gui.close();
                }
                catch (NBTException e)
                {
                    setError(e.toString());
                }
            });
        });
        tabView.tabViews.get(0).add(saveToLocalTemplate);

        loadFromLocalTemplate = new GUITextButton(this, "Load from Local Template", Color.WHITE);
        loadFromLocalTemplate.addClickActions(() ->
        {
            GUIText fake = new GUIText(this, "");
            HashMap<String, CNBTTemplate> map2 = CNBTTemplate.TEMPLATES.computeIfAbsent(category, o -> new HashMap<>());
            ArrayList<String> list = new ArrayList<>(map2.keySet());
            Collections.sort(list);
            TextSelectionGUI gui2 = new TextSelectionGUI(fake, "Load " + category + " Template", list.toArray(new String[0]));
            for (GUIElement element : gui2.root.children)
            {
                if (element instanceof GUIList)
                {
                    for (GUIList.Line line : ((GUIList) element).getLines())
                    {
                        GUIText text = (GUIText) line.getLineElement(0);
                        text.setTooltip(map2.get(text.getText()).description);
                    }
                }
            }

            gui2.addOnClosedActions(() ->
            {
                CNBTTemplate template = map2.get(fake.getText());
                if (template != null) code.setCode(MCTools.legibleNBT(template.objectNBT));
            });
        });
        tabView.tabViews.get(0).add(loadFromLocalTemplate);

        //Code input
        code = new CodeInput(this, 0.98, 2d / 3, lines.toArray(new String[0]));
        tabView.tabViews.get(0).add(code);
        code.get(0).setActive(true);
        codeScroll = new GUIVerticalScrollbar(this, 0.02, 2d / 3, Color.GRAY, Color.BLANK, Color.WHITE, Color.BLANK, code);
        tabView.tabViews.get(0).add(codeScroll);

        //Error log
        log = new GUIScrollView(this, 0.98, 1 - (code.y + code.height));
        tabView.tabViews.get(0).add(log);
        GUIVerticalScrollbar scrollbar = new GUIVerticalScrollbar(this, 0.02, 1 - (code.y + code.height), Color.GRAY, Color.BLANK, Color.WHITE, Color.BLANK, log);
        tabView.tabViews.get(0).add(scrollbar);

        //Warning
        setError("WARNING!!!\nLooking at the NBT should never cause an issue, but editing and saving can cause crashes and world corruption if you don't know what you're doing!  Use at your own risk!  World backup suggested!");


        //Server Template List

        ArrayList<String> serverList = new ArrayList<>(serverMap.keySet());
        Collections.sort(serverList);
        //TODO


        //Local Template List

        HashMap<String, CNBTTemplate> clientMap = CNBTTemplate.TEMPLATES.get(category);
        ArrayList<String> clientList = clientMap == null ? new ArrayList<>() : new ArrayList<>(clientMap.keySet());
        Collections.sort(clientList);
        //TODO
    }

    @Override
    public String title()
    {
        return title;
    }

    @Override
    protected void init()
    {
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
