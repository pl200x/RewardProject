package com.example.Music_management.entity;

public class UserToken {
    private int id;
    private String name;
    private int gender;
    private String interest;

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

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    @Override
    public String toString() {
        return "UserToken{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", gender=" + gender +
                ", interest='" + interest + '\'' +
                '}';
    }


}
