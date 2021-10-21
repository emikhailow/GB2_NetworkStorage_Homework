package com.gb.networkstorage.client;

import com.gb.networkstorage.client.interfaces.ClientCallback;
import com.gb.networkstorage.common.FileDirection;
import com.gb.networkstorage.common.messages.FileMessage;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;
import java.io.IOException;

public class FileMonitor {

    private Client client;
    private FileAlterationMonitor monitor;
    private ClientCallback clientCallback;

    public FileMonitor(Client client) {
        this.client = client;
    }

    public void configFileAlterationMonitor(boolean switchOn, long interval) throws Exception {

        if(monitor != null){
            monitor.stop();
            monitor = null;
        }

        if(!switchOn){
            return;
        }

        monitor = new FileAlterationMonitor(interval);
        FileAlterationObserver observer = new FileAlterationObserver(getUserDir());
        FileAlterationListener listener = new FileAlterationListener() {

            @Override
            public void onStart(FileAlterationObserver fileAlterationObserver) {

            }

            @Override
            public void onDirectoryCreate(File file) {

            }

            @Override
            public void onDirectoryChange(File file) {

            }

            @Override
            public void onDirectoryDelete(File file) {

            }

            @Override
            public void onFileCreate(File file) {
                String fileName = file.getName();
                clientCallback.processTextMessage(String.format("File %s has been created", fileName));
                clientCallback.updateClientFilesList();
                FileMessage fileMessage = new FileMessage(client.getLogin(), fileName, FileDirection.UPLOAD, file.length());
                try {
                    FileMessage.dataFromFileToFileMessage(fileMessage, Constants.ROOT_DIRECTORY);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                client.sendMessage(fileMessage);
            }

            @Override
            public void onFileChange(File file) {
                String fileName = file.getName();
                clientCallback.processTextMessage(String.format("File %s has been changed", fileName));
                clientCallback.updateClientFilesList();
                FileMessage fileMessage = new FileMessage(client.getLogin(), fileName, FileDirection.UPLOAD, file.length());
                try {
                    FileMessage.dataFromFileToFileMessage(fileMessage, Constants.ROOT_DIRECTORY);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                client.sendMessage(fileMessage);
            }

            @Override
            public void onFileDelete(File file) {
                String fileName = file.getName();
                clientCallback.processTextMessage(String.format("File %s has been deleted", fileName));
                clientCallback.updateClientFilesList();
                FileMessage fileMessage = new FileMessage(client.getLogin(), fileName, FileDirection.DELETE_ON_SERVER);
                client.sendMessage(fileMessage);
            }

            @Override
            public void onStop(FileAlterationObserver fileAlterationObserver) {

            }
        };

        observer.addListener(listener);
        monitor.addObserver(observer);
        monitor.start();
    }

    public void registerCallback(ClientCallback clientCallback) {
        this.clientCallback = clientCallback;
    }

    private String getUserDir(){
        return String.format("%s%s%s",
                Constants.ROOT_DIRECTORY, File.separator, client.getLogin());
    }

}
