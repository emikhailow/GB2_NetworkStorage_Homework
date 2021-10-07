package client;

import common.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.*;

public class ClientHandler extends ChannelInboundHandlerAdapter{

    private ChannelHandlerContext ctx;
    private ClientController clientController;

    interface Callback{
        void updateServerFilesList(List<String> list);
        void updateClientFilesList();
    }

    public ClientHandler(ClientController clientController) {
        this.clientController = clientController;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        watch(Constants.ROOT_CLIENT_DIRECTORY);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof String){
            System.out.println(msg);
        } else if (msg instanceof ResponseMessage) {
            clientController.updateServerFilesList(((ResponseMessage) msg).getFilesList());
        } else if (msg instanceof FileMessage){
            FileMessage fileMessage = (FileMessage) msg;
            File file = new File(String.format("%s\\%s", Constants.ROOT_CLIENT_DIRECTORY, fileMessage.getName()));
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            randomAccessFile.setLength(0);
            randomAccessFile.write(fileMessage.getData());
            ctx.writeAndFlush(String.format("File %s has been received from server", fileMessage.getName()));
            clientController.updateClientFilesList();
        }
    }

    private void watch(String dir) throws IOException {

        WatchService watchService = FileSystems.getDefault().newWatchService();
        Path path = Paths.get(dir);
        Files.walkFileTree(path, new SimpleFileVisitor<Path>(){

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                return FileVisitResult.CONTINUE;
            }
        });

        Thread thread = new Thread(() -> {

            try {
                while (true) {
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == OVERFLOW) {
                            continue;
                        }
                        if(event.kind() == ENTRY_DELETE){
                            String name = event.context().toString();
                            String fullName = String.format("%s\\%s", dir, name);
                            System.out.println(String.format("File %s has been deleted", fullName));
                            DeleteMessage deleteMessage = new DeleteMessage(name);
                            ctx.writeAndFlush(deleteMessage);
                        }else if (event.kind() == ENTRY_CREATE){

                        }else if (event.kind() == ENTRY_MODIFY){
                            Path watchableDir = (Path)key.watchable();
                            Path fullPath = watchableDir.resolve(event.context().toString());
                            Path root = Paths.get(dir);
                            Path relative = root.relativize(fullPath);

                            String fullName = fullPath.toString();
                            System.out.println(String.format("File %s has been modified", fullName));
                            FileMessage fileMessage = new FileMessage(fullPath, dir);
                            ctx.writeAndFlush(fileMessage);
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

    public void sendMessage(RequestMessage requestMessage){
        ctx.writeAndFlush(requestMessage);
    }

}
