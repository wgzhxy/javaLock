package org.wanggz.nio.socket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

/**
 * Created by guangzhong.wgz on 2016/3/29.
 */
public class ServiceSocketChannelFactory {

    ServerSocketChannel serverSocket = null;
    private int port = 80;
    private String ip = "127.0.0.1";
    private Selector selector = null;


    public ServiceSocketChannelFactory() {
    }

    public ServiceSocketChannelFactory(String ipAddress, int port) {
        this.port = port;
        this.ip = ip;
    }

    public static void main(String args[]) {

        System.out.println("============================");

        final ServiceSocketChannelFactory factory = new ServiceSocketChannelFactory();
        factory.startServiceSocket();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("==============stopping==============");
                factory.close();
            }
        });
    }

    public void startServiceSocket() {
        try {
            selector = Selector.open();

            serverSocket = ServerSocketChannel.open();
            serverSocket.configureBlocking(false);

            serverSocket.bind(new InetSocketAddress(this.port));

            serverSocket.register(selector, SelectionKey.OP_ACCEPT);

            int i = 0;
            while (true) {
                System.out.println("总共运行," + (i++) + "次");
                int ops = 0;
                if ((ops = selector.select()) > 0) {
                    switch (ops) {
                        case SelectionKey.OP_ACCEPT:
                            System.out.println("=====================");
                            Set<SelectionKey> keys = selector.keys();
                            for (SelectionKey key : keys) {
                                if (key.isAcceptable()) {
                                    SocketChannel socketChannel = (SocketChannel) key.channel();
                                    ByteBuffer buffer = ByteBuffer.allocateDirect(2048);
                                    socketChannel.read(buffer);
                                    buffer.flip();
                                    System.out.println(new String(buffer.array(), "utf-8"));
                                }
                            }
                            break;
                    }
                }
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    public void close() {
        try {
            if (serverSocket != null && serverSocket.isOpen()) {
                serverSocket.close();
            }
            if (selector != null && selector.isOpen()) {
                selector.close();
                selector = null;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
