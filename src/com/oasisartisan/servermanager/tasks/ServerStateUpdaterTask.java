package com.oasisartisan.servermanager.tasks;

import com.oasisartisan.servermanager.Main;
import com.oasisartisan.servermanager.consolecommunication.Printer;
import com.oasisartisan.servermanager.objects.Server;
import static com.oasisartisan.servermanager.objects.ServerState.*;
import com.oasisartisan.servermanager.storage.Storage;
import java.util.ArrayList;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author OasisArtisan
 */
public class ServerStateUpdaterTask extends Thread {

    public static final String path = "lmsm-data";
    public static final String pName = "StateUpdater";
    private static long interval;

    @Override
    public void run() {
        try {
            //The initial wait for the ServerFileCommunicatorTask to finish its first check
            synchronized (this) {
                this.wait(10000);
            }
            interval = Storage.getSettings().getServerStateUpdaterTaskInterval();
            Printer.printBackgroundInfo(pName, "Starting thread with interval \"" + interval + " ms\".");
            Main.getCountDownLatch().countDown();
            while (true) {
                //To start we get the screens that are currently running
                try {
                    ArrayList<String> runningProcesses = Main.getProcessHandler().listProcesses();

                    //We itterate over every server registered
                    HashMap<String, Server> serverList = Storage.getServerList();
                    synchronized (serverList) {
                        for (String serverName : serverList.keySet()) {
                            Server server = serverList.get(serverName);
                            //First, we check if the server is included in our running processes list.
                            boolean hasProcess = false;
                            if (runningProcesses != null) {
                                for (String s : runningProcesses) {
                                    if (Main.getProcessHandler().hasActiveProcess(server, s)) {
                                        hasProcess = true;
                                        break;
                                    }
                                }
                            }
                            //Finally, we conclude the state of the server.
                            if (hasProcess) {
                                if (server.isLinked() && (server.getLastPing() == null || server.ignorePing || server.getFileUpdateInterval() == null
                                        || System.currentTimeMillis() - server.getLastPing() > server.getFileUpdateInterval() * 3)) {
                                    if (server.getState() != STARTING && server.getState() != STOPPING
                                            && server.getState() != TERMINATING) {
                                        server.setState(NOTRESPONDING);
                                    }
                                } else if (server.getState() != STOPPING && server.getState() != TERMINATING) {
                                    server.setState(ONLINE);
                                }
                            } else {
                                server.setState(OFFLINE);
                            }
                        }
                    }
                } catch (IOException e) {
                    Printer.printError(pName, "Cannot update server states. Failed to list running screens.", e);
                }
                Thread.sleep(interval);
                long newInterval = Storage.getSettings().getServerStateUpdaterTaskInterval();
                if (newInterval != interval) {
                    Printer.printBackgroundSuccess(pName, "Successfully updated interval to " + newInterval + " ms.");
                    interval = newInterval;
                }
            }
        } catch (InterruptedException e) {
            Printer.printBackgroundInfo(pName, "Ending thread.");
        } catch (Exception e) {
            Printer.printError(pName, "An unexpected error occured.", e);
        }
    }
}
