/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rockpartymc.servermanager.objects;

import com.rockpartymc.servermanager.consolecommunication.Printer;
import com.rockpartymc.servermanager.Utilities;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author OmarAlama
 */
public class Timing implements Serializable{

    private Calendar calendar;
    private String type;

    public String getType() {
        return type;
    }

    public Timing(String s) {
        calendar = Calendar.getInstance();
        calendar.setLenient(true);
        StringTokenizer st = new StringTokenizer(s, "-");
        int tokens = st.countTokens();
        if (tokens > 4 || tokens < 1) {
            throw new IllegalArgumentException();
        }
        if (tokens == 4) {
            String year = st.nextToken();
            if (year.length() != 4 || !Utilities.isInteger(year)) {
                throw new IllegalArgumentException();
            }
            int y = Integer.parseInt(year);
            if (y < 1) {
                throw new IllegalArgumentException();
            }
            calendar.set(Calendar.YEAR, y);
            type = "ONETIME";
            tokens--;
        }
        if (tokens == 3) {
            String month = st.nextToken();
            if (!Utilities.isInteger(month)) {
                throw new IllegalArgumentException();
            }
            int m = Integer.parseInt(month);
            if (m < 1 || m > 12) {
                throw new IllegalArgumentException();
            }
            calendar.set(Calendar.MONTH, m + -1);
            if (type == null) {
                type = "YEARLY";
            }
            tokens--;
        }
        if (tokens == 2) {
            String day = st.nextToken().toUpperCase();
            Integer d = intDayFromString(day);
            if (Utilities.isInteger(day)) {
                d = Integer.parseInt(day);
                if (d < 1 || d > 31) {
                    throw new IllegalArgumentException();
                }
                calendar.set(Calendar.DAY_OF_MONTH, d);
                if (type == null) {
                    type = "MONTHLY";
                }
            } else if (d != null) {
                calendar.set(Calendar.DAY_OF_WEEK, d);
                if(calendar.getTimeInMillis() - System.currentTimeMillis() <= 0)
                {
                    calendar.add(Calendar.WEEK_OF_YEAR, 1);
                }
                if (type == null) {
                    type = "WEEKLY";
                }
            } else {
                throw new IllegalArgumentException();
            }
            tokens--;
        }
        if (tokens == 1) {
            StringTokenizer timeSt = new StringTokenizer(st.nextToken(), ":");
            tokens = timeSt.countTokens();
            if (tokens == 2) {
                String hour = timeSt.nextToken();
                if (!Utilities.isInteger(hour)) {
                    throw new IllegalArgumentException();
                }
                int h = Integer.parseInt(hour);
                if (h < 0 || h > 24) {
                    throw new IllegalArgumentException();
                }
                calendar.set(Calendar.HOUR_OF_DAY, h);
                if (type == null) {
                    type = "DAILY";
                }
                tokens--;
            }
            if (tokens == 1) {
                String minute = timeSt.nextToken();
                if (!Utilities.isInteger(minute)) {
                    throw new IllegalArgumentException();
                }
                int m = Integer.parseInt(minute);
                if (m < 0 || m > 59) {
                    throw new IllegalArgumentException();
                }
                calendar.set(Calendar.MINUTE, m);
                if (type == null) {
                    type = "HOURLY";
                }
            }
        }
        if(type.equals("ONETIME") && calendar.before(Calendar.getInstance()))
        {
            throw new IllegalArgumentException();
        }
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }
    public long getNextExecutionMillis(boolean computeIfCyclePassed) {
        Calendar c = Calendar.getInstance();
        long millisLeft;
        long systemMillis = System.currentTimeMillis();
            switch (type){
                case "YEARLY":
                    calendar.set(Calendar.YEAR, c.get(Calendar.YEAR));
                    millisLeft = calendar.getTimeInMillis() - systemMillis;
                    if(millisLeft < 0 && computeIfCyclePassed)
                    {
                        calendar.add(Calendar.YEAR, 1);
                    }
                    break;
                case "MONTHLY":
                    calendar.set(Calendar.YEAR, c.get(Calendar.YEAR));
                    calendar.set(Calendar.MONTH, c.get(Calendar.MONTH));
                    millisLeft = calendar.getTimeInMillis() - systemMillis;
                    if(millisLeft < 0 && computeIfCyclePassed)
                    {
                        calendar.add(Calendar.MONTH, 1);
                    }
                    break;
                case "WEEKLY":
                    calendar.set(Calendar.YEAR, c.get(Calendar.YEAR));
                    calendar.set(Calendar.WEEK_OF_YEAR, c.get(Calendar.WEEK_OF_YEAR));
                    millisLeft = calendar.getTimeInMillis() - systemMillis;
                    if(millisLeft < 0 && computeIfCyclePassed)
                    {
                        calendar.add(Calendar.WEEK_OF_YEAR, 1);
                    }
                    break;
                case "DAILY":
                    calendar.set(Calendar.YEAR, c.get(Calendar.YEAR));
                    calendar.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR));
                    millisLeft = calendar.getTimeInMillis() - systemMillis;
                    if(millisLeft < 0 && computeIfCyclePassed)
                    {
                        calendar.add(Calendar.DAY_OF_YEAR, 1);
                    }
                    break;
                case "HOURLY":
                    calendar.set(Calendar.YEAR, c.get(Calendar.YEAR));
                    calendar.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR));
                    calendar.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
                    millisLeft = calendar.getTimeInMillis() - systemMillis;
                    if(millisLeft < 0 && computeIfCyclePassed)
                    {
                        calendar.add(Calendar.HOUR_OF_DAY, 1);
                    }
                    break;
            }
        millisLeft = calendar.getTimeInMillis() - systemMillis;
        return millisLeft;
    }
    public String toString()
    {
        String s = "";
        if(type.equals("ONETIME"))
        {
            s+= calendar.get(Calendar.YEAR) + "-";
        }
        if(type.equals("YEARLY") || type.equals("ONETIME"))
        {
            s+= (calendar.get(Calendar.MONTH) + 1) + "-";
        }
        if (type.equals("MONTHLY") || type.equals("YEARLY") || type.equals("ONETIME"))
        {
            s+= calendar.get(Calendar.DAY_OF_MONTH) + "-";
        }
        if (type.equals("WEEKLY"))
        {
            s+= calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG_FORMAT, Locale.ENGLISH) + "-";
        }
        if(!type.equals("HOURLY"))
        {
            s+= calendar.get(Calendar.HOUR_OF_DAY) + ":";
        }
        s+= calendar.get(Calendar.MINUTE);
        return s;
    }
    public static Integer intDayFromString(String s) {
        switch (s.toUpperCase()) {
            case "SUNDAY":
                return Calendar.SUNDAY;
            case "MONDAY":
                return Calendar.MONDAY;
            case "TUESDAY":
                return Calendar.TUESDAY;
            case "WEDNESDAY":
                return Calendar.WEDNESDAY;
            case "THURSDAY":
                return Calendar.THURSDAY;
            case "FRIDAY":
                return Calendar.FRIDAY;
            case "SATURDAY":
                return Calendar.SATURDAY;
            default:
                return null;
        }
    }
    public static void printTimeFormats(String pName)
    {
        Printer.printSubTitle("Time formats");
        Printer.printItem("For Hourly", "\"00\"");
        Printer.printItem("For Daily", "\"00:00\"");
        Printer.printItem("For Weekly", "\"SATURDAY-00:00\"");
        Printer.printItem("For Monthly", "\"DD-00:00\"");
        Printer.printItem("For Yearly", "\"MM-DD-00:00\"");
        Printer.printItem("For One Time", "\"YYYY-MM-DD-00:00\"");
    }

    public static String getDurationBreakdown(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);
        if (days > 0) {
            sb.append(days);
            sb.append(" d ");
        }
        if (hours > 0) {
            sb.append(hours);
            sb.append(" h ");
        }
        if (minutes > 0) {
            sb.append(minutes);
            sb.append(" m ");
        }
        if (seconds > 0) {
            sb.append(seconds);
            sb.append(" s");
        }
        return sb.toString();
    }
}
