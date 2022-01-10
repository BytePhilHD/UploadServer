package de.bytephil.services;

import de.bytephil.enums.MessageType;
import de.bytephil.utils.Console;

import java.io.File;
import java.sql.*;
import java.util.Arrays;

public class MySQLService {
    private static Connection con = null;

    public static void startMySQL() {
        String dbName = "test";
        String dbUserName = "root";
        String dbPassword = "Collins234!";
        String address = "192.168.178.89:3306";
        String connectionString = "jdbc:mysql://" + address + "/" + dbName + "?user=" + dbUserName + "&password=" + dbPassword;

        try {
            Console.printout("[MySQL] Trying to connect to " + address + " with user " + dbUserName + " ...", MessageType.INFO);
            con = DriverManager.getConnection(connectionString);
            Statement stmt = con.createStatement();
            ResultSet rst = stmt.executeQuery("/* ping */ SELECT 1");
            rst.next();
            Console.printout("[MySQL] Connection established! Ping to Server: " + rst.getInt(1) + "ms", MessageType.INFO);
            //mySQLService();
        } catch (SQLException e) {
            Console.printout("[MySQL] Error while trying to connect to given database!", MessageType.ERROR);
        }
        addApply("Philipp", "BytePhil", "Tester");
        //System.out.println("Test " + getFromMySQL("ApplyServer"));
    }

    public static void addApply(String name, String discord, String job) {
        try {
            Statement stmt = con.createStatement();
            int number = 2;
            String state = "Nostate";

            stmt.executeUpdate("INSERT INTO `ApplyServer`(`Number`, `Name`, `Discord`, `Job`, `State`) VALUES (\"" + number + "\"" +
                    ",\"" + name + "\"" +
                    ",\"" + discord + "\"" +
                    ",\"" + job + "\"" +
                    ",\"" + state + "\")");
        } catch (SQLException e1) {
            Console.printout("[MySQL] Error while trying to add Data! " + e1.getMessage(), MessageType.ERROR);
        }

    }

    public static String getFromMySQL(String table)  {
        try {
            Statement stmt = con.createStatement();
            ResultSet rst = stmt.executeQuery("SELECT * FROM `" + table + "`");
            rst.next();
            String received = rst.getString(1);
            return received;
        } catch (SQLException e1) {
            Console.printout("[MySQL] Error while trying to get Data from " + table + "!", MessageType.ERROR);
        }
        return null;
    }
}
