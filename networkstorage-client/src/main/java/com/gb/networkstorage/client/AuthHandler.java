package com.gb.networkstorage.client;

import com.gb.networkstorage.client.interfaces.ClientAuthCallback;
import com.gb.networkstorage.common.messages.AbstractMessage;
import com.gb.networkstorage.common.messages.AuthMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class AuthHandler extends ChannelInboundHandlerAdapter {

    ClientAuthCallback clientAuthCallback;
    private ChannelHandlerContext ctx;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof AuthMessage){
            AuthMessage authMessage = (AuthMessage) msg;
            clientAuthCallback.processAuthResult(authMessage);
            if(authMessage.isResult()){
                ctx.fireChannelRead(msg);
                ctx.pipeline().remove(this);
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    public void registerClientAuthCallback(ClientAuthCallback clientAuthCallback) {
        this.clientAuthCallback = clientAuthCallback;
    }

    public void sendMessage(AbstractMessage abstractMessage){
        ctx.writeAndFlush(abstractMessage);
    }
}
