package com.rockpartymc.servermanager.objects;

import com.rockpartymc.servermanager.Main;
import com.rockpartymc.servermanager.consolecommunication.Printer;
import com.rockpartymc.servermanager.tasks.ServerCommandSchedulerTask;
import com.rockpartymc.servermanager.Utilities;
import com.rockpartymc.servermanager.processhandlers.ProcessHandler;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import static com.rockpartymc.servermanager.objects.ServerState.*;
import java.util.concurrent.TimeUnit;

public class Server implements Serializable {

    private String name;
    private File file;
    private File monitorFile;
    private Long lastPing;
    private Long fileUpdateInterval;
    private ArrayList<TimedCommand> timedCommands;
    private ServerSettings settings;
    private transient ArrayList<String> customMessages;
    private transient ServerState state;
    private transient Thread currentThread;
    private transient boolean restarting;
    public transient boolean ignorePing = false;
    public Server(String name, String path) {
        this.name = name;
        this.file = new File(path);
        timedCommands = new ArrayList();
        initializeState();
        settings = new ServerSettings();
    }

    public synchronized boolean start() {
        Printer.printBackgroundInfo(name, "Atempting to start the server.");
        if (currentThread != null) {
            Printer.printBackgroundFail(name, "Could not start server. Another process is currently ongoing.");
            return false;
        }
        if (state != OFFLINE) {
            Printer.printBackgroundFail(name, "Could not start server. It is not offline.");
            return false;
        }
        if(!file.isFile())
        {
            Printer.printBackgroundFail(name, "Could not start server. Jar file is missing.");
        }
        Server server = this;
        currentThread = new Thread() {
            public void run() {
                try {
                    Process p = Main.getProcessHandler().startServerProcess(server);
                    ProcessHandler.finishProcess(p, name + " >> START");
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
        Printer.printBackgroundInfo(name, "Atempting to restart the server.");
        if (state == OFFLINE) {
            return start();
        }
        boolean result = stop();
        restarting = result;
        return result;
    }

    public synchronized boolean stop() {
        Printer.printBackgroundInfo(name, "Atempting to stop the server.");
        if (currentThread != null) {
            Printer.printBackgroundFail(name, "Could not stop server. Another process is currently ongoing.");
            return false;
        }
        if (state == NOTRESPONDING) {
            return kill();
        }
        if (state != ONLINE) {
            Printer.printBackgroundFail(name, "Could not stop server. It should be online or not responding to stop.");
            return false;
        }
        Server server = this;
        currentThread = new Thread() {
            public void run() {
                try {
                    Process p = Main.getProcessHandler().sendCommandToServer(server, server.getSettings().getStopCommand());
                    ProcessHandler.finishProcess(p, name + " >> STOP");
                    try {
                        Thread.sleep(server.getSettings().getMaxStoppingDuration());
                        if (server.getState() == STOPPING) {
                            Printer.printBackgroundFail(server.getName(), "Server failed to stop in time. Killing the server session!");
                            server.kill();
                        }
                    } catch (InterruptedException e) {
                    }
                } catch (IOException e) {
                    Printer.printError(server.getName(), "Failed to send stop command to the server.", e);
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
        Printer.printBackgroundInfo(name, "Atempting to kill the server.");
        if (state == OFFLINE) {
            Printer.printBackgroundFail(name, "Could not kill server. It is already offline.");
            return false;
        }
        endCurrentThread();
        Server server = this;
        currentThread = new Thread() {
            public void run() {
                try {
                    ProcessHandler ph = Main.getProcessHandler();
                    Process p = ph.killServerProcess(server, true);
                    Utilities.printStream(p.getErrorStream());
                    ProcessHandler.finishProcess(p, name + " >> KILL(15)");
                    Thread.sleep(5000);
                    if (ph.hasActiveProcess(server)) {
                        p = ph.killServerProcess(server, false);
                        Utilities.printStream(p.getErrorStream());
                        ProcessHandler.finishProcess(p, name + " >> KILL(9)");
                    }
                    Thread.sleep(3000);
                    if (ph.hasActiveProcess(server)) {
                        Printer.printError(server.getName(), "Failed to kill the server process", null);
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

    public boolean sendCommand(String command) {
        if (state == ONLINE || state == NOTRESPONDING) {
            try {
                Process p = Main.getProcessHandler().sendCommandToServer(this, command);
                if (ProcessHandler.finishProcess(p, name + " >> SEND")) {
                    Printer.printBackgroundSuccess(name, "Successfuly sent command \"" + command + "\" to the server.");
                }
                return false;
            } catch (IOException e) {
                Printer.printError(name, "Failed to send command \"" + command + "\" to the server.", e);
                return false;
            }  catch (Exception e) {
                    Printer.printError("send-command-" + name, "An unexpected error occured.", e);
            }
        }
        Printer.printBackgroundFail(name, "Failed to send command \"" + command + "\" to the server. Server must be online or notresponding.");
        return false;
    }
    public boolean backup(BackupProfile bp)
    {
        String pName =name +  "-" + bp.getName() + "-backup";
        Server s = this;
        Thread thread = new Thread()
        {
            public void run()
            {
                Printer.printBackgroundInfo(pName, "Starting backup process.");
                try {
                    Process p = Main.getProcessHandler().executeBackup(s,bp);
                    try {
                        int secs = 10;
                        while(true)
                        {
                            p.waitFor(secs, TimeUnit.SECONDS);
                            if(!p.isAlive())
                            {
                                break;
                            }
                            Printer.printBackgroundInfo(pName, "backing up in progress...");
                            secs += 5;
                        }
                        Printer.printBackgroundSuccess(pName, "has completed the backup successfully.");
                    } catch (InterruptedException e)
                    {
                        Printer.printError(pName,"Possibly failed to execute backup", e);
                    }
                } catch (IOException ex) {
                    Printer.printError(pName, "Failed to execute backup", ex);
                } catch (Exception e) {
                    Printer.printError("backup-" + name, "An unexpected error occured.", e);
                }
            }
        };
        thread.setName(pName);
        thread.start();
        return true;
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
                    || (state != STOPPING && currentThread.getName().equals("stop-" + name))
                    || (state == OFFLINE && currentThread.getName().equals("kill-" + name))  ) {
                endCurrentThread();
            }
        }
        if (this.state != state) {
            Printer.printBackgroundInfo(name, "Server is now \"" + state.toString() + "\".");
            this.state = state;
        }
        if (this.state == NOTRESPONDING && settings.isRestartIfNotResponding()) {
            restart();
        } else if (this.state == OFFLINE && (settings.isStartIfOffline() || restarting)) {
            start();
            if (restarting) {
                restarting = false;
            }
        }
    }

    public void printInfo(int lvl) {
        String defColor = "$_Y";
        Printer.printSubTitle("Server \"" + name + "\"");
        String stateColor = Printer.getCCC(state);
        if (lastPing == null) {
            Printer.printCustom(String.format("%-23s  %-23s",
                    defColor + "State: " + stateColor + state.toString(),
                    defColor + "Last Ping: $BB" + null));
        } else {
            Printer.printCustom(String.format("%-23s  %-23s",
                    defColor + "State: " + stateColor + state.toString(),
                    defColor + "Last Ping: $$W" + Timing.getDurationBreakdown(System.currentTimeMillis() - lastPing)));
        }
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
        if(lvl >= 2) {
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
                if(cmd.length() > 39)
                {
                    cmd = cmd.substring(0, 39) + "...";
                }
                Printer.printCustom(String.format("%-42s  %-23s  %-23s  %-23s",
                        defColor + "Command: $$W" + cmd,
                        defColor + "Type: $$W" + tc.getTime().getType(),
                        defColor + "Schedule: $$W" + tc.getTime().toString(),
                        defColor + "Next: $$W" + Timing.getDurationBreakdown(tc.getTime().getNextExecutionMillis(true))));
            }
        } else {
            Printer.printCustom("$$WThis server has no timed commands");
        }}
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

    public void initializeState() {
        if (state == null) {
            state = OFFLINE;
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
}
