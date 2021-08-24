package de.bytephil.utils;

import de.bytephil.enums.MessageType;
import de.bytephil.main.Main;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Console {

    public static void printout(String message, MessageType type) {
        if (!Main.getInstance().debugMSG) {
            return;
        }
        System.out.println("[" + getTime() + "] " + type + " - " + message);
    }
    public static void empty() {
        System.out.println("");
    }
    private static String getTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
