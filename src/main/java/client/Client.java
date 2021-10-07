package client;

import common.RequestMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class Client {

    private final ClientController clientController;
    private final ClientHandler clientHandler;

    public Client(ClientController clientController) {
        this.clientController = clientController;
        this.clientHandler = new ClientHandler(clientController);
    }

    /* public static void main(String[] args) {
        try{
            new Client().connect(Constants.PORT, Constants.HOST);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }*/

    public void connect(int port, String host) throws InterruptedException {
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
                                    .addLast(new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(null)))
                                    .addLast(clientHandler);
                        }
                    });
            ChannelFuture future = b.connect(host, port).sync();
            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public void sendMessage(RequestMessage requestMessage){
        clientHandler.sendMessage(requestMessage);
    }

}
