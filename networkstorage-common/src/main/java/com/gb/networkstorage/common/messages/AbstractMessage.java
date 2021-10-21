package com.gb.networkstorage.common.messages;

import java.io.Serializable;

public abstract class AbstractMessage implements Serializable {

    protected String login;

    protected String getLogin() {
        return login;
    }

    protected void setLogin(String login) {
        this.login = login;
    }

}
