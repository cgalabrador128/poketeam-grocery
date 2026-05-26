package com.grocery.util;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

public class AlertHandler {
    Alert alert;
    public static Stage stage;

    public void setStage(Stage stage){
        this.stage = stage;
    }

    public Boolean confirmAlert(String title, String content ){
        alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        //stage.toFront();
        //stage.setAlwaysOnTop(true);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK){
            return true;
        } else{
            return false;
        }
    }

    public void showSimpleAlert(String title, String content){
        alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        ///stage.setAlwaysOnTop(true);
        alert.show();
    }

    public void showMessageAlert(String title, String content){
        alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        //stage.setAlwaysOnTop(true);
        alert.show();
    }

}
