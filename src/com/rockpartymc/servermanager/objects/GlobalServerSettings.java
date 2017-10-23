/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rockpartymc.servermanager.objects;

import com.rockpartymc.servermanager.consolecommunication.Printer;
import com.rockpartymc.servermanager.storage.Storage;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author GIGABYTE-S
 */
public class GlobalServerSettings extends ServerSettings{

    @Override
    public void setStopCommand(String stopCommand) {
        synchronized(Storage.getServerList())
        {
            for(Server s : Storage.getServerList().values())
            {
                s.getSettings().setStopCommand(stopCommand);
            }
        }
        
    }
    @Override
    public void setMaxStartingDuration(Long maxStartingDuration) {
        synchronized(Storage.getServerList())
        {
            for(Server s : Storage.getServerList().values())
            {
                s.getSettings().setMaxStartingDuration(maxStartingDuration);
            }
        }
    }

    @Override
    public void setMaxStoppingDuration(Long maxStoppingDuration) {
        synchronized(Storage.getServerList())
        {
            for(Server s : Storage.getServerList().values())
            {
                s.getSettings().setMaxStoppingDuration(maxStoppingDuration);
            }
        }
        
    }

    @Override
    public void setStartRam(String startRam) {
        synchronized(Storage.getServerList())
        {
            for(Server s : Storage.getServerList().values())
            {
                s.getSettings().setStartRam(startRam);
            }
        }
        
    }

    @Override
    public void setMaxRam(String maxRam) {
        synchronized(Storage.getServerList())
        {
            for(Server s : Storage.getServerList().values())
            {
                s.getSettings().setMaxRam(maxRam);
            }
        }
        
    }

    @Override
    public void setStartIfOffline(boolean startIfOffline) {
        synchronized(Storage.getServerList())
        {
            for(Server s : Storage.getServerList().values())
            {
                s.getSettings().setStartIfOffline(startIfOffline);
            }
        }
        
    }

    @Override
    public void setRestartIfNotResponding(boolean restartIfNotResponding) {
        synchronized(Storage.getServerList())
        {
            for(Server s : Storage.getServerList().values())
            {
                s.getSettings().setRestartIfNotResponding(restartIfNotResponding);
            }
        }

    }

    @Override
    public void printSettings(String server) {
        if(Storage.getServerList().isEmpty())
        {
            Printer.printFailedReply("The program has no servers to display global settings.");
            return;
        }
        String[] ls = new String[7];
        boolean[] lsb = new boolean[7];
        synchronized (Storage.getServerList()) {
            Server s = Storage.getServerList().values().iterator().next();
            ls[0] = s.getSettings().getStartRam() + "";
            ls[1] = s.getSettings().getMaxRam() + "";
            ls[2] = s.getSettings().isStartIfOffline() + "";
            ls[3] = s.getSettings().isRestartIfNotResponding() + "";
            ls[4] = s.getSettings().getMaxStartingDuration() + "";
            ls[5] = s.getSettings().getMaxStoppingDuration() + "";
            ls[6] = s.getSettings().getStopCommand() + "";
            boolean skip = true;
            for (Server se : Storage.getServerList().values()) {
                if(skip)
                {
                    skip = false;
                    continue;
                }
                if(!lsb[0] && !ls[0].equals(se.getSettings().getStartRam() + ""))
                {
                    lsb[0] = true;
                }
                if(!lsb[1] && !ls[1].equals(se.getSettings().getMaxRam() + ""))
                {
                    lsb[1] = true;
                }
                if(!lsb[2] && !ls[2].equals(se.getSettings().isStartIfOffline() + ""))
                {
                    lsb[2] = true;
                }
                if(!lsb[3] && !ls[3].equals(se.getSettings().isRestartIfNotResponding() + ""))
                {
                    lsb[3] = true;
                }
                if(!lsb[4] && !ls[4].equals(se.getSettings().getMaxStartingDuration() + ""))
                {
                    lsb[4] = true;
                }
                if(!lsb[5] && !ls[5].equals(se.getSettings().getMaxStoppingDuration() + ""))
                {
                    lsb[5] = true;
                }
                if(!lsb[6] && !ls[6].equals(se.getSettings().getStopCommand() + ""))
                {
                    lsb[6] = true;
                }

            }
        }
        Printer.printSubTitle("\"" + server + "\" Settings");
        Printer.printItem("(1) Start RAM", (lsb[0] ? "Multiple Values" : ls[0]) + "");
        Printer.printItem("(2) Max RAM", (lsb[1] ? "Multiple Values" : ls[1]) + "");
        Printer.printItem("(3) Start if offline", (lsb[2] ? "Multiple Values" : ls[2]) + "");
        Printer.printItem("(4) Restart if not responding", (lsb[3] ? "Multiple Values" : ls[3]) + "");
        Printer.printItem("(5) Max starting duration", (lsb[4] ? "Multiple Values" : ls[4]) + "");
        Printer.printItem("(6) Max stoping duration", (lsb[5] ? "Multiple Values" : ls[5]) + "");
        Printer.printItem("(7) Stop command", (lsb[6] ? "Multiple Values" : ls[6]) + "");
    }
}
