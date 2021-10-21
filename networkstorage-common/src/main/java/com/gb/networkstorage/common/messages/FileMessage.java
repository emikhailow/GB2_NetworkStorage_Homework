package com.gb.networkstorage.common.messages;

import com.gb.networkstorage.common.Constants;
import com.gb.networkstorage.common.FileDirection;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

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

    public FileMessage(String login, String fileName, FileDirection direction) {
        this.login = login;
        this.name = fileName;
        this.direction = direction;
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

    public int getBlockCount() {

        return blockCount;
    }

    public int getBlockSize() {

        return blockSize;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public FileDirection getDirection() {

        return direction;
    }

    public void incBlockNumber(){

        this.blockNumber++;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getLogin() {
        return super.getLogin();
    }

    public void setLogin(String login) {
        super.setLogin(login);
    }

    public static void dataFromFileToFileMessage(FileMessage fileMessage, String root) throws IOException {
        fileMessage.incBlockNumber();
        String fullName = getFullFileName(root, fileMessage.getLogin(), fileMessage.getName());

        RandomAccessFile randomAccessFile = new RandomAccessFile(fullName, "r");

        randomAccessFile.seek((long) (fileMessage.getBlockNumber() - 1) * (long) fileMessage.getBlockSize());
        byte[] bytes = new byte[fileMessage.getBlockSize()];
        int readBytes = randomAccessFile.read(bytes);
        if(readBytes > -1 && readBytes < fileMessage.getBlockSize()){
            fileMessage.setData(Arrays.copyOfRange(bytes, 0, readBytes));
        } else {
            fileMessage.setData(bytes);
        }
        randomAccessFile.close();
    }

    public static void dataFromFileMessageToFile(FileMessage fileMessage, String root) throws IOException {
        String fullName = getFullFileName(root, fileMessage.getLogin(), fileMessage.getName());

        RandomAccessFile randomAccessFile = new RandomAccessFile(new File(fullName), "rw");
        if(fileMessage.getBlockNumber() == 1) {
            randomAccessFile.setLength(0);
        }
        randomAccessFile.seek((long) (fileMessage.getBlockNumber() - 1) * (long) fileMessage.getBlockSize());
        randomAccessFile.write(fileMessage.getData());
        randomAccessFile.close();

        fileMessage.setData(null);
    }

    private static String getFullFileName(String root, String login, String fileName){
        return String.format("%s%s%s%s%s",
                root, File.separator,
                login, File.separator,
                fileName);
    }
}
