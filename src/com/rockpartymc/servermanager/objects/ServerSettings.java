package com.rockpartymc.servermanager.objects;

import com.rockpartymc.servermanager.consolecommunication.Printer;
import com.rockpartymc.servermanager.storage.Storage;
import java.io.Serializable;

public class ServerSettings implements Serializable{
    
    private String stopCommand;
    private String startRam;
    private String maxRam;
    private boolean startIfOffline;
    private boolean restartIfNotResponding;

    private long maxStartingDuration;
    private long maxStoppingDuration;

    public ServerSettings()
    {
        maxStartingDuration = 90000;
        maxStoppingDuration = 30000;
        stopCommand = "stop";
    }
    public String getStopCommand() {
        return stopCommand;
    }

    public void setStopCommand(String stopCommand) {
        this.stopCommand = stopCommand;
        Storage.saveDataToFile();
    }
    public long getMaxStartingDuration() {
        return maxStartingDuration;
    }

    public void setMaxStartingDuration(Long maxStartingDuration) {
        this.maxStartingDuration = maxStartingDuration;
        Storage.saveDataToFile();
    }

    public long getMaxStoppingDuration() {
        return maxStoppingDuration;
    }

    public void setMaxStoppingDuration(Long maxStoppingDuration) {
        this.maxStoppingDuration = maxStoppingDuration;
        Storage.saveDataToFile();
    }
    
    public String getStartRam() {
        return startRam;
    }

    public void setStartRam(String startRam) {
        this.startRam = startRam;
        Storage.saveDataToFile();
    }

    public String getMaxRam() {
        return maxRam;
    }

    public void setMaxRam(String maxRam) {
        this.maxRam = maxRam;
        Storage.saveDataToFile();
    }

    public boolean isStartIfOffline() {
        return startIfOffline;
    }

    public void setStartIfOffline(boolean startIfOffline) {
        this.startIfOffline = startIfOffline;
        Storage.saveDataToFile();
    }
    
    public void toggleStartIfOffline() {
        startIfOffline = !startIfOffline;
        Storage.saveDataToFile();
    }
    
    public boolean isRestartIfNotResponding() {
        return restartIfNotResponding;
    }

    public void setRestartIfNotResponding(boolean restartIfNotResponding) {
        this.restartIfNotResponding = restartIfNotResponding;
        Storage.saveDataToFile();
    }
    public void toggleRestartIfNotResponding() {
        restartIfNotResponding = !restartIfNotResponding;
        Storage.saveDataToFile();
    }
    public void printSettings(String server)
    {
        Printer.printSubTitle("\"" + server + "\" Settings");
        Printer.printItem("(1) Start RAM", startRam + "");
        Printer.printItem("(2) Max RAM", maxRam + "");
        Printer.printItem("(3) Start if offline", startIfOffline + "");
        Printer.printItem("(4) Restart if not responding", restartIfNotResponding + "");
        Printer.printItem("(5) Max starting duration", maxStartingDuration + "");
        Printer.printItem("(6) Max stoping duration", maxStoppingDuration + "");
        Printer.printItem("(7) Stop command", stopCommand + "");
    }
}
