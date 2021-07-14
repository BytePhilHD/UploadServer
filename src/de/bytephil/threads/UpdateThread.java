package de.bytephil.threads;

import io.javalin.websocket.WsConnectContext;

import java.util.ArrayList;

public class UpdateThread {

    public static java.lang.Thread thread = new java.lang.Thread() {
        @Override
        public void run() {
            int u = 1;
            while (thread.isAlive()) {
                    //DO STUFF!
                System.out.println("TEST");
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
}
