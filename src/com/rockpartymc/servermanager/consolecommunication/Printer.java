package com.rockpartymc.servermanager.consolecommunication;

import com.rockpartymc.servermanager.Main;
import com.rockpartymc.servermanager.storage.Storage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Locale;
import static com.rockpartymc.servermanager.consolecommunication.Colors.*;
import com.rockpartymc.servermanager.objects.Pair;
import static com.rockpartymc.servermanager.objects.ServerState.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Title: for a formatted headline. Prompt: for requesting input.
 * SuccessfullReply: for replies to input that are considered successful and
 * indicate no effect on data. FailedReply: for replies to input that are
 * considered a failure and indicate no effect on the data or runtime.
 * DataChange: for anything that holds changes to the data. (logged) Error: for
 * anything that is not supposed to happen normally. (logged) BackgroundInfo:
 * for informing the user about the processes running in the background such as
 * saving data or a server changing states. (logged) BackgroundSuccess: for
 * informing the user of a successful background process result. (logged)
 * BackgroundFail: for informing the user of a failed background process result.
 * (logged)
 */
public class Printer {

    private static PrintStream ps = System.out;
    //For the monitor
    private static final List<String> monitorDisplay = Collections.synchronizedList(new ArrayList());
    private static final List<Pair<String,Long>> monitorMessages = Collections.synchronizedList(new ArrayList());
    //For logging
    private static File logDirectory = new File("Logs");
    private static FileOutputStream logFos;
    private static String currentLogFileName;
    private static PrintWriter logWriter;

    public static void printTitle(String s, int size) {
        if (Storage.getSettings().isClearConsoleBeforeMenu()) {
            Main.getProcessHandler().clearConsole();
        }
        String out;
        if (Storage.getSettings().isUseConsoleColors()) {
            out = WHITE + formatDevider(" ( " + CYAN + s + WHITE + " ) ", "center", size, '-') + RESET;
        } else {
            out = formatDevider(" ( " + s + " ) ", "center", size, '-');
        }
        if (Main.getMonitorTask() != null) {
            monitorDisplay.add(out);
        } else {
            ps.println(out);
        }
    }
    public static void printTitle (String s)
    {
        printTitle(s,80);
    }

    public static void printSubTitle(String s) {
        //BashHandler.clearConsole();
        String out;
        if (Storage.getSettings().isUseConsoleColors()) {
            out = formatDevider(RESET + B_YELLOW + " " + s + " " + RESET, "offleft", 50, '-');
        } else {
            out = formatDevider(" " + s + " ", "offleft", 60, '-');
        }
        if (Main.getMonitorTask() != null) {
            monitorDisplay.add(out);
        } else {
            ps.println(out);
        }
    }

    public static void printPrompt(String s) {
        if (!Main.getInputBuffer().hasNext()) {
            if (Storage.getSettings() != null && Storage.getSettings().isUseConsoleColors()) {
                ps.println(WHITE + s + RESET);
            } else {
                ps.println(s);
            }
        }
    }

    public static void printPrompt(String name, String s) {
        printPrompt(s);
        //printPrompt("[" + name + "] " + s);
    }

    public static void printSuccessfullReply(String s) {
        String res = "";
        if (Storage.getSettings() != null && Storage.getSettings().isUseConsoleColors()) {
            res = WHITE + s + RESET;
        } else {
            res = s;
        }
        if(Main.getMonitorTask() != null)
        {
            monitorDisplay.add(res);
        } else 
        {
            ps.println(res);
        }
    }

    public static void printSuccessfullReply(String name, String s) {
        printSuccessfullReply(s);
        //printSuccessfullReply("[" + name + "] " + s);
    }

    public static void printFailedReply(String s) {
        String out = s;
        if (Storage.getSettings() != null && Storage.getSettings().isUseConsoleColors()) {
            out = B_RED + s + RESET;
        }
        if (Main.getMonitorTask() != null) {
            monitorMessages.add(new Pair(out,null));
        } else {
            ps.println(out);
        }
        Main.getIn().clear();
    }

    public static void printFailedReply(String name, String s) {
        printFailedReply(s);
        //printFailedReply("[" + name + "] " + s);
    }

    public static void printItem(String caption, String detail) {
        if (Storage.getSettings().isUseConsoleColors()) {
            detail = detail.replace("true", B_GREEN + "True" + RESET).replace("false", B_RED + "False" + RESET).replace("null", B_BLUE + "null" + RESET);
            ps.println(YELLOW + caption + ": " + WHITE + detail + RESET);
        } else {
            ps.println(caption + ": " + detail);
        }
    }
    public static void printDataChange(String s) {
        String res = "";
        if (Storage.getSettings().isBackgroundInfoTimeStampsInConsole()) {
            res += getTimeStamp();
        }
        res += s;
        log(res, null);
        if (Storage.getSettings().isUseConsoleColors()) {
            res = GREEN + res + RESET;
        }
        if (Main.getMonitorTask() != null) {
            monitorMessages.add( new Pair(res, null));
        } else {
            ps.println(res);
        }
        Storage.saveDataToFile();
    }

    public static void printDataChange(String name, String s) {
        printDataChange("[" + name + "]" + s);
    }

    public static void printCustom(String message) {
        String out;
        if (Storage.getSettings().isUseConsoleColors()) {
            out = translateColorCodes(message) + RESET;
        } else {
            out = Colors.stripColorCodes(message);
        }
        if (Main.getMonitorTask() != null) {
            monitorDisplay.add(out);
        } else {
            ps.println(out);
        }
    }

    public static void printError(String s, Exception e) {
        String res = "";
        if (Storage.getSettings() == null || Storage.getSettings().isBackgroundInfoTimeStampsInConsole()) {
            res += getTimeStamp();
        }
        res += "[Error]" + s;
        log(res, e);
        if (Storage.getSettings() != null && Storage.getSettings().isUseConsoleColors()) {
            res = RED + res + RESET;
        }
        if (Main.getMonitorTask() != null) {
            monitorMessages.add( new Pair(res, null));;
        } else {
            ps.println(res);
            if (e != null) {
                e.printStackTrace(ps);
            }
        }
    }

    public static void printError(String name, String s, Exception e) {
        printError("[" + name + "]" + s, e);
    }

    public static void printBackgroundInfo(String name, String s) {
        if (Storage.getSettings() == null ||  Storage.getSettings().isPrintBackgroundInfoToConsole()) {
            String res = "";
            if (Storage.getSettings() == null || Storage.getSettings().isBackgroundInfoTimeStampsInConsole()) {
                res += getTimeStamp();
            }
            res += "[" + name + "] " + s;
            log(res, null);
            if (Storage.getSettings() != null && Storage.getSettings().isUseConsoleColors()) {
                res = GREY + res + RESET;
            }
            if (Main.getMonitorTask() != null) {
                monitorMessages.add( new Pair(res, null));
            } else {
                ps.println(res);
            }
        }
    }

    public static void printBackgroundSuccess(String name, String s) {
        if (Storage.getSettings() == null || Storage.getSettings().isPrintBackgroundInfoToConsole()) {
            String res = "";
            if (Storage.getSettings() == null || Storage.getSettings().isBackgroundInfoTimeStampsInConsole()) {
                res += getTimeStamp();
            }
            res += "[" + name + "] " + s;
            log(res, null);
            if (Storage.getSettings() != null && Storage.getSettings().isUseConsoleColors()) {
                res = B_GREEN + res + RESET;
            }
            if (Main.getMonitorTask() != null) {
                monitorMessages.add( new Pair(res, null));
            } else {
                ps.println(res);
            }
        }
    }

    public static void printBackgroundFail(String name, String s) {
        if (Storage.getSettings() == null || Storage.getSettings().isPrintBackgroundInfoToConsole()) {
            String res = "";
            if (Storage.getSettings() == null || Storage.getSettings().isBackgroundInfoTimeStampsInConsole()) {
                res += getTimeStamp();
            }
            res += "[" + name + "] " + s;
            log(res, null);
            if (Storage.getSettings() != null && Storage.getSettings().isUseConsoleColors()) {
                res = B_RED + res + RESET;
            }
            if (Main.getMonitorTask() != null) {
                monitorMessages.add( new Pair(res, null));
            } else {
                ps.println(res);
            }
            
        }
    }

    //Center-right-left-offright-offleft
    public static String formatDevider(String title, String alignment, int max, char filler) {
        String res = "";
        int leftPadding;
        int rightPadding;
        int av = max - stripColors(title).length();
        switch (alignment.toLowerCase()) {
            case "left":
                leftPadding = 0;
                rightPadding = av;
                break;
            case "right":
                leftPadding = av;
                rightPadding = 0;
                break;
            case "offleft":
                leftPadding = (int) Math.round(av * 0.25);
                rightPadding = av - leftPadding;
                break;
            case "offright":
                rightPadding = (int) Math.round(av * 0.25);
                leftPadding = av - rightPadding;
                break;
            default: //Center
                leftPadding = (int) Math.floor(av / 2D);
                rightPadding = (int) Math.ceil(av / 2D);
        }
        for (int i = 0; i < leftPadding; i++) {
            res += filler;
        }
        res += title;
        for (int i = 0; i < rightPadding; i++) {
            res += filler;
        }
        return res;
    }

    public static String getTimeStamp() {
        Calendar c = Calendar.getInstance();
        String hour = c.get(Calendar.HOUR_OF_DAY) + "";
        if (hour.length() < 2) {
            hour = "0" + hour;
        }
        String minute = c.get(Calendar.MINUTE) + "";
        if (minute.length() < 2) {
            minute = "0" + minute;
        }
        String second = c.get(Calendar.SECOND) + "";
        if (second.length() < 2) {
            second = "0" + second;
        }
        return "[" + hour + ":" + minute + ":" + second + "]";
    }
    
    private static void log(String msg, Exception e) {
        if (Storage.getSettings() == null || Storage.getSettings().isLogOutput()) {
            if (!logDirectory.isDirectory()) {
                logDirectory.mkdir();
            }
            if (logDirectory.isDirectory()) {
                Calendar c = Calendar.getInstance();
                String fileName
                        = c.get(Calendar.YEAR)
                        + "-" + c.getDisplayName(Calendar.MONTH, Calendar.SHORT_FORMAT, Locale.ENGLISH)
                        + "-" + c.get(Calendar.DAY_OF_MONTH)
                        + ".smlog";
                if (currentLogFileName != null && !fileName.equals(currentLogFileName)) {
                    logWriter.close();
                }
                if (currentLogFileName == null || !fileName.equals(currentLogFileName)) {
                    File file = new File(logDirectory + "/" + fileName);
                    try {
                        file.createNewFile();
                        logFos = new FileOutputStream(file, true);
                        logWriter = new PrintWriter(logFos);
                        currentLogFileName = fileName;
                    } catch (IOException ex) {
                        ps.println("[Logger] Failed to write to log file \"" + fileName + "\".");
                        e.printStackTrace(ps);
                    }
                }
                logWriter.append(msg + System.lineSeparator());
                if (e != null) {
                    logWriter.append(e.toString());
                    for (StackTraceElement ste : e.getStackTrace()) {
                        logWriter.append("	" + ste + System.lineSeparator());
                    }
                }
                logWriter.flush();
            } else {
                ps.println("[Logger] Could not find or create the logging directory.");
            }
        }
    }

    public static void close() {
        if (ps != null) {
            ps.close();
        }
        if (logWriter != null) {
            logWriter.close();
        }
    }

    public static String getCCC(Object o) {
        if (o == null) {
            return "$BB";
        } else if (o.equals(true)) {
            return "$_G";
        } else if (o.equals(false)) {
            return "$_R";
        } else if (o == ONLINE) {
            return "$_G";
        } else if (o == OFFLINE) {
            return "$$G";
        } else if (o == NOTRESPONDING || o == TERMINATING) {
            return "$_R";
        } else if (o == STARTING) {
            return "$BG";
        } else if (o == STOPPING) {
            return "$BR";
        } else {
            return "$$W";
        }
    }

    public static void flushMonitorDisplay() {
        String out = "";
        synchronized (monitorDisplay) {
            for (String s : monitorDisplay) {
                out += s + System.lineSeparator();
            }
            ps.println(out);
            monitorDisplay.clear();
        }
    }

    public static void flushMonitorMessages(Long displayDuration) {
        if (monitorMessages.size() > 0) {
            List<Pair> done = new ArrayList();
            String out = "";
            synchronized (monitorMessages) {
                for (Pair<String,Long> p : monitorMessages) {
                    out += p.getK() + System.lineSeparator();
                    Long m = p.getV();
                    if (m == null) {
                        p.setV(System.currentTimeMillis());
                    } else if (System.currentTimeMillis() - m > displayDuration) {
                        done.add(p);
                    }
                }
                ps.print(out);
                monitorMessages.removeAll(done);
            }
        }
    }
    public static void newLine()
    {
        if(Main.getMonitorTask() == null)
        {
        ps.println();
        } else
        {
            monitorDisplay.add("");
        }
    }
}
