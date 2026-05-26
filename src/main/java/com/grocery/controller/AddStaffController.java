package com.grocery.controller;

import com.grocery.data.User;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

import static com.grocery.App.loadFXML;


public class AddStaffController {
    @FXML
    private TextField staff_name;
    @FXML
    private TextField staff_id;
    @FXML
    private TextField staff_pass;
    @FXML
    private ChoiceBox staff_role;

    User user;
    Connection dm;

    @FXML
    public void initialize() throws SQLException {
        user = User.getInstance();
        dm = user.getConnection();
        staff_role.setItems(FXCollections.observableArrayList("manager", "staff"));
    }

    @FXML
    public void submit_btn(javafx.event.ActionEvent actionEvent) throws SQLException, IOException {

        String name = staff_name.getText();
        int id = Integer.parseInt(staff_id.getText());
        String pass = staff_pass.getText();
        String role = staff_role.getSelectionModel().getSelectedItem().toString();

        //confirmation
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure to add "+name+" ?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get()==ButtonType.OK) {
            String query = "INSERT INTO users (user_id, user_name, user_password, user_role) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = dm.prepareStatement(query)) {
                pstmt.setInt(1, id);
                pstmt.setString(2, name);
                pstmt.setString(3, pass);
                pstmt.setString(4, role);

                int rowsAffected = pstmt.executeUpdate();
                System.out.println(rowsAffected);
                closeWindow(actionEvent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void closeWindow(ActionEvent event) throws IOException {loadFXML("adm-dashboard-page");}
}