package common;

public class RequestMessage extends AbstractMessage{

    private Commands command;
    private String fileName;

    public RequestMessage(Commands command, String fileName) {
        this.command = command;
        this.fileName = fileName;
    }

    public RequestMessage(Commands command) {
        this.command = command;
    }

    public Commands getCommand() {
        return command;
    }

    public String getFileName() {
        return fileName;
    }
}
