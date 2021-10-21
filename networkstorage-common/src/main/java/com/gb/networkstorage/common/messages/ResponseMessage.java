package com.gb.networkstorage.common.messages;

import com.gb.networkstorage.common.messages.AbstractMessage;

import java.util.ArrayList;

public class ResponseMessage extends AbstractMessage {

    private static final long serialVersionUID = 1L;
    private ArrayList<String> filesList;

    public ArrayList<String> getFilesList() {
        return filesList;
    }

    public void setFilesList(ArrayList<String> filesList) {
        this.filesList = filesList;
    }

}
