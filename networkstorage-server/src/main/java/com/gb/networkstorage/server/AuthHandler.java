package com.gb.networkstorage.server;

import com.gb.networkstorage.common.messages.AuthMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Optional;

public class AuthHandler extends ChannelInboundHandlerAdapter {

    private final UsersDatabase usersDatabase;

    public AuthHandler(UsersDatabase usersDatabase) {
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
            if(nickname != null){
                authMessage.setResult(true);
                ctx.writeAndFlush(authMessage);
                ctx.fireChannelRead(msg);
                ctx.pipeline().remove(this);
            }else
            {
                authMessage.setResult(false);
                authMessage.setMessage("Invalid login or password");
                ctx.writeAndFlush(authMessage);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
