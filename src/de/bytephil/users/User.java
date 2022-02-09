package de.bytephil.users;

import de.bytephil.enums.Rank;
import de.bytephil.utils.PasswordGenerator;

public class User {

    private String id;
    private String name;
    private String password;
    private String email;
    private Rank rank;

    public User(String name, String password, String email, Rank rank) {
        this.id = PasswordGenerator.generateID(10);
        this.name = name;
        this.password = password;
        this.email = email;
        this.rank = rank;
    }

    public User(String id, String name, String password, String email, String rank) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.email = email;
        this.rank = Rank.valueOf(rank.toUpperCase());
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail() {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Rank getRank() {
        return rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
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
