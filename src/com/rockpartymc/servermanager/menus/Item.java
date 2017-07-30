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
public class Item implements Runnable{
    public final List<String> commands;
    public final String help;
    private final Runnable run;
    
    public Item(String command, String help, Runnable run)
    {
        this.commands = new ArrayList();
        this.commands.add(command);
        this.help = help;
        this.run = run;
    }
    public Item(List<String> commands, String help, Runnable run)
    {
        this.commands = commands;
        this.help = help;
        this.run = run;
    }
    public Item(String help, Runnable run, String... commands)
    {
        this.commands = new ArrayList();
        for (String c: commands)
        {
            this.commands.add(c);
        }
        this.help = help;
        this.run = run;
    }
    public void run()
    {
        try {
            run.run();
        }
        catch(Exception e)
        {
            Printer.printError(commands.get(0), "An unexpected error occured", e);
        }
    }
}
