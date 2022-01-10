package de.bytephil.utils;

import de.bytephil.enums.MessageType;
import de.bytephil.main.Main;
import de.bytephil.services.LogService;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Console {
    public static void printout(String message, MessageType type) {

        if (type == MessageType.DEBUG && !Main.getInstance().debugMSG) {
            return;
        }
        if (type == MessageType.ERROR) {
            System.out.println("[" + getTime() + "] " + type + " - ");
            System.out.println("[" + getTime() + "] " + type + " - " + message);
            System.out.println("[" + getTime() + "] " + type + " - ");
        } else {
            System.out.println("[" + getTime() + "] " + type + " - " + message);
        }
        try {
            new LogService().writetoFile(new File("logs/log.txt"), message, type);
        } catch (IOException e1) {}
    }

    public static void empty() {
        System.out.println("");
    }

    private static String getTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public static void sendBanner() {
        System.out.println("        ___                __      _____                          ");
        System.out.println("       /   |  ____  ____  / /_  __/ ___/___  ______   _____  _____");
        System.out.println("      / /| | / __ \\/ __ \\/ / / / /\\__ \\/ _ \\/ ___/ | / / _ \\/ ___/");
        System.out.println("     / ___ |/ /_/ / /_/ / / /_/ /___/ /  __/ /   | |/ /  __/ /    ");
        System.out.println("    /_/  |_/ .___/ .___/_/\\__, //____/\\___/_/    |___/\\___/_/     ");
        System.out.println("          /_/   /_/      /____/                                   ");
        System.out.println(" ");
        System.out.println("           Version: " + Main.getInstance().version + " | Made by BytePhil | https://bytephil.de/");
        System.out.println(" ");
    }
}
