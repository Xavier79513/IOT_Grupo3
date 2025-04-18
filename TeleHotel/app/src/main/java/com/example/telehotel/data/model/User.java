package com.example.telehotel.data.model;

public class User {
    private String uid;
    private String email;
    private String name;

    public User() {} // Requerido por Firebase

    public User(String uid, String email, String name) {
        this.uid = uid;
        this.email = email;
        this.name = name;
    }

    public String getUid() { return uid; }
    public String getEmail() { return email; }
    public String getName() { return name; }

    public void setUid(String uid) { this.uid = uid; }
    public void setEmail(String email) { this.email = email; }
    public void setName(String name) { this.name = name; }
}
