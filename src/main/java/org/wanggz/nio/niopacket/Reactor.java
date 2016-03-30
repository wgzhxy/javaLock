package org.wanggz.nio.niopacket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

public class Reactor implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(Reactor.class);

    final Selector selector;
    final ServerSocketChannel serverSocket;

    public Reactor(String ip, int port) throws IOException {
        selector = Selector.open();
        serverSocket = ServerSocketChannel.open();
        serverSocket.socket().bind(new InetSocketAddress(port));
        serverSocket.configureBlocking(false);
        SelectionKey sk = serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        sk.attach(new Acceptor());
    }

    public void run() {
        try {
            while (!Thread.interrupted()) {
                logger.debug("selector is waitting  event....");
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                if (keys.size() == 0) {
                    logger.debug("nothing happened");
                    continue;
                }

                for (SelectionKey key : keys) {
                    if (key.isAcceptable()) {
                        logger.debug("Acceptable event happened");
                    } else if (key.isReadable()) {
                        logger.debug("Readable event happened");
                    } else if (key.isWritable()) {
                        logger.debug("Writeable event happened");
                    } else {
                        logger.debug("others event happened");
                    }
                    dispatch((SelectionKey) key);
                }
                keys.clear();
            }
        } catch (IOException ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace();
        }
    }

    void dispatch(SelectionKey k) {
        Runnable r = (Runnable) (k.attachment());
        if (r != null) {
            r.run();
        }
    }

    public class Acceptor implements Runnable {
        public synchronized void run() {
            try {
                SocketChannel c = serverSocket.accept();
                logger.info("got a new connection from:  " + c.socket().toString());
                if (c != null) {
                    new Handler(selector, c);
                }
            } catch (IOException ex) {
                logger.error(ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}