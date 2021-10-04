package Client;

import Server.FileUploadFile;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.io.File;

public class FileUploadClient {

    public static void main(String[] args) {
        int port = 8189;
        try{
            FileUploadFile uploadFile = new FileUploadFile();
            File file = new File("C:\\ewm\\1.txt");
            String file_md5 = file.getName();
            uploadFile.setFile(file);
            uploadFile.setFile_md5(file_md5);
            uploadFile.setStarPos(0);
            new FileUploadClient().connect(port, "127.0.0.1", uploadFile);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void connect(int port, String host, final FileUploadFile fileUploadFile) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try{
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<Channel>(){

                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline().addLast(new ObjectEncoder());
                            channel.pipeline().addLast(new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(null)));
                            channel.pipeline().addLast(new FileUploadClientHandler(fileUploadFile));
                        }
                    });
            ChannelFuture future = b.connect(host, port).sync();
            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

}
