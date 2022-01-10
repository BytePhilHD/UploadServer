package de.bytephil.main;

import de.bytephil.enums.MessageType;
import de.bytephil.services.FileService;
import de.bytephil.services.LogInService;
import de.bytephil.services.LogService;
import de.bytephil.users.Application;
import de.bytephil.users.ApplicationService;
import de.bytephil.utils.*;
import de.bytephil.utils.Console;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

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

    public static String password;
    public static String username;
    private static ArrayList<String> clients = new ArrayList<>();
    private static ArrayList<String> logtIn = new ArrayList<>();

    public static ServerConfiguration config;
    public final String version = "0.0.3";
    public boolean debugMSG = false;
    public boolean testing = false;

    public void startUp() throws IOException, URISyntaxException {
        //checkandCreateFile("data/Applications.txt", "data");

        new FileService().copyDirectoryfromResources("public/", "WebPages");

        new LogService().logFileCreation();
        new LogService().writetoFile(new File("logs/log.txt"), "The program is trying to start...", MessageType.INFO);

        if (!new File("server.cfg").exists()) {
            de.bytephil.utils.Console.printout("The config file is missing! Creating default one.", MessageType.WARNING);
            final File newFile = new File("server.cfg");
            copyFile(newFile, "default.cfg");
        }

        checkFolders();

        // Load config
        config = new ServerConfiguration("server.cfg");
        if (config.loaded) {
            Console.printout("Config was successfully loaded!", MessageType.INFO);
            password = config.password;
            debugMSG = config.debugMSG;
            username = config.username;
        } else {
            Console.printout("Config not loaded! Using default.", MessageType.WARNING);
        }

        Console.sendBanner();

        if (new File("testing.xyz").exists()) {
            testing = true;
            Console.empty();
            Console.printout("Test Mode active! To disable, delete the file \"testing.xyz\" from the folder!", MessageType.WARNING);
            Console.empty();
            new FileService().copyDirectoryfromResources("public/", "WebPages");

        }

        // TEST
        //
        // TEST
        startApp();
    }


    public void startApp() throws IOException {

        Javalin app = Javalin.create(config -> {
            config.addStaticFiles("WebPages", Location.EXTERNAL);
            config.server(() -> {
                Server server = new Server();
                ArrayList<Connector> connectors = new ArrayList<>();
                if (this.config.https) {
                    ServerConnector sslConnector = new ServerConnector(server, getSslContextFactory());
                    sslConnector.setPort(this.config.sslPort);
                    connectors.add(sslConnector);
                }
                if (this.config.http) {
                    ServerConnector connector = new ServerConnector(server);
                    connector.setPort(this.config.port);
                    connectors.add(connector);
                }
                server.setConnectors(connectors.toArray(new Connector[0]));
                return server;
            });
            config.showJavalinBanner = false;
        }).start();

        new LogService().writetoFile(new File("logs/log.txt"), "The program successfully started!", MessageType.INFO);
        if (debugMSG) {
            Console.printout("Debug Messages Enabled! To turn off, change \"debugMSG=true\" to \"debugMSG=false\" in your server.cfg!", MessageType.WARNING);
        }


        Main.app = app;

        app.ws("/checklogin", ws -> {
            ws.onConnect(ctx -> {
                Console.printout("[/websockets] Client connected with Session-ID: " + ctx.getSessionId() + " IP: " + ctx.session.getRemoteAddress(), MessageType.DEBUG);
                new LogService().writetoFile(new File("logs/log.txt"), "Client connected with Session-ID: " + ctx.getSessionId() + " IP: " + ctx.session.getRemoteAddress(), MessageType.INFO);
                clients.add(ctx.getSessionId());
            });
            ws.onClose(ctx -> {
                Console.printout("[/websockets] Client disconnected (Session-ID: " + ctx.getSessionId() + ")", MessageType.DEBUG);
                clients.remove(ctx.getSessionId());
            });
            ws.onMessage(ctx -> {
                String message = ctx.message();

                if (message.contains("LOGIN")) {
                    message = message.replace("LOGIN: ", "");
                    if (LogInService.login(message)) {
                        ctx.send("CORRECT " + ctx.getSessionId());
                        logtIn.add(ctx.getSessionId());
                        Console.printout("User logged in successfully (Session-ID: " + ctx.getSessionId() + ", IP: " + ctx.session.getRemoteAddress(), MessageType.INFO);
                        new LogService().writetoFile(new File("logs/log.txt"), "Client logged in successfully with IP: " + ctx.session.getRemoteAddress(), MessageType.INFO);
                    } else {
                        ctx.send("WRONG");
                    }
                }
            });
        });

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
                        List<Application> applications = new ApplicationService().applications;
                        //for (int i = 0; i < applications.size(); i++) {
                        for (Application application : applications) {
                            ctx.send(application.getName() + "|*|" + application.getEmail() + "|'|" + application.getJob());
                        }
                    } else {
                        ctx.send("CLOSE");
                        new LogService().writetoFile(new File("logs/log.txt"), "Client disconnected with IP: " + ctx.session.getRemoteAddress(), MessageType.INFO);
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

        app.ws("/verify", ws -> {
            ws.onMessage(ctx -> {
                String message = ctx.message().replace("?", "");
                if (AccountManager.checkVerify(message)) {
                    ctx.send("VERIFIED");
                }
                else {
                    ctx.send("WRONG");
                }
            });
        });
        app.ws("/register", ws -> {
            ws.onMessage(ctx -> {
                String message = ctx.message();
                boolean alreadyExists = WebSocketHandler.createAccount(message);
                if (!alreadyExists) {
                    ctx.send("ALREADY");
                } else {
                    ctx.send("SENT");
                }
            });
        });
        app.ws("/application", ws -> {
            ws.onMessage(ctx -> {
                String message = ctx.message();
                WebSocketHandler.createApplication(message);
            });
        });
        app.ws("/data", ws -> {
            ws.onMessage(ctx -> {

            });
        });
        app.get("/login", ctx -> {
            ctx.render("/public/login.html");
        });
        app.get("/home", ctx -> {
            ctx.render("/public/home.html");
        });
        app.get("/verify", ctx -> {
            ctx.render("/public/verify.html");
        });
        app.get("/registration", ctx -> {
            ctx.render("/public/registration.html");
        });
        app.get("/apply", ctx -> {
            ctx.render("/public/apply.html");
        });
    }

    public void checkFolders() {
        File dir = new File("data/user");
        if (!dir.exists()) dir.mkdirs();
        dir = new File("data/apply");
        if (!dir.exists()) dir.mkdirs();
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

    public static void checkandCreateFile(String path, String folder) throws IOException {
        if (!new File(path).exists()) {
            File dir = new File(folder);
            if (!dir.exists()) dir.mkdirs();
            final File newFile = new File(path);
            newFile.createNewFile();
        }
    }

    private SslContextFactory getSslContextFactory() {
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(config.keystorePath);
        sslContextFactory.setKeyStorePassword(config.keystorePW);
        return sslContextFactory;
    }

}