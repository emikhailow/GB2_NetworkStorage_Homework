package client;

import common.Commands;
import common.Constants;
import common.RequestMessage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;


public class ClientController implements Initializable, ClientHandler.Callback{

    private Client client;

    @FXML
    TextField tfFileName;
    @FXML
    ListView<String> serverFilesList;
    @FXML
    ListView<String> clientFilesList;

    public void pressOnDownloadBtn(ActionEvent actionEvent) {
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        client = new Client(this);
        new Thread(() -> {
            try {
                client.connect(Constants.PORT, Constants.HOST);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Platform.exit();
            }
        }).start();
    }

    public void pressOnRefreshServerFilesList(ActionEvent actionEvent) {
        client.sendMessage(new RequestMessage(Commands.GET_FILES_LIST));
    }

    @Override
    public void updateServerFilesList(List<String> list) {
        Platform.runLater(() -> {
            serverFilesList.getItems().clear();
            list.stream().forEach(o -> serverFilesList.getItems().add(o));
        });
    }

    @Override
    public void updateClientFilesList() {
        Platform.runLater(() -> {
            clientFilesList.getItems().clear();
            try {
                Files.list(Paths.get(Constants.ROOT_CLIENT_DIRECTORY))
                        .filter(f -> !Files.isDirectory(f))
                        .map(f -> f.getFileName().toString())
                        .forEach(f -> clientFilesList.getItems().add(f));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void pressOnDownloadFileFromServer(ActionEvent actionEvent) {
        String fileName = (String) serverFilesList.getSelectionModel().getSelectedItem();
        client.sendMessage(new RequestMessage(Commands.DOWNLOAD_FILE, fileName));
    }

    public void pressOnRefreshClientFilesList(ActionEvent actionEvent) throws IOException {
        updateClientFilesList();
    }
}
