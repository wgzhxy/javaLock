package org.wanggz.nio.socket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Created by guangzhong.wgz on 2016/3/29.
 */
public class SocketDatagramChannel {

    public static void main(String args[]) {
        SocketDatagramChannel dataGramChannel = new SocketDatagramChannel();
        dataGramChannel.socketDatagramChannelNoBlock();
    }

    public void socketDatagramChannelNoBlock() {
        try {
            final DatagramChannel dataChannle = DatagramChannel.open();
            dataChannle.configureBlocking(false);
            dataChannle.socket().setBroadcast(false);
            dataChannle.socket().setSoTimeout(5000);
            dataChannle.connect(new InetSocketAddress("127.0.0.1", 8080));

            while (true) {
                try {
                    ByteBuffer buffer = ByteBuffer.allocateDirect(2048);
                    //写数据
                    buffer.clear();
                    buffer.put("socketDatagramChannelNoBlock!".getBytes("utf-8"));
                    buffer.flip();
                    dataChannle.write(buffer);
                    break;
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            ByteBuffer buffer = ByteBuffer.allocateDirect(2048);
            while (true) {
                try {
                    //读数据
                    int index = 0;
                    byte[] tpByte = new byte[2048];

                    if (dataChannle.read(buffer) > 0) {
                        buffer.flip();
                        while (buffer.hasRemaining()) {
                            tpByte[index++] = buffer.get();
                        }
                        System.out.println(new String(tpByte, "utf-8"));
                        break;
                    }
                    buffer.clear();

                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void socketDatagramChannelBlock() {
        try {
            final DatagramChannel dataChannle = DatagramChannel.open();
            dataChannle.configureBlocking(true);
            dataChannle.socket().setBroadcast(false);
            dataChannle.socket().setSoTimeout(5000);
            dataChannle.connect(new InetSocketAddress("127.0.0.1", 8080));

            try {
                ByteBuffer buffer = ByteBuffer.allocateDirect(2048);
                //写数据
                buffer.clear();
                buffer.put("socketDatagramChannelBlock!".getBytes("utf-8"));
                buffer.flip();
                dataChannle.write(buffer);

                //读数据
                int index = 0;
                byte[] tpByte = new byte[2048];

                buffer.clear();
                dataChannle.read(buffer);
                buffer.flip();
                while (buffer.hasRemaining()) {
                    tpByte[index++] = buffer.get();
                }
                System.out.println(new String(tpByte, "utf-8"));
            } catch (Throwable e) {
                e.printStackTrace();
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
