package com.grocery;

import com.grocery.data.DataBConnection;
import com.grocery.util.AlertHandler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;


/**
 * JavaFX App
 */
public class App extends Application {

    static Stage stage;

    @Override
    public void start(Stage stages) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("login-page.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        App.stage = stages;
        stage.setScene(scene);
        stage.show();
    }

    public static void loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.show();
    }

    public static void popFXML(String fxml, String title) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        Parent root  = fxmlLoader.load();
        Stage popupStage = new Stage();
        popupStage.setTitle(title);
        popupStage.setScene(new Scene(root));
        popupStage.setResizable(false);
        popupStage.initModality(Modality.APPLICATION_MODAL);
        Stage mainWindow = stage;
        popupStage.initOwner(mainWindow);

        popupStage.setOnCloseRequest(event->{
            event.consume();
            boolean confirm = new AlertHandler().confirmAlert("Exit", "Are you sure you want to quit?");
            if(confirm){
                popupStage.close();
            }
        });

        popupStage.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }

}