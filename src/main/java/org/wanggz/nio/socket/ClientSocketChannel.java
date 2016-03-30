package org.wanggz.nio.socket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by guangzhong.wgz on 2016/3/29.
 */
public class ClientSocketChannel {

    public ClientSocketChannel() {
    }

    public static void main(String args[]) {
        ClientSocketChannel client = new ClientSocketChannel();
        String content = "未来的域，我曾经住过~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~相约在未来的路上";
        client.SocketChannel(content);
    }

    public void SocketChannel(String message) {
        SocketChannel socketChannel = null;
        ByteBuffer buffer = ByteBuffer.allocateDirect(2048);
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);

            //其它socket请求参数
            socketChannel.socket().setTcpNoDelay(true);
            socketChannel.socket().setKeepAlive(true);
            socketChannel.socket().setSoTimeout(5000);
            socketChannel.connect(new InetSocketAddress("127.0.0.1", 8080));

            // 连接是异步的
            while (true) {
                if (socketChannel.finishConnect()) {
                    buffer.put(message.getBytes("utf-8"));
                    buffer.flip();
                    socketChannel.write(buffer);
                    buffer.clear();
                    break;
                } else {
                    Thread.sleep(100);
                }
            }
            //连接异步读
            int readTime = 0, index = 0;
            while (true) {
                readTime++;
                byte[] tpByte = new byte[2048];
                if (socketChannel.finishConnect() && socketChannel.read(buffer) > 0) {
                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        tpByte[index++] = buffer.get();
                    }
                    System.out.println(new String(tpByte, "utf-8"));
                    buffer.clear();
                    break;
                } else {
                    Thread.sleep(100);
                }
            }

            System.out.println("\n读了多少次, " + readTime);
            Thread.sleep(5000);

        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            try {
                if (socketChannel != null) {
                    socketChannel.close();
                    socketChannel = null;
                }
            } catch (Throwable e) {

            }
        }
    }

    public void SocketChannelBlock(String message) {
        SocketChannel socketChannel = null;
        ByteBuffer buffer = ByteBuffer.allocateDirect(2048);
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(true);

            //其它socket请求参数
            socketChannel.socket().setTcpNoDelay(true);
            socketChannel.socket().setKeepAlive(true);
            socketChannel.socket().setSoTimeout(5000);

            socketChannel.connect(new InetSocketAddress("127.0.0.1", 9090));

            if (socketChannel.finishConnect()) {
                buffer.put(message.getBytes("utf-8"));
                buffer.flip();
                socketChannel.write(buffer);
                buffer.clear();

                socketChannel.read(buffer);
                buffer.flip();
                while (buffer.hasRemaining()) {
                    System.out.print((char) buffer.get());
                }
                buffer.clear();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            try {
                if (socketChannel != null && socketChannel.isOpen()) {
                    socketChannel.close();
                    socketChannel = null;
                }
            } catch (Throwable e) {

            }
        }
    }
}
