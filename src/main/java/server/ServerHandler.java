package server;

import common.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof FileMessage){
            FileMessage fileMessage = (FileMessage) msg;
            File file = new File(String.format("%s\\%s", Constants.ROOT_SERVER_DIRECTORY, fileMessage.getName()));
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            randomAccessFile.setLength(0);
            randomAccessFile.write(fileMessage.getData());
            ctx.writeAndFlush(String.format("File %s has been received", fileMessage.getName()));
        } else if (msg instanceof DeleteMessage){
            DeleteMessage deleteMessage = (DeleteMessage) msg;
            File file = new File(String.format("%s\\%s",
                    Constants.ROOT_SERVER_DIRECTORY,
                    deleteMessage.getName()));
            if(file.delete()){
                ctx.writeAndFlush(String.format("File %s has been deleted", deleteMessage.getName()));
            }
        } else if (msg instanceof RequestMessage){

            RequestMessage requestMessage = (RequestMessage) msg;
            if(requestMessage.getCommand() == Commands.DOWNLOAD_FILE){
                String fileName = requestMessage.getFileName();
                String fullName = String.format("%s\\%s", Constants.ROOT_SERVER_DIRECTORY, fileName);
                Path fullPath = Paths.get(fullName);
                if(Files.exists(fullPath)){
                    FileMessage fileMessage = new FileMessage(fullPath, Constants.ROOT_SERVER_DIRECTORY);
                    ctx.writeAndFlush(fileMessage);
                }
           } else if (requestMessage.getCommand() == Commands.GET_FILES_LIST){
                List<String> filesList = Files.list(Paths.get(Constants.ROOT_SERVER_DIRECTORY))
                        .filter(p->!Files.isDirectory(p))
                        .map(p -> p.getFileName().toString())
                        .collect(Collectors.toList());
                ResponseMessage responseMessage = new ResponseMessage();
                responseMessage.setFilesList(new ArrayList<>(filesList));
                ctx.writeAndFlush(responseMessage);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
