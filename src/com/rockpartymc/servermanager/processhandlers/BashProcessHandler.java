package com.rockpartymc.servermanager.processhandlers;

import com.rockpartymc.servermanager.consolecommunication.Printer;
import com.rockpartymc.servermanager.Utilities;
import com.rockpartymc.servermanager.objects.Server;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class BashProcessHandler implements ProcessHandler, Serializable{
    private static final String pName = "BashProcessHandler";
    private Process exec(List<String> cmd) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        return pb.start();
    }
    private Process exec(ArrayList<String> cmd, File dir) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(dir);
        return pb.start();
    }
    private Process execMulti(ArrayList<String> cmds) {
        ProcessBuilder pb;
        try {
            File tempScript = File.createTempFile("script", null);

            Writer streamWriter = new OutputStreamWriter(new FileOutputStream(
                    tempScript));
            PrintWriter printWriter = new PrintWriter(streamWriter);

            printWriter.println("#!/bin/bash");
            pb = new ProcessBuilder("bash", tempScript.toString());
            for (String s : cmds) {
                printWriter.println(s);
            }
            printWriter.close();
        } catch (IOException e) {
            Printer.printError("BashHandler", "Failed to create temporary script for commands.",e);
            return null;
        }
        try {
            return pb.start();
        } catch (IOException e) {
            Printer.printError("BashHandler", "Failed to execute commands.",e);
            return null;
        }
    }
    public boolean clearConsole()
    {
        try{
        ProcessBuilder pb = new ProcessBuilder("clear");
        pb.inheritIO();
        Process p1 = pb.start();
        Process p2 = pb.start();
        p2.waitFor();
        p1.waitFor();
        Utilities.printStream(p1.getErrorStream());
        Utilities.printStream(p2.getErrorStream());
        if(p1.isAlive() || p2.isAlive())
        {
            Printer.printError("BashHandler","Clearing the console took too long." , null);
            if(p1.isAlive()) p1.destroy();
            if(p2.isAlive()) p2.destroy();
            return false;
        }
        return true;
        } catch(IOException | InterruptedException e)
        {
            Printer.printError("BashHandler", "Failed to clear console", e);
            return false;
        }
    }
    public Process sendCommandToServer(Server server, String cmd) throws IOException {
        ArrayList<String> list= new ArrayList();
        list.add("screen");
        list.add("-S");
        list.add(getServerFullScreenName(server));
        list.add("-p");
        list.add("0");
        list.add("-X");
        list.add("stuff");
        list.add(System.lineSeparator() + cmd + System.lineSeparator());
        return exec(list);
    }

    public Process startServerProcess(Server server) throws IOException {
        ArrayList<String> cmd = new ArrayList();
        cmd.add("screen");
        cmd.add("-dmS");
        cmd.add(server.getName());
        cmd.add("java");
        if (server.getSettings().getMaxRam() != null) {
            cmd.add("-Xmx" + server.getSettings().getMaxRam());
        }
        if (server.getSettings().getStartRam() != null) {
            cmd.add("-Xms" + server.getSettings().getStartRam());
        }
        cmd.add("-jar");
        cmd.add(server.getFile().getName());
        cmd.add("##" + server.getName() + "##");
        return exec(cmd, server.getFile().getParentFile());
    }
    public Process killServerProcess(Server server, boolean graceful) throws IOException
    {
        ArrayList<String> cmd = new ArrayList();
        cmd.add("kill");
        if(graceful)
        {
            cmd.add("-15");
        } else
        {
            cmd.add("-9");
        }
        ArrayList<String> p = listProcesses();
        for(String s : p)
        {
            if(s.contains("##" + server.getName() + "##"))
            {
                cmd.add(s.substring(0, s.indexOf(' ')));
                break;
            }
        }
        if(cmd.size() != 3)
        {
            Printer.printError(pName,"The server \"" + server.getName() + "\" has no process to be killed", null);
        }
        return exec(cmd);
    }
    public ArrayList<String> listProcesses() throws IOException {
        ArrayList<String> cmd = new ArrayList();
        cmd.add("jps");
        cmd.add("-m");
        Process p = exec(cmd);
        ProcessHandler.finishProcess(p, "BashProcessHandler >> listProcesses");
        return Utilities.listStream(p.getInputStream());
    }
    public boolean hasActiveProcess(Server server) throws IOException {
        ArrayList<String> cmd = listProcesses();
        for(String s : cmd)
        {
            if(hasActiveProcess(server, s))
            {
                return true;
            }
        }
        return false;
    }
    public boolean hasActiveProcess(Server server, String processListElement){
        return processListElement.contains("##" + server.getName() + "##");
    }
    private String getServerFullScreenName(Server server) throws IOException
    {
        ArrayList<String> ls = listScreens();
        for(String s : ls)
        {
            if(s.contains("." + server.getName() + ((char) 9)))
            {
                String res = "";
                boolean record = false;
                for(char c : s.toCharArray())
                {
                    if(record && c == (char) 9)
                    {
                        break;
                    }
                    if(record)
                    {
                        res += c;
                    }
                    if(c == (char) 9)
                    {
                        record = true;
                    }
                }
                return res;
            }
        }
        return null;
    }
    private ArrayList<String> listScreens() throws IOException{
        ArrayList<String> cmd = new ArrayList();
        cmd.add("screen");
        cmd.add("-ls");
        Process p = exec(cmd);
        ProcessHandler.finishProcess(p, "BashProcessHandler >> listScreens");
        return Utilities.listStream(p.getInputStream());
    }
}
