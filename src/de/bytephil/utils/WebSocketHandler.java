package de.bytephil.utils;

import de.bytephil.main.Main;
import de.bytephil.users.Application;
import de.bytephil.users.ApplicationService;
import de.bytephil.users.UserService;

public class WebSocketHandler {

    public static boolean createAccount(String webSocketAnswer) {
        int iend = webSocketAnswer.indexOf("|*|");
        int iend1 = webSocketAnswer.indexOf("|'|");
        int length = webSocketAnswer.length();
                                                                                                     // The webSocketAnswer should look like this: "BytePhil|*|phitho2018@gmail.com|'|yourpw"
        String email = null;
        String hashedPW = null;
        String user = null;
        if (iend != -1)
        {
            user = webSocketAnswer.substring(0, iend); //this will give abc
            email = webSocketAnswer.substring(iend+3, iend1); //this will give abc
            hashedPW = BCrypt.hashpw(webSocketAnswer.substring(iend1+3, length), BCrypt.gensalt(12));
        }
        if (new UserService().getUserByName(user) != null) {
            return false;
        } else {
            new AccountManager().createAccount(user, email, hashedPW);
            return true;
        }
    }

    public static void createApplication(String webSocketAnswer) {
        int iend = webSocketAnswer.indexOf("|*|");
        int iend1 = webSocketAnswer.indexOf("|'|");
        int iend2 = webSocketAnswer.indexOf("|~|");
        int length = webSocketAnswer.length();
                                                                                                    // The webSocketAnswer should look like this: "BytePhil|*|phitho2018@gmail.com|'|Developer|~|Hello, i want to apply for ...."
        String name = null;
        String email = null;
        String job = null;
        String applicationtext = null;
        if (iend != -1)
        {
            name = webSocketAnswer.substring(0, iend);
            email = webSocketAnswer.substring(iend+3, iend1);
            job = webSocketAnswer.substring(iend1+3, iend2);
            applicationtext = webSocketAnswer.substring(iend2+3, length);
        }
        if (name == null) {
            return;
        }
        Application application = new Application(name, email, job, applicationtext);
        new ApplicationService().createApplication(application);
    }
}
