package de.bytephil.users;

import de.bytephil.utils.PasswordGenerator;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Application {

    private String id;
    private String name;
    private String email;
    private String job;
    private String date;
    private String application;

    public Application (String name, String email, String job, String application) {
        this.id = PasswordGenerator.generateID(10);
        this.date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        this.name = name;
        this.email = email;
        this.job = job;
        this.application = application;
    }

    public Application (String id, String name, String email, String job, String application) {
        this.id = id;
        this.date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        this.name = name;
        this.email = email;
        this.job = job;
        this.application = application;
    }

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getDate() {
        return date;
    }
    public String getEmail() {
        return email;
    }
    public String getJob() {
        return job;
    }
    public String getApplication() {
        return application;
    }
    public void setId(String id) {
        this.id = id;
    }
}
