package com.oasisartisan.servermanager.tasks;

import com.oasisartisan.servermanager.Main;
import com.oasisartisan.servermanager.consolecommunication.Printer;
import com.oasisartisan.servermanager.objects.Server;
import com.oasisartisan.servermanager.storage.Storage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLockInterruptionException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 *
 * @author OmarAlama
 */
public class ServerFileCommunicatorTask extends Thread {

    public static final String PNAME = "ServerFileCommunicator";
    public static final String EXTENSION = ".monitordata";
    private static WatchService ws;
    private Thread locker;

    public void run() {
        Printer.printBackgroundInfo(PNAME, "Starting thread using the directory \"" + Storage.getSettings().getCommunicationDir() + "\".");
        Main.getCountDownLatch().countDown();
        try {
            //Atempt to update all the servers' monitor data.
            HashMap<String, Server> serverList = Storage.getServerList();
            synchronized (serverList) {
                for (Server s : serverList.values()) {
                    updateServerMonitorData(s);
                }
            }
            //Inform the ServerStateUpdaterTask that it can start.
            synchronized (Main.getServerStateUpdaterTask()) {
                Main.getServerStateUpdaterTask().notify();
            }
            //try to create the watch service and register our communication directory
            try {
                ws = FileSystems.getDefault().newWatchService();
                File file = Storage.getSettings().getCommunicationDir();
                if (!file.isDirectory()) {
                    Printer.printBackgroundInfo(PNAME, "The communication directory does not exist. creating it...");
                    if(!file.mkdirs())
                    {
                        Printer.printError(PNAME, "Failed to create the communication directory.", null);
                    }
                }
                Storage.getSettings().getCommunicationDir().toPath()
                        .register(ws, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
            } catch (IOException ex) {
                Printer.printError(PNAME, "Failed to start the file watch service, thread is ending.", ex);
                return;
            }
            HashMap<String, Long> checkHistory = new HashMap();
            while (true) {
                //wait for any changes in the directory
                WatchKey key = ws.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                        Printer.printError(PNAME, "Failed to read an event in the file watch event service", null);
                        continue;
                    }
                    //Get the name of the file that changed
                    Path path = (Path) event.context();
                    String name = path.toString();
                    //If it does not end with our extension it does not concern us
                    if (!name.endsWith(EXTENSION)) {
                        continue;
                    }
                    name = name.replace(EXTENSION, "");

                    Long lastCheck = checkHistory.get(name);
                    if (lastCheck != null && System.currentTimeMillis() - lastCheck < 200) {
                        continue;
                    }
                    Server server;
                    synchronized (serverList) {
                        server = serverList.get(name);
                    }
                    //if there is a server represented bu this file then atempt to update its monitor data because it has changed.
                    if (server != null) {
                        updateServerMonitorData(server);
                        checkHistory.put(name, System.currentTimeMillis());
                    }
                }
                key.reset();
            }
        } catch (InterruptedException ex) {
            try {
                Printer.printBackgroundInfo(PNAME, "Closing watch service and ending thread.");
                ws.close();
            } catch (IOException ex1) {
                Printer.printError(PNAME, "Failed to close watch service.", ex);
            }
        } catch (Exception e) {
            Printer.printError(PNAME, "An unexpected error occured.", e);
        }
    }

    public synchronized boolean updateServerMonitorData(Server s) {
        if (!s.isLinked()) {
            return false;
        }
        File file = new File(Storage.getSettings().getCommunicationDir() + "/" + s.getName() + EXTENSION);
        if (file.isFile()) {
            try (FileOutputStream fos = new FileOutputStream(file, true); FileInputStream fis = new FileInputStream(file)) {
                locker = new Thread() {
                    public void run() {
                        try {
                            fos.getChannel().lock();
                        } catch (FileLockInterruptionException ex) {
                        } catch (IOException ex) {
                            Printer.printError(PNAME, "Failed to acquire lock on file \"" + file + "\".", ex);
                        }
                    }
                };
                locker.start();
                locker.join(3000);
                if (locker.isAlive()) {
                    Printer.printBackgroundFail(PNAME, "Failed to read the file \"" + file + "\". Could not acquire lock.");
                    return false;
                }
                Scanner sc = new Scanner(fis);
                s.setFileUpdateInterval(Long.parseLong(sc.nextLine()));
                s.setLastPing(Long.parseLong(sc.nextLine()));
                ArrayList<String> msgs = new ArrayList();
                while (sc.hasNextLine()) {
                    msgs.add(sc.nextLine());
                }
                s.setCustomMessages(msgs);
                return true;
            } catch (Exception e) {
                Printer.printError(PNAME, "The server \"" + s.getName() + "\" monitor data file is broken.", e);
            }
        } else {
            Printer.printBackgroundFail(PNAME, "The monitor data file \"" + file.getAbsolutePath() + "\" was not found for the server \"" + s.getName() + "\".");
        }
        return false;
    }

    public void close() {
        if (locker != null && locker.isAlive()) {
            locker.interrupt();
        }
        this.interrupt();
    }
}
