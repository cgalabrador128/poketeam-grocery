package com.grocery.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class LoadData {
    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private ObservableList<String> alerts = FXCollections.observableArrayList();
    private ObservableList<Employee> staffList = FXCollections.observableArrayList();

    @SuppressWarnings("StringEquality")
    public ObservableList<String> loadAlerts(Connection dm, User user) throws SQLException {
        if (user.getUserRole() == "staff"){return null;}

        String query = "SELECT * FROM alert ORDER BY alert_time DESC";
        Statement stmt = dm.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        while (rs.next()) {
            String alertString = rs.getTimestamp("alert_time").toString() +
                    " : " + rs.getString("alert_type") +
                    " : " + Long.toString(rs.getLong("product_serial_number")) +
                    " : " + Integer.toString(rs.getInt("alert_id"));

            alerts.add(alertString);
        }
        return alerts;
    }

    public ObservableList<Product> loadInventory(Connection dm) throws SQLException {
        productList = FXCollections.observableArrayList();
        String query = "SELECT product_serial_number, product_name, product_batch_no, product_price, product_expiry_date, product_stock_count FROM inventory";

        try (PreparedStatement pstmt = dm.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                long id = rs.getLong("product_serial_number");
                String name = rs.getString("product_name");
                int batch = rs.getInt("product_batch_no");
                double price = rs.getDouble("product_price");
                LocalDate expiry = rs.getDate("product_expiry_date").toLocalDate();
                int stock = rs.getInt("product_stock_count");

                productList.add(new Product(id, name, batch, price, expiry, stock));
            }
        }
        return productList;
    }

    public ObservableList<Employee> loadStaff(Connection dm, User user) throws SQLException {
        if (user.getUserRole() == "staff"){return null;}
        staffList = FXCollections.observableArrayList();
        String query = "SELECT user_id, user_name, user_role, has_admin FROM users";

        try (PreparedStatement pstmt = dm.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("user_id");
                String username = rs.getString("user_name");
                String role = rs.getString("user_role");
                Boolean permit = rs.getBoolean("has_admin");
                Employee emp = new Employee(id, username, role, permit );
                staffList.add(emp);
            }
        }
        return staffList;
    }

    // update Lists
    public ObservableList<Employee> deleteStaff(int id) {
        staffList.removeIf(emp -> emp.getId() == id);
        return staffList;
    }

}
