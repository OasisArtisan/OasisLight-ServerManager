/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oasisartisan.servermanager.tasks;

import com.oasisartisan.servermanager.Main;
import com.oasisartisan.servermanager.consolecommunication.Printer;
import com.oasisartisan.servermanager.objects.Server;
import com.oasisartisan.servermanager.storage.Storage;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author OmarAlama
 */
public class MonitorTask extends Thread {

    private long interval;
    private int lvl;
    private List<String> serverNames;
    public static final String pName = "Monitor";

    public MonitorTask(long interval, int lvl) {
        this.interval = interval;
        this.lvl = lvl;
    }

    public MonitorTask(long interval, int lvl, List<String> serverNames) {
        this.interval = interval;
        this.serverNames = serverNames;
        this.lvl = lvl;
    }

    public void run() {
        try {
            HashMap<String, Server> serverList = Storage.getServerList();
            //Add the servers that will be displayed to a seperate list
            List<Server> orderedServerList = new ArrayList();
            if (serverNames.isEmpty()) {
                for (Server s : serverList.values()) {
                    orderedServerList.add(s);
                }
            } else {
                for (String name : serverNames) {
                    boolean found = false;
                    for (Server s : serverList.values()) {
                        if (name.equalsIgnoreCase(s.getName())) {
                            orderedServerList.add(s);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        Printer.printFailedReply(pName, "Server \"" + name + "\" was not found.");
                    }
                }
            }
            //Enter the monitor loop
            while (true) {
                Printer.printTitle(pName);
                Calendar c = Calendar.getInstance();
                Printer.printCustom(Printer.formatDevider("$BC"
                        + c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT_FORMAT, Locale.ENGLISH) + ", "
                        + c.get(Calendar.DAY_OF_MONTH) + " "
                        + c.getDisplayName(Calendar.MONTH, Calendar.SHORT_FORMAT, Locale.ENGLISH) + " "
                        + c.get(Calendar.YEAR) + " " + Printer.getTimeStamp(), "CENTER", 80, ' '));
                if (lvl >= 2) {
                    Storage.getGlobalServer().printTimedCommands();
                }
                synchronized (serverList) {
                    for (Server s : orderedServerList) {
                        s.printInfo(lvl);
                    }
                }
                Printer.printSuccessfullReply("");
                Printer.printSuccessfullReply("Hit 'Enter' to get back to the main menu...");
                Main.getProcessHandler().clearConsole();
                Printer.flushMonitorDisplay();
                Printer.flushMonitorMessages(Storage.getSettings().getMonitorMessagesDuration());
                Thread.sleep(interval);
            }
        } catch (InterruptedException e) {
            Main.getProcessHandler().clearConsole();
        } catch (Exception e) {
            Printer.printError(pName, "An unexpected error occured.", e);
        }
    }
}
