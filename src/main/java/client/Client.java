package client;

import client.authorization.ClientAuthController;
import client.interfaces.ClientAuthCallback;
import client.interfaces.ClientCallback;
import common.messages.AbstractMessage;
import common.Commands;
import common.messages.RequestMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class Client {

    private String login;
    private final ClientHandler clientHandler;

    public Client() {
        this.clientHandler = new ClientHandler();
    }

    public void registerCallback(ClientCallback clientCallback){
        clientHandler.registerCallback(clientCallback);
    }

    public void connect(int port, String host, CountDownLatch countDownLatch) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try{
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<Channel>(){

                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(new ObjectEncoder())
                                    .addLast(new ObjectDecoder(1024 * 1024 * 100, ClassResolvers.cacheDisabled(null)))
                                    .addLast(clientHandler);
                        }
                    });
            ChannelFuture future = b.connect(host, port).sync();
            countDownLatch.countDown();
            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public void downloadFile(String fileName){
        RequestMessage requestMessage = new RequestMessage(login, Commands.DOWNLOAD_FILE, fileName);
        clientHandler.sendMessage(requestMessage);
    }

    public void sendMessage(AbstractMessage abstractMessage){
        clientHandler.sendMessage(abstractMessage);
    }

    public void uploadFile(String fileName) throws IOException {
        clientHandler.uploadFile(fileName);
    }

    public void setLogin(String login) {
        this.login = login;
        clientHandler.setLogin(login);
    }

    public void registerClientAuthCallback(ClientAuthCallback clientAuthCallback) {
        clientHandler.registerClientAuthCallback(clientAuthCallback);
    }
}
