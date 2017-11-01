package com.oasisartisan.servermanager.processhandlers;

import com.oasisartisan.servermanager.consolecommunication.Printer;
import com.oasisartisan.servermanager.Utilities;
import com.oasisartisan.servermanager.objects.BackupProfile;
import com.oasisartisan.servermanager.objects.Server;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author OasisArtisan
 */
public class BashProcessHandler implements ProcessHandler, Serializable {

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
            Printer.printError("BashHandler", "Failed to create temporary script for commands.", e);
            return null;
        }
        try {
            return pb.start();
        } catch (IOException e) {
            Printer.printError("BashHandler", "Failed to execute commands.", e);
            return null;
        }
    }

    @Override
    public boolean checkPrerequisites() throws InterruptedException{
        List<String> msgs = new ArrayList();
        try {
            listProcesses();
        } catch (IOException e) {
            msgs.add("\"jps\" used to keep track of all java processes.");

        }
        try {
            listScreens();
        } catch (IOException e) {
            msgs.add("\"screen\" used to start server processes in seperate screens to allow access to consoles.");
        }
        try {
            List<String> cmds = new ArrayList();
            cmds.add("zip");
            cmds.add("--help");
            exec(cmds);
        } catch (IOException e) {
            msgs.add("\"zip\" used for backup commands.");
        }
        if (!msgs.isEmpty()) {
            Printer.printBackgroundFail(pName, "The following essential prerequisites have not been found:");
            for (String s : msgs) {
                Printer.printBackgroundFail(pName, s);
            }
            Printer.printCustom("$$WTo install the missing prerequisites, try typing the command in your bash terminal linux should give you a hint on how to get the command.");
            return false;
        }
        return true;
    }

    @Override
    public boolean clearConsole() {
        try {
            ProcessBuilder pb = new ProcessBuilder("clear");
            pb.inheritIO();
            Process p1 = pb.start();
            Process p2 = pb.start();
            p2.waitFor();
            p1.waitFor();
            Utilities.printStream(p1.getErrorStream());
            Utilities.printStream(p2.getErrorStream());
            if (p1.isAlive() || p2.isAlive()) {
                Printer.printError("BashHandler", "Clearing the console took too long.", null);
                if (p1.isAlive()) {
                    p1.destroy();
                }
                if (p2.isAlive()) {
                    p2.destroy();
                }
                return false;
            }
            return true;
        } catch (IOException | InterruptedException e) {
            Printer.printError("BashHandler", "Failed to clear console", e);
            return false;
        }
    }

    @Override
    public Process sendCommandToServer(Server server, String cmd) throws IOException, InterruptedException{
        ArrayList<String> list = new ArrayList();
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

    @Override
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

    @Override
    public Process killServerProcess(Server server, boolean graceful) throws IOException, InterruptedException {
        ArrayList<String> cmd = new ArrayList();
        cmd.add("kill");
        if (graceful) {
            cmd.add("-15");
        } else {
            cmd.add("-9");
        }
        ArrayList<String> p = listProcesses();
        for (String s : p) {
            if (s.contains("##" + server.getName() + "##")) {
                cmd.add(s.substring(0, s.indexOf(' ')));
                break;
            }
        }
        if (cmd.size() != 3) {
            Printer.printError(pName, "The server \"" + server.getName() + "\" has no process to be killed", null);
        }
        return exec(cmd);
    }

    @Override
    public ArrayList<String> listProcesses() throws IOException, InterruptedException {
        ArrayList<String> cmd = new ArrayList();
        cmd.add("jps");
        cmd.add("-m");
        Process p = exec(cmd);
        ProcessHandler.finishProcess(p, "BashProcessHandler >> listProcesses");
        return Utilities.listStream(p.getInputStream());
    }

    @Override
    public boolean hasActiveProcess(Server server) throws IOException, InterruptedException {
        ArrayList<String> cmd = listProcesses();
        for (String s : cmd) {
            if (hasActiveProcess(server, s)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasActiveProcess(Server server, String processListElement) {
        return processListElement.contains("##" + server.getName() + "##");
    }

    @Override
    public Process executeBackup(Server s, BackupProfile bp) throws IOException {
        ArrayList<String> cmd = new ArrayList();
        cmd.add("zip");
        cmd.add("-db");
        cmd.add("-dd");
        cmd.add("-r");
        String dir;
        if (bp.getDir() == null) {
            dir = s.getFile().getParent();
        } else {
            dir = bp.getDir().getPath();
        }
        String name = dir + "/" + bp.getName() + "_" + s.getName() + "_" + Utilities.getDateStamp() + ".zip";
        File dirFile = new File(dir);
        if (!dirFile.isDirectory()) {
            dirFile.mkdirs();
        }
        for (File f : dirFile.listFiles()) {
            if (f.getName().contains(bp.getName() + "_" + s.getName()) && f.getName().endsWith(".zip")) {
                f.renameTo(new File(name));
                break;
            }
        }
        cmd.add("-FS");
        cmd.add(name);
        cmd.add(s.getFile().getParent());
        if (!bp.getExcludeList().isEmpty()) {
            cmd.add("-x");
        }
        for (String str : bp.getExcludeList()) {
            cmd.add(str);
        }
        if (!bp.getIncludeList().isEmpty()) {
            cmd.add("-i");
        }
        for (String str : bp.getIncludeList()) {
            cmd.add(str);
        }
        return exec(cmd);
    }
    @Override
    public String translateBackupOutputToProgress(String[] out) {
        String[] output = out.clone();
        if (output[0].isEmpty() && output[1].isEmpty()) {
            return "";
        }
        boolean complete = output[1].matches("\\[.*\\/.*\\].*");
        if (complete || !output[0].isEmpty()) {
            int i = complete ? 1 : 0;
            String processedStr = output[i].substring(1, output[i].indexOf("/")).trim();
            double processed = Utilities.convertSizeToBytes(processedStr);
            String leftStr = output[i].substring(output[i].indexOf("/") + 1, output[i].indexOf("]")).trim();
            double left = Utilities.convertSizeToBytes(leftStr);
            //If there are extra dots calculate their value
            if (output[i].matches(".*\\ \\.\\.+")) {
                String[] t = output[i].split(".*\\ \\.\\.");
                if (t.length > 1 && !t[1].matches(".*[^\\.]+.*")) //If the string only contains dots
                {
                    long dotsValue = (t[1].length() + 2L) * 10L * 1048576L;
                    processed += dotsValue;
                    left -= dotsValue;
                }
            }
            //In case the rounding up makes the bytes left negative
            if(left < 0)
            {
                left = 0;
            }
            
            return "(" + Utilities.humanReadableByteCount(Math.round(processed), false) + "/" + Utilities.humanReadableByteCount(Math.round(left), false)  + ")"
                    + " %" + String.format("%3.1f", 100*processed/(processed + left));
        } else {
            return "";
        }
    }
    
    @Override
    public void waitForKeyPress() {
        ArrayList<String> cmd = new ArrayList();
        cmd.add("bash");
        cmd.add("-c");
        cmd.add("read -n1 -s");
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.inheritIO();
        try {
            Process p = pb.start();
            p.waitFor();
        } catch (Exception e) {
            Printer.printError(pName, "Could not wait for key press.", e);
        }

    }

    private String getServerFullScreenName(Server server) throws IOException, InterruptedException{
        ArrayList<String> ls = listScreens();
        for (String s : ls) {
            if (s.contains("." + server.getName() + ((char) 9))) {
                String res = "";
                boolean record = false;
                for (char c : s.toCharArray()) {
                    if (record && c == (char) 9) {
                        break;
                    }
                    if (record) {
                        res += c;
                    }
                    if (c == (char) 9) {
                        record = true;
                    }
                }
                return res;
            }
        }
        return null;
    }

    private ArrayList<String> listScreens() throws IOException, InterruptedException{
        ArrayList<String> cmd = new ArrayList();
        cmd.add("screen");
        cmd.add("-ls");
        Process p = exec(cmd);
        ProcessHandler.finishProcess(p, "BashProcessHandler >> listScreens");
        return Utilities.listStream(p.getInputStream());
    }
}
