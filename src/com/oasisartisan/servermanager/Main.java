package com.oasisartisan.servermanager;

import com.oasisartisan.servermanager.consolecommunication.InputBuffer;
import com.oasisartisan.servermanager.consolecommunication.Printer;
import com.oasisartisan.servermanager.objects.BackupProfile;
import com.oasisartisan.servermanager.objects.GlobalServer;
import com.oasisartisan.servermanager.objects.Pair;
import com.oasisartisan.servermanager.tasks.ShutDownTask;
import com.oasisartisan.servermanager.tasks.MonitorTask;
import com.oasisartisan.servermanager.tasks.ServerCommandSchedulerTask;
import com.oasisartisan.servermanager.tasks.ServerStateUpdaterTask;
import java.util.ArrayList;
import com.oasisartisan.servermanager.objects.Server;
import com.oasisartisan.servermanager.objects.ServerState;
import com.oasisartisan.servermanager.objects.Timing;
import com.oasisartisan.servermanager.objects.TimedCommand;
import com.oasisartisan.servermanager.processhandlers.BashProcessHandler;
import com.oasisartisan.servermanager.processhandlers.ProcessHandler;
import com.oasisartisan.servermanager.storage.Settings;
import com.oasisartisan.servermanager.storage.Storage;
import com.oasisartisan.servermanager.tasks.ServerFileCommunicatorTask;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

public class Main {

    public static final String PROGRAM_NAME = "Oasis Server Manager";
    public static final String WIKI_LINK = "www.github.placeholder.com";

    public static final String VERSION = "1.1";
    private static ProcessHandler processHandler;

    private static ServerStateUpdaterTask serverStateUpdaterTask;
    private static ServerFileCommunicatorTask serverFileCommunicatorTask;
    private static ServerCommandSchedulerTask serverCommandSchedulerTask;
    private static MonitorTask monitorTask;
    private static InputBuffer in;
    private static Pair<String, String> activeMenu;
    private static CountDownLatch cdl;

    private final static HashMap<String, String[]> MENUS;

    static {
        MENUS = new HashMap();
        String[] MAIN = {"open", "list", "add", "remove", "monitor", "settings", "backup-profiles", "about", "help"};
        MENUS.put("MAIN", MAIN);
        String[] SETTINGS = {"info", "1-9", "default", "help ", "back"};
        MENUS.put("SETTINGS", SETTINGS);
        String[] SERVER = {"start", "stop", "kill", "restart", "send", "backup", "timedcommands", "settings", "info", "help", "back"};
        MENUS.put("SERVER", SERVER);
        String[] SERVER_SETTINGS = {"rename", "path", "link", "unlink", "1-6", "info", "help", "back"};
        MENUS.put("SERVER_SETTINGS", SERVER_SETTINGS);
        String[] GLOBAL_SERVER_SETTINGS = {"link", "unlink", "1-6", "info", "help", "back"};
        MENUS.put("GLOBAL_SERVER_SETTINGS", GLOBAL_SERVER_SETTINGS);
        String[] TIMED_COMMANDS = {"add", "remove", "list", "help", "back"};
        MENUS.put("TIMED_COMMANDS", TIMED_COMMANDS);
        String[] BACKUP_PROFILES = {"open", "list", "add", "remove", "help", "back"};
        MENUS.put("BACKUP_PROFILES", BACKUP_PROFILES);
        String[] BACKUP_PROFILE_EDITOR = {"info", "save-directory", "exclude", "include", "help", "back"};
        MENUS.put("BACKUP_PROFILE_EDITOR", BACKUP_PROFILE_EDITOR);
        String[] STRING_LIST_EDITOR = {"list", "add", "remove", "help", "back"};
        MENUS.put("STRING_LIST_EDITOR", STRING_LIST_EDITOR);
    }

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

    public static CountDownLatch getCountDownLatch() {
        return cdl;
    }

    public static Pair<String, String> getActiveMenu() {
        return activeMenu;
    }

    public static ProcessHandler getProcessHandler() {
        return processHandler;
    }

    public static void main(String[] args) {
        in = new InputBuffer(System.in);
        in.start();
        processHandler = new BashProcessHandler();
        try {
        if (!processHandler.checkPrerequisites()) {
            Printer.printPrompt("Hit 'Enter' to exit...");
            Main.getIn().waitForEnter();
            System.exit(0);
        }
        } catch(InterruptedException e)
        {
            Printer.printError("Main", "Failed to check for prerequisites.", e);
            Printer.printPrompt("Hit 'Enter' to exit...");
            Main.getIn().waitForEnter();
            System.exit(0);
        }
        processHandler.clearConsole();
        Storage.loadDataFromFile();
        serverStateUpdaterTask = new ServerStateUpdaterTask();
        serverStateUpdaterTask.setName("ServerStateUpdaterTask");
        serverFileCommunicatorTask = new ServerFileCommunicatorTask();
        serverFileCommunicatorTask.setName("ServerFileCommunicatorTask");
        serverCommandSchedulerTask = new ServerCommandSchedulerTask();
        serverCommandSchedulerTask.setName("ServerCommandSchedulerTask");
        cdl = new CountDownLatch(3);
        serverStateUpdaterTask.start();
        serverCommandSchedulerTask.start();
        serverFileCommunicatorTask.start();
        ShutDownTask sdt = new ShutDownTask();
        sdt.setName("ShutDownTask");
        Runtime.getRuntime().addShutdownHook(sdt);
        try {
            cdl.await();
        } catch (InterruptedException ex) {
        }
        while (true) {
            try {
                Printer.printPrompt("Hit 'Enter' to open the main menu...");
                in.waitForEnter();
                mainMenu();
            } catch (Exception e) {
                Printer.printError("Main", "An unexpected error occured.", e);
            }
        }
    }

    public static void changeMenu(String title, String menu) {
        if (activeMenu == null || !activeMenu.getK().equals(title)) {
            printMenu(title, menu);
        }
    }

    public static void printMenu(String title, String menu) {
        String items = "| ";
        for (String s : MENUS.get(menu)) {
            if (s == null) {
                continue;
            }
            items += s + " | ";
        }
        items = items.trim();
        items += " quit |";
        int size = items.length();
        if (size < 70) {
            size = 70;
        }

        Printer.printTitle(title, size);
        Printer.printCustom("$BC" + Printer.formatDevider(items, "CENTER", size, ' '));
        activeMenu = new Pair(title, menu);
    }

    public static void mainMenu() {
        String pName = "Main Menu";
        while (true) {
            changeMenu(pName, "MAIN");
            String input = in.next().toLowerCase();
            switch (input) {
                case "help":
                case "h":
                    Printer.printSubTitle("Main Menu Commands");
                    Printer.printItem("quit", "Exits the program.");
                    Printer.printItem("open(o)", "Opens up the specified server's control menu."
                            + "The command can also be followed by \"" + Storage.getGlobalServer().getName() + "\" to open up a global control panel");
                    Printer.printItem("list(ls)", "Lists existing servers and their states.");
                    Printer.printItem("add", "Atempts to add a new server to the manager.");
                    Printer.printItem("remove(rm)", "Atempts to remove an existing server from the manager.");
                    Printer.printItem("monitor(m)", "Displays a realtime updated overview of the servers.");
                    Printer.printCustom("$$WThe command can be followed by 0-3 to specify the level of detail to show." + System.lineSeparator()
                            + "It can also be followed by server names to only include those in the monitor display");
                    Printer.printItem("settings(st)", "Enters the program's settings menu.");
                    Printer.printItem("backup-profiles(bp)", "Enters the program's backup profiles menu.");
                    Printer.printItem("about", "Shows information about the program and the author.");
                    Printer.printCustom("$BYFor guides and more on information on how  to use the program and where to find monitor plugins,\nvisit the wiki at $_Y" + WIKI_LINK);
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
                case "backups":
                case "backup":
                case "backup-profiles":
                case "bp":
                    backupProfilesMenu();
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
                            "$BC" + PROGRAM_NAME + " Version: " + VERSION + System.lineSeparator()
                            + "$BCAuthor:" + System.lineSeparator()
                            + "$$WOmar Alama, Saudi Arabia, Computer Engineering student." + System.lineSeparator()
                            + "$BCGoal:" + System.lineSeparator()
                            + "$$WThe $BBServerManager$$W program was initially designed to enable easy management of multiple minecraft servers all in one place." + System.lineSeparator()
                            + "The main usage was for managing the RockParty minecraft servers." + System.lineSeparator()
                            + "Nevertheless, the program can manage all java processes." + System.lineSeparator()
                            + "$BCSupported platforms:" + System.lineSeparator()
                            + "$$WCurrently the only platform that the manager can run on is bash. However supporting multiple platforms is planned in the future." + System.lineSeparator()
                            + System.lineSeparator()
                            + "$BYFor more information visit the wiki at $_Y" + WIKI_LINK
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
            boolean printSettings = activeMenu == null || !activeMenu.getK().equals(pName);
            changeMenu(pName, "SETTINGS");
            if (printSettings) {
                st.printSettings();
            }
            String input = in.next().toLowerCase();
            switch (input) {
                case "1":
                    while (true) {
                        Printer.printPrompt("Enter the communication directory path");
                        input = in.nextLine();
                        if (input.equalsIgnoreCase("back") || input.equalsIgnoreCase("b")) {
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
                        if (input.equalsIgnoreCase("back") || input.equalsIgnoreCase("b")) {
                            break;
                        }
                        if (Utilities.isLong(input)) {
                            long l = Long.parseLong(input);
                            if (l > 100) {
                                Storage.getSettings().setServerStateUpdaterTaskInterval(Long.parseLong(input));
                                Printer.printDataChange(pName, "The server state updater task interval has been set to " + input + " ms.");

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
                        if (input.equalsIgnoreCase("back") || input.equalsIgnoreCase("b")) {
                            break;
                        }
                        if (Utilities.isLong(input)) {
                            long l = Long.parseLong(input);
                            if (l > 100) {
                                Storage.getSettings().setMonitorRefreshRate(Long.parseLong(input));
                                Printer.printDataChange(pName, "The monitor refresh rate has been set to " + input + " ms");

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
                        if (input.equalsIgnoreCase("back") || input.equalsIgnoreCase("b")) {
                            break;
                        }
                        if (Utilities.isLong(input)) {
                            long l = Long.parseLong(input);
                            if (l > 0) {
                                Storage.getSettings().setMonitorMessagesDuration(Long.parseLong(input));
                                Printer.printDataChange(pName, "The monitor messages duration has been set to " + input + " ms");

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
                        if (input.equalsIgnoreCase("back") || input.equalsIgnoreCase("b")) {
                            break;
                        }
                        if (Utilities.isLong(input)) {
                            long l = Long.parseLong(input);
                            if (l > 60000) {
                                Storage.getSettings().setCommandSchedulerTaskInterval(Long.parseLong(input));
                                Printer.printDataChange(pName, "The command scheduler task interval has been set to " + input + " ms");

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
                    Printer.printDataChange(pName, "Using colores in console is now set to " + Storage.getSettings().isUseConsoleColors());

                    break;
                case "7":
                    Storage.getSettings().toggleClearConsoleBeforeMenu();
                    Printer.printDataChange(pName, "Clearing the console before menu is now set to " + Storage.getSettings().isClearConsoleBeforeMenu());

                    break;
                case "8":
                    Storage.getSettings().toggleLogOutput();
                    Printer.printDataChange(pName, "Logging output is now set to " + Storage.getSettings().isLogOutput());

                    break;
                case "9":
                    Storage.getSettings().togglePrintBackgroundInfoToConsole();
                    Printer.printDataChange(pName, "Printing background info to console is now set to " + Storage.getSettings().isPrintBackgroundInfoToConsole());

                    break;
                case "10":
                    Storage.getSettings().toggleBackgroundInfoTimeStampsInConsole();
                    Printer.printDataChange(pName, "Printing time stamps on background info in console is now set to " + Storage.getSettings().isBackgroundInfoTimeStampsInConsole());

                    break;
                case "11":

                    out:
                    while (true) {
                        Printer.printPrompt("Choose one of the following ");
                        String s = "| ";
                        for (String type : Storage.getStorageTypes().keySet()) {
                            s += type + " | ";
                        }
                        Printer.printPrompt(s);
                        input = in.next();
                        if (input.equalsIgnoreCase("back") || input.equalsIgnoreCase("b")) {
                            break;
                        }
                        for (String type : Storage.getStorageTypes().keySet()) {
                            if (input.equals(type)) {
                                Storage.getSettings().setStorageType(type);
                                Printer.printDataChange(pName, "Storage type has been changed to \"" + type + "\".");
                                break out;
                            }
                        }
                        Printer.printFailedReply("\"" + input + "\" is not a valid option.");
                    }
                    break;
                case "default":
                case "d":
                    Storage.getSettings().saveDefault();

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
                    Printer.printItem("1-10", "Changes the setting corresponding to that number according to \"info\".");
                    Printer.printItem("default(d)", "Resets all settings to their default values.");
                    Printer.printItem("back(b)", "Goes back to the main menu.");
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
            if (input.equalsIgnoreCase("back") || input.equalsIgnoreCase("b")) {
                return;
            }
            if (input.equals(Storage.getGlobalServer().getName())) {
                Printer.printFailedReply(pName, "The name \"" + Storage.getGlobalServer().getName() + "\" is reserved to refer to the global panel.");
                continue;
            }
            boolean available = true;
            synchronized (serverList) {
                for (String s : serverList.keySet()) {
                    if (s.equals(input)) {
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
                if (input.equalsIgnoreCase("back") || input.equalsIgnoreCase("b")) {
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

                        Printer.printDataChange(pName, "Server \"" + name + "\" has been added successfully.");
                        serverFileCommunicatorTask.updateServerMonitorData(server);
                        return;
                    } else {
                        Printer.printFailedReply(pName, "The path \"" + input + "\" is not valid.");
                    }
                } catch (SecurityException e) {
                    Printer.printFailedReply(pName, "The program was denied access to the file");
                }
            }
        }
    }

    public static void removeServer() {
        String pName = "RemoveServer";
        while (true) {
            Printer.printPrompt(pName, "Enter the server name that you wish to remove:");
            String input = in.next();
            if (input.equalsIgnoreCase("back") || input.equalsIgnoreCase("b")) {
                return;
            }
            boolean removed = false;
            HashMap<String, Server> serverList = Storage.getServerList();
            synchronized (serverList) {
                for (String s : serverList.keySet()) {
                    if (s.equals(input)) {
                        serverList.remove(s);

                        removed = true;
                        break;
                    }
                }
            }
            if (removed) {

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
            if (input.equalsIgnoreCase("back") || input.equalsIgnoreCase("b")) {
                return;
            }
            if (input.equals(Storage.getGlobalServer().getName())) {
                serverMenu(Storage.getGlobalServer());
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
            Printer.printFailedReply(pName, "Server \"" + input + "\" does not exist.");
        }
    }

    public static void serverMenu(Server server) {
        String pName = server.getName() + " Control Panel";
        while (true) {
            changeMenu(pName, "SERVER");
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
                case "backup":
                    backupServer(server);
                    break;
                case "settings":
                case "st":
                    serverSettingsMenu(server);
                    break;
                case "info":
                case "i":
                    server.printInfo(3);
                    break;
                case "help":
                case "h":
                    Printer.printSubTitle("Server Menu Commands");
                    Printer.printItem("start", "Atempts to start the server if its offline.");
                    Printer.printItem("stop", "Atempts to stop the server gracefully if its online, or kills it if its not responding..");
                    Printer.printItem("restart", "Calls the stop then start commands to restart..");
                    Printer.printItem("kill", "Forcefully shuts down the server's process.");
                    Printer.printItem("send", "Sends a command to the server.");
                    Printer.printItem("timedcommands(tc)", "Opens up the timed commands editor for this server.");
                    Printer.printItem("backup", "follow by a backup profile's name to backup the server or type nothing to preform a default backup.");
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
        boolean isGlobal = server instanceof GlobalServer;
        if (isGlobal && Storage.getServerList().isEmpty()) {
            Printer.printFailedReply("The program has no servers to control global settings.");
            return;
        }
        String pName = server.getName() + " Settings";
        while (true) {
            boolean printSettings = activeMenu == null || !activeMenu.getK().equals(pName);
            if (isGlobal) {
                changeMenu(pName, "GLOBAL_SERVER_SETTINGS");
            } else {
                changeMenu(pName, "SERVER_SETTINGS");
            }
            if (printSettings) {
                server.getSettings().printSettings(server.getName());
            }
            String input = in.next().toLowerCase();
            switch (input) {
                case "rename":
                case "rn":
                    if (isGlobal) {
                        Printer.printFailedReply(pName, "\"" + input + "\" was not identified. Type help to list available commands.");
                        break;
                    }
                    if (server.getState() != ServerState.OFFLINE) {
                        Printer.printFailedReply(pName, "The server must be offline to be renamed.");
                        break;
                    }
                    while (true) {
                        Printer.printPrompt(pName, "Enter a new unique server name.\nCurrent name: " + server.getName());
                        input = in.next();
                        if (input.equalsIgnoreCase("back") || input.equalsIgnoreCase("b")) {
                            break;
                        }
                        if (input.equals(server.getName())) {
                            Printer.printFailedReply(pName, "You can't choose the same name. type \"back\" to cancel renaming.");
                            continue;
                        }
                        if (input.equals(Storage.getGlobalServer().getName())) {
                            Printer.printFailedReply(pName, "The name \"" + Storage.getGlobalServer().getName() + "\" is reserved to refer to the global panel.");
                            continue;
                        }
                        boolean available = true;
                        synchronized (Storage.getServerList()) {
                            for (String s : Storage.getServerList().keySet()) {
                                if (s.equals(input)) {
                                    available = false;
                                    break;
                                }
                            }
                        }
                        if (!available) {
                            Printer.printFailedReply(pName, "That name is already taken!");
                            continue;
                        }
                        String oldName = server.getName();
                        synchronized (Storage.getServerList()) {
                            Storage.getServerList().remove(server.getName());
                            server.setName(input);
                            Storage.getServerList().put(server.getName(), server);
                        }
                        Printer.printDataChange(pName, "The server \"" + oldName + "\" has been renamed to \"" + input + "\".");
                        break;
                    }
                    break;
                case "path":
                case "p":
                    if (isGlobal) {
                        Printer.printFailedReply(pName, "\"" + input + "\" was not identified. Type help to list available commands.");
                        break;
                    }
                    if (server.getState() != ServerState.OFFLINE) {
                        Printer.printFailedReply(pName, "The server must be offline to change the jar path.");
                        break;
                    }
                    while (true) {
                        Printer.printPrompt(pName, "Enter the new jar path.\nCurrent jar path: " + server.getFile().getPath());
                        input = in.next();
                        if (input.equalsIgnoreCase("back") || input.equalsIgnoreCase("b")) {
                            break;
                        }
                        File file = new File(input);
                        try {
                            if (input.endsWith(".jar") && file.isFile()) {
                                synchronized (Storage.getServerList()) {
                                    server.setFile(file);
                                }
                                Printer.printDataChange(pName, "The jar file for the server \"" + server.getName() + "\" has been changed to \"" + input + "\".");
                                break;
                            } else {
                                Printer.printFailedReply(pName, "The path \"" + input + "\" is not valid.");
                            }
                        } catch (SecurityException e) {
                            Printer.printFailedReply(pName, "The program was denied access to the file");
                        }
                    }
                    break;
                case "link":
                    if (!isGlobal && server.isLinked()) {
                        Printer.printFailedReply(pName, "server is already linked.");
                        break;
                    }
                    Printer.printBackgroundInfo(pName, "Attempting to link to a monitor file.");
                    server.link();
                    break;
                case "unlink":
                    if (!isGlobal && !server.isLinked()) {
                        Printer.printFailedReply(pName, "server is already not linked.");
                        break;
                    }
                    server.unlink();
                    break;
                case "1":
                    while (true) {
                        Printer.printPrompt(pName, "Enter the new starting ram:");
                        input = in.next();
                        if (input.equalsIgnoreCase("back") || input.equalsIgnoreCase("b")) {
                            break;
                        }
                        if (input.equals("null")) {
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
                        if (input.equalsIgnoreCase("back") || input.equalsIgnoreCase("b")) {
                            break;
                        }
                        if (input.equals("null")) {
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
                        if (input.equalsIgnoreCase("back") || input.equalsIgnoreCase("b")) {
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
                        if (input.equalsIgnoreCase("back") || input.equalsIgnoreCase("b")) {
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
                    Printer.printPrompt("Enter the new stop command: ");
                    input = in.nextLine();
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
                    if (!isGlobal) {
                        Printer.printItem("rename(rn)", "Attempts to rename the server. (Server must be offline)");
                        Printer.printItem("path(p)", "Attempts to change the server's jar path. (Server must be offline)");
                    }
                    Printer.printItem("link", "Attempts to link the server to a monitor file in the communications directory."
                            + "\nFor more information on how this is done and what it means, "
                            + "\nvisit the wiki at www.github.placeholder.com");
                    Printer.printItem("unlink", "Changes the server to an unlinked state.");
                    Printer.printItem("1-6", "changes the setting corresponding to that number according to \"info\".");
                    Printer.printItem("info(i)", "displays current server settings.");
                    Printer.printItem("back(b)", "goes back to the server menu.");
                    break;
                default:
                    Printer.printFailedReply(pName, "\"" + input + "\" was not identified. Type help to list available commands.");
            }
        }
    }

    public static void timedCommandsMenu(Server server) {
        String pName = server.getName() + " Timed Commands";
        while (true) {
            changeMenu(pName, "TIMED_COMMANDS");
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
                    Printer.printItem("add", "adds a timed command to the server.");
                    Printer.printItem("remove(rm)", "removes a timed command from the server.");
                    Printer.printItem("list(ls)", "lists all timed commands in the server.");
                    Printer.printItem("back(b)", "Goes back to the server menu.");
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
            if (input.equalsIgnoreCase("back") || input.equalsIgnoreCase("b")) {
                return;
            }
            if (input.equals("help") || input.equals("h")) {
                Printer.printSubTitle("Acceptable Timed Commands");
                Printer.printItem("start", "Atempts to start the server if its offline.");
                Printer.printItem("stop", "Atempts to stop the server gracefully if its online, or kills it if its not responding..");
                Printer.printItem("restart", "Calls the stop then start commands to restart..");
                Printer.printItem("kill", "Forcefully shuts down the server's process.");
                Printer.printItem("send", "Sends a command to the server.");
                Printer.printItem("backup", "Attempts to backup the server.");
                continue;
            }
            if (TimedCommand.isValidCommand(input)) {
                String cmd = input;
                if (input.equals("send")) {
                    Printer.printPrompt("Enter the command to send to the server:");
                    cmd += " " + in.nextLine();
                } else if (input.equals("backup")) {
                    if (in.hasNext()) {
                        String bpName = in.next();
                        BackupProfile bp = Storage.getBackupProfileList().get(bpName);
                        if (bp == null) {
                            Printer.printFailedReply(pName,
                                    "The backup profile \"" + bpName + "\" does not exist. Choose one of the following:\n"
                                    + Utilities.listArgs(new ArrayList(Storage.getBackupProfileList().keySet()), ", ")
                                    + "\nor leave blank for a default backup.");
                            continue;
                        }
                        cmd += " " + bpName;
                    }
                }
                while (true) {
                    Printer.printPrompt(pName, "Enter the time to execute:");
                    input = in.next();
                    if (input.equalsIgnoreCase("back") || input.equalsIgnoreCase("b")) {
                        return;
                    }
                    if (input.equalsIgnoreCase("help") || input.equalsIgnoreCase("h")) {
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
            if (input.equalsIgnoreCase("back") || input.equalsIgnoreCase("b")) {
                return;
            }
            if (input.equals("help") || input.equals("h")) {
                Printer.printSuccessfullReply(pName, "Use the command \"back\" to go back to the server's menu.");
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
                    String s = server.getTimedCommands().get(i).getCommand();
                    server.removeTimedCommand(i);
                    Printer.printDataChange(pName, "The timed command \"" + s + "\" has been removed.");
                    break;
                } catch (IndexOutOfBoundsException e) {
                    Printer.printFailedReply(pName, "The index must be from 0 to " + (server.timedCommandsSize() - 1) + " inclusive.");
                }
            } else {
                Printer.printFailedReply(pName, "\"" + input + "\" is not a number.");
            }
        }
    }

    public static void backupServer(Server s) {
        String pName = s.getName() + "-BackupServer";
        if (!in.hasNext()) {
            s.backup(new BackupProfile("default", null));
            return;
        }
        String input = in.next();
        BackupProfile bp = Storage.getBackupProfileList().get(input);
        if (bp != null) {
            s.backup(bp);
        } else {
            Printer.printFailedReply(pName,
                    "The backup profile \"" + input + "\" does not exist. Choose one of the following:\n"
                    + Utilities.listArgs(new ArrayList(Storage.getBackupProfileList().keySet()), ", ")
                    + "\nor leave blank for a default backup.");
        }
    }

    public static void backupProfilesMenu() {
        String pName = "BackupProfilesMenu";
        while (true) {
            changeMenu(pName, "BACKUP_PROFILES");
            String input = in.next();
            switch (input.toLowerCase()) {
                case "open":
                case "o":
                    openBackupProfile();
                    break;
                case "add":
                    addBackupProfile();
                    break;
                case "remove":
                case "rm":
                    if (!Storage.getBackupProfileList().isEmpty()) {
                        removeBackupProfile();
                    } else {
                        Printer.printFailedReply(pName, "The program has no backup profiles.");
                    }
                    break;
                case "list":
                case "ls":
                    synchronized (Storage.getBackupProfileList()) {
                        if (Storage.getBackupProfileList().isEmpty()) {
                            Printer.printFailedReply(pName, "The program has no backup profiles.");
                            break;
                        }
                        Printer.printSubTitle("(" + Storage.getBackupProfileList().values().size() + ") Saved backup profiles: ");
                        for (BackupProfile bp : Storage.getBackupProfileList().values()) {
                            bp.printInfo();
                        }
                    }
                    break;
                case "help":
                case "h":
                    Printer.printSubTitle("Backup Profiles Menu Commands");
                    Printer.printItem("open(o)", "Opens up the specified backup profile's editor menu.");
                    Printer.printItem("list(ls)", "Lists existing backup profiles.");
                    Printer.printItem("add", "Atempts to add a new backup profile to the manager.");
                    Printer.printItem("remove(rm)", "Atempts to remove an existing backup profile from the manager.");
                    Printer.printItem("back(b)", "Goes back to the main menu.");
                    Printer.printItem("quit", "Exits the program.");

                    break;
                case "back":
                case "b":
                    return;
                default:
                    Printer.printFailedReply(pName, "\"" + input + "\" is not a valid command. Type help to see acceptable commands.");
            }
        }
    }

    public static void openBackupProfile() {
        String pName = "OpenBackupProfile";
        while (true) {
            Printer.printPrompt(pName, "Enter the backup profile you wish to open:");
            String input = in.next();
            if (input.equalsIgnoreCase("back") || input.equalsIgnoreCase("b")) {
                return;
            }
            BackupProfile bp;
            synchronized (Storage.getBackupProfileList()) {
                bp = Storage.getBackupProfileList().get(input);
            }
            if (bp != null) {
                backupProfileEditorMenu(bp);
                return;
            }
            Printer.printFailedReply(pName, "Backup profile \"" + input + "\" does not exist.");
        }
    }

    public static void addBackupProfile() {
        String pName = "AddBackupProfile";
        main:
        while (true) {
            Printer.printPrompt(pName, "Enter the name of the new Backup Profile.");
            String input = in.next();
            if (input.equalsIgnoreCase("back") || input.equalsIgnoreCase("b")) {
                return;
            }
            synchronized (Storage.getBackupProfileList()) {
                for (String name : Storage.getBackupProfileList().keySet()) {
                    if (name.equals(input)) {
                        Printer.printFailedReply(pName, "The backup profile name \"" + input + "\" already exists.");
                        continue main;
                    }
                }
            }
            synchronized (Storage.getBackupProfileList()) {
                Storage.getBackupProfileList().put(input, new BackupProfile(input, null));
                Printer.printDataChange(pName, "The backup profile \"" + input + "\" has been created.");
                return;
            }
        }
    }

    public static void removeBackupProfile() {
        String pName = "RemoveBackupProfile";
        while (true) {
            Printer.printPrompt(pName, "Enter the name of the Backup Profile you want to remove.");
            String input = in.next();
            if (input.equalsIgnoreCase("back") || input.equalsIgnoreCase("b")) {
                return;
            }
            synchronized (Storage.getBackupProfileList()) {
                if (Storage.getBackupProfileList().containsKey(input)) {
                    Storage.getBackupProfileList().remove(input);
                    Printer.printDataChange(pName, "The backup profile \"" + input + "\" has been removed.");
                    return;
                } else {
                    Printer.printFailedReply(pName, "\"" + input + "\" does not exist.");
                }
            }
        }
    }

    public static void backupProfileEditorMenu(BackupProfile bp) {
        String pName = bp.getName() + "-BackupProfileEditorMenu";
        while (true) {
            changeMenu(pName, "BACKUP_PROFILE_EDITOR");
            String input = in.next();
            switch (input) {
                case "info":
                case "i":
                    bp.printInfo();
                    break;
                case "save-directory":
                case "dir":
                case "directory":
                case "sd":
                    while (true) {
                        Printer.printPrompt(pName, "Enter the directory where you wish to save backups of this type.\nOr type null to use the server's directory.");
                        input = in.nextLine();
                        if (input.equalsIgnoreCase("back") || input.equalsIgnoreCase("b")) {
                            break;
                        }
                        if (input.equalsIgnoreCase("null")) {
                            bp.setDir(null);
                            Printer.printDataChange(pName, "The save directory has been changed to \"null\".");
                        }
                        File f = new File(input);
                        if (f.isFile()) {
                            Printer.printFailedReply(pName, "\"" + input + "\" is not a valid directory.");
                        } else {
                            if (!f.exists()) {
                                Printer.printSuccessfullReply(pName, "The directory \"" + input + "\" does not exist. Creating it...");
                                f.mkdirs();
                            }
                            bp.setDir(f);
                            Printer.printDataChange(pName, "The save directory has been changed to \"" + input + "\".");
                            break;
                        }
                        Printer.printFailedReply(pName, "\"" + input + "\" is not a valid directory.");
                    }
                    break;
                case "exclude":
                case "ex":
                    StringListEditor(bp.getName() + " Exclude Arguments List", bp.getExcludeList());
                    break;
                case "include":
                case "in":
                    StringListEditor(bp.getName() + " Include Arguments List", bp.getIncludeList());
                    break;
                case "help":
                case "h":
                    Printer.printSubTitle("Backup Profile Editor Menu Commands");
                    Printer.printItem("info(i)", "Prints out backup profile's info.");
                    Printer.printItem("exclude(ex)", "Enters the exclude arguments list editor.");
                    Printer.printItem("include(in)", "Enters the include arguments list editor.");
                    Printer.printCustom("$$WFor more information on how to include and exclude visit the program's github wiki.");
                    Printer.printItem("back(b)", "Goes back to the backup profiles menu.");
                    Printer.printItem("quit", "Exits the program.");
                    break;
                case "back":
                case "b":
                    return;
                default:
                    Printer.printFailedReply(pName, "\"" + input + "\" is not a valid command. Type help to see acceptable commands.");
            }
        }
    }

    public static void StringListEditor(String pName, List<String> ls) {
        while (true) {
            boolean printItems = activeMenu == null || !activeMenu.getK().equals(pName);
            changeMenu(pName + " Editor", "STRING_LIST_EDITOR");
            if (printItems) {
                for (int i = 0; i < ls.size(); i++) {
                    Printer.printItem(String.valueOf(i), ls.get(i));
                }
            }
            String input = in.next();
            switch (input.toLowerCase()) {
                case "list":
                case "ls":
                    synchronized (Storage.getBackupProfileList()) {
                        if (ls.isEmpty()) {
                            Printer.printFailedReply(pName, "There are no arguments.");
                            break;
                        }
                        Printer.printSubTitle("(" + ls.size() + ") Saved arguments: ");
                        for (int i = 0; i < ls.size(); i++) {
                            Printer.printItem(String.valueOf(i), ls.get(i));
                        }
                    }
                    break;
                case "add":
                    addToStringList(pName, ls);
                    break;
                case "remove":
                case "rm":
                    if (ls.isEmpty()) {
                        Printer.printFailedReply(pName, pName + " has no arguments to remove.");
                    } else {
                        removeFromStringList(pName, ls);
                    }
                    break;
                case "help":
                case "h":
                    Printer.printSubTitle(pName + " Editor Menu Commands");
                    Printer.printItem("list(ls)", "Prints out the existing arguments.");
                    Printer.printItem("add", "Attempts to add a new argument to the list.");
                    Printer.printItem("remove(rm)", "Attempts to remove an existing argument from the list.");
                    Printer.printItem("back(b)", "Goes back to the backup profile editor menu.");
                    Printer.printItem("quit", "Exits the program.");
                    break;
                case "back":
                case "b":
                    return;
                default:
                    Printer.printFailedReply(pName, "\"" + input + "\" is not a valid command. Type help to see acceptable commands.");
            }
        }
    }

    public static void addToStringList(String pName, List<String> ls) {
        Printer.printPrompt(pName, "Enter the argument to add");
        String input = in.nextLine();
        if (!(input.equalsIgnoreCase("back") || input.equalsIgnoreCase("b"))) {
            ls.add(input);
            Printer.printDataChange(pName + "Editor", "\"" + input + "\" has been added to the " + pName);
        }
    }

    public static void removeFromStringList(String pName, List<String> ls) {
        while (true) {
            Printer.printPrompt(pName, "Enter an index from 0 to " + (ls.size() - 1) + " to remove.");
            String input = in.next();
            if (input.equalsIgnoreCase("back") || input.equalsIgnoreCase("b")) {
                return;
            }
            if (!Utilities.isInteger(input)) {
                Printer.printFailedReply(pName, "The index must be an integer.");
                continue;
            }
            int i = Integer.parseInt(input);
            if (!(i >= 0 && i < ls.size())) {
                Printer.printFailedReply(pName, "The index is not in bounds.");
                continue;
            }
            String s = ls.get(i);
            ls.remove(i);
            Printer.printDataChange(pName, "The argument \"" + s + "\" has been removed.");
            return;
        }
    }

    public static void monitorScreen() {
        if (monitorTask == null) {
            activeMenu = null;
            boolean detail = false;
            String input = "";
            if (in.hasNextLine()) {
                input = in.nextLine();
            }
            int lvl = 2;

            if (input.isEmpty()) {
                monitorTask = new MonitorTask(Storage.getSettings().getMonitorRefreshRate(), lvl, new ArrayList());
            } else {
                List<String> args = Utilities.seperateArgs(input, " ");
                if (Utilities.isInteger(args.get(0))) {
                    lvl = Integer.parseInt(args.get(0));
                    args.remove(0);
                }
                monitorTask = new MonitorTask(Storage.getSettings().getMonitorRefreshRate(), lvl, args);
            }
            monitorTask.setName("Monitor");
            monitorTask.start();
            in.clear();
            in.waitForEnter();
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
