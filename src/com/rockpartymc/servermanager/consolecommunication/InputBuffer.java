/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rockpartymc.servermanager.consolecommunication;

import com.rockpartymc.servermanager.Main;
import com.rockpartymc.servermanager.storage.Storage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author OmarAlama
 */
public class InputBuffer {

    private List<List<String>> input;
    private Scanner sc;
    private Thread thread;
    private Object enterKeyLock = new Object();
    private boolean waitingForEnter = false;

    public InputBuffer(InputStream is) {
        input = Collections.synchronizedList(new ArrayList());
        sc = new Scanner(is);
        InputBuffer ib = this;
        thread = new Thread() {
            public void run() {
                try {
                    Printer.printBackgroundInfo("InputBuffer", "Starting thread");
                    Scanner sc = new Scanner(System.in);

                    try {
                        while (sc.hasNextLine()) {
                            String s = sc.nextLine().trim();
                            if (waitingForEnter) {
                                synchronized (enterKeyLock) {
                                    enterKeyLock.notifyAll();
                                }
                                waitingForEnter = false;
                            } else if (!s.isEmpty()) {
                                List<String> list = Collections.synchronizedList(new ArrayList());
                                StringTokenizer st = new StringTokenizer(s);
                                while (st.hasMoreTokens()) {
                                    String t = st.nextToken();
                                    if (t.equals("quit") || t.equals("exit")) {
                                        sc.close();
                                        System.exit(0);
                                    } else if (t.equals("clear") || t.equals("clr")) {
                                        Main.printMenu(Main.getActiveMenu().getK(), Main.getActiveMenu().getV());
                                    } else {
                                        list.add(t);
                                    }
                                }
                                if (list.size() > 0) {
                                    input.add(list);
                                    synchronized (ib) {
                                        ib.notifyAll();
                                    }
                                }
                            }
                        }
                    } catch (IllegalStateException e) {
                        Printer.printBackgroundInfo("InputBuffer", "Stopping thread and closing input stream");
                    }
                } catch (Exception e) {
                    Printer.printError("InputBuffer", "An unexpected error occured.", e);
                }
            }
        };
    }

    public void close() {
        sc.close();
    }

    public void start() {
        thread.setName("InputBuffer");
        thread.start();
    }

    public boolean hasNextLine() {
        return !input.isEmpty();
    }

    public boolean hasNext() {
        return !input.isEmpty() && !input.get(0).isEmpty();
    }

    public synchronized String next() {
        try {
            if (!hasNext()) {
                this.wait();
            }
        } catch (InterruptedException e) {

        }
        String s = input.get(0).get(0);
        input.get(0).remove(0);
        if (input.get(0).isEmpty()) {
            input.remove(0);
        }
        return s;
    }

    public synchronized String nextLine() {
        try {
            if (!hasNextLine()) {
                this.wait();
            }
        } catch (InterruptedException e) {

        }
        String next = "";
        List<String> l = input.get(0);
        for (String s : l) {
            next += s + " ";
        }
        input.remove(0);
        return next.trim();
    }

    public void waitForEnter() {
        waitingForEnter = true;
        synchronized (enterKeyLock) {
            try {
                enterKeyLock.wait();
            } catch (InterruptedException ex) {
            }
        }
    }

    public synchronized void clear() {
        input.clear();
    }
}
