package com.oasisartisan.servermanager.storage;

import com.oasisartisan.servermanager.objects.BackupProfile;
import com.oasisartisan.servermanager.objects.GlobalServer;
import com.oasisartisan.servermanager.objects.Server;
import com.oasisartisan.servermanager.objects.TimedCommand;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
/**
 *
 * @author OasisArtisan
 */
public class Data implements Serializable {

    private HashMap<String, Server> serverList;
    private Settings settings;
    private HashMap<String, BackupProfile> backupProfilesList;
    private List<TimedCommand> globalTimedCommands;

    public Data(HashMap<String, Server> serverList, Settings settings, HashMap<String, BackupProfile> backupProfilesList, GlobalServer gs) {
        this.serverList = serverList;
        this.settings = settings;
        this.backupProfilesList = backupProfilesList;
        this.globalTimedCommands = gs.getTimedCommands();
    }

    public HashMap<String, Server> getServerList() {
        return serverList;
    }

    public Settings getSettings() {
        return settings;
    }

    public HashMap<String, BackupProfile> getBackupProfilesList() {
        return backupProfilesList;
    }

    public List<TimedCommand> getGlobalTimedCommands() {
        return globalTimedCommands;
    }
}
