package com.gb.networkstorage.client;

import com.gb.networkstorage.client.interfaces.ClientAuthCallback;
import com.gb.networkstorage.client.interfaces.ClientCallback;
import com.gb.networkstorage.common.Commands;
import com.gb.networkstorage.common.messages.AbstractMessage;
import com.gb.networkstorage.common.messages.AuthMessage;
import com.gb.networkstorage.common.messages.RequestMessage;
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

    private FileMonitor fileMonitor;

    private final ClientHandler clientHandler;
    private final AuthHandler authHandler;

    public Client() {

        this.clientHandler = new ClientHandler();
        this.authHandler = new AuthHandler();
        this.fileMonitor = new FileMonitor(this);
    }

    public void registerCallback(ClientCallback clientCallback){
        clientHandler.registerCallback(clientCallback);
        fileMonitor.registerCallback(clientCallback);
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
                                    .addLast(authHandler)
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

    public void sendMessage(AbstractMessage msg){
        if(msg instanceof AuthMessage){
            authHandler.sendMessage(msg);
        } else {
            clientHandler.sendMessage(msg);
        }
    }

    public void uploadFile(String fileName) throws IOException {
        clientHandler.uploadFile(fileName);
    }

    public void setLogin(String login) {
        this.login = login;
        clientHandler.setLogin(login);
    }

    public String getLogin() {
        return login;
    }

    public void registerClientAuthCallback(ClientAuthCallback clientAuthCallback) {
        authHandler.registerClientAuthCallback(clientAuthCallback);
    }

    public void configFileAlterationMonitor(boolean switchOn, long interval) throws Exception {
        fileMonitor.configFileAlterationMonitor(switchOn, interval);
    }

}
