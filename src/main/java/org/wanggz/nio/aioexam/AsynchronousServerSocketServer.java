package org.wanggz.nio.aioexam;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * Created by guangzhong.wgz on 2016/3/31.
 */
public class AsynchronousServerSocketServer {

    static int PORT = 8080;
    static int BUFFER_SIZE = 1024;
    static String CHARSET = "utf-8"; //默认编码
    static CharsetDecoder decoder = Charset.forName(CHARSET).newDecoder(); //解码

    int port;
    //ByteBuffer buffer;
    AsynchronousServerSocketChannel serverChannel;

    public AsynchronousServerSocketServer(int port) throws IOException {
        this.port = port;
        //this.buffer = ByteBuffer.allocate(BUFFER_SIZE);
        this.decoder = Charset.forName(CHARSET).newDecoder();
    }

    private static void close(AsynchronousSocketChannel client) {
        try {
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("running...");
            AsynchronousServerSocketServer server = new AsynchronousServerSocketServer(PORT);
            server.listen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listen() throws Exception {
        //打开一个服务通道
        //绑定服务端口
        this.serverChannel = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(port), 100);
        this.serverChannel.accept(this, new AcceptHandler());

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    System.out.println("运行中...");
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();
    }

    /**
     * accept到一个请求时的回调
     */
    private class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketServer> {
        @Override
        public void completed(final AsynchronousSocketChannel client, AsynchronousServerSocketServer attachment) {
            try {
                System.out.println("远程IP：" + client.getRemoteAddress());
                //tcp各项参数
                client.setOption(StandardSocketOptions.TCP_NODELAY, true);
                client.setOption(StandardSocketOptions.SO_SNDBUF, 1024);
                client.setOption(StandardSocketOptions.SO_RCVBUF, 1024);

                if (client.isOpen()) {
                    final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                    buffer.clear();
                    client.read(buffer, client, new ReadHandler(buffer));
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                attachment.serverChannel.accept(attachment, this);// 监听新的请求，递归调用。
            }
        }

        @Override
        public void failed(Throwable exc, AsynchronousServerSocketServer attachment) {
            try {
                exc.printStackTrace();
            } finally {
                attachment.serverChannel.accept(attachment, this);// 监听新的请求，递归调用。
            }
        }
    }

    /**
     * Read到请求数据的回调
     */
    private class ReadHandler implements CompletionHandler<Integer, AsynchronousSocketChannel> {

        private ByteBuffer buffer;

        public ReadHandler(ByteBuffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public void completed(Integer result, AsynchronousSocketChannel attachment) {
            try {
                if (result < 0) {
                    AsynchronousServerSocketServer.close(attachment);
                } else if (result == 0) {
                    System.out.println("空数据");
                } else {
                    buffer.flip();
                    CharBuffer charBuffer = AsynchronousServerSocketServer.decoder.decode(buffer);
                    System.out.println(charBuffer.toString()); //接收请求
                    //响应操作，服务器响应结果
                    buffer.clear();
                    String res = "HTTP/1.1 200 OK" + "\r\n\r\n" + "hellworld";
                    buffer = ByteBuffer.wrap(res.getBytes());
                    attachment.write(buffer, attachment, new WriteHandler(buffer));//Response：响应。
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
            exc.printStackTrace();
            AsynchronousServerSocketServer.close(attachment);
        }
    }

    /**
     * Write响应完请求的回调
     */
    private class WriteHandler implements CompletionHandler<Integer, AsynchronousSocketChannel> {
        private ByteBuffer buffer;

        public WriteHandler(ByteBuffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public void completed(Integer result, AsynchronousSocketChannel attachment) {
            buffer.clear();
            AsynchronousServerSocketServer.close(attachment);
        }

        @Override
        public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
            exc.printStackTrace();
            AsynchronousServerSocketServer.close(attachment);
        }
    }


}
