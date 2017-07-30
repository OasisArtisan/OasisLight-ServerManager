
package com.rockpartymc.servermanager;

import com.rockpartymc.servermanager.consolecommunication.Printer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Utilities {

    public static ArrayList<String> listStream(InputStream stream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        String s = br.readLine();
        ArrayList<String> list = new ArrayList();
        while (s != null) {
            list.add(s);
            s=br.readLine();
        }
        return list;
    }

    public static void printStream(InputStream stream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        String s = br.readLine();
        while (s != null) {
            Printer.printBackgroundInfo("Process",s);
            s=br.readLine();
        }
    }
    public static List<String> seperateArgs(String s)
    {
        List<String> list = new ArrayList();
        StringTokenizer st = new StringTokenizer(s);
        while(st.hasMoreTokens())
        {
            list.add(st.nextToken());
        }
        return list;
    }
    public static String isValidRam(String s)
    {
        if(isInteger(s))
        {
            int ram  = Integer.parseInt(s);
            if(ram%1024 == 0)
            {
                return null;
            } else
            {
                return "Value must be a multiple of 1024";
            }
        }
        if (isInteger(s.substring(0, s.length() - 1))) {
            int ram = Integer.parseInt(s.substring(0, s.length() - 1));
            char last = s.toUpperCase().charAt(s.length() - 1);
            if (last == 'K' || last == 'M' || last == 'G') {
                if (last == 'K') {
                    if (ram % 1024 == 0)
                    {
                    return null;
                    } else
                    {
                        return "Value must be a multiple of 1024";
                    }
                }
                return null;
            } else
            {
                return "Acceptable units are \"K\" \"M\" \"G\" or no unit to indicate Bytes";
            }
        }
        return "Invalid ram format. Examples: 83886080, 81920K, 80M, 2G";
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
}
