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

public class ApplicationService {

    public final List<Application> applications;
    private String path = "data/apply";

    public ApplicationService() {
        this.applications = new ArrayList<>();
        loadAll();
    }

    public void createApplication(Application Application) {
        if (getApplicationByName(Application.getName()) != null)
            Console.printout("Application already existsts!", MessageType.ERROR);

        if (existsApplication(Application.getId())) {
            Application.setId(PasswordGenerator.generateID(10));
            createApplication(Application);
            return;
        }
        saveApplication(Application);
    }

    public void saveApplication(Application Application) {
        for (int i = 0; i < applications.size(); i++) {
            if (applications.get(i).getName().equals(Application.getName())) {
                applications.set(i, Application);
                return;
            }
        }
        applications.add(Application);
        updateAll();
    }

    public void loadAll() {
        loadAll(new File(path));
    }

    public void loadAll(File space) {
        Arrays.stream(Objects.requireNonNull(space.listFiles())).forEach(file -> {
            final JSONObject jsonApplication = load(file);

            final String name = file.getName().replaceFirst(".json", "");

            if (name.contains("%") || name.contains("#") || name.contains("'")) {
                Console.printout("Error whilst trying to read Applications!", MessageType.ERROR);
            } else {
                final Application Application = new Application(name, jsonApplication.getString("name"), jsonApplication.getString("email"), jsonApplication.getString("job"), jsonApplication.getString("application"));

                if (!Application.getName().contains("%") && !Application.getName().contains("#") && !Application.getName().contains("'"))
                    applications.add(Application);
                else
                    Console.printout("Error at ApplicationService!", MessageType.ERROR);
            }
        });
    }


    public Application getApplicationByName(String name) {
        return applications.stream().filter(Application -> Application.getName().equals(name)).findFirst().orElse(null);
    }

    public Application getApplicationByEmail(String email) {
        return applications.stream().filter(Application -> Application.getEmail().equals(email)).findFirst().orElse(null);
    }

    public boolean existsApplication(String id) {
        return applications.stream().anyMatch(Application -> Application.getId().equals(id));
    }

    public void updateAll() {
        updateAll(new File(path));
    }

    public void updateAll(File space) {
        applications.forEach(Application -> {
            final File file = new File(space.getPath() + "/" + Application.getId() + ".json");
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", Application.getName());
            jsonObject.put("email", Application.getEmail());
            jsonObject.put("job", Application.getJob());
            jsonObject.put("application", Application.getApplication());
            save(file, jsonObject);
        });
    }

    public void convertApplications() {
        for (int i = 0; i <= applications.size(); i++) {
            Application application = applications.get(i);

        }
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
