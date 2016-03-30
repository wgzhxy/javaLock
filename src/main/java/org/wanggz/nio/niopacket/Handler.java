package org.wanggz.nio.niopacket;

import com.sof.bas.Bytes2util;
import com.sof.bas.Util2Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

final public class Handler implements Runnable {

    static final int MESSAGE_LENGTH_HEAD = 4;
    private static Logger logger = LoggerFactory.getLogger(Handler.class);
    final SocketChannel socket;
    final SelectionKey sk;
    byte[] head = new byte[4];
    int bodylen = -1;

    Handler(Selector selector, SocketChannel socket) throws IOException {
        this.socket = socket;
        socket.configureBlocking(false);
        sk = socket.register(selector, 0);
        sk.attach(this);
        sk.interestOps(SelectionKey.OP_READ);
        selector.wakeup();
    }

    public void run() {
        try {
            read();
        } catch (IOException ex) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            logger.info("got a disconnect from " + socket.socket().toString());
            sk.cancel();
        }
    }

    public synchronized void read() throws IOException {
        ByteBuffer input = ByteBuffer.allocate(1024);
        socket.read(input);
        input.flip();

        //读取数据的原则: 要么读取一个完整的包头，要么读取一个完整包体。不满足这两种情况，不对ByteBuffer进行任何的get操作
        //但是要注意可能发生上次读取了一个完整的包头，下次读才读取一个完整包体情况。
        //所以包头部分必须用类的成员变量进行暂时的存储，当完整读取包头和包体后，在给业务处理部分。
        logger.debug("1: remain=" + input.remaining() + " bodylen=" + bodylen);
        while (input.remaining() > 0) {
            if (bodylen < 0) //还没有生成完整的包头部分, 该变量初始值为-1，并且在拼凑一个完整的消息包以后，再将该值设置为-1
            {
                if (input.remaining() >= MESSAGE_LENGTH_HEAD) //ByteBuffer缓冲区的字节数够拼凑一个包头
                {
                    input.get(head, 0, 4);
                    bodylen = Util2Bytes.bytes2bigint(head);
                    logger.debug("2: remain=" + input.remaining() + " bodylen=" + bodylen);
                } else//ByteBuffer缓冲区的字节数不够拼凑一个包头，什么操作都不做，退出这次处理，继续等待
                {
                    logger.debug("3: remain=" + input.remaining() + " bodylen=" + bodylen);
                    break;
                }
            } else if (bodylen > 0) //包头部分已经完整生成.
            {
                if (input.remaining() >= bodylen) //缓冲区的内容够一个包体部分
                {
                    byte[] body = new byte[bodylen];
                    input.get(body, 0, bodylen);
                    byte[] headandbody = new byte[MESSAGE_LENGTH_HEAD + bodylen];
                    System.arraycopy(head, 0, headandbody, 0, head.length);
                    System.arraycopy(body, 0, headandbody, head.length, body.length);
                    bodylen = -1;
                    logger.debug("4: remain=" + input.remaining() + " bodylen=" + bodylen);
                    Bytes2util.outputHex(headandbody, 16);
                } else  ///缓冲区的内容不够一个包体部分，继续等待，跳出循环等待下次再出发该函数
                {
                    System.out.println("5: remain=" + input.remaining() + " bodylen=" + bodylen);
                    break;
                }
            } else if (bodylen == 0) //没有包体部分，仅仅有包头的情况
            {
                byte[] headandbody = new byte[MESSAGE_LENGTH_HEAD + bodylen];
                System.arraycopy(head, 0, headandbody, 0, head.length);
                Bytes2util.outputHex(headandbody, 16);
                bodylen = -1;
            }
        }

        sk.interestOps(SelectionKey.OP_READ);
    }
}