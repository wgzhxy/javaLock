package org.wanggz.nio.socket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Created by guangzhong.wgz on 2016/3/29.
 */
public class ServerSocketDatagramChannel {

    public static void main(String args[]) {
        ServerSocketDatagramChannel serverChannel = new ServerSocketDatagramChannel();
        serverChannel.serverSocketDatagramChannelNoBlock();
    }

    public void serverSocketDatagramChannelNoBlock() {
        try {
            final DatagramChannel dataChannleNo = DatagramChannel.open();

            dataChannleNo.configureBlocking(false);

            dataChannleNo.socket().setBroadcast(false);
            dataChannleNo.socket().setSoTimeout(5000);
            dataChannleNo.socket().bind(new InetSocketAddress(8080));

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (dataChannleNo != null) {
                            dataChannleNo.close();
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }));

            while (true) {
                try {
                    //读数据
                    ByteBuffer buffer = ByteBuffer.allocateDirect(2048);
                    InetSocketAddress sockaddress = (InetSocketAddress) dataChannleNo.receive(buffer);
                    if (sockaddress != null) {
                        System.out.println(sockaddress.getAddress());
                        System.out.println(sockaddress.getPort());

                        int index = 0;
                        byte[] tpByte = new byte[2048];

                        buffer.flip();
                        while (buffer.hasRemaining()) {
                            tpByte[index++] = buffer.get();
                        }
                        System.out.println(new String(tpByte, "utf-8"));

                        //写数据
                        buffer.clear();
                        buffer.put("serverresp~~~~~~~~~~~~~~~".getBytes("utf-8"));
                        buffer.flip();
                        dataChannleNo.send(buffer, sockaddress);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void serverSocketDatagramChannelBlock() {
        try {
            final DatagramChannel dataChannle = DatagramChannel.open();

            dataChannle.configureBlocking(true);

            dataChannle.socket().setBroadcast(false);
            dataChannle.socket().setSoTimeout(5000);
            dataChannle.socket().bind(new InetSocketAddress(8080));

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (dataChannle != null) {
                            dataChannle.close();
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }));

            while (true) {
                try {
                    //读数据
                    ByteBuffer buffer = ByteBuffer.allocateDirect(2048);
                    InetSocketAddress sockaddress = (InetSocketAddress) dataChannle.receive(buffer);
                    System.out.println(sockaddress.getAddress());
                    System.out.println(sockaddress.getPort());

                    int index = 0;
                    byte[] tpByte = new byte[2048];

                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        tpByte[index++] = buffer.get();
                    }
                    System.out.println(new String(tpByte, "utf-8"));

                    //写数据
                    buffer.clear();
                    buffer.put("serverresp~~~~~~~~~~~~~~~".getBytes("utf-8"));
                    buffer.flip();
                    dataChannle.send(buffer, sockaddress);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
