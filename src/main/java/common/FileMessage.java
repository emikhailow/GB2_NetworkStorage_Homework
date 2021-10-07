package common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileMessage extends AbstractMessage {

    private String name;
    private String parents;
    private byte[] data;

    public FileMessage(Path fullPath, String root) throws IOException {
        this.name = fullPath.getFileName().toString();
        this.parents = Paths.get(root).relativize(fullPath).toString();
        this.data = Files.readAllBytes(fullPath);
    }

    public String getName() {
        return name;
    }

    public byte[] getData() {
        return data;
    }

}
