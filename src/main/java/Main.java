import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Main {
    public static void main(String[] args) throws IOException {
        RandomAccessFile file = new RandomAccessFile("1.txt", "rw");
        FileChannel channel = file.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(8);
        int bytesRead = channel.read(buffer);
        while(bytesRead > -1){
            buffer.flip();
            while(buffer.hasRemaining()){
                System.out.print((char)buffer.get());
            }
            buffer.clear();
            bytesRead = channel.read(buffer);
        }
        file.close();
    }
}
