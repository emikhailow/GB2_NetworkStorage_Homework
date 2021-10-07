package common;

import java.io.IOException;

public class DeleteMessage extends AbstractMessage {

    private String name;

    public DeleteMessage(String name) throws IOException {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
