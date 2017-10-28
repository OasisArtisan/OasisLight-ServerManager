/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rockpartymc.servermanager.menus;

import com.rockpartymc.servermanager.Main;
import com.rockpartymc.servermanager.consolecommunication.Printer;
import com.rockpartymc.servermanager.objects.Server;
import com.rockpartymc.servermanager.storage.Storage;
import java.io.File;
import java.util.HashMap;

/**
 *
 * @author OmarAlama
 */
public class AddServerDialog implements Runnable {

    public void run() {
        HashMap<String, Server> serverList = Storage.getServerList();
        String pName = "CreateServer";
        while (true) {
            Printer.printPrompt(pName, "Enter the server name:");
            String input = in.next();
            if (input.toLowerCase().equals("back") || input.toLowerCase().equals("b")) {
                return;
            }
            boolean available = true;
            synchronized (serverList) {
                for (Server server : serverList.values()) {
                    if (server.getName().equals(input)) {
                        available = false;
                        break;
                    }
                }
            }
            if (!available) {
                Printer.printFailedReply(pName, "That name is already taken!");
                continue;
            }
            String name = input;
            while (true) {
                Printer.printPrompt(pName, "Enter the server jar path:");
                input = Main.getIn().next();
                if (input.toLowerCase().equals("back") || input.toLowerCase().equals("b")) {
                    return;
                }
                File file = new File(input);
                try {
                    if (input.endsWith(".jar") && file.isFile()) {
                        String path = input;
                        Server server = new Server(name, path);
                        synchronized (serverList) {
                            serverList.put(name, server);
                        }
                        Storage.saveDataToFile();
                        Printer.printDataChange(pName, "Server \"" + name + "\" has been added successfully.");
                        try {
                            serverFileCommunicatorTask.updateServerMonitorData(server);
                        } catch (InterruptedException ex) {
                            
                        }
                        return;
                    } else {
                        Printer.printFailedReply(pName, "The path \"" + input + "\" is not valid.");
                    }
                } catch (SecurityException e) {
                    Printer.printFailedReply(pName, "LMSM was denied access to the file");
                }
            }
        }
    }
}
