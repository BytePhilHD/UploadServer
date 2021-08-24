package de.bytephil.main;

import de.bytephil.enums.MessageType;
import de.bytephil.threads.UpdateThread;
import de.bytephil.utils.Console;
import de.bytephil.utils.ServerConfiguration;
import io.javalin.Javalin;

import java.io.*;
import java.util.ArrayList;

public class Main {
    private static Javalin app;
    private static java.lang.Thread thread;

    private static Main instance;

    public static Main getInstance() {
        return instance;
    }
    public Main() {
        instance = this;
    }

    private static String password;
    private static ArrayList<String> clients = new ArrayList<>();
    private static ArrayList<String> logtIn = new ArrayList<>();

    private ServerConfiguration config;
    public String version = "0.0.2";
    public boolean debugMSG = false;

    public void start() throws IOException {

        if (!new File("server.cfg").exists()) {
            de.bytephil.utils.Console.printout("The config file is missing! Creating default one.", MessageType.WARNING);
            final File newFile = new File("server.cfg");
            copyFile(newFile, "default.cfg");
        }

        // Load config
        config = new ServerConfiguration("server.cfg");
        if (config.loaded) {
            Console.printout("Config was successfully loaded!", MessageType.INFO);
            password = config.password;
            debugMSG = config.debugMSG;
        } else {
            Console.printout("Config not loaded! Using default.", MessageType.WARNING);
        }

        Javalin app = Javalin.create(config -> {
            config.addStaticFiles("/public");
        }).start(config.port);

        if (debugMSG) {
            Console.printout("Debug Messages Enabled! To turn off, change \"debugMSG=true\" to \"debugMSG=false\" in your server.cfg!", MessageType.WARNING);
        }


        Main.app = app;
        thread = UpdateThread.thread;
        // thread.start();

        app.ws("/websockets", ws -> {
            ws.onConnect(ctx -> {
                Console.printout("[/websockets] Client connected with Session-ID: " + ctx.getSessionId() + " IP: " + ctx.session.getRemoteAddress(), MessageType.DEBUG);
                clients.add(ctx.getSessionId());
               /* if (!thread.isAlive()) {
                    thread.stop();
                    thread.start();
                }
                //TODO Fixing strange Error
                */
            });
            ws.onClose(ctx -> {
                Console.printout("[/websockets] Client disconnected (Session-ID: " + ctx.getSessionId() + ")", MessageType.DEBUG);
                clients.remove(ctx.getSessionId());
                /*if (clients.size() == 0 && thread.isAlive()) {
                    thread.stop();
                }
                 */
            });
            ws.onMessage(ctx -> {
                String message = ctx.message();

                if (message.contains("PASSWORD")) {
                    message = message.replace("PASSWORD: ", "");
                    if (message.equalsIgnoreCase(password)) {
                        ctx.send("CORRECT " + ctx.getSessionId());
                        logtIn.add(ctx.getSessionId());
                        Console.printout("User logged in successfully (Session-ID: " + ctx.getSessionId(), MessageType.INFO);
                    } else {
                        ctx.send("WRONG");
                    }
                }
            });
        });

        //TODO - home.html file input at line 25 (currently not creating a imageURL maybe because file is not found with document.upload ...)

        app.ws("/login", ws -> {
            ws.onConnect(ctx -> {
                Console.printout("[/login] Client connected with Session-ID: " + ctx.getSessionId() + " IP: " + ctx.session.getRemoteAddress(), MessageType.DEBUG);
            });
            ws.onClose(ctx -> {
                Console.printout("[/login] Client disconnected (Session-ID: " + ctx.getSessionId() + ")", MessageType.DEBUG);
            });
            ws.onMessage(ctx -> {
                String message = ctx.message();
                if (message.contains("LOGIN")) {
                    message = message.replace("LOGIN: ", "").replace("?", "");

                    if (logtIn.contains(message)) {
                        logtIn.add(ctx.getSessionId());
                        logtIn.remove(message);
                    } else {
                        ctx.send("CLOSE");
                    }
                } else if (message.contains("FILE")) {
                    String url = message.replace("FILE: ", "").replace("blob:", "");
                    System.out.println(url);
                }
            });
            ws.onError(ctx -> {
                Console.printout(ctx.error().toString(), MessageType.ERROR);
            });
        });

        app.get("/home", ctx -> {
            ctx.render("/public/home.html");
        });
        app.get("/logout", ctx -> {
            ctx.render("/public/logout.html");
        });
    }

    public void copyFile(File newFile, String existingFile) throws IOException {
        newFile.createNewFile();
        final FileOutputStream configOutputStream = new FileOutputStream(newFile);
        byte[] buffer = new byte[4096];
        final InputStream defaultConfStream = getClass().getClassLoader().getResourceAsStream(existingFile);
        int readBytes;
        while ((readBytes = defaultConfStream.read(buffer)) > 0) {
            configOutputStream.write(buffer, 0, readBytes);
        }
        defaultConfStream.close();
    }

}

