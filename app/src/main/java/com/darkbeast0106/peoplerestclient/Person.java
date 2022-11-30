package com.darkbeast0106.peoplerestclient;

import android.annotation.SuppressLint;

import com.google.gson.annotations.Expose;

public class Person {
    private int id;
    @Expose
    private String name;
    @Expose
    private String email;
    @Expose
    private int age;

    public Person(int id, String name, String email, int age) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getAgeText() {
        return String.valueOf(this.age);
    }

    public void setAgeText(String ageText) {
        if (ageText.equals("")) {
            this.age = 0;
        } else {
            this.age = Integer.parseInt(ageText);
        }
    }

    @SuppressLint("DefaultLocale")
    @Override
    public String toString() {
        return String.format("%s (%d)", this.name, this.age);
    }
}
