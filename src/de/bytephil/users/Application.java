package de.bytephil.users;

import de.bytephil.utils.PasswordGenerator;

public class Application {

    private String id;
    private String name;
    private String email;
    private String job;
    private String application;

    public Application (String name, String email, String job, String application) {
        this.id = PasswordGenerator.generateID(10);
        this.name = name;
        this.email = email;
        this.job = job;
        this.application = application;
    }

    public Application (String id, String name, String email, String job, String application) {
        this.id = id;
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
