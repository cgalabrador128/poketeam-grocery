package com.grocery.data;

import java.sql.Connection;
import java.sql.SQLException;

public class User {
    //Session manager singleton

    private static User instance;

    private int id;
    private String role;
    private String name;
    private Connection connection;
    private DataBConnection dbconn = DataBConnection.getInstance();
    private String search;
    private User(){
        this.id = -99;
        this.name = null;
        this.role = null;
        this.connection = null;
        this.search = "";
    }

    public static synchronized User getInstance(){
        if (instance == null){
            instance = new User();
        }
        return instance;
    }

    public void clearSession() throws SQLException { //clear data on logout
        this.id = -99;
        this.name = null;
        this.role = null;
        connection.close();
        this.connection = null;
        this.search = "";
    }

    private void setData() throws SQLException {
        if (role.equals("manager")){
            this.connection = dbconn.getConnection(true);
        }else{
            this.connection = dbconn.getConnection(false);
        }
    }

    //getters and setters
    public Connection getConnection() throws SQLException {
        if (this.connection == null || this.connection.isClosed()) {

            System.out.println("Connection was dead or missing. Reconnecting quietly...");

            if (this.role != null) {
                setData();
            } else {
                throw new SQLException("Cannot connect: User role is not set!");
            }
        }
        return this.connection;}

    public int getUserId(){return id;}
    public void setUserId(int id) {this.id = id;}
    public String getUsername(){return name;}
    public void setUsername(String name){this.name = name;}
    public String getUserRole() {return role;}
    public void setUserRole(String role) throws SQLException {
        this.role = role;
        setData();
    }
    public void setSearch(String search) {this.search = search;}
    public String getSearch() {return search;}
}
