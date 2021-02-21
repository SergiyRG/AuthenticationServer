package org.example.app.models;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class User implements Serializable {

    public static final Predicate<String> emailPattern = Pattern.compile(".+@.+\\..+").asMatchPredicate();
    public static final int MIN_LENGTH = 8;

    private String name;
    private String email;
    private String password;

    public User() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        checkName(name);
        this.name = name;
    }

    private void checkName(String name) {
        try {
            checkOnEmpty(name);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format("Name {%s} is blank!", name), e);
        }
    }

    private void checkOnEmpty(String name) {
        if (name.trim().isBlank()) {
            throw new IllegalArgumentException();
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        checkEmail(email);
        this.email = email;
    }

    private void checkEmail(String email) {
        if (!emailPattern.test(email)) {
            throw new IllegalArgumentException(String.format("Email {%s} is not valid!", email));
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        checkPassword(password);
        this.password = password;
    }

    private void checkPassword(String password) {
        if (password.length() < MIN_LENGTH || password.contains(" ")) {
            throw new IllegalArgumentException(String.format("Password {%s} is not valid!", password));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
