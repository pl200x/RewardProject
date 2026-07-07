package com.example.Music_management.entity;

public class User {
    private int id;
    private String name;
    private String password;
    private int age;
    private int gender;
    private String email;
    private String job;
    private String interest;
    /** 默认头像序号 0-9(MinIO avatars bucket 里的 <n>.svg),注册时随机分配 */
    private int avatar;


    public User(String name, String password, int age, int gender, String email, String job, String interest) {
        this.name = name;
        this.password = password;
        this.age = age;
        this.gender = gender;
        this.email = email;
        this.job = job;
        this.interest = interest;
    }

    public User() {
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", age=" + age +
                ", gender=" + gender +
                ", email='" + email + '\'' +
                ", job='" + job + '\'' +
                ", interest='" + interest + '\'' +
                '}';
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

    public int getAvatar() {
        return avatar;
    }

    public void setAvatar(int avatar) {
        this.avatar = avatar;
    }
}
