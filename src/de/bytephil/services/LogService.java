package de.bytephil.services;

import de.bytephil.enums.MessageType;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogService {

    public void logFileCreation() throws IOException {
        if (!new File("logs/log.txt").exists()) {
            File dir = new File("logs");
            if (!dir.exists()) dir.mkdirs();
            de.bytephil.utils.Console.printout("The log file is missing. Creating a new one.", MessageType.WARNING);
            final File newFile = new File("logs/log.txt");
            newFile.createNewFile();
        }
    }

    public void writetoFile(File fileName, String write, MessageType messageType) throws IOException {
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        FileWriter fr = new FileWriter(fileName, true);
        BufferedWriter br = new BufferedWriter(fr);
        PrintWriter pr = new PrintWriter(br);
        pr.println("[" + time + "] " + messageType + " - " + write);
        pr.close();
        br.close();
        fr.close();
    }
}
