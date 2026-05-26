package com.grocery.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBConnection { //Uses Singleton Design

    private static DataBConnection instance;
    private static Connection connection;
    private final String admnuser = "admin";
    private final String admnpass = "Admin123!";
    private final String genuser = "staff";
    private final String genpass = "Staff123!";
    private final String url = "jdbc:mysql://192.168.1.15:3306/grocery" +
            "?allowPublicKeyRetrieval=true" +
            "&useSSL=false" +
            "&autoReconnect=true" +
            "&socketTimeout=100000" +
            "&connectTimeout=5000";

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

    public Connection getConnection(Boolean admn) throws SQLException {
        if (connection == null || connection.isClosed()) {
            if (admn == true) {
                connection = DriverManager.getConnection(this.url, this.admnuser, this.admnpass);
            } else if (admn == false) {
                connection = DriverManager.getConnection(this.url, this.genuser, this.genpass);
            }
        }
        return connection;
    }

    public static Connection closeConnection(Connection conn){
        if (conn!=null){
            try{
                if (!conn.isClosed()){
                    conn.close();
                    return conn;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}