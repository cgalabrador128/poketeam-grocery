package com.grocery.controller;

import java.io.IOException;
import java.sql.*;
import java.util.Objects;

import com.grocery.data.DataBConnection;
import com.grocery.data.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import static com.grocery.App.loadFXML;

public class LoginController {
    @FXML
    private PasswordField pass_field;

    @FXML
    private TextField user_id;

    @FXML
    private void initialize() {
        // Hint: initialize() will be called when the associated FXML has been completely loaded.
    }
    
    
    @FXML
    private void submit(ActionEvent actionEvent) throws IOException, SQLException, ClassNotFoundException {
        String pass = pass_field.getText();
        int id = Integer.parseInt(user_id.getText());

        String role = null;
        String name = null;
        User user = User.getInstance();
        user.setUserRole("0");
        try(Connection dm = user.getConnection()){

            String query = "SELECT * FROM users WHERE user_id = '"+id+"' AND user_password = '"+pass +"'";
            Statement stmt = dm.createStatement();

            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                id = rs.getInt("user_id");
                name = rs.getString("user_name");
                role = rs.getString("user_role");
                System.out.println(id + name + role);
            } else {
                System.out.println("No user found with that ID.");
            }

            user.setUserRole(role);
            user.setUserId(id);
            user.setUsername(name);

            if (Objects.equals(role, "manager")) {
                loadFXML("adm-dashboard-page");
            } else if (Objects.equals(role, "staff")) {
                loadFXML("staff-dsdboard");
            }
        } catch (Exception e) {
            System.out.println(e);
            showAlert(Alert.AlertType.ERROR, "Form Error!", "Invalid Credentials");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }
}
