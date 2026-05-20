package com.grocery.controller;

import java.io.IOException;
import java.sql.*;
import java.util.Objects;

import com.grocery.util.DataBConnection;
import com.grocery.util.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

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

        DataBConnection connect = DataBConnection.getInstance();
        try(Connection dm = connect.getConnection()){

            String query = "SELECT * FROM users WHERE user_id = '"+id+"' AND user_password = '"+pass +"'";
            Statement stmt = dm.createStatement();

            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                id = rs.getInt("user_id");
                name = rs.getString("user_name");
                role = rs.getString("user_role");
                System.out.println(id +name + role);
            } else {
                System.out.println("No user found with that ID.");
            }

            User user = User.getInstance();
            user.setUserRole(role);
            user.setUserId(id);
            user.setUsername(name);

            if (Objects.equals(role, "manager")) {

                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/grocery/adm-dashboard-page.fxml"));
                Scene scene = new Scene(fxmlLoader.load());
                Node node = (Node) actionEvent.getSource();
                Stage stage = (Stage) node.getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } else if (Objects.equals(role, "staff")) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/grocery/staff-dsdboard.fxml"));
                Scene scene = new Scene(fxmlLoader.load());
                Node node = (Node) actionEvent.getSource();
                Stage stage = (Stage) node.getScene().getWindow();
                stage.setScene(scene);
                stage.show();
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
