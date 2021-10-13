package client.authorization;

import client.Client;
import client.interfaces.ClientAuthCallback;
import client.ClientController;
import common.*;
import common.messages.AbstractMessage;
import common.messages.AuthMessage;
import common.messages.RequestMessage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;

public class ClientAuthController implements Initializable, ClientAuthCallback {

    private Client client;
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    @FXML
    public TextField loginTextField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public TextField password;
    @FXML
    public Button signInButton;
    @FXML
    public Label labelLoginPasswordAreValid;

    public void onClickSignInButton(ActionEvent actionEvent) throws Exception {

        String login = loginTextField.getText().trim();
        String password = passwordField.getText().trim();

        if(login.isEmpty()){
            return;
        }

        connectToServer();
        client.registerClientAuthCallback(this);
        client.sendMessage(new AuthMessage(login, password));

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //client.sendMessage(new RequestMessage(Commands.GET_FILES_LIST));
    }

    private void connectToServer(){

        if(client == null){

            client = new Client();
            new Thread(() -> {
                try {
                    client.connect(Constants.PORT, Constants.HOST, countDownLatch);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Platform.exit();
                }
            }).start();
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Platform.exit();
            }
        }

    }

    @Override
    public void processAuthResult(AbstractMessage msg) {
        Platform.runLater(() -> {
            if(msg instanceof AuthMessage){
                AuthMessage authMessage = (AuthMessage) msg;
                if(authMessage.isResult()){
                    labelLoginPasswordAreValid.setText("Login and password are valid");
                    loginTextField.setStyle("");
                    passwordField.setStyle("");
                    String login = authMessage.getLogin();
                    client.setLogin(login);
                    goToMainInterface(login);
                }
                else
                {
                    labelLoginPasswordAreValid.setText("Invalid login or password");
                    labelLoginPasswordAreValid.setStyle("-fx-font-color: red");
                    loginTextField.setStyle("-fx-border-color: red");
                    passwordField.setStyle("-fx-border-color: red");
                    System.out.println(authMessage.getMessage());
                }
            }
        });
    }

    private void goToMainInterface(String login){

        Platform.runLater(() ->{
            Stage currentStage = (Stage) signInButton.getScene().getWindow();

            FXMLLoader fmxLoader = new FXMLLoader(getClass().getResource("/main.fxml"));
            Parent root = null;
            try {
                root = fmxLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
                Platform.exit();
            }
            ClientController clientController = fmxLoader.getController();
            clientController.setClient(client);
            clientController.setLogin(login);
            clientController.updateClientFilesList();
            client.sendMessage(new RequestMessage(login, Commands.GET_FILES_LIST));

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(String.format("Network storage 1.0 (%s)", login));
            stage.show();
            stage.setUserData(client);

            currentStage.close();
        });
    }

}
