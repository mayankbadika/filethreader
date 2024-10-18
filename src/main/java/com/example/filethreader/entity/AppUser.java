package com.example.filethreader.entity;

import io.micrometer.common.lang.NonNull;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "AppUsers")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generate ID
    private int id; // Renamed for convention

    private String username;
    private String email;
    private List<String> roles;
    private String password;

    public void setUsername(String firstName) {
        this.username = firstName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword( String password) {
        this.password = password;
    }
}
