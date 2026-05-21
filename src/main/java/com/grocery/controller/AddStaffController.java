package com.grocery.controller;

import com.grocery.util.DataBConnection;

import javafx.event.ActionEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class AddStaffController {
    @FXML
    private TextField staff_name;
    @FXML
    private TextField staff_id;
    @FXML
    private TextField staff_pass;

    @FXML
    public void initialize() {
    }

    @FXML
    public void cancel_btn(javafx.event.ActionEvent actionEvent) throws IOException {
        closeWindow(actionEvent);
    }

    @FXML
    public void submit_btn(javafx.event.ActionEvent actionEvent) throws SQLException, IOException {

        String name = staff_name.getText();
        int id = Integer.parseInt(staff_id.getText());
        String pass = staff_pass.getText();
        //confirmation
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure to add"+name+"?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get()==ButtonType.OK) {
            String query = "INSERT INTO users (user_id, user_name, user_password, user_role) VALUES (?, ?, ?, ?)";
            try (Connection dm = DataBConnection.getInstance().getConnection();
                 PreparedStatement pstmt = dm.prepareStatement(query)) {
                pstmt.setInt(1, id);
                pstmt.setString(2, name);
                pstmt.setString(3, pass);
                pstmt.setString(4, "staff");

                int rowsAffected = pstmt.executeUpdate();
                System.out.println(rowsAffected);
                closeWindow(actionEvent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void closeWindow(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/grocery/adm-dashboard-page.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Node node = (Node) event.getSource();
        Stage stage = (Stage) node.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
}