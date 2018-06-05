package com.oasisartisan.servermanager.objects;

import com.oasisartisan.servermanager.Main;
import com.oasisartisan.servermanager.consolecommunication.Printer;
import com.oasisartisan.servermanager.tasks.ServerCommandSchedulerTask;
import com.oasisartisan.servermanager.processhandlers.ProcessHandler;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import static com.oasisartisan.servermanager.objects.ServerState.*;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
/**
 *
 * @author OasisArtisan
 */
public class Server implements Serializable {

    private String name;
    private File file;
    private Long lastPing;
    private Long fileUpdateInterval;
    private ArrayList<TimedCommand> timedCommands;
    private ServerSettings settings;
    private boolean linked;
    private transient ArrayList<String> customMessages;
    private transient ServerState state;
    
    private transient Thread currentThread;
    private transient boolean restarting;
    public transient boolean ignorePing = false;
    
    //Backup variables
    private transient Thread backupThread;
    private transient Thread backupProgressReader;
    private transient String[] backupOutput;
    private transient long backupStartTime;
    private Long lastBackup;
    private String lastBackupType;
    
    public Server(String name, String path) {
        this.name = name;
        this.file = new File(path);
        timedCommands = new ArrayList();
        settings = new ServerSettings();
        linked = false;
        backupOutput = new String[2];
    }

    public synchronized boolean start() {
        Printer.printBackgroundInfo(name, "Attempting to start the server.");
        if (currentThread != null) {
            Printer.printBackgroundFail(name, "Could not start server. Another process is currently ongoing.");
            return false;
        }
        if (state != OFFLINE) {
            Printer.printBackgroundFail(name, "Could not start server. It is not offline.");
            return false;
        }
        if (!file.isFile()) {
            Printer.printBackgroundFail(name, "Could not start server. Jar file is missing.");
            return false;
        }
        Server server = this;
        currentThread = new Thread() {
            @Override
            public void run() {
                try {
                    Process p = Main.getProcessHandler().startServerProcess(server);
                    ProcessHandler.finishProcess(p, name + " >> START", 10);
                    try {
                        Thread.sleep(server.getSettings().getMaxStartingDuration());
                        if (server.getState() == STARTING) {
                            server.setState(NOTRESPONDING);
                        }

                    } catch (InterruptedException e) {
                    }
                } catch (IOException e) {
                    Printer.printError(server.getName(), "Failed to send start command to the server.", e);
                } catch (Exception e) {
                    Printer.printError("start-" + name, "An unexpected error occured.", e);
                }
                currentThread = null;
            }
        };
        currentThread.setName("start-" + name);
        currentThread.start();
        ignorePing = true;
        setState(STARTING);
        return true;
    }

    public synchronized boolean restart() {
        Printer.printBackgroundInfo(name, "Attempting to restart the server.");
        if (state == OFFLINE) {
            return start();
        }
        boolean result = stop();
        restarting = result;
        return result;
    }

    public synchronized boolean stop() {
        Printer.printBackgroundInfo(name, "Attempting to stop the server.");
        if (currentThread != null) {
            Printer.printBackgroundFail(name, "Could not stop server. Another process is currently ongoing.");
            return false;
        }
        if (state == TERMINATING) {
            return kill();
        }
        if (state != ONLINE && state != NOTRESPONDING) {
            Printer.printBackgroundFail(name, "Could not stop server. It is " + state);
            return false;
        }
        Server server = this;
        currentThread = new Thread() {
            @Override
            public void run() {
                try {
                    Process p = Main.getProcessHandler().sendCommandToServer(server, server.getSettings().getStopCommand());
                    ProcessHandler.finishProcess(p, name + " >> STOP", 10);
                    try {
                        Thread.sleep(server.getSettings().getMaxStoppingDuration());
                        if (server.getState() == STOPPING) {
                            Printer.printBackgroundFail(server.getName(), "Server failed to stop in time. Killing the server process!");
                            server.kill();
                        }
                    } catch (InterruptedException e) {
                    }
                } catch (IOException | NullPointerException e) {
                    Printer.printError(server.getName(), "Failed to send stop command to the server.\nMake sure that you have started the server using the manager !", e);
                    if (server.getState() == STOPPING) {
                        Printer.printBackgroundFail(server.getName(), "Killing the server process!");
                        server.kill();
                    }
                } catch (Exception e) {
                    Printer.printError("stop-" + name, "An unexpected error occured.", e);
                }
                currentThread = null;
            }
        };
        currentThread.setName("stop-" + name);
        currentThread.start();
        setState(STOPPING);
        return true;
    }

    public synchronized boolean kill() {
        Printer.printBackgroundInfo(name, "Attempting to kill the server.");
        if (state == OFFLINE) {
            Printer.printBackgroundFail(name, "Could not kill server. It is already offline.");
            return false;
        } else if (state == TERMINATING && currentThread != null)
        {
            Printer.printBackgroundFail(name, "Could not kill server. It is already TERMINATING.");
            return false;
        }
        endCurrentThread();
        Server server = this;
        currentThread = new Thread()
        {
            @Override
            public void run() {
                try {
                    long wait = 3000;
                    ProcessHandler ph = Main.getProcessHandler();
                    Process p;
                    boolean firstTry = true;
                    while (true) {
                        try {
                            if (firstTry) {
                                p = ph.killServerProcess(server, true);
                                if (p == null) {
                                    break;
                                }
                                ProcessHandler.finishProcess(p, name + " >> KILL(15)", 15);
                                firstTry = false;
                            }
                            long time = System.currentTimeMillis();
                            while (System.currentTimeMillis() - time < wait) {
                                synchronized (Main.getServerStateUpdaterTask()) {
                                    Main.getServerStateUpdaterTask().wait();
                                }
                            }
                            Printer.printBackgroundInfo(name, "Retrying to kill the server process...");
                            p = ph.killServerProcess(server, false);
                            if (p == null) {
                                break;
                            }
                            ProcessHandler.finishProcess(p, name + " >> KILL(9)", 20);
                            Thread.sleep(3000);
                            Printer.printError(name, "Possibly failed to kill the server process.", null);
                        } catch (IOException | NullPointerException e) {
                            Printer.printError(name, "Possibly failed to kill the server process.", null);
                        }
                        wait *= 2;
                    }
                } catch (InterruptedException e) {

                } catch (Exception e) {
                    Printer.printError("kill-" + name, "An unexpected error occured.", e);
                }
                currentThread = null;
            }
        };
        currentThread.setName("kill-" + name);
        currentThread.start();
        setState(TERMINATING);
        return true;
    }

    public synchronized boolean sendCommand(String command) {
        if (state == ONLINE || state == NOTRESPONDING) {
            try {
                Process p = Main.getProcessHandler().sendCommandToServer(this, command);
                if (ProcessHandler.finishProcess(p, name + " >> SEND", 10)) {
                    Printer.printBackgroundSuccess(name, "Successfuly sent command \"" + command + "\" to the server.");
                }
                return false;
            } catch (IOException e) {
                Printer.printError(name, "Failed to send command \"" + command + "\" to the server.", e);
                return false;
            } catch (Exception e) {
                Printer.printError("send-command-" + name, "An unexpected error occured.", e);
            }
        }
        Printer.printBackgroundFail(name, "Failed to send command \"" + command + "\" to the server. Server must be online or notresponding.");
        return false;
    }

    public boolean backup(BackupProfile bp) {
        if (backupThread != null) {
            Printer.printBackgroundFail(name, "Could not backup server. Another backup process is currently ongoing.");
            return false;
        }
        String pName = "backup-" + name + ":" + bp.getName();
        Server s = this;
        backupThread = new Thread()
        {
            @Override
            public void run() {
                Printer.printBackgroundInfo(pName, "Starting backup process.");
                backupStartTime = System.currentTimeMillis();
                try {
                    Process p = Main.getProcessHandler().executeBackup(s, bp);
                    //Start a thread that will read the backup's process output and store the latest message in lastBackupMessage
                    if(backupOutput == null) {
                        backupOutput = new String[2];
                    }
                    backupOutput[0] = "";
                    backupOutput[1] = "";
                    Thread backupProgressReader = new Thread() {
                        @Override
                        public void run() {
                            try {
                                InputStreamReader isr = new InputStreamReader(p.getInputStream());
                                char c = (char) isr.read();
                                char endcharactar = (int)'\n';
                                while (p.isAlive()) {
                                    //System.out.print(c + " " + (int) c);
                                    if (c == endcharactar) {
                                        backupOutput[0] = backupOutput[1];
                                        backupOutput[1] = "";
                                    } else {
                                        backupOutput[1] += c;
                                    }
                                    c = (char) isr.read();
                                }
                            } catch (IOException e) {
                                Printer.printError(this.getName(), "The reader has encountered an error", null);
                            }
                        }
                    };
                    backupProgressReader.setName("BackupProgressReader-" + s.getName() + "-" + bp.getName());
                    backupProgressReader.start();
                    //Now print the progress every now and then to keep the user informed
                    try {
                        while (true) {
                            p.waitFor(300, TimeUnit.SECONDS);
                            if (!p.isAlive()) {
                                break;
                            }
                            String progress = Main.getProcessHandler().translateBackupOutputToProgress(backupOutput);
                            if(progress.isEmpty()) {
                                Printer.printBackgroundInfo(pName, "backing up in progress...");
                            } else
                            {
                                Printer.printBackgroundInfo(pName, "backing up in progress " + progress);
                            }
                        }
                        lastBackup = System.currentTimeMillis();
                        lastBackupType = bp.getName();
                        Printer.printDataChange(pName, "has completed the backup successfully. Backing up took " + Timing.getDurationBreakdown(lastBackup-backupStartTime));
                    } catch (InterruptedException e) {
                        p.destroy();
                        Printer.printError(pName, "Failed to complete backup. The process has been interrupted.", null);
                    }
                } catch (IOException ex) {
                    Printer.printError(pName, "Failed to execute backup", ex);
                } catch (Exception e) {
                    Printer.printError("backup-" + name, "An unexpected error occured.", e);
                }
                backupThread = null;
            }
        };
        backupThread.setName(pName);
        backupThread.start();
        return true;
    }
    public void endBackupThread() {
        try {
            if (backupThread != null) {
                backupThread.interrupt();
                if (backupThread != null) {
                    backupThread.join(2000);
                    if (backupThread != null) {
                        if (backupThread.isAlive()) {
                            Printer.printError(name, "Thread \"" + backupThread.getName() + "\" is not responding with state \"" + backupThread.getState() + "\" !", null);
                        }
                        backupThread = null;
                    }
                }
            }
        } catch (InterruptedException e) {
            backupThread = null;
        }
    }
    public void endCurrentThread() {
        try {
            if (currentThread != null) {
                currentThread.interrupt();
                if (currentThread != null) {
                    currentThread.join(2000);
                    if (currentThread != null) {
                        if (currentThread.isAlive()) {
                            Printer.printError(name, "Thread \"" + currentThread.getName() + "\" is not responding with state \"" + currentThread.getState() + "\" !", null);
                        }
                        currentThread = null;
                    }
                }
            }
        } catch (InterruptedException e) {
            currentThread = null;
        }
    }

    public synchronized void setState(ServerState state) {
        if (currentThread != null) {
            if (       (state != STARTING && currentThread.getName().equals("start-" + name))
                    || (state != STOPPING && currentThread.getName().equals("stop-"  + name))
                    || (state == OFFLINE  && currentThread.getName().equals("kill-"  + name))) {
                endCurrentThread();
            }
        }
        if (this.state != state) {
            boolean wasStopping = this.state == STOPPING || this.state == TERMINATING;
            Printer.printBackgroundInfo(name, "Server is \"" + state.toString() + "\".");
            this.state = state;
            if (this.state == NOTRESPONDING && settings.isRestartIfNotResponding()) {
                restart();
            } else if (this.state == OFFLINE && ((!wasStopping && settings.isStartIfOffline()) || restarting)) {
                start();
                if (restarting) {
                    restarting = false;
                }
            }
        }
    }

    public void printInfo(int lvl) {
        String defColor = "$_Y";
        Printer.printSubTitle("Server \"" + name + "\"");
        String stateColor = Printer.getCCC(state);
        String pingstr;
        if (linked) {
            if (lastPing == null) {
                pingstr = "Last Ping: $BB" + null;
            } else {
                pingstr = "Last Ping: $$W" + Timing.getDurationBreakdown(System.currentTimeMillis() - lastPing);
            }
        } else {
            pingstr = "Last Ping: $BBNot linked";
        }
        String backupstr;
        if(backupThread == null)
        {
            if (lastBackup == null) {
                backupstr = "$BBNever";
            } else {
                backupstr = "$$W\"" + lastBackupType + "\" " + Timing.getDurationBreakdown(System.currentTimeMillis() - lastBackup) + " ago";
            }
        } else {
            backupstr = "$BGOn going " + Main.getProcessHandler().translateBackupOutputToProgress(backupOutput);
        }
        Printer.printCustom(String.format("%-23s  %-23s  %-23s",
                defColor + "State: " + stateColor + state.toString(),
                defColor + pingstr,
                defColor + "Last Backup: " + backupstr));
        if (lvl >= 1 && customMessages != null && customMessages.size() > 0 && state == ONLINE) {
            synchronized (customMessages) {
                for (String msg : customMessages) {
                    Printer.printCustom(msg);
                }
            }
        }
        if (lvl >= 3) {
            Printer.printCustom(String.format("%-42s  %-23s",
                    defColor + "Directory: $$W" + file.getParent(),
                    defColor + "Server jar: $$W" + file.getName()));
            Printer.printCustom(String.format("%-42s  %-48s",
                    defColor + "Start if offline: " + Printer.getCCC(settings.isStartIfOffline()) + settings.isStartIfOffline(),
                    defColor + "Restart if not responding: " + Printer.getCCC(settings.isRestartIfNotResponding()) + settings.isRestartIfNotResponding()));
            Printer.printCustom(String.format("%-23s  %-23s",
                    defColor + "Max RAM: " + Printer.getCCC(settings.getMaxRam()) + settings.getMaxRam(),
                    defColor + "Start RAM: " + Printer.getCCC(settings.getStartRam()) + settings.getStartRam()));
        }
        if (lvl >= 2) {
            if (!timedCommands.isEmpty()) {
                Printer.printCustom("$$W(" + timedCommands.size() + ") Timed Commands:");
                List<TimedCommand> sortedTimedCommands = (List<TimedCommand>) timedCommands.clone();
                Collections.sort(sortedTimedCommands, new Comparator<TimedCommand>() {
                    @Override
                    public int compare(TimedCommand tc1, TimedCommand tc2) {
                        long l1 = tc1.getTime().getNextExecutionMillis(true);
                        long l2 = tc2.getTime().getNextExecutionMillis(true);
                        if (l1 > l2) {
                            return 1;
                        } else if (l1 < l2) {
                            return -1;
                        }
                        return 0;
                    }
                });
                for (TimedCommand tc : sortedTimedCommands) {
                    String cmd = tc.getCommand();
                    if (cmd.length() > 43) {
                        cmd = cmd.substring(0, 43) + "...";
                    }
                    Printer.printCustom(String.format("%-42s  %-23s  %-23s  %-23s",
                            defColor + "Command: $$W" + cmd,
                            defColor + "Type: $$W" + tc.getTime().getType(),
                            defColor + "Schedule: $$W" + tc.getTime().toString(),
                            defColor + "Next: $$W" + Timing.getDurationBreakdown(tc.getTime().getNextExecutionMillis(true))));
                }
            } else {
                Printer.printCustom("$$WThis server has no timed commands");
            }
        }
        Printer.newLine();
    }

    public ArrayList<TimedCommand> getTimedCommands() {
        synchronized (timedCommands) {
            return timedCommands;
        }
    }

    public int timedCommandsSize() {
        synchronized (timedCommands) {
            return timedCommands.size();
        }
    }

    public void listTimedCommands(String pName) {
        String defColor = "$_Y";
        synchronized (timedCommands) {
            for (int i = 0; i < timedCommands.size(); i++) {
                TimedCommand tc = timedCommands.get(i);
                Printer.printCustom(String.format(i + ") %-48s  %-23s  %-23s  %-23s",
                        defColor + "Command: $$W" + tc.getCommand(),
                        defColor + "Type: $$W" + tc.getTime().getType(),
                        defColor + "Schedule: $$W" + tc.getTime().toString(),
                        defColor + "Next: $$W" + Timing.getDurationBreakdown(tc.getTime().getNextExecutionMillis(true))));
            }
        }
    }

    public void addTimedCommand(TimedCommand tc) {
        synchronized (timedCommands) {
            this.timedCommands.add(tc);
        }

        ServerCommandSchedulerTask.processTimedCommand(this, tc);
    }

    public void removeTimedCommand(int i) {
        synchronized (timedCommands) {
            this.timedCommands.remove(i);
        }

    }

    public void removeTimedCommand(TimedCommand i) {
        synchronized (timedCommands) {
            this.timedCommands.remove(i);
        }
    }


    public ServerState getState() {
        return state;
    }

    public Long getFileUpdateInterval() {
        return fileUpdateInterval;
    }

    public void setFileUpdateInterval(Long fileUpdateInterval) {
        this.fileUpdateInterval = fileUpdateInterval;
    }

    public ServerSettings getSettings() {
        return settings;
    }

    public void setSettings(ServerSettings settings) {
        this.settings = settings;
    }

    public String getName() {
        return name;
    }

    public File getFile() {
        return file;
    }

    public Long getLastPing() {
        return lastPing;
    }

    public void setLastPing(Long lastPing) {
        this.lastPing = lastPing;
        ignorePing = false;
    }

    public void setCustomMessages(ArrayList<String> msgs) {
        if (customMessages != null) {
            synchronized (customMessages) {
                customMessages = msgs;
                return;
            }
        }
        customMessages = msgs;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean isLinked() {
        return linked;
    }

    public boolean link() {
        if (!linked) {
            linked = true;
            linked = Main.getServerFileCommunicatorTask().updateServerMonitorData(this);
            if (linked) {
                Printer.printDataChange(name, "linked successfully to a monitor file.");
            } else {
                Printer.printBackgroundFail(name, "Failed to link to a monitor file.");
            }
        } else {
            Printer.printBackgroundFail(name, "The server is already linked.");
        }

        return linked;
    }

    public void unlink() {
        if (linked) {
            linked = false;
            lastPing = null;
            Printer.printDataChange(name, "unlinked successfully.");
        } else {
            Printer.printBackgroundFail(name, "The server is already not linked.");
        }
    }

    public void setLinked(boolean linked) {
        this.linked = linked;
    }
    public Long getLastBackup() {
        return lastBackup;
    }

    public String getLastBackupType() {
        return lastBackupType;
    }
    public void setLastBackup(Long lastBackup) {
        this.lastBackup = lastBackup;
    }

    public void setLastBackupType(String lastBackupType) {
        this.lastBackupType = lastBackupType;
    }

    public Thread getBackupThread() {
        return backupThread;
    }

    public Thread getBackupProgressReader() {
        return backupProgressReader;
    }
}
