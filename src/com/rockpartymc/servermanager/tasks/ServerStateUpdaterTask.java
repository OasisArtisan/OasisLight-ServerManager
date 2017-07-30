package com.rockpartymc.servermanager.tasks;

import com.rockpartymc.servermanager.consolecommunication.Printer;
import com.rockpartymc.servermanager.objects.Server;
import com.rockpartymc.servermanager.objects.ServerState;
import com.rockpartymc.servermanager.storage.Storage;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author OmarAlama
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
            while (true) {
                //To start we get the screens that are currently running
                ArrayList<String> runningProcesses = null;
                try {
                    runningProcesses = Storage.getSettings().getProcessHandler().listProcesses();

                    //We itterate over every server registered
                    HashMap<String, Server> serverList = Storage.getServerList();
                    synchronized (serverList) {
                        for (String serverName : serverList.keySet()) {
                            Server server = serverList.get(serverName);
                            //First, we check if the server is included in our running processes list.
                            boolean hasProcess = false;
                            if (runningProcesses != null) {
                                for (String s : runningProcesses) {
                                    if (Storage.getSettings().getProcessHandler().hasActiveProcess(server, s)) { //The char is the horizontal tab
                                        hasProcess = true;
                                        break;
                                    }
                                }
                            }
                            //Finally, we conclude the state of the server.
                            if (hasProcess) {
                                if (server.getLastPing() == null || server.ignorePing || server.getFileUpdateInterval() == null
                                        || System.currentTimeMillis() - server.getLastPing() > server.getFileUpdateInterval() * 3) {
                                    if (server.getState() != ServerState.STARTING && server.getState() != ServerState.STOPPING
                                            && server.getState() != ServerState.TERMINATING) {
                                        server.setState(ServerState.NOTRESPONDING);
                                    }
                                } else if (server.getState() != ServerState.STOPPING && server.getState() != ServerState.TERMINATING) {
                                    server.setState(ServerState.ONLINE);
                                }
                            } else {
                                server.setState(ServerState.OFFLINE);
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
        }
    }
}
