package com.oasisartisan.servermanager.tasks;

import com.oasisartisan.servermanager.Main;
import com.oasisartisan.servermanager.consolecommunication.Printer;
import com.oasisartisan.servermanager.objects.Server;
import com.oasisartisan.servermanager.objects.TimedCommand;
import com.oasisartisan.servermanager.objects.Timing;
import com.oasisartisan.servermanager.storage.Storage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author OmarAlama
 */
public class ServerCommandSchedulerTask extends Thread {

    private static ScheduledExecutorService ses;
    private static final int poolSize = 3;
    private static long interval;
    public static final String pName = "CommandSchedulerTask";

    public void run() {
        try {
            interval = Storage.getSettings().getCommandSchedulerTaskInterval();
            Printer.printBackgroundInfo(pName, "Starting thread with interval " + interval + " ms.");
            Main.getCountDownLatch().countDown();
            ses = Executors.newScheduledThreadPool(poolSize);
            try {
                while (true) {
                    HashMap<String, Server> servers = Storage.getServerList();
                    synchronized (servers) {
                        for (Server s : servers.values()) {
                            ArrayList<TimedCommand> cmds = s.getTimedCommands();
                            synchronized (cmds) {
                                for (TimedCommand tc : cmds) {
                                    processTimedCommand(s, tc);
                                }
                            }
                        }
                    }
                    synchronized(Storage.getGlobalServer().getTimedCommands())
                    {
                        for(TimedCommand tc : Storage.getGlobalServer().getTimedCommands())
                        {
                            processTimedCommand(Storage.getGlobalServer(), tc);
                        }
                    }
                    Thread.sleep(interval);
                    long newInterval = Storage.getSettings().getCommandSchedulerTaskInterval();
                    if (interval != newInterval) {
                        Printer.printBackgroundSuccess(pName, "Successfully updated interval to " + newInterval + " ms.");
                        interval = newInterval;
                    }
                }
            } catch (InterruptedException e) {
                Printer.printBackgroundInfo(pName, "Ending thread and shutting down the scheduled command thread pool");
                ses.shutdown();
                try {
                    ses.awaitTermination(3, TimeUnit.SECONDS);
                } catch (Exception ee) {
                    ses.shutdownNow();
                }
            }
        } catch (Exception e) {
            Printer.printError(pName, "An unexpected error occured.", e);
        }
    }

    public static void processTimedCommand(Server s, TimedCommand tc) {
        long millis = tc.getTime().getNextExecutionMillis(true);
        //Add a one second lineancy to avoid missing a command execution
        if (millis <= interval + 1000 && tc.getTask() == null) {
            Future task = ses.schedule(() -> {
                Printer.printBackgroundSuccess(pName, "The command \"" + tc.getCommand() + "\" for the server \"" + s.getName() + "\" is executing.");
                tc.exec(s);
                tc.setTask(null);
                if (tc.getTime().getType().equals("ONETIME")) {
                    s.removeTimedCommand(tc);
                    Storage.saveDataToFile();
                }
            }, millis, TimeUnit.MILLISECONDS);
            Printer.printBackgroundInfo(pName, "The command " + tc.getCommand() + " for the server \"" + s.getName() + "\" will run in " + Timing.getDurationBreakdown(millis));
            tc.setTask(task);
        }
    }
}
