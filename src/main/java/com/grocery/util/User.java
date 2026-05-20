package com.grocery.util;

public class User {
    //Session manager singleton

    private static User instance;

    private int id;
    private String role;
    private String name;

    private User(){
        this.id = -99;
        this.name = null;
        this.role = null;
    }

    public static User getInstance(){
        if (instance == null){
            instance = new User();
        }
        return instance;
    }

    public void clearSession() { //clear data on logout
        this.id = -99;
        this.name = null;
        this.role = null;
    }

    //getters and setters
    public int getUserId(){return id;}
    public void setUserId(int id) {this.id = id;}
    public String getUsername(){return name;}
    public void setUsername(String name){this.name = name;}
    public String getUserRole() {return role;}
    public void setUserRole(String role){this.role = role;}
}
