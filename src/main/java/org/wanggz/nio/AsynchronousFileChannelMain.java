package org.wanggz.nio;

import com.taobao.common.sentinel.hsf.util.common.StringUtils;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Created by guangzhong.wgz on 2016/3/31.
 */
public class AsynchronousFileChannelMain {

    static Thread current;

    public static void main(String args[]) {
        String path = "test.txt";
        read(path);
    }

    public static void write(String path, int size) {
        if (StringUtils.isBlank(path)) {
            return;
        }
        AsynchronousFileChannel synFile = null;
        try {
            Path filePath = Paths.get(path);
            synFile = AsynchronousFileChannel.open(filePath, StandardOpenOption.WRITE);
            final ByteBuffer buffer = ByteBuffer.allocate(1024);

            buffer.put("  \n this is a test, my babi~~~~~~~~~~~~~~~~~~~!".getBytes());
            buffer.flip();
            synFile.write(buffer, size, synFile, new CompletionHandler<Integer, AsynchronousFileChannel>() {

                @Override
                public void completed(Integer result, AsynchronousFileChannel attachment) {
                    System.out.println("result : " + result);
                    if (attachment != null) {
                        try {
                            attachment.close();
                        } catch (Throwable e) {
                        }
                    }
                }

                @Override
                public void failed(Throwable exc, AsynchronousFileChannel attachment) {
                    exc.printStackTrace();
                }
            });

            Thread.sleep(15 * 1000);

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void read(String path) {
        if (StringUtils.isBlank(path)) {
            return;
        }
        AsynchronousFileChannel synFile = null;
        try {
            Path filePath = Paths.get(path);
            synFile = AsynchronousFileChannel.open(filePath, StandardOpenOption.READ);
            final ByteBuffer buffer = ByteBuffer.allocate(1024);

            synFile.read(buffer, 0, synFile, new CompletionHandler<Integer, AsynchronousFileChannel>() {

                @Override
                public void completed(Integer result, AsynchronousFileChannel attachment) {
                    System.out.println("result : " + result);
                    buffer.flip();
                    System.out.println(new String(buffer.array()));
                    if (attachment != null) {
                        try {
                            attachment.close();
                        } catch (Throwable e) {
                        }
                    }
                    write("test.txt", result);
                }

                @Override
                public void failed(Throwable exc, AsynchronousFileChannel attachment) {
                    exc.printStackTrace();
                }
            });

            Thread.sleep(15 * 1000);

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /*private static void futureDemo(AsynchronousFileChannel asyfileChannel, ByteBuffer byteBuffer) {
        try {
            Future<Integer> result = asyfileChannel.read(byteBuffer, 0);
            while (!result.isDone()) {
                System.out.println("Waiting file channel finished....");
                Thread.sleep(1);
            }
            System.out.println("Finished? = " + result.isDone());
            System.out.println("byteBuffer = " + result.get());
            System.out.println(new String(byteBuffer.array()));

            byteBuffer.clear();
            byteBuffer.put("测试一下\n".getBytes());
            byteBuffer.put(("给一个随机数, random = " + Math.random()).getBytes());
            byteBuffer.flip();
            Future<Integer> writeFuture = asyfileChannel.write();
            while(!result.isDone()) {
                Thread.sleep(1);
            }
            asyfileChannel.close();
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e.getCause());
        }
    }*/
}
