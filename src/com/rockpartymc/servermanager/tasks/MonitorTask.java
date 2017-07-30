/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rockpartymc.servermanager.tasks;

import com.rockpartymc.servermanager.processhandlers.BashProcessHandler;
import com.rockpartymc.servermanager.consolecommunication.Printer;
import com.rockpartymc.servermanager.objects.Server;
import com.rockpartymc.servermanager.storage.Storage;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

/**
 *
 * @author OmarAlama
 */
public class MonitorTask extends Thread {

    private long interval;
    private boolean inDetail;

    public MonitorTask(long interval, boolean inDetail) {
        this.interval = interval;
        this.inDetail = inDetail;
    }

    public void run() {
        try {
            while (true) {
                Printer.printTitle("Monitor");
                Calendar c = Calendar.getInstance();
                Printer.printCustom(Printer.formatDevider("$BC"
                        + c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT_FORMAT, Locale.ENGLISH) + ", "
                        + c.get(Calendar.DAY_OF_MONTH) + " "
                        + c.getDisplayName(Calendar.MONTH, Calendar.SHORT_FORMAT, Locale.ENGLISH) + " "
                        + c.get(Calendar.YEAR) + " " + Printer.getTimeStamp(), "CENTER", 80, ' '));
                HashMap<String, Server> serverList = Storage.getServerList();
                synchronized (serverList) {
                    for (Server s : serverList.values()) {
                        s.printInfo(inDetail);
                    }
                }
                Printer.printSuccessfullReply("");
                Printer.printSuccessfullReply("Enter any charactar to exit");
                Storage.getSettings().getProcessHandler().clearConsole();
                Printer.flushMonitorDisplay();
                Printer.flushMonitorMessages(Storage.getSettings().getMonitorMessagesDuration());
                Thread.sleep(interval);
            }
        } catch (InterruptedException e) {
        }
    }
}
