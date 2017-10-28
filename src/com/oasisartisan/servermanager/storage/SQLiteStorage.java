package com.oasisartisan.servermanager.storage;

import com.oasisartisan.servermanager.Utilities;
import com.oasisartisan.servermanager.consolecommunication.Printer;
import com.oasisartisan.servermanager.objects.BackupProfile;
import com.oasisartisan.servermanager.objects.GlobalServer;
import com.oasisartisan.servermanager.objects.Server;
import com.oasisartisan.servermanager.objects.ServerSettings;
import com.oasisartisan.servermanager.objects.TimedCommand;
import com.oasisartisan.servermanager.objects.Timing;
import java.sql.*;
import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author OmarAlama
 */
public class SQLiteStorage extends Storage {

    public static final String pName = "SQLiteStorage";
    private static String schema = Utilities.readLocalFile("SQLiteDatabase.sql");
    public static final String exe = "db";
    private static final String[] settingsColumns = {
        "ID",
        "Communication_dir",
        "Server_State_Updater_Task_Interval",
        "Monitor_Refresh_Rate",
        "Monitor_Messages_Duration",
        "Command_Scheduler_Task_Interval",
        "Use_Console_Colors",
        "Clear_Console_Before_Menu",
        "Log_Output",
        "Print_Background_Info_To_Console",
        "Background_Info_Time_Stamps_In_Console",
        "Storage_Type"};
    private static final String[] serversSettingsColumns = {
        "Server_Name",
        "Start_RAM",
        "Max_RAM",
        "Start_if_offline",
        "Restart_if_not_responding",
        "Max_starting_duration",
        "Max_stopping_duration",
        "Stop_command"};
    private static final String[] serversColumns = {
        "Server_Name",
        "Server_File",
        "Linked",
        "Last_Ping",
        "File_Update_Interval",
        "LastBackup",
        "LastBackupType"};
    private static final String[] serversTimedCommandsColumns = {
        "Server_Name",
        "Timing",
        "Command"};
    private static final String[] backupProfilesColumns = {
        "Name",
        "Directory",
        "Exclude_List",
        "Include_List"
    };
    private Connection c;
    int tries = 0;

    @Override
    public boolean saveData() {
        try {
            tries++;
            if (c == null) {
                c = DriverManager.getConnection("jdbc:sqlite:" + getFile().getName());
            }
            int schema_version = c.createStatement().executeQuery("PRAGMA user_version").getInt(1);
            if (schema_version != 1) {
                if (schema_version != 0) {
                    c.close();
                    getFile().delete();
                    c = DriverManager.getConnection("jdbc:sqlite:" + getFile().getName());
                }
                c.createStatement().executeUpdate(Utilities.readLocalFile("SQLiteDatabase.sql"));
            }
            String settingsValues = "VALUES (0"
                    + ",'" + settings.getCommunicationDir().getAbsolutePath() + "'"
                    + "," + settings.getServerStateUpdaterTaskInterval()
                    + "," + settings.getMonitorRefreshRate()
                    + "," + settings.getMonitorMessagesDuration()
                    + "," + settings.getCommandSchedulerTaskInterval()
                    + "," + (settings.isUseConsoleColors() ? 1 : 0)
                    + "," + (settings.isClearConsoleBeforeMenu() ? 1 : 0)
                    + "," + (settings.isLogOutput() ? 1 : 0)
                    + "," + (settings.isPrintBackgroundInfoToConsole() ? 1 : 0)
                    + "," + (settings.isBackgroundInfoTimeStampsInConsole() ? 1 : 0)
                    + ",'" + settings.getStorageType() + "')";

            c.createStatement().executeUpdate("INSERT OR REPLACE INTO Program_Settings (" + getStringFromArray(settingsColumns, ',') + ") " + settingsValues + ";");
            //Store Server info
            c.createStatement().executeUpdate("DELETE FROM Servers;DELETE FROM Servers_Settings;DELETE FROM Servers_Timed_Commands");
            synchronized (serverList) {
                for (Server s : serverList.values()) {
                    String serverValues = "VALUES('" + s.getName() + "'"
                            + ",'" + s.getFile().getAbsolutePath() + "'"
                            + "," + (s.isLinked() ? 1 : 0)
                            + "," + s.getLastPing()
                            + "," + s.getFileUpdateInterval() 
                            + "," + s.getLastBackup()
                            + "," + (s.getLastBackupType() == null ? "NULL" : "'" + s.getLastBackupType() + "'") + ")";
                    c.createStatement().executeUpdate("INSERT INTO Servers (" + getStringFromArray(serversColumns, ',') + ") " + serverValues + ";");
                    //Store server's settings
                    ServerSettings st = s.getSettings();
                    String serverSettingsValues = "VALUES ('" + s.getName() + "'"
                            + "," + (st.getStartRam() == null ? "NULL" : "'" + st.getStartRam() + "'")
                            + "," + (st.getMaxRam() == null ? "NULL" : "'" + st.getMaxRam() + "'")
                            + "," + (st.isStartIfOffline() ? 1 : 0)
                            + "," + (st.isRestartIfNotResponding() ? 1 : 0)
                            + "," + st.getMaxStartingDuration()
                            + "," + st.getMaxStoppingDuration()
                            + ",'" + st.getStopCommand() + "')";
                    c.createStatement().executeUpdate("INSERT INTO Servers_Settings (" + getStringFromArray(serversSettingsColumns, ',') + ") " + serverSettingsValues + ";");
                    //Store server's timed commands
                    List<TimedCommand> tcs = s.getTimedCommands();
                    synchronized (tcs) {
                        for (TimedCommand tc : tcs) {
                            String tcValues = "VALUES('" + s.getName() + "','" + tc.getTime().toString() + "','" + tc.getCommand() + "')";
                            c.createStatement().executeUpdate("INSERT INTO Servers_Timed_Commands (" + getStringFromArray(serversTimedCommandsColumns, ',') + ") " + tcValues + ";");
                        }
                    }
                }
                //Store global server's timed commands
                for (TimedCommand tc : globalServer.getTimedCommands()) {
                    String tcValues = "VALUES('" + globalServer.getName() + "','" + tc.getTime().toString() + "','" + tc.getCommand() + "')";
                    c.createStatement().executeUpdate("INSERT INTO Servers_Timed_Commands (" + getStringFromArray(serversTimedCommandsColumns, ',') + ") " + tcValues + ";");
                }
            }
            //Store BackupProfiles
            c.createStatement().execute("DELETE FROM Backup_Profiles;");
            synchronized (backupProfilesList) {
                for (BackupProfile bp : backupProfilesList.values()) {
                    String bpValues = "VALUES ('" + bp.getName() + "'"
                            + "," + (bp.getDir() == null ? "NULL" : ("'" + bp.getDir().getPath() + "'"))
                            + ",'" + Utilities.listArgs(bp.getExcludeList(), "$#@") + "'"
                            + ",'" + Utilities.listArgs(bp.getIncludeList(), "$#@") + "')";
                    c.createStatement().executeUpdate("INSERT INTO Backup_Profiles (" + getStringFromArray(backupProfilesColumns, ',') + ") " + bpValues);
                }
            }
            tries = 0;
            Printer.printBackgroundSuccess(pName, "Changes have been saved successfuly to storage.");
            return true;
        } catch (SQLException e) {
            if (tries >= 2) {
                Printer.printError(pName, "Failed to save data to file", e);
                return false;
            }
            Printer.printError(pName, "Failed to save data to file.. retrying with a new file.", e);
            try {
                c.close();
            } catch (SQLException ex) {
                Printer.printError(pName, "Failed to close the connection before renaming corrupted data file.", e);
            }
            c = null;
            for (int i = 1; i <= 100; i++) {
                File f = new File(getFile().getName() + "_corrupt_" + i);
                if (!f.exists() && getFile().renameTo(f)) {
                    break;
                }
            }
            return saveData();
        }
    }

    @Override
    public boolean loadData() {
        try {
            Printer.printBackgroundInfo(pName, "Loading data...");
            if (c == null) {
                c = DriverManager.getConnection("jdbc:sqlite:" + getFile().getName());
            }
            int schema_version = c.createStatement().executeQuery("PRAGMA user_version").getInt(1);
            switch (schema_version) {
                case 1:
                    //Load program settings
                    ResultSet rs = c.createStatement().executeQuery("SELECT " + "*" + " FROM Program_Settings");
                    settings = new Settings();
                    settings.setCommunicationDir(new File(rs.getString(settingsColumns[1])));
                    settings.setServerStateUpdaterTaskInterval(rs.getInt(settingsColumns[2]));
                    settings.setMonitorRefreshRate(rs.getInt(settingsColumns[3]));
                    settings.setMonitorMessagesDuration(rs.getInt(settingsColumns[4]));
                    settings.setCommandSchedulerTaskInterval(rs.getInt(settingsColumns[5]));
                    settings.setUseConsoleColors(rs.getInt(settingsColumns[6]) == 1);
                    settings.setClearConsoleBeforeMenu(rs.getInt(settingsColumns[7]) == 1);
                    settings.setLogOutput(rs.getInt(settingsColumns[8]) == 1);
                    settings.setPrintBackgroundInfoToConsole(rs.getInt(settingsColumns[9]) == 1);
                    settings.setBackgroundInfoTimeStampsInConsole(rs.getInt(settingsColumns[10]) == 1);
                    settings.setStorageType(rs.getString(settingsColumns[11]));
                    serverList = new HashMap();
                    rs = c.createStatement().executeQuery("SELECT " + "*" + " FROM Servers");
                    if (!rs.isClosed()) {

                        //Load Servers
                        while (rs.next()) {
                            Server s = new Server(rs.getString(serversColumns[0]), rs.getString(serversColumns[1]));
                            s.setLinked(rs.getInt(serversColumns[2]) == 1);
                            long l = (long) rs.getInt(serversColumns[3]);
                            s.setLastPing(l == 0 ? null : l);
                            s.setFileUpdateInterval((long) rs.getInt(serversColumns[4]));
                            l = (long) rs.getLong(serversColumns[5]);
                            s.setLastBackup(l == 0 ? null : l);
                            s.setLastBackupType(rs.getString(serversColumns[6]));

                            serverList.put(s.getName(), s);
                        }

                    }
                    //Load Server settings
                    rs = c.createStatement().executeQuery("SELECT " + "*" + " FROM Servers_Settings");
                    if (!rs.isClosed()) {
                        while (rs.next()) {
                            String name = rs.getString(serversSettingsColumns[0]);
                            Server s = serverList.get(name);
                            s.getSettings().setStartRam(rs.getString(serversSettingsColumns[1]));
                            s.getSettings().setMaxRam(rs.getString(serversSettingsColumns[2]));
                            s.getSettings().setStartIfOffline(rs.getInt(serversSettingsColumns[3]) == 1);
                            s.getSettings().setRestartIfNotResponding(rs.getInt(serversSettingsColumns[4]) == 1);
                            s.getSettings().setMaxStartingDuration((long) rs.getInt(serversSettingsColumns[5]));
                            s.getSettings().setMaxStoppingDuration((long) rs.getInt(serversSettingsColumns[6]));
                            s.getSettings().setStopCommand(rs.getString(serversSettingsColumns[7]));
                        }
                    }
                    //Load timed commands
                    globalServer = new GlobalServer();
                    rs = c.createStatement().executeQuery("SELECT " + "*" + " FROM Servers_Timed_Commands");
                    if (!rs.isClosed()) {
                        while (rs.next()) {
                            String name = rs.getString(serversTimedCommandsColumns[0]);
                            List<TimedCommand> tcls;
                            if (name.equals(globalServer.getName())) {
                                tcls = globalServer.getTimedCommands();
                            } else {
                                tcls = serverList.get(name).getTimedCommands();
                            }
                            TimedCommand tc = new TimedCommand(rs.getString(serversTimedCommandsColumns[2]), new Timing(rs.getString(serversTimedCommandsColumns[1])));
                            synchronized (tcls) {
                                tcls.add(tc);
                            }
                        }
                    }
                    //Load backup profiles
                    backupProfilesList = new HashMap();
                    rs = c.createStatement().executeQuery("SELECT " + "*" + " FROM Backup_Profiles");
                    if (!rs.isClosed()) {
                        while (rs.next()) {
                            String name = rs.getString(backupProfilesColumns[0]);
                            File dir = null;
                            String dirPath = rs.getString(backupProfilesColumns[1]);
                            if (dirPath != null) {
                                dir = new File(dirPath);
                            }
                            List<String> exList = Utilities.seperateArgs(rs.getString(backupProfilesColumns[2]), "$#@");
                            List<String> inList = Utilities.seperateArgs(rs.getString(backupProfilesColumns[3]), "$#@");
                            BackupProfile bp = new BackupProfile(name, dir);
                            for (String s : exList) {
                                bp.getExcludeList().add(s);
                            }
                            for (String s : inList) {
                                bp.getIncludeList().add(s);
                            }
                            backupProfilesList.put(name, bp);
                        }
                    }
                    Printer.printBackgroundSuccess(pName, "Data has been loaded successfuly from storage.");
                    return true;
                default:
                    Printer.printBackgroundInfo(pName, "No data found");
                    return false;
            }
        } catch (SQLException e) {
            Printer.printError(pName, "Failed to load data from file", e);
            try {
                c.close();
            } catch (SQLException ex) {
                Printer.printError(pName, "Failed to close the connection before renaming corrupted data file.", e);
            }
            c = null;
            for (int i = 1; i <= 100; i++) {
                File f = new File(getFile().getName() + "_corrupt_" + i);
                if (!f.exists() && getFile().renameTo(f)) {
                    break;
                }
            }
            return false;
        }
    }

    private String getStringFromArray(String[] strs, char seperator) {
        String res = "";
        for (int i = 0; i < strs.length; i++) {
            res += strs[i];
            if (i < strs.length - 1) {
                res += seperator;
            }
        }
        return res;
    }

    @Override
    public String getType() {
        return "sqlite";
    }

    @Override
    public File getFile() {
        return new File(path + "." + exe);
    }
}
