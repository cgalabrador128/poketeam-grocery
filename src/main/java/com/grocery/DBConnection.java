package com.grocery;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private String username;
    private String password;
    private String url;
    
    public DBConnection(String user, String pass){
        this.init("grocery", user, pass, "server", "3306");
    }

    private void init(String database, String user, String pass, String host, String port){
        this.url = String.format("jdbc:mysql://%s:%s/%s", host, port, database);
        this.username = user;
        this.password = pass;
    }

    
    public Connection getConnection() throws SQLException{
        return DriverManager.getConnection(this.url, username, password);
    }

}