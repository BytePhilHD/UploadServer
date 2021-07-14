package de.bytephil.main;


import de.bytephil.enums.MessageType;
import de.bytephil.utils.Console;
import io.javalin.Javalin;

import java.awt.*;
import java.net.InetSocketAddress;
import java.util.ArrayList;

public class Main {
    private static Javalin app;

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

        app.ws("/websockets", ws -> {
            ws.onConnect(ctx -> {
                Console.printout("Client connected with Session-ID: " + ctx.getSessionId() + " IP: " + ctx.session.getRemoteAddress(), MessageType.INFO);
                clients.add(ctx.getSessionId());
            });
            ws.onClose(ctx -> {
                Console.printout("Client disconnected (Session-ID: " + ctx.getSessionId() + ")", MessageType.INFO);
                clients.remove(ctx.getSessionId());
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
                /*if (!logtIn.contains(ctx.session.getRemoteAddress())) {
                    ctx.send("CLOSE");
                } */
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
                }
            });
        });

        app.get("/home", ctx -> {
            ctx.render("/public/home.html");
        });
        app.get("/logout", ctx -> {
            ctx.render("/public/logout.html");
        });
    }
}
