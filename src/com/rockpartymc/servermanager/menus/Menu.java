/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rockpartymc.servermanager.menus;

import com.rockpartymc.servermanager.consolecommunication.Printer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author OmarAlama
 */
public class Menu extends InterfaceComponent{
    public final List<Item> items;
    public Menu(String name)
    {
        super(name);
        items = new ArrayList();
    }
    public Menu(String name, List<Item> items)
    {
        super(name);
        this.items = items;
    }
    public void invoke(String s)
    {
        for(Item i: items)
        {
            for(String cmd : i.commands)
            {
                if(s.equals(cmd))
                {
                    i.run();
                    return;
                }
            }
        }
        Printer.printFailedReply("Command: " + s + " not identified type help to list available commands.");
    }
    public void showHelp()
    {
        Printer.printSubTitle(name + " Menu Commands");
        for(Item i: items)
        {
            String cmds = i.commands.get(0);
            for(int j = 1; j < i.commands.size(); j++)
            {
                cmds += "," + i.commands.get(j);
            }
            Printer.printItem(cmds, i.help);
        }
    }
}
