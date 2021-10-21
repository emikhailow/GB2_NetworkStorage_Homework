package com.gb.networkstorage.client.interfaces;

import com.gb.networkstorage.common.messages.AbstractMessage;

public interface ClientAuthCallback {

    void processAuthResult(AbstractMessage abstractMessage);

}
