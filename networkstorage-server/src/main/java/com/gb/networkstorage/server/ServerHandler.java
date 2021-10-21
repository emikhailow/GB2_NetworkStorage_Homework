package com.gb.networkstorage.server;

import com.gb.networkstorage.common.Commands;
import com.gb.networkstorage.common.FileDirection;
import com.gb.networkstorage.common.messages.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    String rootDir;

    public ServerHandler(String rootDir) {
        this.rootDir = rootDir;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof FileMessage){
            FileMessage fileMessage = (FileMessage) msg;
            if(fileMessage.getDirection() == FileDirection.UPLOAD){
                processFileMessageToUpload(ctx, fileMessage);
            } else if(fileMessage.getDirection() == FileDirection.DOWNLOAD){
                processFileMessageToDownload(ctx, fileMessage);
            } else if(fileMessage.getDirection() == FileDirection.DELETE_ON_SERVER){
                processFileMessageToDelete(ctx, fileMessage);
            }
        } else if (msg instanceof RequestMessage){
            RequestMessage requestMessage = (RequestMessage) msg;
            if(requestMessage.getCommand() == Commands.DOWNLOAD_FILE){
                processRequestMessageToDownloadFile(ctx, requestMessage);
            } else if (requestMessage.getCommand() == Commands.GET_FILES_LIST){
                processRequestMessageToGetFilesList(ctx, requestMessage);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private String getFullFileName(String login, String fileName){
        return String.format("%s%s%s%s%s",
                rootDir, File.separator,
                login, File.separator,
                fileName);
    }

    private String getServerDir(String login){
        return String.format("%s%s%s",
                rootDir, File.separator, login);
    }

    private List<String> getServerFilesList(String login) throws IOException {
        String fullDir = getServerDir(login);
        return Files.list(Paths.get(fullDir))
                .filter(f -> !Files.isDirectory(f))
                .map(f -> f.getFileName().toString())
                .collect(Collectors.toList());
    }

    private void processFileMessageToUpload(ChannelHandlerContext ctx, FileMessage fileMessage) throws IOException {
        String root = rootDir;
        FileMessage.dataFromFileMessageToFile(fileMessage, root);
        ctx.writeAndFlush(fileMessage);
    }

    private void processFileMessageToDownload(ChannelHandlerContext ctx, FileMessage fileMessage) throws IOException {
        if(fileMessage.getBlockNumber() < fileMessage.getBlockCount()){
            FileMessage.dataFromFileToFileMessage(fileMessage, rootDir);
            ctx.writeAndFlush(fileMessage);
        }
    }

    private void processFileMessageToDelete(ChannelHandlerContext ctx, FileMessage fileMessage) throws IOException {
        File file = new File(getFullFileName(fileMessage.getLogin(), fileMessage.getName()));
        if(file.exists() && file.delete()){
            ctx.writeAndFlush(String.format("File %s has been deleted on server", fileMessage.getName()));

            ResponseMessage responseMessage = new ResponseMessage();
            responseMessage.setFilesList(new ArrayList<>(getServerFilesList(fileMessage.getLogin())));
            ctx.writeAndFlush(responseMessage);
        }
    }

    private void processRequestMessageToDownloadFile(ChannelHandlerContext ctx, RequestMessage requestMessage) throws IOException {
        try {
            String fullName = getFullFileName(requestMessage.getLogin(), requestMessage.getFileName());
            FileMessage fileMessage = new FileMessage(requestMessage.getLogin(),
                    requestMessage.getFileName(),
                    FileDirection.DOWNLOAD,
                    new File(fullName).length());
            FileMessage.dataFromFileToFileMessage(fileMessage, rootDir);
            ctx.writeAndFlush(fileMessage);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void processRequestMessageToGetFilesList(ChannelHandlerContext ctx, RequestMessage requestMessage) throws IOException {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setFilesList(new ArrayList<>(getServerFilesList(requestMessage.getLogin())));
        ctx.writeAndFlush(responseMessage);
    }
}
