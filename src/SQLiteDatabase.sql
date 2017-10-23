/**
 * Author:  OmarAlama
 * Created: Aug 1, 2017
 */
PRAGMA user_version=1;
CREATE TABLE IF NOT EXISTS Servers (
    Server_Name TEXT NOT NULL UNIQUE,
    Server_File  TEXT NOT NULL,
    Last_Ping BIGINT,
    File_Update_Interval BIGINT
);
CREATE TABLE IF NOT EXISTS Servers_Timed_Commands (
    Server_Name TEXT NOT NULL,
    Timing TEXT NOT NULL,
    Command TEXT NOT NULL
);
CREATE TABLE IF NOT EXISTS Servers_Settings (
    Server_Name TEXT NOT NULL UNIQUE,
    Start_RAM TEXT,
    Max_RAM TEXT,
    Start_if_offline INT DEFAULT 0 NOT NULL CHECK(Start_if_offline == 0 OR Start_if_offline ==1),
    Restart_if_not_responding INT DEFAULT 0 NOT NULL CHECK(Start_if_offline == 0 OR Start_if_offline ==1),
    Max_starting_duration BIGINT DEFAULT 90000 NOT NULL CHECK(Max_stopping_duration > 0),
    Max_stopping_duration BIGINT DEFAULT 30000 NOT NULL CHECK(Max_stopping_duration > 0),
    Stop_command TEXT DEFAULT 'stop' NOT NULL
);
CREATE TABLE IF NOT EXISTS Program_Settings (
    ID INT NOT NULL PRIMARY KEY,
    Communication_dir TEXT NOT NULL,
    Server_State_Updater_Task_Interval BIGINT DEFAULT 10000 NOT NULL CHECK(Server_State_Updater_Task_Interval > 0),
    Monitor_Refresh_Rate BIGINT DEFAULT 3000 NOT NULL CHECK(Monitor_Refresh_Rate > 0),
    Monitor_Messages_Duration BIGINT DEFAULT 30000 NOT NULL CHECK(Monitor_Messages_Duration > 0),
    Command_Scheduler_Task_Interval BIGINT DEFAULT 1800000 NOT NULL CHECK(Command_Scheduler_Task_Interval > 0),
    Use_Console_Colors INT DEFAULT 1 NOT NULL CHECK(Use_Console_Colors == 0 OR Use_Console_Colors ==1),
    Clear_Console_Before_Menu INT DEFAULT 1 NOT NULL CHECK(Clear_Console_Before_Menu == 0 OR Clear_Console_Before_Menu ==1),
    Log_Output INT DEFAULT 1 NOT NULL CHECK(Log_Output == 0 OR Log_Output ==1),
    Print_Background_Info_To_Console INT DEFAULT 1 NOT NULL CHECK(Print_Background_Info_To_Console == 0 OR Print_Background_Info_To_Console ==1),
    Background_Info_Time_Stamps_In_Console INT DEFAULT 1 NOT NULL CHECK(Background_Info_Time_Stamps_In_Console == 0 OR Background_Info_Time_Stamps_In_Console ==1),
    Storage_Type TEXT DEFAULT 'sqlite' NOT NULL
);
CREATE TABLE IF NOT EXISTS Backup_Profiles (
    Name TEXT NOT NULL UNIQUE,
    Directory TEXT,
    Exclude_List TEXT,
    Include_List TEXT
);