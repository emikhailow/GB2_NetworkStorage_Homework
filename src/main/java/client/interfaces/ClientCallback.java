package client.interfaces;

import java.util.ArrayList;

public interface ClientCallback {

    void updateServerFilesList(ArrayList<String> filesList);

    void updateClientFilesList();

    void appendMessage(String msg);

    void setLogText(String text);
}
