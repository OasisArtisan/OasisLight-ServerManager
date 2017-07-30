package com.rockpartymc.servermanager.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JFrame;

public class BackupProfile {
    private List<String> excludeList;
    private List<String> includeList;
    private String type;
    private int copies;
    
    public BackupProfile()
    {
        includeList = Collections.synchronizedList(new ArrayList());
        
    }
}
