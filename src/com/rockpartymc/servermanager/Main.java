package com.rockpartymc.servermanager;

import com.rockpartymc.servermanager.consolecommunication.InputBuffer;
import com.rockpartymc.servermanager.consolecommunication.Printer;
import com.rockpartymc.servermanager.tasks.ShutDownTask;
import com.rockpartymc.servermanager.tasks.MonitorTask;
import com.rockpartymc.servermanager.tasks.ServerCommandSchedulerTask;
import com.rockpartymc.servermanager.tasks.ServerStateUpdaterTask;
import java.util.ArrayList;
import com.rockpartymc.servermanager.objects.Server;
import com.rockpartymc.servermanager.objects.Timing;
import com.rockpartymc.servermanager.objects.TimedCommand;
import com.rockpartymc.servermanager.storage.Settings;
import com.rockpartymc.servermanager.storage.Storage;
import com.rockpartymc.servermanager.tasks.ServerFileCommunicatorTask;
import java.io.File;
import java.util.HashMap;
import java.util.concurrent.Future;

public class Main {

    private static ServerStateUpdaterTask serverStateUpdaterTask;
    private static ServerFileCommunicatorTask serverFileCommunicatorTask;
    private static ServerCommandSchedulerTask serverCommandSchedulerTask;
    private static MonitorTask monitorTask;
    private static InputBuffer in;

    private static String activeMenu;

    public static InputBuffer getIn() {
        return in;
    }

    public static MonitorTask getMonitorTask() {
        return monitorTask;
    }

    public static InputBuffer getInputBuffer() {
        return in;
    }

    public static ServerFileCommunicatorTask getServerFileCommunicatorTask() {
        return serverFileCommunicatorTask;
    }

    public static ServerStateUpdaterTask getServerStateUpdaterTask() {
        return serverStateUpdaterTask;
    }

    public static ServerCommandSchedulerTask getServerCommandSchedulerTask() {
        return serverCommandSchedulerTask;
    }

    public static String getActiveMenu() {
        return activeMenu;
    }

    public static void main(String[] args) {
        Storage.loadDataFromFile();
        serverStateUpdaterTask = new ServerStateUpdaterTask();
        serverStateUpdaterTask.setName("ServerStateUpdaterTask");
        serverFileCommunicatorTask = new ServerFileCommunicatorTask();
        serverFileCommunicatorTask.setName("ServerFileCommunicatorTask");
        serverCommandSchedulerTask = new ServerCommandSchedulerTask();
        serverCommandSchedulerTask.setName("ServerCommandSchedulerTask");
        in = new InputBuffer(System.in);
        serverStateUpdaterTask.start();
        serverCommandSchedulerTask.start();
        serverFileCommunicatorTask.start();
        in.start();
        ShutDownTask sdt = new ShutDownTask();
        sdt.setName("ShutDownTask");
        Runtime.getRuntime().addShutdownHook(sdt);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }
        mainMenu();
    }

    public static void mainMenu() {
        String pName = "Main Menu";
        Printer.printSuccessfullReply("Welcome to the Linux Minecraft Server Manager !");
        while (true) {
            if (activeMenu == null || !activeMenu.equals(pName)) {
                Printer.printTitle(pName);
                Printer.printCustom("$BC" + Printer.formatDevider("| list | add | remove | open | monitor | settings | about | help | quit |","CENTER",80,' '));
                activeMenu = pName;
            }
            String input = in.next().toLowerCase();
            switch (input) {
                case "help":
                case "h":
                    Printer.printSubTitle("Main Menu Commands");
                    Printer.printItem("quit", "Exits the program.");
                    Printer.printItem("list(ls)", "Lists existing servers and their states.");
                    Printer.printItem("add", "Atempts to add a new server to the manager.");
                    Printer.printItem("remove(rm)", "Atempts to remove an existing server from the manager.");
                    Printer.printItem("open(o)", "Opens up the specified server's control menu.");
                    Printer.printItem("monitor(m)", "A realtime updated overview of all servers.");
                    Printer.printItem("settings(st)", "Enter the program's settings menu.");
                    Printer.printItem("about", "Shows information about the program and the author.");
                    break;
                case "list":
                case "ls":
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
                    break;
                case "add":
                    createServer();
                    break;
                case "remove":
                case "rm":
                    removeServer();
                    break;
                case "open":
                case "o":
                    openServer();
                    break;
                case "monitor":
                case "m":
                    monitorScreen();
                    break;
                case "settings":
                case "st":
                    settingsMenu();
                    break;
                case "about":
                    Printer.printCustom(
                            "$BCAuthor:" + System.lineSeparator()
                            + "$$WOmar Alama, Saudi Arabia, Co-Owner of play.rockpartymc.com, 19 years old upon finishing this program." + System.lineSeparator()
                            + "$BCGoal:" + System.lineSeparator() 
                            + "$$WThe $BBServerManager$$W program was initially designed to enable easy management of multiple minecraft servers all in one place." + System.lineSeparator()
                            + "The main usage was for managing the RockParty minecraft servers." + System.lineSeparator()
                            + "Nevertheless, the program can manage all java processes if the process knows how to communicate with the manager." + System.lineSeparator()
                            + "$BCSupported platforms:" + System.lineSeparator()
                            + "$$WCurrently the only platform that the manager can run on is bash. However supporting multiple platforms is planned in the future." + System.lineSeparator()
                            + "$BCCurrent available plugins:" + System.lineSeparator()
                            + "$$WServerManagerMonitor for Spigot | Plugin author: Ben Burum (Owner of RockParty)" + System.lineSeparator()
                            + System.lineSeparator()
                            + "$BYFor information on how to make your own communication plugin contact me."
                            );
                    break;
                default:
                    Printer.printFailedReply("Command: " + input + " not identified type help to list available commands.");
            }
        }
    }

    public static void settingsMenu() {
        String pName = "Settings";
        while (true) {
            Settings st = Storage.getSettings();
            if (activeMenu == null || !activeMenu.equals(pName)) {
                Printer.printTitle(pName);
                Printer.printCustom("$BC" + Printer.formatDevider("| info | 1-9 | default | help | back |","CENTER",80,' '));
                activeMenu = pName;
                st.printSettings();
            }
            String input = in.next().toLowerCase();
            switch (input) {
                case "1":
                    while (true) {
                        Printer.printPrompt("Enter the communication directory path");
                        input = in.nextLine();
                        if (input.equals("back") || input.equals("b")) {
                            break;
                        }
                        File file = new File(input);
                        boolean success = true;
                        if (!file.isDirectory()) {
                            Printer.printFailedReply("The directory does not exist. creating it.");
                            try {
                                success = file.mkdirs();
                            } catch (Exception e) {
                                Printer.printError("An exception occured while trying to create the specified directory.", e);
                                continue;
                            }
                        }
                        if (success) {
                            Storage.getSettings().setCommunicationDir(file);
                            Printer.printDataChange("The communication directory has been successfully set to " + file);
                            break;
                        }
                        Printer.printFailedReply("\"" + file + "\" is not a valid directory.");
                    }
                    break;
                case "2":
                    Printer.printPrompt("Only change this if you know what you are doing.");
                    while (true) {
                        Printer.printPrompt("Enter the new interval in milliseconds:");
                        input = in.next();
                        if (input.equals("back") || input.equals("b")) {
                            break;
                        }
                        if (Utilities.isLong(input)) {
                            long l = Long.parseLong(input);
                            if (l > 100) {
                                Storage.getSettings().setServerStateUpdaterTaskInterval(Long.parseLong(input));
                                Printer.printDataChange(pName,"The server state updater task interval has been set to " + input + " ms.");
                                break;
                            }
                            Printer.printFailedReply("The interval must be bigger than 100 ms.");
                            continue;
                        }
                        Printer.printFailedReply("\"" + input + "\" is not a valid interval.");
                    }
                    break;
                case "3":
                    while (true) {
                        Printer.printPrompt("Enter the new refresh rate in milliseconds:");
                        input = in.next();
                        if (input.equals("back") || input.equals("b")) {
                            break;
                        }
                        if (Utilities.isLong(input)) {
                            long l = Long.parseLong(input);
                            if (l > 100) {
                                Storage.getSettings().setMonitorRefreshRate(Long.parseLong(input));
                                Printer.printDataChange(pName,"The monitor refresh rate has been set to " + input + " ms");
                                break;
                            }
                            Printer.printFailedReply("The refresh rate must be bigger than 100 ms.");
                            continue;
                        }
                        Printer.printFailedReply("\"" + input + "\" is not a valid refresh rate.");
                    }
                    break;
                case "4":
                    while (true) {
                        Printer.printPrompt("Enter the new monitor messages duration in milliseconds:");
                        input = in.next();
                        if (input.equals("back") || input.equals("b")) {
                            break;
                        }
                        if (Utilities.isLong(input)) {
                            long l = Long.parseLong(input);
                            if (l > 0) {
                                Storage.getSettings().setMonitorMessagesDuration(Long.parseLong(input));
                                Printer.printDataChange(pName,"The monitor messages duration has been set to " + input + " ms");
                                break;
                            }
                            Printer.printFailedReply("The refresh rate must be bigger than 0.");
                            continue;
                        }
                        Printer.printFailedReply("\"" + input + "\" is not a valid refresh rate.");
                    }
                    break;
                case "5":
                    Printer.printPrompt("Only change this if you know what you are doing.");
                    while (true) {
                        Printer.printPrompt("Enter the new interval in milliseconds:");
                        input = in.next();
                        if (input.equals("back") || input.equals("b")) {
                            break;
                        }
                        if (Utilities.isLong(input)) {
                            long l = Long.parseLong(input);
                            if (l > 60000) {
                                Storage.getSettings().setCommandSchedulerTaskInterval(Long.parseLong(input));
                                Printer.printDataChange(pName,"The command scheduler task interval has been set to " + input + " ms");
                                break;
                            }
                            Printer.printFailedReply("The command scheduler task interval must be greater than 60000 ms.");
                            continue;
                        }
                        Printer.printFailedReply("\"" + input + "\" is not a valid interval.");
                    }
                    break;
                case "6":
                    Storage.getSettings().toggleUseConsoleColors();
                    Printer.printDataChange(pName,"Using colores in console is now set to " + Storage.getSettings().isUseConsoleColors());
                    break;
                case "7":
                    Storage.getSettings().toggleClearConsoleBeforeMenu();
                    Printer.printDataChange(pName,"Clearing the console before menu is now set to " + Storage.getSettings().isUseConsoleColors());
                    break;
                case "8":
                    Storage.getSettings().toggleLogOutput();
                    Printer.printDataChange(pName,"Logging output is now set to " + Storage.getSettings().isUseConsoleColors());
                    break;
                case "9":
                    Storage.getSettings().togglePrintBackgroundInfoToConsole();
                    Printer.printDataChange(pName,"Printing background info to console is now set to " + Storage.getSettings().isUseConsoleColors());
                    break;
                case "10":
                    Storage.getSettings().toggleBackgroundInfoTimeStampsInConsole();
                    Printer.printDataChange(pName,"Printing time stamps on background info in console is now set to " + Storage.getSettings().isUseConsoleColors());
                    break;
                case "default":
                case "d":
                    Storage.getSettings().saveDefault();
                    Storage.saveDataToFile();
                    Printer.printDataChange(pName, "Settings have been set to default.");
                    break;
                case "info":
                case "i":
                    Storage.getSettings().printSettings();
                    break;
                case "help":
                case "h":
                    Printer.printSubTitle("Settings Commands");
                    Printer.printItem("info(i)", "Prints out current settings.");
                    Printer.printItem("1-10", "To change the setting corresponding to that number according to \"info\".");
                    Printer.printItem("default(d)", "To reset all settings to their default values.");
                    Printer.printItem("back(b)", "To go back to the main menu.");
                    break;
                case "back":
                case "b":
                    return;
                default:
                    Printer.printFailedReply("\"" + input + "\" is not identified, use \"help\" to list available commands.");
            }
        }
    }

    public static void createServer() {
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
                input = in.next();
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

    public static void removeServer() {
        String pName = "RemoveServer";
        while (true) {
            Printer.printPrompt(pName, "Enter the server name that you wish to remove:");
            String input = in.next();
            if (input.toLowerCase().equals("back") || input.toLowerCase().equals("b")) {
                return;
            }
            boolean removed = false;
            HashMap<String, Server> serverList = Storage.getServerList();
            synchronized (serverList) {
                for (String s : serverList.keySet()) {
                    if (s.equals(input)) {
                        serverList.remove(s);
                        Storage.saveDataToFile();
                        removed = true;
                        break;
                    }
                }
            }
            if (removed) {
                Storage.saveDataToFile();
                Printer.printDataChange(pName, "Server \"" + input + "\" has been removed successfully.");
                return;
            } else {
                Printer.printFailedReply(pName, "Server \"" + input + "\" was not found.");
            }
        }
    }

    public static void openServer() {
        String pName = "OpenServer";
        while (true) {
            Printer.printPrompt(pName, "Enter the server you wish to open:");
            String input = in.next();
            if (input.toLowerCase().equals("back") || input.toLowerCase().equals("b")) {
                return;
            }
            HashMap<String, Server> serverList = Storage.getServerList();
            Server server;
            synchronized (serverList) {
                server = serverList.get(input);
            }
            if (server != null) {
                serverMenu(server);
                return;
            }
            Printer.printFailedReply(pName, "Server \"" + input + "\" was not found.");
        }
    }

    public static void serverMenu(Server server) {
        String pName = server.getName() + " Editor";
        while (true) {
            if (activeMenu == null || !activeMenu.equals(pName)) {
                activeMenu = pName;
                Printer.printTitle(pName);
                Printer.printCustom("$BC" + Printer.formatDevider("| start | stop | kill | restart | send | timedcommands | settings | info | help | back | quit |","CENTER",80,' '));
            }
            String input = in.next().toLowerCase();
            switch (input) {
                case "start":
                    server.start();
                    break;
                case "stop":
                    server.stop();
                    break;
                case "kill":
                    server.kill();
                    break;
                case "restart":
                    server.restart();
                    break;
                case "send":
                    Printer.printPrompt(pName, "Enter the command you would like to send:");
                    String cmd = in.nextLine().trim();
                    while (cmd.isEmpty()) {
                        cmd = in.nextLine().trim();
                    }
                    server.sendCommand(cmd);
                    break;
                case "timedcommands":
                case "tc":
                    timedCommandsMenu(server);
                    break;
                case "settings":
                case "st":
                    serverSettingsMenu(server);
                    break;
                case "info":
                case "i":
                    server.printInfo(true);
                    break;
                case "help":
                case "h":
                    Printer.printSubTitle("Server Menu Commands");
                    Printer.printItem("start", "Atempts to start the server if its offline.");
                    Printer.printItem("stop", "Atempts to stop the server gracefully if its online, or kills it if its not responding..");
                    Printer.printItem("restart", "Calls the stop then start commands to restart..");
                    Printer.printItem("kill", "Forcefully shuts down the server's process.");
                    Printer.printItem("send", "Send a command to the server.");
                    Printer.printItem("timedcommands(tc)", "Opens up the timed commands editor for this server.");
                    Printer.printItem("info(i)", "Prints out this server's detailed info.");
                    Printer.printItem("settings(st)", "Enters this server's settings menu.");
                    Printer.printItem("back(b)", "Returns to the main menu.");
                    break;
                case "back":
                case "b":
                    return;
                default:
                    Printer.printFailedReply("Command: " + input + " not identified type help to list available commands.");
            }
        }
    }

    public static void serverSettingsMenu(Server server) {
        String pName = server.getName() + " Settings";
        while (true) {
            if (activeMenu == null || !activeMenu.equals(pName)) {
                activeMenu = pName;
                Printer.printTitle(pName);
                Printer.printCustom("$BC" + Printer.formatDevider("| 1-6 | info | help | back | quit |","CENTER",80,' '));
                server.getSettings().printSettings(server.getName());
            }
            String input = in.next().toLowerCase();
            switch (input) {
                case "1":
                    while (true) {
                        Printer.printPrompt(pName, "Enter the new starting ram:");
                        input = in.next();
                        if (input.equals("back") || input.equals("b")) {
                            break;
                        }
                        if(input.equals("clear") || input.equals("null") )
                        {
                            server.getSettings().setStartRam(null);
                            Printer.printDataChange(pName, "Starting ram has been cleared.");
                            break;
                        }
                        String ramValidation = Utilities.isValidRam(input);
                        if (ramValidation == null) {
                            server.getSettings().setStartRam(input);
                            Printer.printDataChange(pName, "Starting ram has been set to \"" + input + "\".");
                            break;
                        } else {
                            Printer.printFailedReply(pName, ramValidation);
                        }
                    }
                    break;
                case "2":
                    while (true) {
                        Printer.printPrompt(pName, "Enter the new max ram:");
                        input = in.next();
                        if (input.equals("back") || input.equals("b")) {
                            break;
                        }
                        if(input.equals("clear") || input.equals("null") )
                        {
                            server.getSettings().setMaxRam(null);
                            Printer.printDataChange(pName, "Max ram has been cleared.");
                            break;
                        }
                        String ramValidation = Utilities.isValidRam(input);
                        if (ramValidation == null) {
                            server.getSettings().setMaxRam(input);
                            Printer.printDataChange(pName, "Max ram has been set to \"" + input + "\".");
                            break;
                        } else {
                            Printer.printFailedReply(pName, ramValidation);
                        }
                    }
                    break;
                case "3":
                    server.getSettings().toggleStartIfOffline();
                    Printer.printSuccessfullReply(pName, "Start if offline has been set to " + server.getSettings().isStartIfOffline());
                    break;
                case "4":
                    server.getSettings().toggleRestartIfNotResponding();
                    Printer.printSuccessfullReply(pName, "Restart if not responding has been set to " + server.getSettings().isRestartIfNotResponding());
                    break;
                case "5":
                    while (true) {
                        Printer.printPrompt("Enter the new max waiting duration for starting: ");
                        input = in.next();
                        if (input.equals("back") || input.equals("b")) {
                            break;
                        }
                        if (Utilities.isLong(input)) {
                            long l = Long.parseLong(input);
                            if (l > 0) {
                                server.getSettings().setMaxStartingDuration(l);
                                Printer.printDataChange(pName, "The max waiting duration for starting the server has been changed to " + l + " ms.");
                                break;
                            }
                        }
                        Printer.printFailedReply(pName, "\"" + input + "\" is not a valid number.");
                    }
                    break;
                case "6":
                    while (true) {
                        Printer.printPrompt("Enter the new max waiting duration for stopping the server: ");
                        input = in.next();
                        if (input.equals("back") || input.equals("b")) {
                            break;
                        }
                        if (Utilities.isLong(input)) {
                            long l = Long.parseLong(input);
                            if (l > 0) {
                                server.getSettings().setMaxStoppingDuration(l);
                                Printer.printDataChange(pName, "The max waiting duration for stopping the server has been changed to " + l + " ms.");
                                break;
                            }
                        }
                        Printer.printFailedReply(pName, "\"" + input + "\" is not a valid number.");
                    }
                    break;
                case "7":
                    input = in.nextLine();
                    Printer.printPrompt("Enter the new stop command: ");
                    server.getSettings().setStopCommand(input);
                    Printer.printDataChange(pName, "The stop command has been changed successfully to \"" + input + "\".");
                    break;
                case "info":
                case "i":
                    server.getSettings().printSettings(server.getName());
                    break;
                case "back":
                case "b":
                    return;
                case "help":
                case "h":
                    Printer.printSubTitle("Server Settings Menu Commands");
                    Printer.printItem("1-6", "To change the setting corresponding to that number according to \"info\".");
                    Printer.printItem("info(i)", "To display current server settings.");
                    Printer.printItem("back(b)", "To go back to the server menu.");
                    break;
                default:
                    Printer.printFailedReply(pName, "\"" + input + "\" was not identified. Type help to list available commands.");
            }
        }
    }

    public static void timedCommandsMenu(Server server) {
        String pName = server.getName() + " Timed Commands";
        while (true) {
            if (activeMenu == null || !activeMenu.equals(pName)) {
                activeMenu = pName;
                Printer.printTitle(pName);
                Printer.printCustom("$BC" + Printer.formatDevider("| add | remove | list | help | back | quit |","CENTER",80,' '));
            }
            String input = in.next();
            switch (input) {
                case "add":
                    addTimedCommand(server);
                    break;
                case "remove":
                case "rm":
                    if (server.timedCommandsSize() != 0) {
                        removeTimedCommand(server);
                    } else {
                        Printer.printFailedReply(pName, "The server has no timed commands.");
                    }
                    break;
                case "list":
                case "ls":
                    if (server.timedCommandsSize() != 0) {
                        Printer.printSuccessfullReply(pName, "The server has the following timed commands:");
                        server.listTimedCommands(pName);
                    } else {
                        Printer.printFailedReply(pName, "The server has no timed commands.");
                    }
                    break;
                case "back":
                case "b":
                    return;
                case "help":
                case "h":
                    Printer.printSubTitle("Timed Commands Menu Commands");
                    Printer.printItem("add", "To add a timed command to the server.");
                    Printer.printItem("remove(rm)", "To remove a timed command from the server.");
                    Printer.printItem("list(ls)", "To list all timed commands in the server.");
                    Printer.printItem("back(b)", "To go back to the server menu.");
                    break;
                default:
                    Printer.printFailedReply(pName, "\"" + input + "\" was not identified. Type help to list available commands.");
            }
        }
    }

    public static void addTimedCommand(Server server) {
        String pName = server.getName() + "-AddTimedCommand";
        while (true) {
            Printer.printPrompt(pName, "Enter the command:");
            String input = in.next().toLowerCase();
            if (input.equals("back") || input.equals("b")) {
                return;
            }
            if (input.equals("help") || input.equals("h")) {
                Printer.printSubTitle("Acceptable Timed Commands");
                Printer.printItem("start", "Atempts to start the server if its offline.");
                Printer.printItem("stop", "Atempts to stop the server gracefully if its online, or kills it if its not responding..");
                Printer.printItem("restart", "Calls the stop then start commands to restart..");
                Printer.printItem("kill", "Forcefully shuts down the server's process.");
                Printer.printItem("send", "Send a command to the server.");
                continue;
            }
            if (TimedCommand.isValidCommand(input)) {
                String cmd = input;
                if(input.equals("send"))
                {
                    Printer.printPrompt("Enter the command to send to the server:");
                    cmd += " " + in.nextLine();
                }
                while (true) {
                    Printer.printPrompt(pName, "Enter the time to execute:");
                    input = in.next();
                    if (input.equals("back") || input.equals("b")) {
                        return;
                    }
                    if (input.equals("help") || input.equals("h")) {
                        Timing.printTimeFormats(pName);
                        continue;
                    }
                    try {
                        Timing time = new Timing(input);
                        server.addTimedCommand(new TimedCommand(cmd, time));
                        Printer.printDataChange(pName, "Command \"" + cmd + "\" has been added successfully to be run " + time.getType() + " at " + time.toString() + ".");
                        return;
                    } catch (IllegalArgumentException e) {
                        Printer.printFailedReply(pName, "Wrong time format. Type help to see correct formats.");
                    }
                }
            } else {
                Printer.printFailedReply(pName, "\"" + input + "\" is not a valid command. Type help to see acceptable commands.");
            }
        }
    }

    public static void removeTimedCommand(Server server) {
        String pName = server.getName() + "-RemoveTimedCommand";
        while (true) {
            Printer.printPrompt(pName, "Enter the index of the command to remove.");
            String input = in.next();
            if (input.equals("back") || input.equals("b")) {
                return;
            }
            if (input.equals("help") || input.equals("h")) {
                Printer.printSuccessfullReply("Use the command \"back\" to go back to the server's menu.");
                continue;
            }
            if (Utilities.isInteger(input)) {
                int i = Integer.parseInt(input);
                try {
                    ArrayList<TimedCommand> list = server.getTimedCommands();
                    synchronized (list) {
                        Future f = list.get(i).getTask();
                        if (f != null) {
                            f.cancel(true);
                            list.get(i).setTask(null);
                        }
                    }
                    server.removeTimedCommand(i);
                    break;
                } catch (IndexOutOfBoundsException e) {
                    Printer.printFailedReply("The index must be from 0 to " + (server.timedCommandsSize() - 1) + " inclusive.");
                }
            } else {
                Printer.printFailedReply(pName, "\"" + input + "\" is not a number.");
            }
        }
    }

    public static void monitorScreen() {
        if (monitorTask == null) {
            activeMenu = null;
            boolean detail = false;
            if (in.hasNext() && in.next().contains("detail")) {
                detail = true;
            }
            monitorTask = new MonitorTask(Storage.getSettings().getMonitorRefreshRate(), detail);
            monitorTask.setName("Monitor");
            monitorTask.start();
            in.clear();
            in.next();
            Printer.flushMonitorMessages(-1L);

            monitorTask.interrupt();
            try {
                monitorTask.join(2000);
                if (monitorTask.isAlive()) {
                    Printer.printError("Monitor", "Thread is taking too long to end.", null);
                }
            } catch (InterruptedException ex) {
            }
            monitorTask = null;
        } else {
            Printer.printError("Monitor", "Another thread atempted to start another monitor screen", null);
        }
    }
}
