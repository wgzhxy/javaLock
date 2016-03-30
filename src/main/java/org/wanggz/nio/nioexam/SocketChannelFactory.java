package org.wanggz.nio.nioexam;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SocketChannelFactory {

    private ConcurrentMap<String, SocketChannelPool> hostPoolMap = new ConcurrentHashMap<String, SocketChannelPool>(40);
    // SocketChannel连接池是否已经关闭
    private boolean closed;

    protected SocketChannelFactory() {
    }

    public static SocketChannelFactory getInstance() {
        return SingletonHolder.FACTORY;
    }

    /**
     * @param host
     * @param port
     * @return
     * @throws IOException
     * @throws IOException
     */
    public SocketChannel getSocketChannel(String host, int port) throws IOException {
        if (closed) {
            throw new RuntimeException("the SocketChannelFactory is closed");
        }
        SocketChannelPool pool = getChannelPool(host, port);
        return pool.getChannel();
    }

    /**
     * @param socketChannel
     * @param forceClose    强行关闭
     * @param host
     * @param port
     */
    public void releaseSocketChannel(SocketChannel socketChannel, boolean forceClose, String host, int port) {
        if (closed) {
            try {
                socketChannel.close();
            } catch (IOException e) {
            }
            return;
        }
        if (socketChannel != null) {
            SocketChannelPool pool = getChannelPool(host, port);
            pool.releaseChannel(socketChannel, forceClose);
        }
    }

    public SocketChannel closeAndCreateChannel(SocketChannel socketChannel, String host, int port) throws IOException {
        if (socketChannel == null) {
            return null;
        }
        if (closed) {
            try {
                socketChannel.close();
            } catch (IOException e) {
            }
            throw new RuntimeException("the SocketChannelFactory is closed");
        }
        SocketChannelPool pool = getChannelPool(host, port);
        return pool.closeAndCreateChannel(socketChannel);
    }

    private SocketChannelPool getChannelPool(String host, int port) {

        StringBuilder address = new StringBuilder(host).append(":").append(port);
        SocketChannelPool pool = hostPoolMap.get(address.toString());
        if (pool == null) {
            pool = new SocketChannelPool(new InetSocketAddress(host, port));

            pool.setHostPortKey(address.toString());
            SocketChannelPool temp = hostPoolMap.putIfAbsent(address.toString(), pool);
            if (temp != null) {// 不为空，已经有其他线程已经增加过了,则返回原来那个
                pool = temp;
            }
        }
        return pool;
    }

    public void destory() {
        closed = true;
        Set<Entry<String, SocketChannelPool>> poolSet = hostPoolMap.entrySet();
        for (Entry<String, SocketChannelPool> entry : poolSet) {
            SocketChannelPool pool = entry.getValue();
            poolSet.remove(entry);
            pool.destory();
        }
    }

    private static class SingletonHolder {

        static SocketChannelFactory FACTORY = new SocketChannelFactory();
    }

}
