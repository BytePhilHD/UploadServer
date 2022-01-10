package de.bytephil.users;

import de.bytephil.utils.PasswordGenerator;

public class User {

    private String id;
    private String name;
    private String password;
    private String email;

    public User(String name, String password, String email) {
        this.id = PasswordGenerator.generateID(10);
        this.name = name;
        this.password = password;
        this.email = email;
    }

    public User(String id, String name, String password, String email) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail() { this.email = email; }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
