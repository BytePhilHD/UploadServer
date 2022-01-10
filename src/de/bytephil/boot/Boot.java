package de.bytephil.boot;

import de.bytephil.main.Main;

import java.io.IOException;
import java.net.URISyntaxException;

public class Boot {
    public static void main(String[] args) throws IOException, URISyntaxException {
        new Main().startUp();
    }
}
