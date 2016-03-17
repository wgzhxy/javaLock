package org.wanggz.nio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.FileChannel;

/**
 * Created by guangzhong.wgz on 2016/3/11.
 */
public class IoMain {

    public static void main(String args[]) {
        readChannel();
    }


    public static void readChannel() {

        RandomAccessFile aFile = null;
        FileChannel channel = null;

        try {
            aFile = new RandomAccessFile("data/nio-data.txt", "rw");

            channel = aFile.getChannel();
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);

            long totalSize = 0;
            StringBuilder builder = new StringBuilder();

            Integer length = Integer.parseInt(String.valueOf(channel.size()));
            byte[] totalBuffer = new byte[length];

            int index = 0;
            while ((totalSize = channel.read(byteBuffer)) != -1) {
                byteBuffer.flip();
                while (byteBuffer.hasRemaining()) {
                    totalBuffer[index++] = byteBuffer.get();
                }
                byteBuffer.clear();
            }
            System.out.println(new String(totalBuffer));

        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            try {
                if (aFile != null) {
                    aFile.close();
                    aFile = null;
                }
                if (channel != null) {
                    channel.close();
                    channel = null;
                }

            } catch (Throwable e) {
            }
        }
       /* FileChannel inChannel = aFile.getChannel();
        ByteBuffer buf = ByteBuffer.allocate(48);
        int bytesRead = inChannel.read(buf);
        while (bytesRead != -1) {
            System.out.println("Read " + bytesRead);
            buf.flip();
            while(buf.hasRemaining()){
                System.out.print((char) buf.get());
            }
            buf.clear();
            bytesRead = inChannel.read(buf);
        }
        aFile.close();*/
    }

    public static void writeChannel() {
        FileOutputStream fileOut = null;
        FileChannel channel = null;
        try {
            InputStream input = IoMain.class.getClassLoader().getResourceAsStream("test.txt");
            byte[] byteBuffer = new byte[1024];

            int size = 0;
            StringBuilder builer = new StringBuilder();
            while ((size = input.read(byteBuffer)) != -1) {
                builer.append(new String(byteBuffer));
            }
            System.out.println(builer.toString());

            File file = new File("./test.txt");
            if (!file.exists()) {
                file.createNewFile();
                file.setWritable(true);
            }
            fileOut = new FileOutputStream(file, true);
            channel = fileOut.getChannel();

            ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
            buffer.putChar('\n');

            buffer.put("我是一个中国人，我的家乡在北京".getBytes("utf-8"));

            buffer.mark();

            buffer.putChar('&');
            buffer.putChar('#');

            buffer.reset();
            buffer.flip();

            //buffer.rewind();

            channel.write(buffer);

            buffer.clear();

        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOut != null) {
                    fileOut.close();
                    fileOut = null;
                }
                if (channel != null) {
                    channel.close();
                    channel = null;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }

        }
    }
}
