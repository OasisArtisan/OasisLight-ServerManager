
    /**
     * The method currently uses a very tedious loading method just for the
     * purpose of Being able to load data from changed table schemas. A more
     * efficient method could have been used if all the values stored were used
     * as maps as opposed to individual variables. However thats not the case
     * which is why i couldn't use loops to load the data. The functionality
     * that this adds is: If a column is added in future versions, older data
     * files can still be loaded by filling missing columns with default values
     * If a column is removed in future versions, older data will still be
     * loaded
     *
     * @return
     */
    /*    public boolean loadData() {

        try {
            if (c == null) {
                c = DriverManager.getConnection("jdbc:sqlite:" + getFile().getName());
                System.out.println("Re intitializing the database");
                c.createStatement().executeUpdate(Utilities.readLocalFile("SQLiteDatabase.sql"));
            }
            //Load Program Settings Safely
            System.out.println("Loading settings");
            ResultSet rs = c.createStatement().executeQuery("SELECT " + "*" + " FROM Program_Settings");
            settings = new Settings();
            boolean outdated = false;
            List<String> existingColumns = new ArrayList();
            int count = 0;
            if (!rs.isClosed()) {
                count++;
                for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                    existingColumns.add(rs.getMetaData().getColumnName(i + 1));
                }
                if (existingColumns.contains(settingsColumns[1])) {
                    settings.setCommunicationDir(new File(rs.getString(settingsColumns[1])));
                    count++;
                }
                if (existingColumns.contains(settingsColumns[2])) {
                    settings.setServerStateUpdaterTaskInterval(rs.getInt(settingsColumns[2]));
                    count++;
                }
                if (existingColumns.contains(settingsColumns[3])) {
                    settings.setMonitorRefreshRate(rs.getInt(settingsColumns[3]));
                    count++;
                }
                if (existingColumns.contains(settingsColumns[4])) {
                    settings.setMonitorMessagesDuration(rs.getInt(settingsColumns[4]));
                    count++;
                }
                if (existingColumns.contains(settingsColumns[5])) {
                    settings.setCommandSchedulerTaskInterval(rs.getInt(settingsColumns[5]));
                    count++;
                }
                if (existingColumns.contains(settingsColumns[6])) {
                    settings.setUseConsoleColors(rs.getInt(settingsColumns[6]) == 1);
                    count++;
                }
                if (existingColumns.contains(settingsColumns[7])) {
                    settings.setClearConsoleBeforeMenu(rs.getInt(settingsColumns[7]) == 1);
                    count++;
                }
                if (existingColumns.contains(settingsColumns[8])) {
                    settings.setLogOutput(rs.getInt(settingsColumns[8]) == 1);
                    count++;
                }
                if (existingColumns.contains(settingsColumns[9])) {
                    settings.setPrintBackgroundInfoToConsole(rs.getInt(settingsColumns[9]) == 1);
                    count++;
                }
                if (existingColumns.contains(settingsColumns[10])) {
                    settings.setBackgroundInfoTimeStampsInConsole(rs.getInt(settingsColumns[10]) == 1);
                    count++;
                }
                if (existingColumns.contains(settingsColumns[11])) {
                    settings.setStorageType(rs.getString(settingsColumns[11]));
                    count++;
                }
                outdated = count != settingsColumns.length || existingColumns.size() != settingsColumns.length;
                existingColumns.clear();
            }
            //Load servers safely
            serverList = new HashMap();
            rs = c.createStatement().executeQuery("SELECT " + "*" + " FROM Servers");
            if (!rs.isClosed()) {
                for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                    existingColumns.add(rs.getMetaData().getColumnName(i + 1));
                }
                
                while (rs.next()) {
                    count = 0;
                    if (existingColumns.contains(serversColumns[0]) && existingColumns.contains(serversColumns[1])) {
                        count += 2;
                        Server s = new Server(rs.getString(serversColumns[0]), serversColumns[1]);
                        if (existingColumns.contains(serversColumns[2])) {
                            s.setLastPing((long) rs.getInt(serversColumns[2]));
                            count++;
                        }
                        if (existingColumns.contains(serversColumns[3])) {
                            s.setFileUpdateInterval((long) rs.getInt(serversColumns[3]));
                            count++;
                        }
                        serverList.put(s.getName(), s);
                    } else {
                        Printer.printError("Storage", "Missing server name or path columns in the Servers Table from the sqlite data file", null);
                        break;
                    }
                }

                outdated = outdated ? true : existingColumns.size() != serversColumns.length || count != serversColumns.length;
                existingColumns.clear();
            }
            rs = c.createStatement().executeQuery("SELECT " + "*" + " FROM Servers_Settings");
            if (!rs.isClosed()) {
                for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                    existingColumns.add(rs.getMetaData().getColumnName(i + 1));
                }
                while (rs.next()) {
                    count = 0;
                    if (existingColumns.contains(serversSettingsColumns[0])) {
                        String name = rs.getString(serversSettingsColumns[0]);
                        Server s = serverList.get(name);
                        if (s != null) {
                            count++;
                            if (existingColumns.contains(serversSettingsColumns[1])) {
                                s.getSettings().setStartRam(rs.getString(serversSettingsColumns[1]));
                                count++;
                            }
                            if (existingColumns.contains(serversSettingsColumns[2])) {
                                s.getSettings().setMaxRam(rs.getString(serversSettingsColumns[2]));
                                count++;
                            }
                            if (existingColumns.contains(serversSettingsColumns[3])) {
                                s.getSettings().setStartIfOffline(rs.getInt(serversSettingsColumns[3]) == 1);
                                count++;
                            }
                            if (existingColumns.contains(serversSettingsColumns[4])) {
                                s.getSettings().setRestartIfNotResponding(rs.getInt(serversSettingsColumns[4]) == 1);
                                count++;
                            }
                            if (existingColumns.contains(serversSettingsColumns[5])) {
                                s.getSettings().setMaxStartingDuration((long) rs.getInt(serversSettingsColumns[5]));
                                count++;
                            }
                            if (existingColumns.contains(serversSettingsColumns[6])) {
                                s.getSettings().setMaxStoppingDuration((long) rs.getInt(serversSettingsColumns[6]));
                                count++;
                            }
                            if (existingColumns.contains(serversSettingsColumns[7])) {
                                s.getSettings().setStopCommand(rs.getString(serversSettingsColumns[7]));
                                count++;
                            }
                        } else {
                            Printer.printError("Storage", "Unknown server name \"" + name + "\"  mentioned in the Servers_Settings Table from the sqlite data file", null);
                            continue;
                        }
                    } else {
                        Printer.printError("Storage", "Missing server name column in the Servers_Settings Table from the sqlite data file", null);
                        break;
                    }
                }
                outdated = outdated ? true : existingColumns.size() != serversSettingsColumns.length || count != serversSettingsColumns.length;
                existingColumns.clear();
            }
            rs = c.createStatement().executeQuery("SELECT " + "*" + " FROM Servers_Timed_Commands");
            if (!rs.isClosed()) {
                for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                    existingColumns.add(rs.getMetaData().getColumnName(i + 1));
                }

                while (rs.next()) {
                    count = 0;
                    if (existingColumns.contains(serversTimedCommandsColumns[0])) {
                        String name = rs.getString(serversTimedCommandsColumns[0]);
                        Server s = serverList.get(name);
                        if (s != null) {
                            List<TimedCommand> tcls = s.getTimedCommands();
                            count++;
                            if (existingColumns.contains(serversTimedCommandsColumns[1]) && existingColumns.contains(serversTimedCommandsColumns[2])) {
                                count += 2;
                                TimedCommand tc = new TimedCommand(rs.getString(serversTimedCommandsColumns[2]), new Timing(rs.getString(serversTimedCommandsColumns[1])));
                                synchronized (tcls) {
                                    tcls.add(tc);
                                }
                                count++;
                            } else {
                                Printer.printError("Storage", "Missing timing or command column in the Servers_Timed_Commands Table from the sqlite data file ", null);
                                break;
                            }
                        } else {
                            Printer.printError("Storage", "Unknown server name \"" + name + "\"  mentioned in the Servers_Timed_Commands Table from the sqlite data file", null);
                            continue;
                        }
                    } else {
                        Printer.printError("Storage", "Missing server name column in the Servers_Timed_Commands Table from the sqlite data file", null);
                        break;
                    }
                }
                outdated = outdated ? true : existingColumns.size() != serversTimedCommandsColumns.length || count != serversTimedCommandsColumns.length;
            }
            if (outdated) {
                Printer.printBackgroundInfo("Storage", "The sqlite data file is corrupt or outdated. updating the file.");
                getFile().delete();
                saveData();
            }
            return true;
        } catch (SQLException e) {
            Printer.printError("Storage", "Failed to load data from file.", e);
            return false;
        }
    }

    public boolean saveData() {
        try {
            if (c == null) {
                c = DriverManager.getConnection("jdbc:sqlite:" + getFile().getName());
            }
            c.createStatement().executeUpdate(schema);
            //Check the tables
            //Store Program Settings
            String settingsValues = "VALUES (" + settings.getCommunicationDir().getAbsolutePath()
                    + "," + settings.getServerStateUpdaterTaskInterval()
                    + "," + settings.getMonitorRefreshRate()
                    + "," + settings.getMonitorMessagesDuration()
                    + "," + settings.getCommandSchedulerTaskInterval()
                    + "," + (settings.isUseConsoleColors() ? 1 : 0)
                    + "," + (settings.isClearConsoleBeforeMenu() ? 1 : 0)
                    + "," + (settings.isLogOutput() ? 1 : 0)
                    + "," + (settings.isPrintBackgroundInfoToConsole() ? 1 : 0)
                    + "," + (settings.isBackgroundInfoTimeStampsInConsole() ? 1 : 0)
                    + ",'" + settings.getStorageType() + "')";
            c.createStatement().executeUpdate("INSERT OR REPLACE INTO Program_Settings (" + getStringFromArray(settingsColumns, ',') + ") " + settingsValues + ";");
            //Store Server info
            synchronized (serverList) {
                for (Server s : serverList.values()) {
                    String serverValues = "VALUES('" + s.getName() + "'"
                            + ",'" + s.getFile().getAbsolutePath() + "'"
                            + "," + s.getLastPing()
                            + "," + s.getFileUpdateInterval() + ")";
                    c.createStatement().executeUpdate("INSERT OR REPLACE INTO Servers (" + getStringFromArray(serversColumns, ',') + ") " + serverValues + ";");
                    //Store server's settings
                    ServerSettings st = s.getSettings();
                    String serverSettingsValues = "VALUES ('" + s.getName() + "'"
                            + "," + (st.getStartRam() == null ? "NULL" : "'" + st.getStartRam() + "'")
                            + "," + (st.getMaxRam() == null ? "NULL" : "'" + st.getMaxRam() + "'")
                            + "," + (st.isStartIfOffline() ? 1 : 0)
                            + "," + (st.isRestartIfNotResponding() ? 1 : 0)
                            + "," + st.getMaxStartingDuration()
                            + "," + st.getMaxStoppingDuration() + ")";
                    c.createStatement().executeUpdate("INSERT OR REPLACE INTO Servers_Settings (" + getStringFromArray(serversSettingsColumns, ',') + ") " + serverSettingsValues + ";");
                    //Store server's timed commands
                    List<TimedCommand> tcs = s.getTimedCommands();
                    c.createStatement().executeUpdate("DELETE FROM Servers_Timed_Commands;");
                    synchronized (tcs) {
                        for (TimedCommand tc : tcs) {
                            String tcValues = "'" + s.getName() + "','" + tc.getCommand() + "','" + tc.getTime().toString() + "'";
                            c.createStatement().executeUpdate("INSERT INTO Servers_Timed_Commands (" + getStringFromArray(serversTimedCommandsColumns, ',') + ") " + tcValues + ";");
                        }
                    }

                }
            }
            return true;
        } catch (SQLException e) {
            Printer.printError("Storage", "Failed to save data to file.", e);
            return false;
        }
    }
     */