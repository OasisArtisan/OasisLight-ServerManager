package com.oasisartisan.servermanager.tasks;

import com.oasisartisan.servermanager.Main;
import com.oasisartisan.servermanager.consolecommunication.Printer;
import com.oasisartisan.servermanager.objects.Server;
import com.oasisartisan.servermanager.storage.Storage;
import java.util.HashMap;

/**
 *
 * @author OmarAlama
 */
public class ShutDownTask extends Thread {

    @Override
    public void run() {
        Main.getServerStateUpdaterTask().interrupt();
        Main.getServerCommandSchedulerTask().interrupt();
        Main.getServerFileCommunicatorTask().close();
        if (Main.getMonitorTask() != null) {
            Main.getMonitorTask().interrupt();
        }
        //End all server threads
        HashMap<String, Server> serverList = Storage.getServerList();
        synchronized (serverList) {
            for (Server s : serverList.values()) {
                s.endCurrentThread();
                s.endBackupThread();
            }
        }
        try {
            Main.getServerCommandSchedulerTask().join();
            Main.getServerStateUpdaterTask().join();
            Main.getServerFileCommunicatorTask().join();
            if (Main.getMonitorTask() != null) {
                Main.getMonitorTask().join();
            }
        } catch (InterruptedException e) {
            Printer.printError("ShutdownTask", "Shutting down task has been interrupted", e);
        }
        Printer.printBackgroundInfo("ShutdownTask", "The program has shutdown.");
        Printer.close();
    }
}
