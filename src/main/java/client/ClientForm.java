package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientForm extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fmxLoader = new FXMLLoader(getClass().getResource("/main.fxml"));
        Parent root = fmxLoader.load();
        stage.setTitle("Network storage 1.0");
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
