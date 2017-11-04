package com.oasisartisan.servermanager;

import com.oasisartisan.servermanager.consolecommunication.Printer;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 *
 * @author OasisArtisan
 */
public class Utilities {

    public static ArrayList<String> listStream(InputStream stream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        String s = br.readLine();
        ArrayList<String> list = new ArrayList();
        while (s != null) {
            list.add(s);
            s = br.readLine();
        }
        return list;
    }

    public static void printStream(InputStream stream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        String s = br.readLine();
        while (s != null) {
            Printer.printBackgroundInfo("Process", s);
            s = br.readLine();
        }
    }

    public static List<String> seperateArgs(String s, String delim) {
        List<String> list = new ArrayList();
        if (!s.trim().isEmpty()) {
            StringTokenizer st = new StringTokenizer(s, delim);
            while (st.hasMoreTokens()) {
                list.add(st.nextToken());
            }
        }
        return list;
    }

    public static String listArgs(List<String> ls, String delim) {
        String rs = "";
        for (String s : ls) {
            rs += s + delim;
        }
        if (ls.isEmpty()) {
            return rs;
        }
        return rs.substring(0, rs.length() - delim.length());
    }

    /**
     * Source:
     * http://programming.guide/java/formatting-byte-size-to-human-readable-format.html
     */
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f%sB", bytes / Math.pow(unit, exp), pre);
    }

    public static String isValidRam(String s) {
        if (isInteger(s)) {
            int ram = Integer.parseInt(s);
            if (ram % 1024 == 0) {
                return null;
            } else {
                return "Value must be a multiple of 1024";
            }
        }
        if (isInteger(s.substring(0, s.length() - 1))) {
            int ram = Integer.parseInt(s.substring(0, s.length() - 1));
            char last = s.toUpperCase().charAt(s.length() - 1);
            if (last == 'K' || last == 'M' || last == 'G') {
                if (last == 'K') {
                    if (ram % 1024 == 0) {
                        return null;
                    } else {
                        return "Value must be a multiple of 1024";
                    }
                }
                return null;
            } else {
                return "Acceptable units are \"K\" \"M\" \"G\" or no unit to indicate Bytes";
            }
        }
        return "Invalid ram format. Examples: 83886080, 81920K, 80M, 2G";
    }
    
    public static double convertSizeToBytes(String s)
    {
        if (s.endsWith("G")) {
            return Double.parseDouble(s.substring(0, s.length() - 1)) * 1073741824D; // 2 ^ 30
        } else if (s.endsWith("M")) {
            return Double.parseDouble(s.substring(0, s.length() - 1)) * 1048576D; // 2 ^ 20
        } else if (s.endsWith("K")) {
            return Double.parseDouble(s.substring(0, s.length() - 1)) * 1024D; // 2 ^ 10
        } else {
            return Double.parseDouble(s);
        }
    }
    
    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }

    }

    public static boolean isLong(String s) {
        try {
            Long.parseLong(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }

    }

    public static List removeNulls(List ls) {
        List nls = new ArrayList(ls);
        for (Object o : ls) {
            if (o != null) {
                nls.add(o);
            }
        }
        return nls;
    }

    public static String getFileInfo(File file) {
        String s = null;
        if (file.isFile()) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            s = file.getName() + " Size: " + Utilities.humanReadableByteCount(file.length(), false) + " Last Modified: " + sdf.format(file.lastModified());
        }
        return s;
    }

    public static String readLocalFile(String path) {
        InputStream is = Class.class.getResourceAsStream("/" + path);
        Scanner sc = new Scanner(is);
        String s = "";
        while (sc.hasNextLine()) {
            s += sc.nextLine();
            if (sc.hasNextLine()) {
                s += System.lineSeparator();
            }
        }
        return s;
    }

    public static String getDateStamp() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DAY_OF_MONTH);
    }
}
