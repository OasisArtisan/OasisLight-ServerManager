package com.rockpartymc.servermanager.storage;

import com.rockpartymc.servermanager.Main;
import com.rockpartymc.servermanager.Utilities;
import com.rockpartymc.servermanager.consolecommunication.Printer;
import com.rockpartymc.servermanager.objects.BackupProfile;
import com.rockpartymc.servermanager.objects.GlobalServer;
import java.util.HashMap;
import com.rockpartymc.servermanager.objects.Server;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class Storage {
    protected static final String path = "smdata";
    protected static HashMap<String, Server> serverList;
    protected static HashMap<String, BackupProfile> backupProfilesList;
    protected static Settings settings;
    protected static GlobalServer globalServer;
    private static HashMap<String, Storage> storages;

    public static boolean saveDataToFile() {
        return storages.get(settings.getStorageType()).saveData();
    }

    public static boolean loadDataFromFile() {
        if (storages == null) {
            initStorageTypes();
        }
        List<Storage> choices = new ArrayList();
        synchronized (storages) {
            for (Storage st : storages.values()) {
                if (st.getFile().isFile()) {
                    choices.add(st);
                }
            }
        }
        if (choices.size() == 1) {
            boolean c = choices.get(0).loadData();
            if (!c) {
                initObjects();
                return false;
            }
            return true;
        } else if (choices.isEmpty()) {
            Printer.printBackgroundFail("Storage", "Failed to load data. Server Manager did not find any data files.");
            initObjects();
            return false;
        } else {
            Printer.printPrompt("Server Manager has detected multiple storage files please choose one of them:");
            Printer.printPrompt("If you do not wish to see this on every start, only keep one data file in the program's directory.");
            for (Storage st : choices) {
                Printer.printPrompt("(" + st.getType() + ") " + Utilities.getFileInfo(st.getFile()));
            }
            while (true) {
                String choice = Main.getIn().next().toLowerCase();
                Storage st = storages.get(choice);
                if (st != null) {
                    if(!st.loadData())
                    {
                        initObjects();
                        return false;
                    }
                    return true;
                }
                Printer.printFailedReply("\"" + choice + "\" is not a valid option please choose from the options above");
            }
        }
    }
    public static GlobalServer getGlobalServer() {
        return globalServer;
    }
    private static void initObjects() {
        settings = new Settings();
        serverList = new HashMap();
        backupProfilesList = new HashMap();
        globalServer = new GlobalServer();
        saveDataToFile();
    }

    private static void initStorageTypes() {
        storages = new HashMap();
        Storage st = new FlatStorage();
        storages.put(st.getType(), st);
        st = new SQLiteStorage();
        storages.put(st.getType(), st);
    }

    public static HashMap<String, Storage> getStorageTypes() {
        return storages;
    }

    public abstract boolean loadData();

    public abstract boolean saveData();

    public abstract String getType();

    public abstract File getFile();

    public static HashMap<String, Server> getServerList() {
        synchronized (serverList) {
            return serverList;
        }
    }
    public static HashMap<String, BackupProfile> getBackupProfileList() {
        return backupProfilesList;
    }
    public static Settings getSettings() {
        return settings;
    }
}
