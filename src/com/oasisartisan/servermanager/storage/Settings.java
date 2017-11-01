package com.oasisartisan.servermanager.storage;

import com.oasisartisan.servermanager.consolecommunication.Printer;
import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author OasisArtisan
 */
public final class Settings implements Serializable {

    private File communicationDir;

    private long serverStateUpdaterTaskInterval;
    private long monitorRefreshRate;
    private long monitorMessagesDuration;
    private long commandSchedulerTaskInterval;

    private boolean useConsoleColors;
    private boolean clearConsoleBeforeMenu;
    private boolean logOutput;
    private boolean printBackgroundInfoToConsole;
    private boolean backgroundInfoTimeStampsInConsole;

    private String storageType;

    public Settings() {
        saveDefault();
    }

    public File getCommunicationDir() {
        return communicationDir;
    }

    public void setCommunicationDir(File communicationDir) {
        this.communicationDir = communicationDir;
    }

    public long getServerStateUpdaterTaskInterval() {
        return serverStateUpdaterTaskInterval;
    }

    public void setServerStateUpdaterTaskInterval(long serverStateUpdaterTaskInterval) {
        this.serverStateUpdaterTaskInterval = serverStateUpdaterTaskInterval;
    }

    public long getMonitorRefreshRate() {
        return monitorRefreshRate;
    }

    public void setMonitorRefreshRate(long monitorRefreshRate) {
        this.monitorRefreshRate = monitorRefreshRate;

    }

    public long getMonitorMessagesDuration() {
        return monitorMessagesDuration;
    }

    public void setMonitorMessagesDuration(long monitorMessagesDuration) {
        this.monitorMessagesDuration = monitorMessagesDuration;
    }

    public long getCommandSchedulerTaskInterval() {
        return commandSchedulerTaskInterval;
    }

    public void setCommandSchedulerTaskInterval(long commandSchedulerTaskInterval) {
        this.commandSchedulerTaskInterval = commandSchedulerTaskInterval;
    }

    public boolean isUseConsoleColors() {
        return useConsoleColors;
    }

    public void setUseConsoleColors(boolean useConsoleColors) {
        this.useConsoleColors = useConsoleColors;
    }

    public void toggleUseConsoleColors() {
        this.useConsoleColors = !this.useConsoleColors;
    }

    public boolean isClearConsoleBeforeMenu() {
        return clearConsoleBeforeMenu;
    }

    public void setClearConsoleBeforeMenu(boolean clearConsoleBeforeMenu) {
        this.clearConsoleBeforeMenu = clearConsoleBeforeMenu;

    }

    public void toggleClearConsoleBeforeMenu() {
        this.clearConsoleBeforeMenu = !this.clearConsoleBeforeMenu;

    }

    public boolean isLogOutput() {
        return logOutput;
    }

    public void setLogOutput(boolean logOutput) {
        this.logOutput = logOutput;

    }

    public void toggleLogOutput() {
        this.logOutput = !this.logOutput;

    }

    public boolean isPrintBackgroundInfoToConsole() {
        return printBackgroundInfoToConsole;
    }

    public void setPrintBackgroundInfoToConsole(boolean printBackgroundInfoToConsole) {
        this.printBackgroundInfoToConsole = printBackgroundInfoToConsole;

    }

    public void togglePrintBackgroundInfoToConsole() {
        this.printBackgroundInfoToConsole = !this.printBackgroundInfoToConsole;

    }

    public boolean isBackgroundInfoTimeStampsInConsole() {
        return backgroundInfoTimeStampsInConsole;
    }

    public void setBackgroundInfoTimeStampsInConsole(boolean backgroundInfoTimeStampsInConsole) {
        this.backgroundInfoTimeStampsInConsole = backgroundInfoTimeStampsInConsole;

    }

    public void toggleBackgroundInfoTimeStampsInConsole() {
        this.backgroundInfoTimeStampsInConsole = !this.backgroundInfoTimeStampsInConsole;

    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public void printSettings() {
        Printer.printSubTitle("Program Settings");
        Printer.printItem("(1) Communication directory", communicationDir.getPath() + "");
        Printer.printItem("(2) Server state updater task interval", serverStateUpdaterTaskInterval + "");
        Printer.printItem("(3) Monitor refresh rate", monitorRefreshRate + "");
        Printer.printItem("(4) Monitor Messages Duration", monitorMessagesDuration + "");
        Printer.printItem("(5) Command scheduler task interval", commandSchedulerTaskInterval + "");
        Printer.printItem("(6) Use colors in console", useConsoleColors + "");
        Printer.printItem("(7) Clear console when openning a new menu", clearConsoleBeforeMenu + "");
        Printer.printItem("(8) Log output", logOutput + "");
        Printer.printItem("(9) Print background info to console", printBackgroundInfoToConsole + "");
        Printer.printItem("(10) Print time stamps on background info in console", backgroundInfoTimeStampsInConsole + "");
        Printer.printItem("(11) Storage type", storageType);
    }

    public void saveDefault() {
        communicationDir = new File("SMMonitorData");
        if (!communicationDir.isDirectory()) {
            Printer.printFailedReply("The directory for communication does not exist. creating it.");
            try {
                if (!communicationDir.mkdirs()) {
                    Printer.printError("An error occured while trying to create the communication directory.", null);
                }
            } catch (Exception e) {
                Printer.printError("An exception occured while trying to create the communication directory.", e);

            }
        }
        serverStateUpdaterTaskInterval = 10000;
        monitorRefreshRate = 3000;
        monitorMessagesDuration = 30000;
        commandSchedulerTaskInterval = 1800000;
        useConsoleColors = true;
        clearConsoleBeforeMenu = true;
        logOutput = true;
        printBackgroundInfoToConsole = true;
        backgroundInfoTimeStampsInConsole = true;
        storageType = "sqlite";
    }

    public void saveFromList(List<Object> ls) {

    }
}
