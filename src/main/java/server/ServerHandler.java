package server;

import common.*;
import common.messages.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    UsersDatabase usersDatabase;

    public ServerHandler(UsersDatabase usersDatabase) {
        this.usersDatabase = usersDatabase;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if(msg instanceof AuthMessage){
            AuthMessage authMessage = (AuthMessage) msg;
            String nickname = usersDatabase.getNickname(authMessage.getLogin(), authMessage.getPassword());
            if(nickname == null){
                authMessage.setResult(false);
                authMessage.setMessage("Invalid login or password");
            }else
            {
                authMessage.setResult(true);
            }
            ctx.writeAndFlush(authMessage);
        }
        else if(msg instanceof FileMessage){
            FileMessage fileMessage = (FileMessage) msg;

            if(fileMessage.getDirection() == FileDirection.UPLOAD){
                dataFromFileMessageToFile(fileMessage);
                ctx.writeAndFlush(fileMessage);
            } else if(fileMessage.getDirection() == FileDirection.DOWNLOAD){
                if(fileMessage.getBlockNumber() < fileMessage.getBlockCount()){
                    dataFromFileToFileMessage(fileMessage);
                    ctx.writeAndFlush(fileMessage);
                }
            }
        } else if (msg instanceof RequestMessage){

            RequestMessage requestMessage = (RequestMessage) msg;
            if(requestMessage.getCommand() == Commands.DOWNLOAD_FILE){
                try {
                    String fullName = getFullFileName(requestMessage.getLogin(), requestMessage.getFileName());
                    FileMessage fileMessage = new FileMessage(requestMessage.getLogin(),
                            requestMessage.getFileName(),
                            FileDirection.DOWNLOAD,
                            new File(fullName).length());
                    dataFromFileToFileMessage(fileMessage);
                    ctx.writeAndFlush(fileMessage);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            } else if (requestMessage.getCommand() == Commands.GET_FILES_LIST){
                String fullDir = String.format("%s%s%s",
                        Constants.ROOT_SERVER_DIRECTORY,
                        File.separator,
                        requestMessage.getLogin());

                new File(fullDir).mkdirs();

                List<String> filesList = Files.list(Paths.get(fullDir))
                        .filter(p->!Files.isDirectory(p))
                        .map(p -> p.getFileName().toString())
                        .collect(Collectors.toList());
                ResponseMessage responseMessage = new ResponseMessage();
                responseMessage.setFilesList(new ArrayList<>(filesList));
                ctx.writeAndFlush(responseMessage);
            } else if (requestMessage.getCommand() == Commands.DELETE_FILE){
                String fullName = getFullFileName(requestMessage.getLogin(), requestMessage.getFileName());
                File file = new File(fullName);
                if(file.delete()){
                    ctx.writeAndFlush(String.format("File %s has been deleted", requestMessage.getFileName()));
                }
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
                Constants.ROOT_SERVER_DIRECTORY, File.separator,
                login, File.separator,
                fileName);

    }

    private void dataFromFileToFileMessage(FileMessage fileMessage) throws IOException {
        fileMessage.incBlockNumber();
        String fullName = getFullFileName(fileMessage.getLogin(), fileMessage.getName());

        RandomAccessFile randomAccessFile = new RandomAccessFile(fullName, "r");
        randomAccessFile.seek((long) (fileMessage.getBlockNumber() - 1) * (long) fileMessage.getBlockSize());
        byte[] bytes = new byte[fileMessage.getBlockSize()];
        int readBytes = randomAccessFile.read(bytes);
        if(readBytes < fileMessage.getBlockSize()){
            fileMessage.setData(Arrays.copyOfRange(bytes, 0, readBytes));
        } else {
            fileMessage.setData(bytes);
        }
        randomAccessFile.close();
    }

    private void dataFromFileMessageToFile(FileMessage fileMessage) throws IOException {
        String fullName = getFullFileName(fileMessage.getLogin(), fileMessage.getName());

        RandomAccessFile randomAccessFile = new RandomAccessFile(new File(fullName), "rw");
        if(fileMessage.getBlockNumber() == 1) {
            randomAccessFile.setLength(0);
        }
        randomAccessFile.seek((long) (fileMessage.getBlockNumber() - 1) * (long) fileMessage.getBlockSize());
        randomAccessFile.write(fileMessage.getData());
        randomAccessFile.close();

        fileMessage.setData(null);
    }
}
