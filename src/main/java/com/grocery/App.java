package com.grocery;

import java.io.IOException;
import java.sql.SQLException;

import com.grocery.data.User;
import com.grocery.util.AlertHandler;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;


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
        
        stage.setOnCloseRequest(event -> {
            event.consume();

            new AlertHandler().setStage(stage);
            boolean confirm = new AlertHandler().confirmAlert("Exit", "Are you sure you want to quit?");

            if (confirm) {
                stage.close();
                try {
                    User user = User.getInstance();
                    user.clearSession();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        new AlertHandler().setStage(stage);
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
            new AlertHandler().setStage(popupStage);
            boolean confirm = new AlertHandler().confirmAlert("Exit", "Are you sure you want to quit?");
            if(confirm){
                popupStage.close();
            }
        });
        new AlertHandler().setStage(popupStage);
        popupStage.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }

}