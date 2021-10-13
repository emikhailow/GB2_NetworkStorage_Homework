package client;

import client.interfaces.ClientAuthCallback;
import client.interfaces.ClientCallback;
import common.*;
import common.messages.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.*;
import java.util.Arrays;

import static java.nio.file.StandardWatchEventKinds.*;

public class ClientHandler extends ChannelInboundHandlerAdapter{

    private String login;
    private ChannelHandlerContext ctx;
    private ClientCallback clientCallback;
    private ClientAuthCallback clientAuthCallback;

    public void uploadFile(String fileName) throws IOException {

        try {

            String fullName = getFullFileName(login, fileName);
            FileMessage fileMessage = new FileMessage(login,
                    fileName,
                    FileDirection.UPLOAD,
                    new File(fullName).length());

            dataFromFileToFileMessage(fileMessage);
            ctx.writeAndFlush(fileMessage);

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        //watch(Constants.ROOT_CLIENT_DIRECTORY);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if(msg instanceof AuthMessage){
            AuthMessage authMessage = (AuthMessage) msg;
            clientAuthCallback.processAuthResult(authMessage);
        } else if (msg instanceof String){
            System.out.println(msg);
        } else if (msg instanceof ResponseMessage) {
            clientCallback.updateServerFilesList(((ResponseMessage) msg).getFilesList());
        } else if (msg instanceof FileMessage){
            FileMessage fileMessage = (FileMessage) msg;
            if(fileMessage.getDirection() == FileDirection.UPLOAD){

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
                    clientCallback.setLogText(text);

                    dataFromFileToFileMessage(fileMessage);
                    ctx.writeAndFlush(fileMessage);
                } else {
                    String text = String.format("File %s: all bytes sent",
                            fileMessage.getName());
                    clientCallback.setLogText(text);
                    sendMessage(new RequestMessage(login, Commands.GET_FILES_LIST));
                }
            } else if (fileMessage.getDirection() == FileDirection.DOWNLOAD){

                try{
                    dataFromFileMessageToFile(fileMessage);
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
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
                    clientCallback.setLogText(text);

                    ctx.writeAndFlush(fileMessage);
                } else {
                    String text = String.format("File %s: all bytes received",
                            fileMessage.getName());
                    clientCallback.setLogText(text);
                    clientCallback.updateClientFilesList();
                }
            }

        }
    }

    private void watch(String dir) throws IOException {

        WatchService watchService = FileSystems.getDefault().newWatchService();
        Path path = Paths.get(dir);
        /*Files.walkFileTree(path, new SimpleFileVisitor<Path>(){

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                return FileVisitResult.CONTINUE;
            }
        });*/
        path.register(watchService, ENTRY_MODIFY);

        Thread thread = new Thread(() -> {

            try {
                while (true) {
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == OVERFLOW) {
                            continue;
                        }
                        if(event.kind() == ENTRY_DELETE){
                            /*String name = event.context().toString();
                            String fullName = String.format("%s\\%s", dir, name);
                            System.out.println(String.format("File %s has been deleted", fullName));
                            DeleteMessage deleteMessage = new DeleteMessage(name);
                            ctx.writeAndFlush(deleteMessage);*/
                        }else if (event.kind() == ENTRY_CREATE){

                        }else if (event.kind() == ENTRY_MODIFY){
                            String fileName = event.context().toString();
                            clientCallback.setLogText(String.format("File %s has been modified", fileName));
                            uploadFile(fileName);
                            /*Path watchableDir = (Path)key.watchable();
                            Path fullPath = watchableDir.resolve(event.context().toString());
                            Path root = Paths.get(dir);
                            Path relative = root.relativize(fullPath);

                            String fullName = fullPath.toString();
                            System.out.println(String.format("File %s has been modified", fullName));
                            FileMessage fileMessage = new FileMessage(fullPath, dir);
                            ctx.writeAndFlush(fileMessage);*/
                        }
                    }
                    if (!key.reset()) {
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
         });
        thread.setDaemon(true);
        thread.start();
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

    public void registerClientAuthCallback(ClientAuthCallback clientAuthCallback) {
        this.clientAuthCallback = clientAuthCallback;
    }

    private String getFullFileName(String login, String fileName){

        return String.format("%s%s%s%s%s",
                Constants.ROOT_CLIENT_DIRECTORY, File.separator,
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
