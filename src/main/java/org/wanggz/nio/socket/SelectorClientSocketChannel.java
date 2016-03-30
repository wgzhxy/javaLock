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
public class SelectorClientSocketChannel {

    public static void main(String args[]) {
        try {
            SocketChannel clientChannel = SocketChannel.open();
            clientChannel.configureBlocking(false);
            clientChannel.connect(new InetSocketAddress("127.0.0.1", 8080));

            Selector selector = Selector.open();
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_CONNECT);

            while (true) {
                try {
                    //阻赛选择
                    selector.select();
                    //处理所有准备就绪通道
                    Set selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                    while (keyIterator.hasNext()) {
                        //盾环处理
                        SelectionKey keyChannel = keyIterator.next();
                        if (keyChannel.isConnectable()) {
                            //获取当前socketChannel
                            SocketChannel clientChannel1 = (SocketChannel) keyChannel.channel();
                            //等待连接成功
                            if (clientChannel1.isConnectionPending()) {
                                clientChannel1.finishConnect();
                            }
                            clientChannel1.configureBlocking(false);
                            //写数据数据
                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            buffer.put("clientSocket~~~~~~~~~~~~~~~~~~~~~~~~~".getBytes("utf-8"));
                            buffer.flip();
                            clientChannel1.write(buffer);
                            buffer.clear();
                            //注册OP_READ事件
                            clientChannel1.register(selector, SelectionKey.OP_READ);

                        } else if (keyChannel.isReadable()) {
                            //读数据
                            SocketChannel clientChannel1 = (SocketChannel) keyChannel.channel();
                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            clientChannel1.read(buffer);
                            buffer.flip();
                            System.out.println(new String(buffer.array(), "utf-8"));

                            //写数据
                            buffer.clear();
                            buffer.put("测试数据".getBytes("utf-8"));
                            buffer.flip();
                            clientChannel1.write(buffer);
                        }
                        keyIterator.remove();
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
