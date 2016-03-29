package org.wanggz.nio.socket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by guangzhong.wgz on 2016/3/29.
 */
public class ServiceSocketChannel {

    public static void ServiceSocket(int port) {
        try {
            final ServerSocketChannel serverSocket = ServerSocketChannel.open();
            serverSocket.configureBlocking(false);

            serverSocket.socket().setSoTimeout(5000);
            serverSocket.socket().bind(new InetSocketAddress(port));

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (serverSocket != null) {
                            serverSocket.close();
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }));

            //服务提供
            int total = 0, index = 0, capacity = 0;
            SocketChannel socketChannel = null;
            ByteBuffer buffer = ByteBuffer.allocateDirect(2048);
            while (true) {
                total++;
                index = 0;
                try {
                    if ((socketChannel = serverSocket.accept()) != null) {
                        //读取最大字节数
                        capacity = socketChannel.read(buffer);
                        buffer.flip();

                        byte[] totalBuffer = new byte[2048];
                        while (buffer.hasRemaining()) {
                            totalBuffer[index++] = buffer.get();
                        }
                        System.out.println(total + "," + new String(totalBuffer, "utf-8"));

                        buffer.clear();
                        buffer.put((total + ", service socket rsp : " + total).getBytes("utf-8"));
                        buffer.flip();
                        socketChannel.write(buffer);
                        buffer.clear();
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void serverSocketBlocking(int port) {
        try {
            final ServerSocketChannel serverSocket = ServerSocketChannel.open();
            serverSocket.configureBlocking(true);

            serverSocket.socket().setSoTimeout(5000);
            serverSocket.socket().bind(new InetSocketAddress(port));

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (serverSocket != null) {
                            serverSocket.close();
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }));

            //服务提供
            int total = 0, index = 0;
            SocketChannel socketChannel = null;
            ByteBuffer buffer = ByteBuffer.allocateDirect(2048);
            while (true) {
                total++;
                index = 0;
                try {
                    socketChannel = serverSocket.accept();
                    socketChannel.read(buffer);
                    buffer.flip();

                    byte[] totalBuffer = new byte[2048];
                    while (buffer.hasRemaining()) {
                        totalBuffer[index++] = buffer.get();
                    }
                    System.out.println(total + "," + new String(totalBuffer, "utf-8"));

                    buffer.clear();
                    buffer.put((total + ", service socket rsp : " + total).getBytes("utf-8"));
                    buffer.flip();
                    socketChannel.write(buffer);
                    buffer.clear();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        ServiceSocketChannel server = new ServiceSocketChannel();
        server.serverSocketBlocking(9090);
    }
}
