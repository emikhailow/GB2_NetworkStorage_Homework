package Client;

import Server.FileUploadFile;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileUploadClientHandler extends ChannelInboundHandlerAdapter {
    private int byteRead;
    private volatile int start = 0;
    private volatile int lastLength = 0;
    public RandomAccessFile randomAccessFile;
    private FileUploadFile fileUploadFile;

    public FileUploadClientHandler(FileUploadFile fileUploadFile) {
        if(fileUploadFile.getFile().exists()){
            if(!fileUploadFile.getFile().isFile()){
                System.out.println("Not a file: " + fileUploadFile.getFile());
                return;
            }
        }
        this.fileUploadFile = fileUploadFile;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        try{
            randomAccessFile = new RandomAccessFile(fileUploadFile.getFile(), "r");
            randomAccessFile.seek(fileUploadFile.getStarPos());
            lastLength = (int) randomAccessFile.length() / 10;
            byte[] bytes = new byte[lastLength];
            if((byteRead = randomAccessFile.read(bytes)) != -1){
                fileUploadFile.setEndPos(byteRead);
                fileUploadFile.setBytes(bytes);
                ctx.writeAndFlush(fileUploadFile);
            } else {
                System.out.println("File has been read");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof Integer){
            start = (Integer) msg;
            if(start != -1){
                randomAccessFile = new RandomAccessFile(fileUploadFile.getFile(), "r");
                randomAccessFile.seek(start);
                System.out.println("Block length: " + (randomAccessFile.length() / 10));
                System.out.println("Length: " + (randomAccessFile.length() - start));
                int a = (int) (randomAccessFile.length() - start);
                int b = (int) (randomAccessFile.length() / 10);
                if (a < b){
                    lastLength = a;
                }
                byte[] bytes = new byte[lastLength];
                System.out.println("--------" + bytes.length);
                if((byteRead = randomAccessFile.read(bytes)) != -1 && (randomAccessFile.length() - start) > 0){
                    System.out.println("Byte length: " + bytes.length);
                    fileUploadFile.setEndPos(byteRead);
                    fileUploadFile.setBytes(bytes);
                    try{
                        ctx.writeAndFlush(fileUploadFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    randomAccessFile.close();
                    ctx.close();
                    System.out.println("File has been read -------" + byteRead);
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
