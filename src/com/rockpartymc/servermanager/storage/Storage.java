package com.rockpartymc.servermanager.storage;

import com.rockpartymc.servermanager.consolecommunication.Printer;
import java.util.HashMap;
import com.rockpartymc.servermanager.objects.Server;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Storage {

    private static final String path = "sm.data";
    private static HashMap<String, Server> serverList; // turn into hashmap
    private static Settings settings;

    public static boolean loadDataFromFile() {
        try {
            File file = new File(path);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(path);
                ObjectInputStream ois = new ObjectInputStream(fis);
                Data data = (Data) ois.readObject();
                serverList = data.getServerList();
                settings = data.getSettings();
                Printer.printBackgroundSuccess("Storage", "Data has been loaded successfuly from program data file.");
                return true;
            } else {
                settings = new Settings();
                Printer.printBackgroundFail("Storage", "Failed to find program data file.");
            }
        } catch (ClassNotFoundException | InvalidClassException e) {
            settings = new Settings();
            Printer.printError("Storage", "Failed to load data. Data file is corrupted.", e);
        } catch (IOException e) {
            settings = new Settings();
            Printer.printError("Storage", "Failed to load data. Cannot find or access program data file.", e);
        }
        serverList = new HashMap();
        saveDataToFile();
        return false;
    }

    public static boolean saveDataToFile() {
        try {
            File file = new File(path);
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(path);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(new Data(serverList, settings));
            Printer.printBackgroundSuccess("Storage", "Changes have been saved successfuly to storage.");
            return true;
        } catch (IOException e) {
            Printer.printError("Storage", "Failed to save data to file.", e);
            return false;
        }

    }

    public static HashMap<String, Server> getServerList() {
        synchronized (serverList) {
            return serverList;
        }
    }

    public static Settings getSettings() {
        return settings;
    }
}
