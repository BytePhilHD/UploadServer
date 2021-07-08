package de.bytephil.main;

import io.javalin.Javalin;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        App();
    }

    public static void App() {
        Javalin app = Javalin.create(config -> {
            config.addStaticFiles("/public");

        }).start();
    }
}
