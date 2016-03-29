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
        String content = "未来的域，我曾经住过~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~";
        client.SocketChannelBlock(content);
    }

    public void SocketChannelBlock(String message) {
        SocketChannel socketChannel = null;
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(true);
            socketChannel.connect(new InetSocketAddress("127.0.0.1", 9090));

            if (socketChannel.finishConnect()) {
                ByteBuffer buffer = ByteBuffer.allocateDirect(2048);
                buffer.put(message.getBytes("utf-8"));
                buffer.flip();
                socketChannel.write(buffer);
            }

            ByteBuffer dst = ByteBuffer.allocateDirect(2048);
            socketChannel.read(dst);
            dst.flip();
            while (dst.hasRemaining()) {
                System.out.print((char) dst.get());
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
