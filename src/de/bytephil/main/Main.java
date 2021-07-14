package de.bytephil.main;


import de.bytephil.enums.MessageType;
import de.bytephil.threads.UpdateThread;
import de.bytephil.utils.Console;
import io.javalin.Javalin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class Main {
    private static Javalin app;
    private static java.lang.Thread thread;

    public static void main(String[] args) {
        App();
    }

    private static String password = "Admin!";
    private static ArrayList<String> clients = new ArrayList<>();
    private static ArrayList<String> logtIn = new ArrayList<>();

    public static void App() {
        Javalin app = Javalin.create(config -> {
            config.addStaticFiles("/public");

        }).start();
        Main.app = app;
        thread = UpdateThread.thread;
       // thread.start();

        app.ws("/websockets", ws -> {
            ws.onConnect(ctx -> {
                Console.printout("[/websockets] Client connected with Session-ID: " + ctx.getSessionId() + " IP: " + ctx.session.getRemoteAddress(), MessageType.INFO);
                clients.add(ctx.getSessionId());
               /* if (!thread.isAlive()) {
                    thread.stop();
                    thread.start();
                }
                //TODO Fixing strange Error
                */
            });
            ws.onClose(ctx -> {
                Console.printout("[/websockets] Client disconnected (Session-ID: " + ctx.getSessionId() + ")", MessageType.INFO);
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
                        System.out.println("Correct password typed!");
                    }
                    else {
                        ctx.send("WRONG");
                        System.out.println("Wrong password typed!");
                    }
                }
            });
        });

        app.ws("/login", ws -> {
            ws.onConnect(ctx -> {
                Console.printout("[/login] Client connected with Session-ID: " + ctx.getSessionId() + " IP: " + ctx.session.getRemoteAddress(), MessageType.INFO);
            });
            ws.onClose(ctx -> {
                Console.printout("[/login] Client disconnected (Session-ID: " + ctx.getSessionId() + ")", MessageType.INFO);
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
    static String FILEPATH = "/public/files";
    static File file = new File(FILEPATH);
    static void writeByte(byte[] bytes)
    {
        try {

            // Initialize a pointer
            // in file using OutputStream
            OutputStream
                    os
                    = new FileOutputStream(file);

            // Starts writing the bytes in it
            os.write(bytes);
            System.out.println("Successfully"
                    + " byte inserted");

            // Close the file
            os.close();
        }

        catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }
}
