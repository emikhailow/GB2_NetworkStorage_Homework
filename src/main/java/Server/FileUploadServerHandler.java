package Server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.RandomAccessFile;

public class FileUploadServerHandler extends ChannelInboundHandlerAdapter {
    private int byteRead;
    private volatile int start = 0;
    private String file_dir = "D:";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FileUploadFile){
            FileUploadFile ef = (FileUploadFile) msg;
            byte[] bytes = ef.getBytes();
            byteRead = ef.getEndPos();
            String md5 = ef.getFile_md5();
            String path = file_dir + File.separator + md5;
            File file = new File(path);
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            randomAccessFile.seek(start);
            randomAccessFile.write(bytes);
            start += byteRead;
            if(byteRead > 0){
                ctx.writeAndFlush(start);
            } else {
                randomAccessFile.close();
                ctx.close();
            }


        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
