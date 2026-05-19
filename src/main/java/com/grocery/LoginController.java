package com.grocery;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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
    private void submit(ActionEvent actionEvent) throws IOException{
        String pass = pass_field.getText();
        String id = user_id.getText();

        DBConnection connect = new DBConnection(id, pass);
        System.out.print(connect);
        
        if(connect != null){
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("dashboard-page.fxml"));
            Parent root = (Parent)fxmlLoader.load();
            Scene scene = new Scene(root);
            Node node = (Node) actionEvent.getSource();
            Stage stage = (Stage) node.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        }
    }
}
