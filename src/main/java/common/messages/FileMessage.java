package common.messages;

import common.Constants;
import common.FileDirection;
import common.messages.AbstractMessage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileMessage extends AbstractMessage {

    private String name;
    private String parents;
    private byte[] data;
    int blockNumber;
    int blockCount;
    int blockSize;
    long fileSize;

    private FileDirection direction;

    public FileMessage(Path fullPath, String root) throws IOException {
        this.name = fullPath.getFileName().toString();
        this.parents = Paths.get(root).relativize(fullPath).toString();
        this.data = Files.readAllBytes(fullPath);
    }

    public FileMessage(String login, String fileName, FileDirection direction, long fileSize) {

        this.login = login;
        this.name = fileName;
        this.fileSize = fileSize;
        this.direction = direction;
        this.blockNumber = 0;
        this.blockSize = Constants.BUF_SIZE;
        this.blockCount = Long.valueOf(fileSize / blockSize).intValue();
        if (this.fileSize % this.blockSize != 0) {
            this.blockCount++;
        }

    }

    public String getName() {

        return name;
    }

    public byte[] getData() {

        return data;
    }

    public int getBlockNumber() {

        return blockNumber;
    }

    public void setBlockNumber(int blockNumber) {

        this.blockNumber = blockNumber;
    }

    public int getBlockCount() {

        return blockCount;
    }

    public void setBlockCount(int blockCount) {

        this.blockCount = blockCount;
    }

    public int getBlockSize() {

        return blockSize;
    }

    public void setBlockSize(int blockSize) {

        this.blockSize = blockSize;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public FileDirection getDirection() {

        return direction;
    }

    public void setDirection(FileDirection direction) {

        this.direction = direction;
    }

    public void incBlockNumber(){

        this.blockNumber++;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize){
        this.fileSize = fileSize;
    }

    public String getLogin() {
        return super.getLogin();
    }

    public void setLogin(String login) {
        super.setLogin(login);
    }
}
