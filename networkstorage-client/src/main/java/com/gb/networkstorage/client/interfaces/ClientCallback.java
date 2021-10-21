package com.gb.networkstorage.client.interfaces;

import java.util.ArrayList;

public interface ClientCallback {

    void updateServerFilesList(ArrayList<String> filesList);

    void updateClientFilesList();

    void processTextMessage(String text);
}
