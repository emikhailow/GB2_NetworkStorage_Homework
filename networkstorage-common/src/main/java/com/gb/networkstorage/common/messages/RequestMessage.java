package com.gb.networkstorage.common.messages;

import com.gb.networkstorage.common.Commands;

public class RequestMessage extends AbstractMessage {

    private Commands command;
    private String fileName;

    public RequestMessage(String login, Commands command, String fileName) {
        this.login = login;
        this.command = command;
        this.fileName = fileName;
    }

    public RequestMessage(String login, Commands command) {
        this.command = command;
        this.login = login;
    }

    public Commands getCommand() {
        return command;
    }

    public String getFileName() {
        return fileName;
    }

    public String getLogin() {
        return super.getLogin();
    }
}
