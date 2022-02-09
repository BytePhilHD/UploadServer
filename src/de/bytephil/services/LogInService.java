package de.bytephil.services;

import de.bytephil.main.Main;
import de.bytephil.users.User;
import de.bytephil.users.UserService;
import de.bytephil.utils.BCrypt;

import java.util.ArrayList;
import java.util.HashMap;

public class LogInService {

    public static HashMap<String, String> loggedinUsers = new HashMap<>();

    public static boolean login(String webSocketAnswer, String sessionID) {
        int iend = webSocketAnswer.indexOf("|*|");
        int length = webSocketAnswer.length();
        String passwordADMIN = Main.password;
        String usernameADMIN = Main.username;

        String username = null;
        String password = null;
        if (iend != -1) {
            username = webSocketAnswer.substring(0, iend); //this will give abc
            password = webSocketAnswer.substring(iend + 3, length); //this will give abc
        }
        try {
            User user = new UserService().getUserByName(username);
            String passwordUser = user.getPassword();

            if (BCrypt.checkpw(password, passwordUser)) {
                loggedinUsers.put(sessionID, username);
                return true;
            }
        } catch (Exception e1) {
            try {
                User user = new UserService().getUserByEmail(username);
                String passwordUser = user.getPassword();

                if (BCrypt.checkpw(password, passwordUser)) {
                    loggedinUsers.put(sessionID, username);
                    return true;
                }
            } catch (Exception e2) {}
        }

        if (username.equalsIgnoreCase(usernameADMIN) && password.equalsIgnoreCase(passwordADMIN)) {
            loggedinUsers.put(sessionID, username);
            return true;
        } else return false;
    }

}
