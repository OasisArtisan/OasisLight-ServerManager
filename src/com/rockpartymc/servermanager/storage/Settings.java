package com.rockpartymc.servermanager.storage;

import com.rockpartymc.servermanager.consolecommunication.Printer;
import com.rockpartymc.servermanager.processhandlers.BashProcessHandler;
import com.rockpartymc.servermanager.processhandlers.ProcessHandler;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 *
 * @author OmarAlama
 */
public final class Settings implements Serializable {

    private ProcessHandler processHandler;

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

    public Settings() {
        saveDefault();
    }

    public ProcessHandler getProcessHandler() {
        return processHandler;
    }

    public void setProcessHandler(ProcessHandler processHandler) {
        this.processHandler = processHandler;
        Storage.saveDataToFile();
    }

    public File getCommunicationDir() {
        return communicationDir;
    }

    public void setCommunicationDir(File communicationDir) {
        this.communicationDir = communicationDir;
        Storage.saveDataToFile();
    }

    public long getServerStateUpdaterTaskInterval() {
        return serverStateUpdaterTaskInterval;
    }

    public void setServerStateUpdaterTaskInterval(long serverStateUpdaterTaskInterval) {
        this.serverStateUpdaterTaskInterval = serverStateUpdaterTaskInterval;
        Storage.saveDataToFile();
    }

    public long getMonitorRefreshRate() {
        return monitorRefreshRate;
    }

    public void setMonitorRefreshRate(long monitorRefreshRate) {
        this.monitorRefreshRate = monitorRefreshRate;
        Storage.saveDataToFile();
    }
    public long getMonitorMessagesDuration() {
        return monitorMessagesDuration;
    }

    public void setMonitorMessagesDuration(long monitorMessagesDuration) {
        this.monitorMessagesDuration = monitorMessagesDuration;
        Storage.saveDataToFile();
    }
    public long getCommandSchedulerTaskInterval() {
        return commandSchedulerTaskInterval;
    }

    public void setCommandSchedulerTaskInterval(long commandSchedulerTaskInterval) {
        this.commandSchedulerTaskInterval = commandSchedulerTaskInterval;
        Storage.saveDataToFile();
    }

    public boolean isUseConsoleColors() {
        return useConsoleColors;
    }

    public void setUseConsoleColors(boolean useConsoleColors) {
        this.useConsoleColors = useConsoleColors;
        Storage.saveDataToFile();
    }

    public void toggleUseConsoleColors() {
        this.useConsoleColors = !this.useConsoleColors;
        Storage.saveDataToFile();
    }

    public boolean isClearConsoleBeforeMenu() {
        return clearConsoleBeforeMenu;
    }

    public void setClearConsoleBeforeMenu(boolean clearConsoleBeforeMenu) {
        this.clearConsoleBeforeMenu = clearConsoleBeforeMenu;
        Storage.saveDataToFile();
    }

    public void toggleClearConsoleBeforeMenu() {
        this.clearConsoleBeforeMenu = !this.clearConsoleBeforeMenu;
        Storage.saveDataToFile();
    }

    public boolean isLogOutput() {
        return logOutput;
    }

    public void setLogOutput(boolean logOutput) {
        this.logOutput = logOutput;
        Storage.saveDataToFile();
    }

    public void toggleLogOutput() {
        this.logOutput = !this.logOutput;
        Storage.saveDataToFile();
    }

    public boolean isPrintBackgroundInfoToConsole() {
        return printBackgroundInfoToConsole;
    }

    public void setPrintBackgroundInfoToConsole(boolean printBackgroundInfoToConsole) {
        this.printBackgroundInfoToConsole = printBackgroundInfoToConsole;
        Storage.saveDataToFile();
    }

    public void togglePrintBackgroundInfoToConsole() {
        this.printBackgroundInfoToConsole = !this.printBackgroundInfoToConsole;
        Storage.saveDataToFile();
    }

    public boolean isBackgroundInfoTimeStampsInConsole() {
        return backgroundInfoTimeStampsInConsole;
    }

    public void setBackgroundInfoTimeStampsInConsole(boolean backgroundInfoTimeStampsInConsole) {
        this.backgroundInfoTimeStampsInConsole = backgroundInfoTimeStampsInConsole;
        Storage.saveDataToFile();
    }

    public void toggleBackgroundInfoTimeStampsInConsole() {
        this.backgroundInfoTimeStampsInConsole = !this.backgroundInfoTimeStampsInConsole;
        Storage.saveDataToFile();
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
        Printer.printItem("(8) Log output: ", logOutput + "");
        Printer.printItem("(9) Print background info to console", printBackgroundInfoToConsole + "");
        Printer.printItem("(10) Print time stamps on background info in console", backgroundInfoTimeStampsInConsole + "");
    }

    public void saveDefault() {
        processHandler = new BashProcessHandler();
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
    }
}
