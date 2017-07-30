package com.rockpartymc.servermanager.storage;

import com.rockpartymc.servermanager.objects.Server;
import java.io.Serializable;
import java.util.HashMap;

public class Data implements Serializable{
    private HashMap<String,Server> serverList;
    private Settings settings;
    public Data(HashMap<String,Server> serverList, Settings settings)
    {
        this.serverList = serverList;
        this.settings = settings;
    }
    public HashMap<String,Server> getServerList() {
        for(Server s: serverList.values())
        {
            s.initializeState();
        }
        return serverList;
    }
    public Settings getSettings()
    {
        return settings;
    }
    
}