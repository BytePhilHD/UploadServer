package de.bytephil.users;

import de.bytephil.enums.MessageType;
import de.bytephil.utils.Console;
import de.bytephil.utils.PasswordGenerator;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class UserService {

    private final List<User> users;
    private String path = "data/user";

    public UserService() {
        this.users = new ArrayList<>();
        loadAll();
    }

    public void createUser(User user) {
        if (getUserByName(user.getName()) != null)
            Console.printout("User already existsts!", MessageType.ERROR);

        if (existsUser(user.getId())) {
            user.setId(PasswordGenerator.generateID(10));
            createUser(user);
            return;
        }
        saveUser(user);
    }

    public void saveUser(User user) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getName().equals(user.getName())) {
                users.set(i, user);
                return;
            }
        }
        users.add(user);
        updateAll();
    }

    public void loadAll() {
        loadAll(new File(path));
    }

    public void loadAll(File space) {
        Arrays.stream(Objects.requireNonNull(space.listFiles())).forEach(file -> {
            final JSONObject jsonUser = load(file);

            final String name = file.getName().replaceFirst(".json", "");

            if (name.contains("%") || name.contains("#") || name.contains("'")) {
                Console.printout("Error whilst trying to read users!", MessageType.ERROR);
            } else {
                final User user = new User(name, jsonUser.getString("name"), jsonUser.getString("password"), jsonUser.getString("email"));

                if (!user.getName().contains("%") && !user.getName().contains("#") && !user.getName().contains("'"))
                    users.add(user);
                else
                    Console.printout("Error at UserService!", MessageType.ERROR);
            }
        });
    }


    public User getUserByName(String name) {
        return users.stream().filter(user -> user.getName().equals(name)).findFirst().orElse(null);
    }

    public User getUserByEmail(String email) {
        return users.stream().filter(user -> user.getEmail().equals(email)).findFirst().orElse(null);
    }

    public boolean existsUser(String id) {
        return users.stream().anyMatch(user -> user.getId().equals(id));
    }

    public void updateAll() {
        updateAll(new File(path));
    }

    public void updateAll(File space) {
        users.forEach(user -> {
            final File file = new File(space.getPath() + "/" + user.getId() + ".json");
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", user.getName());
            jsonObject.put("password", user.getPassword());
            jsonObject.put("email", user.getEmail());
            save(file, jsonObject);
        });
    }

    public JSONObject load(File file) {
        try {
            FileReader reader = new FileReader(file);
            StringBuilder builder = new StringBuilder();

            int read;

            while ((read = reader.read()) != -1) {
                builder.append((char) read);
            }

            reader.close();

            return new JSONObject(builder.toString());
        } catch (Exception ex) {
            return null;
        }
    }

    public void save(File file, JSONObject jsonObject) {
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(jsonObject.toString());
            writer.close();
        } catch (Exception ex) {
            Console.printout("Cant save file!", MessageType.ERROR);
        }
    }

}

