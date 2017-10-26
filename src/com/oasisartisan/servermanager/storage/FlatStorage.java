/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oasisartisan.servermanager.storage;

import com.oasisartisan.servermanager.Utilities;
import com.oasisartisan.servermanager.consolecommunication.Printer;
import com.oasisartisan.servermanager.objects.GlobalServer;
import com.oasisartisan.servermanager.objects.TimedCommand;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 *
 * @author OmarAlama
 */
public class FlatStorage extends Storage {

    public static final String pName = "FlatStorage";
    public static final String exe = "flat";

    public boolean loadData() {
        Printer.printBackgroundInfo(pName, "Loading data...");
        File file = new File(path + ".flat");
        try (FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis);) {
            Data data = (Data) ois.readObject();
            serverList = data.getServerList();
            settings = data.getSettings();
            backupProfilesList = data.getBackupProfilesList();
            List<TimedCommand> ls = data.getGlobalTimedCommands();
            globalServer = new GlobalServer();
            for (TimedCommand tc : ls) {
                globalServer.addTimedCommand(tc);
            }
            Printer.printBackgroundSuccess(pName, "Data has been loaded successfuly from storage.");
            return true;
        } catch (ClassNotFoundException | InvalidClassException e) {
            Printer.printError(pName, "Failed to load data. Data file is corrupted or from an old version.", e);
            for (int i = 1; i <= 100; i++) {
                System.out.print(i);
                File f = new File(getFile().getName() + "_corrupt_" + i);
                if (!f.exists() && getFile().renameTo(f)) {
                    break;
                }
            }
        } catch (IOException e) {
            Printer.printError(pName, "Failed to load data. Cannot find or access program data file.", e);
        }
        return false;
    }

    public boolean saveData() {
        try {
            File file = new File(path + ".flat");
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(new Data(serverList, settings, backupProfilesList, globalServer));
            Printer.printBackgroundSuccess(pName, "Changes have been saved successfuly to storage.");
            return true;
        } catch (IOException e) {
            Printer.printError(pName, "Failed to save data to file.", e);
            return false;
        }

    }

    public String getType() {
        return "flat";
    }

    public File getFile() {
        return new File(path + "." + exe);
    }
}
