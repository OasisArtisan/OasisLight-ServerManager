package com.oasisartisan.servermanager.objects;

import com.oasisartisan.servermanager.consolecommunication.Printer;
import com.oasisartisan.servermanager.storage.Storage;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author Omar Alama
 */
public class GlobalServer extends Server {

    public GlobalServer() {
        super("global", "");
    }

    @Override
    public boolean start() {
        if (Storage.getServerList().isEmpty()) {
            Printer.printBackgroundFail(this.getName(), "Failed to start all servers. No servers exist.");
            return false;
        }
        synchronized (Storage.getServerList()) {
            for (Server s : Storage.getServerList().values()) {
                s.start();
            }
        }
        return true;
    }

    @Override
    public boolean stop() {
        if (Storage.getServerList().isEmpty()) {
            Printer.printBackgroundFail(this.getName(), "Failed to stop all servers. No servers exist.");
            return false;
        }
        synchronized (Storage.getServerList()) {
            for (Server s : Storage.getServerList().values()) {
                s.stop();
            }
        }
        return true;
    }

    @Override
    public boolean restart() {
        if (Storage.getServerList().isEmpty()) {
            Printer.printBackgroundFail(this.getName(), "Failed to restart all servers. No servers exist.");
            return false;
        }
        synchronized (Storage.getServerList()) {
            for (Server s : Storage.getServerList().values()) {
                s.restart();
            }
        }
        return true;
    }

    @Override
    public boolean kill() {
        if (Storage.getServerList().isEmpty()) {
            Printer.printBackgroundFail(this.getName(), "Failed to kill all servers. No servers exist.");
            return false;
        }
        synchronized (Storage.getServerList()) {
            for (Server s : Storage.getServerList().values()) {
                s.kill();
            }
        }
        return true;
    }

    @Override
    public boolean backup(BackupProfile bp) {
        if (Storage.getServerList().isEmpty()) {
            Printer.printBackgroundFail(this.getName(), "Failed to backup all servers using \"" + bp.getName() + "\". No servers exist.");
            return false;
        }
        synchronized (Storage.getServerList()) {
            for (Server s : Storage.getServerList().values()) {
                s.backup(bp);
            }
        }
        return true;
    }

    @Override
    public boolean sendCommand(String cmd) {
        if (Storage.getServerList().isEmpty()) {
            Printer.printBackgroundFail(this.getName(), "Failed to send command to all servers. No servers exist.");
            return false;
        }
        synchronized (Storage.getServerList()) {
            for (Server s : Storage.getServerList().values()) {
                s.sendCommand(cmd);
            }
        }
        return true;
    }

    @Override
    public void printInfo(int lvl) {
        if (lvl >= 2) {
            printTimedCommands();
        }
        synchronized (Storage.getServerList()) {
            for (Server s : Storage.getServerList().values()) {
                s.printInfo(lvl);
            }
        }
    }

    public void printTimedCommands() {
        String defColor = "$_Y";
        Printer.printSubTitle("\"" + this.getName() + "\"");
        if (!this.getTimedCommands().isEmpty()) {
            Printer.printCustom("$$W(" + this.getTimedCommands().size() + ") Timed Commands:");
            List<TimedCommand> sortedTimedCommands = (List<TimedCommand>) this.getTimedCommands().clone();
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
                if (cmd.length() > 39) {
                    cmd = cmd.substring(0, 39) + "...";
                }
                Printer.printCustom(String.format("%-42s  %-23s  %-23s  %-23s",
                        defColor + "Command: $$W" + cmd,
                        defColor + "Type: $$W" + tc.getTime().getType(),
                        defColor + "Schedule: $$W" + tc.getTime().toString(),
                        defColor + "Next: $$W" + Timing.getDurationBreakdown(tc.getTime().getNextExecutionMillis(true))));
            }
        } else {
            Printer.printCustom("$$WThere are no global timed commands");
        }
        Printer.newLine();
    }

    @Override
    public boolean link() {
        if (Storage.getServerList().isEmpty()) {
            Printer.printBackgroundFail(this.getName(), "Failed to link all servers. No servers exist.");
            return false;
        }
        synchronized (Storage.getServerList()) {
            for (Server s : Storage.getServerList().values()) {
                s.link();
            }
        }
        return true;
    }

    @Override
    public void unlink() {
        if (Storage.getServerList().isEmpty()) {
            Printer.printBackgroundFail(this.getName(), "Failed to unlink all servers. No servers exist.");
            return;
        }
        synchronized (Storage.getServerList()) {
            for (Server s : Storage.getServerList().values()) {
                s.unlink();
            }
        }
    }

    @Override
    public ServerSettings getSettings() {
        return new GlobalServerSettings();
    }
}
