package com.oasisartisan.servermanager.storage;

import com.oasisartisan.servermanager.objects.BackupProfile;
import com.oasisartisan.servermanager.objects.GlobalServer;
import com.oasisartisan.servermanager.objects.Server;
import com.oasisartisan.servermanager.objects.ServerSettings;
import com.oasisartisan.servermanager.objects.TimedCommand;
import com.oasisartisan.servermanager.objects.Timing;
import static com.oasisartisan.servermanager.storage.Storage.path;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 *
 * @author OasisArtian
 */
public class JSONStorage extends Storage
{

    public static final String pName = "JSONStorage";
    public static final String exe = "json";

    @Override
    public boolean loadData() {
        //Read data
        StringBuilder sb = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(path + ".json");){
            int i = -1;
            while (true){
                i = fis.read();
                if (i == -1) {
                    break;
                }
                sb.append((char)i);
            }
        } catch (IOException ex) {
            return false;
        }
        JSONObject data = new JSONObject(sb.toString());
        //Deserialize settings
        JSONObject jsonSettings = data.getJSONObject("settings");
        settings = new Settings();
        settings.setCommunicationDir(new File(jsonSettings.getString("communicationDir")));
        settings.setServerStateUpdaterTaskInterval(jsonSettings.getLong("serverStateUpdaterTaskInterval"));
        settings.setMonitorRefreshRate(jsonSettings.getLong("monitorRefreshRate"));
        settings.setMonitorMessagesDuration(jsonSettings.getLong("monitorMessagesDuration"));
        settings.setCommandSchedulerTaskInterval(jsonSettings.getLong("commandSchedulerTaskInterval"));
        settings.setUseConsoleColors(jsonSettings.getBoolean("useConsoleColors"));
        settings.setClearConsoleBeforeMenu(jsonSettings.getBoolean("clearConsoleBeforeMenu"));
        settings.setLogOutput(jsonSettings.getBoolean("logOutput"));
        settings.setPrintBackgroundInfoToConsole(jsonSettings.getBoolean("printBackgroundInfoToConsole"));
        settings.setBackgroundInfoTimeStampsInConsole(jsonSettings.getBoolean("backgroundInfoTimeStampsInConsole"));
        settings.setStorageType(jsonSettings.getString("storageType"));
        //Deserialize serverList
        JSONObject jsonServerList = data.getJSONObject("serverList");
        serverList = new HashMap();
        for(String serverName: jsonServerList.keySet()){
            JSONObject jsonServer = jsonServerList.getJSONObject(serverName);
            Server s = new Server(serverName, jsonServer.getString("file"));
            s.setLastPing(jsonServer.get("lastPing") == JSONObject.NULL ? null : jsonServer.getLong("lastPing"));
            //s.setFileUpdateInterval(jsonServer.get("fileUpdateInterval") == JSONObject.NULL ? null : jsonServer.getLong("fileUpdateInterval"));
            s.setLinked(jsonServer.getBoolean("linked"));
            s.setLastBackup(jsonServer.get("lastBackup") == JSONObject.NULL ? null : jsonServer.getLong("lastBackup"));
            s.setLastBackupType(jsonServer.get("lastBackupType") == JSONObject.NULL ? null : jsonServer.getString("lastBackupType"));
            JSONArray jsonTimedCommands = jsonServer.getJSONArray("timedCommands");
            for (Object jtc: jsonTimedCommands){
                JSONObject jsonTimedCommand = (JSONObject) jtc;
                TimedCommand tc = new TimedCommand(jsonTimedCommand.getString("command"), new Timing(jsonTimedCommand.getString("time")));
                s.getTimedCommands().add(tc);
            }
            JSONObject jss = jsonServer.getJSONObject("settings");
            s.getSettings().setStopCommand(jss.getString("stopCommand"));
            s.getSettings().setStartRam(jss.get("startRam") == JSONObject.NULL ? null : jss.getString("startRam"));
            s.getSettings().setMaxRam(jss.get("maxRam") == JSONObject.NULL ? null : jss.getString("maxRam"));
            s.getSettings().setCustomJavaArgs(jss.get("customJavaArgs") == JSONObject.NULL ? null : jss.getString("customJavaArgs"));
            s.getSettings().setJavaPath(jss.getString("javaPath"));
            s.getSettings().setStartIfOffline(jss.getBoolean("startIfOffline"));
            s.getSettings().setRestartIfNotResponding(jss.getBoolean("restartIfNotResponding"));
            s.getSettings().setMaxStartingDuration(jss.getLong("maxStartingDuration"));
            s.getSettings().setMaxStoppingDuration(jss.getLong("maxStoppingDuration"));
            serverList.put(serverName, s);
        }
        //Deserialize global server
        JSONObject jsonGlobalServer = data.getJSONObject("globalServer");
        JSONArray jsonTimedCommands = jsonGlobalServer.getJSONArray("timedCommands");
        globalServer = new GlobalServer();
        for (Object jtc: jsonTimedCommands){
            JSONObject jsonTimedCommand = (JSONObject) jtc;
            TimedCommand tc = new TimedCommand(jsonTimedCommand.getString("command"), new Timing(jsonTimedCommand.getString("time")));
            globalServer.getTimedCommands().add(tc);
        }
        //Deserialize backup profiles
        backupProfilesList = new HashMap();
        JSONObject jsonBackupProfilesList = data.getJSONObject("backupProfilesList");
        for (String backupProfileName: jsonBackupProfilesList.keySet()){
            JSONObject jbp = jsonBackupProfilesList.getJSONObject(backupProfileName);
            BackupProfile bp = new BackupProfile(backupProfileName, jbp.get("dir") == JSONObject.NULL? null : new File(jbp.getString("dir")));
            JSONArray jsonExcludeList = jbp.getJSONArray("excludeList");
            for(Object o : jsonExcludeList){
                bp.getExcludeList().add((String) o);
            }
            JSONArray jsonIncludeList = jbp.getJSONArray("includeList");
            for(Object o : jsonIncludeList){
                bp.getIncludeList().add((String) o);
            }
            backupProfilesList.put(backupProfileName, bp);
        }
        return true;
    }

    @Override
    public boolean saveData() {
        //Serialize Settings
        JSONObject jsonSettings = new JSONObject();
        jsonSettings.put("communicationDir", settings.getCommunicationDir().getAbsolutePath());
        jsonSettings.put("serverStateUpdaterTaskInterval", settings.getServerStateUpdaterTaskInterval());
        jsonSettings.put("monitorRefreshRate", settings.getMonitorRefreshRate());
        jsonSettings.put("monitorMessagesDuration", settings.getMonitorMessagesDuration());
        jsonSettings.put("commandSchedulerTaskInterval", settings.getCommandSchedulerTaskInterval());
        jsonSettings.put("useConsoleColors", settings.isUseConsoleColors());
        jsonSettings.put("clearConsoleBeforeMenu", settings.isClearConsoleBeforeMenu());
        jsonSettings.put("logOutput", settings.isLogOutput());
        jsonSettings.put("printBackgroundInfoToConsole", settings.isPrintBackgroundInfoToConsole());
        jsonSettings.put("backgroundInfoTimeStampsInConsole", settings.isBackgroundInfoTimeStampsInConsole());
        jsonSettings.put("storageType", settings.getStorageType());
        //Serialize serverList
        JSONObject jsonServerList = new JSONObject();
        synchronized (serverList) {
            for (String serverName : serverList.keySet()) {
                JSONObject jsonServer = new JSONObject();
                Server server = serverList.get(serverName);
                jsonServer.put("name", server.getName());
                jsonServer.put("file", server.getFile().getAbsolutePath());
                jsonServer.put("lastPing", JSONObject.wrap(server.getLastPing()));
                //jsonServer.put("fileUpdateInterval", JSONObject.wrap(server.getFileUpdateInterval()));
                jsonServer.put("linked", server.isLinked());
                jsonServer.put("lastBackup", JSONObject.wrap(server.getLastBackup()));
                jsonServer.put("lastBackupType", JSONObject.wrap(server.getLastBackupType()));
                JSONArray jsonTimedCommands = new JSONArray();
                synchronized (server.getTimedCommands()) {
                    for (TimedCommand tc : server.getTimedCommands()) {
                        JSONObject jsonTimedCommand = new JSONObject();
                        jsonTimedCommand.put("time", tc.getTime().toString());
                        jsonTimedCommand.put("command", tc.getCommand());
                        jsonTimedCommands.put(jsonTimedCommand);
                    }
                }
                jsonServer.put("timedCommands", jsonTimedCommands);
                JSONObject jsonServerSettings = new JSONObject();
                ServerSettings serverSettings = server.getSettings();
                jsonServerSettings.put("stopCommand", serverSettings.getStopCommand());
                jsonServerSettings.put("startRam", JSONObject.wrap(serverSettings.getStartRam()));
                jsonServerSettings.put("maxRam", JSONObject.wrap(serverSettings.getMaxRam()));
                jsonServerSettings.put("customJavaArgs", JSONObject.wrap(serverSettings.getCustomJavaArgs()));
                jsonServerSettings.put("javaPath", serverSettings.getJavaPath());
                jsonServerSettings.put("startIfOffline", serverSettings.isStartIfOffline());
                jsonServerSettings.put("restartIfNotResponding", serverSettings.isRestartIfNotResponding());
                jsonServerSettings.put("maxStartingDuration", serverSettings.getMaxStartingDuration());
                jsonServerSettings.put("maxStoppingDuration", serverSettings.getMaxStoppingDuration());
                jsonServer.put("settings", jsonServerSettings);
                jsonServerList.put(serverName, jsonServer);
            }
        }
        //Serialize global server
        JSONObject jsonGlobalServer = new JSONObject();
        JSONArray jsonTimedCommands = new JSONArray();
        synchronized (globalServer.getTimedCommands()) {
            for (TimedCommand tc : globalServer.getTimedCommands()) {
                JSONObject jsonTimedCommand = new JSONObject();
                jsonTimedCommand.put("time", tc.getTime().toString());
                jsonTimedCommand.put("command", tc.getCommand());
                jsonTimedCommands.put(jsonTimedCommand);
            }
        }
        jsonGlobalServer.put("timedCommands", jsonTimedCommands);
        //Serialize backup profiles
        JSONObject jsonBackupProfiles = new JSONObject();
        for (String backupProfileName: backupProfilesList.keySet()){
            JSONObject jsonBackupProfile = new JSONObject();
            BackupProfile bp = backupProfilesList.get(backupProfileName);
            jsonBackupProfile.put("name", bp.getName());
            jsonBackupProfile.put("dir", bp.getDir() == null ? JSONObject.NULL: bp.getDir().getAbsolutePath());
            jsonBackupProfile.put("excludeList", new JSONArray(bp.getExcludeList()));
            jsonBackupProfile.put("includeList", new JSONArray(bp.getIncludeList()));
            jsonBackupProfiles.put(bp.getName(), jsonBackupProfile);
        }
        JSONObject data = new JSONObject();
        data.put("settings", jsonSettings);
        data.put("serverList", jsonServerList);
        data.put("backupProfilesList", jsonBackupProfiles);
        data.put("globalServer", jsonGlobalServer);
        try (FileOutputStream fos = new FileOutputStream(path + ".json");){
            fos.write(data.toString(4).getBytes());
        } catch (IOException ex) {
            return false;
        }
        return true;
    }
    @Override
    public String getType() {
        return "json";
    }

    @Override
    public File getFile() {
        return new File(path + "." + exe);
    }

}
