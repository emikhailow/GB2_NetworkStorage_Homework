package client.interfaces;

import common.messages.AbstractMessage;

public interface ClientAuthCallback {

    void processAuthResult(AbstractMessage abstractMessage);

}
