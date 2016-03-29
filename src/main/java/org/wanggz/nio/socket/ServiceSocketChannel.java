package org.wanggz.nio.socket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by guangzhong.wgz on 2016/3/29.
 */
public class ServiceSocketChannel {

    public ServiceSocketChannel() {
    }

    public static void serverSocket(int port) {
        ServerSocketChannel serverSocket = null;
        try {
            serverSocket = ServerSocketChannel.open();
            serverSocket.configureBlocking(true);
            serverSocket.socket().bind(new InetSocketAddress(port));

            //服务提供
            int total = 0, index = 0;
            SocketChannel socketChannel = null;
            ByteBuffer buffer = ByteBuffer.allocateDirect(2048);
            byte[] totalBuffer = new byte[12288];
            while (true) {
                total++;
                try {
                    if ((socketChannel = serverSocket.accept()) != null) {

                        socketChannel.read(buffer);
                        buffer.flip();

                        while (buffer.hasRemaining()) {
                            totalBuffer[index++] = buffer.get();
                        }
                        System.out.println(total + new String(totalBuffer, "utf-8"));

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
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                    serverSocket = null;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String args[]) {
        ServiceSocketChannel server = new ServiceSocketChannel();
        server.serverSocket(9090);
    }
}
