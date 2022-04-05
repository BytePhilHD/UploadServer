package de.bytephil.main;

import de.bytephil.enums.MessageType;
import de.bytephil.enums.Rank;
import de.bytephil.services.EmailService;
import de.bytephil.services.FileService;
import de.bytephil.services.LogInService;
import de.bytephil.services.LogService;
import de.bytephil.users.Application;
import de.bytephil.users.ApplicationService;
import de.bytephil.users.User;
import de.bytephil.users.UserService;
import de.bytephil.utils.*;
import de.bytephil.utils.Console;
import io.javalin.Javalin;
import io.javalin.core.security.AccessManager;
import io.javalin.core.util.FileUtil;
import io.javalin.http.Cookie;
import io.javalin.http.UploadedFile;
import io.javalin.http.staticfiles.Location;
import jline.internal.Log;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Main {

    private static Javalin app;

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
    private static HashMap<String, String> cookies = new HashMap<>();
    private static HashMap<String, String> cookiesRenew = new HashMap<>();
    private static ArrayList<UploadedFile> uploadedFiles = new ArrayList<>();

    public static ServerConfiguration config;
    public final String version = "0.0.6";
    public boolean debugMSG = false;
    public boolean testing = false;

    public void startUp() throws IOException, URISyntaxException {
        //checkandCreateFile("data/Applications.txt", "data");

        new FileService().copyDirectoryfromResources("public/", "WebPages");

        new LogService().logFileCreation();
        new LogService().writetoFile(new File("logs/log.txt"), "The program is trying to start...", MessageType.INFO);

        Console.sendBanner();

        if (!new File("server.cfg").exists()) {
            de.bytephil.utils.Console.printout("The config file is missing! Creating default one.", MessageType.WARNING);
            final File newFile = new File("server.cfg");
            copyFile(newFile, "default.cfg");
            firstStart();
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

        if (new File("testing.xyz").exists()) {                                 // If file "testing.xyz" exists the test mode gets activated
            debugMSG = true;
            testing = true;
            Console.empty();
            Console.printout("Test Mode active! To disable, delete the file \"testing.xyz\" from the folder!", MessageType.WARNING);
            Console.empty();
            new FileService().copyDirectoryfromResources("public/", "WebPages");

            //new Test().testMethod();
        }

        // TEST

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

        // LOGIN PAGE

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
                    if (LogInService.login(message, ctx.getSessionId())) {
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

        // HOME PAGE

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

                        String id = ctx.cookie("id");               // Changing the cookie value from null to the old session id
                        cookies.replace(id, message);

                        String id1 = ctx.cookie("id1");               // Changing the cookie value from null to the session id
                        cookiesRenew.replace(id1, ctx.getSessionId());

                        String username = getUsername(message);             // Getting
                        String rank = getRank(username);
                        ctx.send("IN*FO" + username + "|*|" + rank);

                        String oldvalue = LogInService.loggedinUsers.get(message);
                        LogInService.loggedinUsers.remove(message);
                        LogInService.loggedinUsers.put(ctx.getSessionId(), oldvalue);

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
                } else {
                    ctx.send("WRONG");
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
            // Cookie 1 (for identifying the user later on upload)
                String id = PasswordGenerator.generateID(10);
                ctx.cookie("id", id);
                cookies.put(id, "null");
            // Cookie 2 (for getting the current SessionID for re-login after upload)
            ctx.cookie("id1", id);
            cookiesRenew.put(id, "null");

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
        app.post("/upload", ctx -> {
            ctx.render("/public/home.html");
            ctx.uploadedFiles("files").forEach(uploadedFile -> {
                String time = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
                if (uploadedFile == null) {
                    return;
                }
                FileUtil.streamToFile(uploadedFile.getContent(), "upload/" + time + "/" + uploadedFile.getFilename());
                Console.printout("Saved file", MessageType.DEBUG);
                uploadedFiles.add(uploadedFile);
                String cookieID = ctx.cookie("id");

                String id = cookies.get(cookieID);

                User user = new UserService().getUserByName(LogInService.loggedinUsers.get(id));
                /*
                EmailService.send(user.getEmail(), "Uploaded file on UploadServer", "Hi " + user.getName() + "! \n \n" +
                        "You just uploaded a file (" + uploadedFile.getFilename() + ") to the UploadServer system. \n " +
                        "It is now stored at " + Main.config.address + ". Warning: This service may not be safe and is for personal use only! \n \n" +
                        "Made by BytePhil.de");

                 */
                ctx.removeCookie("id");
                ctx.removeCookie("id1");
                ctx.redirect("/home?" + cookiesRenew.get(cookieID));

            });
        });
        while (true) {
            Console.reader();
        }
    }

    public String getUsername(String sessionID) {
        String username = LogInService.loggedinUsers.get(sessionID);

        try {
            username = new UserService().getUserByName(username).getName();
        } catch (Exception e1) {
            Console.printout(e1.getMessage(), MessageType.ERROR);
        }
        return username;
    }


    public String getRank(String username) {
        try {
            String rank = new UserService().getUserByName(username).getRank().toString();
            rank.toLowerCase();
            rank.substring(0, 1).toUpperCase();

            return rank;
        } catch (Exception e1) {
            return null;
        }
    }

    public void checkFolders() {
        File dir = new File("data/user");
        if (!dir.exists()) dir.mkdirs();
        dir = new File("data/apply");
        if (!dir.exists()) dir.mkdirs();
        dir = new File("upload");
        if (!dir.exists()) dir.mkdirs();
    }

    public void firstStart() {
        Console.empty();
        Console.empty();
        Console.printout("Hi, this seems to be your first start! In your folder there should be some new files and folders!", MessageType.INFO);
        Console.printout("Please make sure to edit the server.cfg file to your needs, otherwise the program will not work correctly!", MessageType.INFO);
        Console.printout("If you still have problems, make sure to check out my GitHub page! https://github.com/BytePhilHD", MessageType.INFO);
        Console.empty();
        Console.printout("The App is starting soon!", MessageType.INFO);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e1) {
        }

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


    private SslContextFactory getSslContextFactory() {
        SslContextFactory sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(config.keystorePath);
        sslContextFactory.setKeyStorePassword(config.keystorePW);
        return sslContextFactory;
    }

}
