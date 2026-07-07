package com.example.Music_management.controller.cmd;

public class RegisterCmd {
    private String name;
    private String password;
    private int age;
    private int gender;
    private String email;
    private String job;
    private String interest;
    public RegisterCmd(String name, String password, int age, int gender, String email, String job, String interest) {
        this.name = name;
        this.password = password;
        this.age = age;
        this.gender = gender;
        this.email = email;
        this.job = job;
        this.interest = interest;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    @Override
    public String toString() {
        return "RegisterCmd{" +
                "name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", age=" + age +
                ", gender=" + gender +
                ", email='" + email + '\'' +
                ", job='" + job + '\'' +
                ", interest='" + interest + '\'' +
                '}';
    }
}
