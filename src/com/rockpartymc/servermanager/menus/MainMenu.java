/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rockpartymc.servermanager.menus;

import com.rockpartymc.servermanager.consolecommunication.Printer;
import com.rockpartymc.servermanager.objects.Server;
import com.rockpartymc.servermanager.storage.Storage;
import java.util.HashMap;

/**
 *
 * @author OmarAlama
 */
public class MainMenu extends Menu {

    public MainMenu() {
        super("Main Menu");
        items.add(new Item(
                "Lists existing servers and their states.",
                new Runnable() {
            @Override
            public void run() {
                int c = 0;
                HashMap<String, Server> list = Storage.getServerList();
                Printer.printSubTitle("(" + list.size() + ") Saved Servers: ");
                synchronized (list) {
                    for (Server server : list.values()) {
                        c++;
                        Printer.printCustom(String.format("%-23s  %-23s  %-48s", "$_YServer: $$W"
                                + server.getName(), "$_YStatus: "
                                + Printer.getCCC(server.getState())
                                + server.getState().toString(),
                                "$$W" + server.getFile().toString()));
                    }
                }
                if (c == 0) {
                    Printer.printFailedReply("No servers added, use the \"add\" command to add servers");
                }
            }
        }, "list", "ls"
        ));
        items.add(new Item(
                "Atempts to add a new server to the manager.",
                new Runnable() {
            @Override
            public void run() {
                int c = 0;
                HashMap<String, Server> list = Storage.getServerList();
                Printer.printSubTitle("(" + list.size() + ") Saved Servers: ");
                synchronized (list) {
                    for (Server server : list.values()) {
                        c++;
                        Printer.printCustom(String.format("%-23s  %-23s  %-48s", "$_YServer: $$W"
                                + server.getName(), "$_YStatus: "
                                + Printer.getCCC(server.getState())
                                + server.getState().toString(),
                                "$$W" + server.getFile().toString()));
                    }
                }
                if (c == 0) {
                    Printer.printFailedReply("No servers added, use the \"add\" command to add servers");
                }
            }
        }, "add"
        ));
    }
}
