package de.bytephil.utils;

import de.bytephil.enums.MessageType;
import de.bytephil.enums.Rank;
import de.bytephil.main.Main;
import de.bytephil.users.Application;
import de.bytephil.users.ApplicationService;
import de.bytephil.users.User;
import de.bytephil.users.UserService;

import java.util.List;

public class ConsoleCommands {

    public void handleCommand(String command) {
        String[] commandargs = command.split("\\s+");

        if (command.toLowerCase().equalsIgnoreCase("help")) {
            Console.sendHelp();
            Console.printout("          USER HELP ", MessageType.INFO);
            Console.printout("Create new user        - user create [name] [password] [email]", MessageType.INFO);
            Console.printout("Remove user            - user delete [name]", MessageType.INFO);
            Console.printout("Get rank of user       - user getrank [name]", MessageType.INFO);
            Console.printout("Set rank of user       - user setrank [name] [rank]", MessageType.INFO);
            Console.printout("List all users         - user list", MessageType.INFO);
            Console.empty();
            Console.printout("          APPLICATION HELP ", MessageType.INFO);
            Console.printout("List all applications  - applies list", MessageType.INFO);

        } else if (commandargs[0].toLowerCase().equalsIgnoreCase("user")) {
            if (commandargs.length >= 2) {
                if (commandargs[1].toLowerCase().equalsIgnoreCase("setrank")) {
                    if (commandargs.length == 4) {
                        changeRank(commandargs);
                    } else {
                        Console.printout("Usage: user setrank [name] [rank]", MessageType.WARNING);
                    }
                } else if (commandargs[1].toLowerCase().equalsIgnoreCase("getrank")) {
                    if (commandargs.length == 3) {
                        getRank(commandargs);
                    } else {
                        Console.printout("Usage: user getrank [name]", MessageType.WARNING);
                    }
                } else if (commandargs[1].toLowerCase().equalsIgnoreCase("delete") || commandargs[1].toLowerCase().equalsIgnoreCase("remove")) {
                    if (commandargs.length == 3) {
                        deleteUser(commandargs[2]);
                    } else {
                        Console.printout("Usage: user delete [name]", MessageType.WARNING);
                    }
                } else if (commandargs[1].toLowerCase().equalsIgnoreCase("create")) {
                    if (commandargs.length == 5) {
                        createUser(commandargs);
                    } else {
                        Console.printout("Usage: user create [name] [password] [email]", MessageType.WARNING);
                    }
                } else if (commandargs[1].toLowerCase().equalsIgnoreCase("list")) {
                    listUsers();
                } else {
                    Console.printout("Usage: user [setrank/getrank/delete/create/list]", MessageType.WARNING);
                }
            } else {
                Console.printout("Usage: user [setrank/getrank/delete/create/list]", MessageType.WARNING);
            }
        } else if (commandargs[0].toLowerCase().equalsIgnoreCase("applies")) {
            if (commandargs.length >= 2) {
                if (commandargs[1].toLowerCase().equalsIgnoreCase("list")) {
                    listApplies();
                }
            } else {
                Console.printout("Usage: applies [list]", MessageType.WARNING);
            }
        } else {
            Console.printout("Unknown command! Type \"help\" for help!", MessageType.INFO);
        }
    }

    private void deleteUser(String commandargs) {
        User user = new UserService().getUserByName(commandargs);
        new UserService().removeUser(user);

    }

    private void createUser(String[] commandargs) {
        String pw = BCrypt.hashpw(commandargs[3], BCrypt.gensalt(12));
        new UserService().createUser(new User(commandargs[2], pw, commandargs[4], Rank.USER));
        Console.printout("Succesfully added new user " + commandargs[2] + "!", MessageType.INFO);
    }

    private void listApplies() {
        List<Application> applications = new ApplicationService().getApplications();
        Console.printout("Currently there are " + applications.size() + " applications: ", MessageType.INFO);
        Console.printout("        Date        |     Job     |     Name     ", MessageType.INFO);
        for (int i = 0; i < applications.size(); i++) {
            Application application = applications.get(i);
            Console.printout(application.getDate() + " | " + application.getJob() + " | " + application.getName(), MessageType.INFO);
        }
    }

    private void listUsers() {
        List<User> users = new UserService().getAll();
        Console.printout("Currently there are " + users.size() + " users registered: ", MessageType.INFO);
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            Console.printout(user.getRank() + " - " + user.getName(), MessageType.INFO);
        }
    }

    private void changeRank(String[] commandargs) {
        User user = null;
        Rank rank = null;
        try {
            user = new UserService().getUserByName(commandargs[2]);
            rank = Rank.valueOf(commandargs[3].toUpperCase());
        } catch (Exception e1) {
            return;
        }
        user.setRank(rank);
        new UserService().saveUser(user);

        Console.printout("Changed user " + user.getName() + "'s rank to " + user.getRank(), MessageType.INFO);
    }

    private void getRank(String[] commandargs) {
        User user = null;
        try {
            user = new UserService().getUserByName(commandargs[2]);
        } catch (Exception e1) {
            return;
        }
        Console.printout("User " + user.getName() + "'s rank is currently " + user.getRank(), MessageType.INFO);
    }
}
