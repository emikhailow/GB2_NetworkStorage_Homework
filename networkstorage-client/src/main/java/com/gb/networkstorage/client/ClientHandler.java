package com.gb.networkstorage.client;

import com.gb.networkstorage.common.Additional;
import com.gb.networkstorage.common.Commands;
import com.gb.networkstorage.common.FileDirection;
import com.gb.networkstorage.client.interfaces.ClientCallback;
import com.gb.networkstorage.common.messages.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.IOException;

public class ClientHandler extends ChannelInboundHandlerAdapter{

    private String login;
    private ChannelHandlerContext ctx;
    private ClientCallback clientCallback;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if(this.ctx == null){
            this.ctx = ctx;
        }
        if (msg instanceof String){
            System.out.println(msg);
        } else if (msg instanceof ResponseMessage) {
            clientCallback.updateServerFilesList(((ResponseMessage) msg).getFilesList());
        } else if (msg instanceof FileMessage){
            FileMessage fileMessage = (FileMessage) msg;
            if(fileMessage.getDirection() == FileDirection.UPLOAD){
                processFileMessageToUpload(ctx, fileMessage);
            } else if (fileMessage.getDirection() == FileDirection.DOWNLOAD){
                processFileMessageToDownload(ctx, fileMessage);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    public void uploadFile(String fileName) throws IOException {
        try {
            String fullName = getFullFileName(login, fileName);
            FileMessage fileMessage = new FileMessage(login,
                    fileName,
                    FileDirection.UPLOAD,
                    new File(fullName).length());

            FileMessage.dataFromFileToFileMessage(fileMessage, Constants.ROOT_DIRECTORY);
            ctx.writeAndFlush(fileMessage);

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(AbstractMessage abstractMessage){
        ctx.writeAndFlush(abstractMessage);
    }

    public void registerCallback(ClientCallback clientCallback) {
        this.clientCallback = clientCallback;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    private String getFullFileName(String login, String fileName){
        return String.format("%s%s%s%s%s",
                Constants.ROOT_DIRECTORY, File.separator,
                login, File.separator,
                fileName);
    }

    private void processFileMessageToUpload(ChannelHandlerContext ctx, FileMessage fileMessage) throws IOException {
        if(fileMessage.getBlockNumber() < fileMessage.getBlockCount()){
            long bytesSent = Math.min(
                    (long) fileMessage.getBlockNumber() * (long) fileMessage.getBlockSize(),
                    fileMessage.getFileSize());
            long bytesTotal = fileMessage.getFileSize();

            String text = String.format("File %s: %s of %s sent (%.2f %%)",
                    fileMessage.getName(),
                    Additional.humanReadableByteCountBin(bytesSent),
                    Additional.humanReadableByteCountBin(bytesTotal),
                    100 * ((double) bytesSent / bytesTotal));
            clientCallback.processTextMessage(text);

            FileMessage.dataFromFileToFileMessage(fileMessage, Constants.ROOT_DIRECTORY);
            ctx.writeAndFlush(fileMessage);
        } else {
            String text = String.format("File %s: all bytes sent",
                    fileMessage.getName());
            clientCallback.processTextMessage(text);
            sendMessage(new RequestMessage(login, Commands.GET_FILES_LIST));
        }
    }

    private void processFileMessageToDownload(ChannelHandlerContext ctx, FileMessage fileMessage) throws IOException {
        FileMessage.dataFromFileMessageToFile(fileMessage,  Constants.ROOT_DIRECTORY);
        if(fileMessage.getBlockNumber() < fileMessage.getBlockCount()){

            long bytesReceived = Math.min(
                    (long) fileMessage.getBlockNumber() * (long) fileMessage.getBlockSize(),
                    fileMessage.getFileSize());
            long bytesTotal = fileMessage.getFileSize();
            String text = String.format("File %s: %s of %s received (%.2f %%)",
                    fileMessage.getName(),
                    Additional.humanReadableByteCountBin(bytesReceived),
                    Additional.humanReadableByteCountBin(bytesTotal),
                    100 * ((double) bytesReceived / bytesTotal));
            clientCallback.processTextMessage(text);

            ctx.writeAndFlush(fileMessage);
        } else {
            String text = String.format("File %s: all bytes received",
                    fileMessage.getName());
            clientCallback.processTextMessage(text);
            clientCallback.updateClientFilesList();
        }
    }

}
