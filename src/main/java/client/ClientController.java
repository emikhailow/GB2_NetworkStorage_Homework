package client;

import client.interfaces.ClientCallback;
import common.Commands;
import common.Constants;
import common.messages.RequestMessage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;


public class ClientController implements ClientCallback {


    private String login;
    private Client client;

    @FXML
    TextField tfFileName;
    @FXML
    ListView<String> serverFilesList;
    @FXML
    ListView<String> clientFilesList;
    @FXML
    TextArea logTextArea;
    @FXML
    TextField loginTextField;
    @FXML
    Label logLabel;

    public ClientController() {
    }

    public void pressOnRefreshServerFilesList(ActionEvent actionEvent) {
        client.sendMessage(new RequestMessage(login, Commands.GET_FILES_LIST));
    }

    @Override
    public void updateServerFilesList(ArrayList<String> filesList) {
        Platform.runLater(() -> {
            serverFilesList.getItems().clear();
            filesList.stream().forEach(o -> serverFilesList.getItems().add(o));
        });
    }

    @Override
    public void updateClientFilesList() {
        Platform.runLater(() -> {
            clientFilesList.getItems().clear();
            try {

                String fullDir = String.format("%s%s%s",
                        Constants.ROOT_CLIENT_DIRECTORY,
                        File.separator,
                        login);

                new File(fullDir).mkdirs();

                Files.list(Paths.get(fullDir))
                        .filter(f -> !Files.isDirectory(f))
                        .map(f -> f.getFileName().toString())
                        .forEach(f -> clientFilesList.getItems().add(f));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void appendMessage(String msg) {
         logTextArea.appendText(msg + "\n");
    }

    @Override
    public void setLogText(String text) {
        Platform.runLater(() -> logLabel.setText(text));
    }

    public void pressOnDownloadFileFromServer(ActionEvent actionEvent) {
        String fileName = serverFilesList.getSelectionModel().getSelectedItem();
        client.downloadFile(fileName);
    }

    public void pressOnRefreshClientFilesList(ActionEvent actionEvent) throws IOException {
        updateClientFilesList();
    }

    public void pressOnUploadFile(ActionEvent actionEvent) throws IOException {
        String fileName = clientFilesList.getSelectionModel().getSelectedItem();
        client.uploadFile(fileName);
    }

    public void setClient(Client client) {
        this.client = client;
        client.registerCallback(this);
}

    public void setLogin(String login) {
        this.login = login;
    }

    public Client getClient() {
        return client;
    }
}
