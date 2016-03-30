package org.wanggz.nio.socket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by guangzhong.wgz on 2016/3/29.
 */
public class SelectorServerChannel {

    public static void main(String args[]) {
        try {

            java.nio.channels.ServerSocketChannel serverChannel = java.nio.channels.ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(new InetSocketAddress(8080));

            Selector selector = Selector.open();
            serverChannel.configureBlocking(false);
            SelectionKey key = serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                try {
                    //阻赛选择
                    int readyChannels = selector.select();

                    if (readyChannels == 0) {
                        continue;
                    }
                    System.out.println("准备就绪通道数: " + readyChannels);
                    //处理所有准备就绪通道
                    Set selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                    while (keyIterator.hasNext()) {
                        //盾环处理
                        SelectionKey keyChannel = keyIterator.next();
                        keyIterator.remove();
                        if (keyChannel.isAcceptable()) {
                            //接收请求
                            System.out.println("Acceptable channel!");
                            java.nio.channels.ServerSocketChannel ServerChannel1 = (java.nio.channels.ServerSocketChannel) keyChannel.channel();
                            SocketChannel channel1 = ServerChannel1.accept();
                            //注册
                            channel1.configureBlocking(false);
                            //客户端发送数据
                            ByteBuffer buffer = ByteBuffer.allocate(2048);
                            buffer.put("serverSocket~~~~~~~~~~~~~~~~~~~~~~~~~".getBytes("utf-8"));
                            buffer.flip();
                            channel1.write(buffer);
                            //注册读事件
                            channel1.register(selector, SelectionKey.OP_READ);

                        } else if (keyChannel.isReadable()) {
                            //读数据
                            SocketChannel channel1 = (SocketChannel) keyChannel.channel();
                            int index = 0;
                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            channel1.read(buffer);
                            buffer.flip();
                            System.out.println(new String(buffer.array(), "utf-8"));
                            //写数据给客户端
                            buffer.clear();
                            buffer.put("测义乌一下".getBytes("utf-8"));
                            buffer.flip();
                            channel1.write(buffer);
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
