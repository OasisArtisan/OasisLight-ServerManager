package com.rockpartymc.servermanager.objects;

import com.rockpartymc.servermanager.Utilities;
import com.rockpartymc.servermanager.consolecommunication.Printer;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BackupProfile implements Serializable{
    private List<String> excludeList;
    private List<String> includeList;
    private String name;
    private File dir;
    
    public BackupProfile(String name, File dir)
    {
        this.name = name;
        this.dir = dir;
        excludeList = Collections.synchronizedList(new ArrayList());
        includeList = Collections.synchronizedList(new ArrayList());
    }
    public void printInfo() {
        Printer.printSubTitle(name);
        String content;
        if(dir == null)
        {
            content = " null (same as server)";
        } else
        {
            content = dir.getPath();
        }
        Printer.printCustom("$_YSave directory: $$W" +content);
        Printer.printCustom(String.format("%-23s  %-23s",
          "$_YExclude: $$W" + Utilities.listArgs(excludeList, " ")
        , "$_YInclude: $$W" + Utilities.listArgs(includeList, " ")));
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public File getDir() {
        return dir;
    }

    public void setDir(File dir) {
        this.dir = dir;
    }
    public List<String> getExcludeList() {
        return excludeList;
    }

    public List<String> getIncludeList() {
        return includeList;
    }

}
