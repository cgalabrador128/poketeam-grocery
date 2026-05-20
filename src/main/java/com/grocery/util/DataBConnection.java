package com.grocery.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBConnection { //Uses Singleton Design

    private static DataBConnection instance;
    private Connection connection;
    private final String username = "admin";
    private final String password = "Admin123!";
    private final String url = "jdbc:mysql://10.102.166.97:3306/grocery?allowPublicKeyRetrieval=true&useSSL=false";
    
    private DataBConnection() { //dont initialize via here
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver not found!");
            e.printStackTrace();
        }
    }

    public static DataBConnection getInstance(){ //global entry point
        if (instance == null){
            instance = new DataBConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(this.url, this.username, this.password);
        }
        return connection;
    }

    public void closeConnection(){
        if (connection!=null){
            try{
                if (!connection.isClosed()){
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}