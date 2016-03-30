package org.wanggz.nio.nioexam;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SocketChannelPool {

    private static Log logger = LogFactory.getLog(SocketChannelPool.class);
    private final Lock lock = new ReentrantLock();
    private final Condition emptyCondition = lock.newCondition();
    private Queue<SocketChannel> channelQueue = new LinkedList<SocketChannel>();
    // 当前已打开的连接总数
    private int channelsTotal;
    private SocketAddress address;
    private int maxChannels;
    private int waitSocketFromPoolTime;
    private int socketTimeout;
    private int soTimeout;
    private Map<String, Integer> socketTimeoutByHost;
    private Map<String, Integer> waitSocketFromPoolTimeByHost;
    private long lastRequestTime = 0L;

    //缓存下面的多次根据 address获取 host+port的计算
    private String hostPortKey;
    // 是否连接池已经关闭
    private boolean closed;

    protected SocketChannelPool(SocketAddress address) {
        ConnectionConfig connectionConfig = ConnectionConfig.getInstance();
        this.address = address;
        this.maxChannels = connectionConfig.getHostMaxSockets();
        this.soTimeout = connectionConfig.getSoTimeout();
        this.socketTimeout = connectionConfig.getSocketTimeout();
        this.waitSocketFromPoolTime = connectionConfig.getWaitSocketFromPoolTime();
        this.socketTimeoutByHost = connectionConfig.getSocketTimeoutByHost();
        this.waitSocketFromPoolTimeByHost = connectionConfig.getWaitSocketFromPoolTimeByHost();
    }

    public void setHostPortKey(String hostPortKey) {
        this.hostPortKey = hostPortKey;
    }

    public SocketChannel getChannel() throws IOException {
        if (closed) {
            throw new RuntimeException("the socketchannel[address:" + address + "] pool is closed!");
        }
        boolean createNew = false;

        // 引擎目前会有1分钟自动强制断开的问题，socketChannel无法感知，
        // 故记录每次请求的时间，如果距离上次请求超过1分钟就全部release
        long nowRequestTime = System.currentTimeMillis();
        lock.lock();
        try {
            if (nowRequestTime - lastRequestTime > 60 * 1000) {
                if (channelsTotal > 0) {
                    while (!channelQueue.isEmpty()) {
                        releaseChannel(channelQueue.poll(), true);
                    }
                }
            }
            lastRequestTime = nowRequestTime;
            if ((channelsTotal >= maxChannels) && channelQueue.isEmpty()) {
                // socketchannel使用已达上限且队列为空
                if (logger.isInfoEnabled()) {
                    logger.info("socketchannel pool is empty and channelCount is upper limit!");
                }

                // 如果已经配置了当前host+":"+port的waitSocketFromPoolTime则使用该值
                // 可以精确控制每个host+port等待socket的时间
                Integer waittimeout = this.waitSocketFromPoolTimeByHost.get(hostPortKey);
                waitSocketFromPoolTime = (waittimeout != null) ? waittimeout : waitSocketFromPoolTime;

                long nanosTimeout = TimeUnit.MILLISECONDS.toNanos(waitSocketFromPoolTime);
                while ((channelsTotal >= maxChannels) && channelQueue.isEmpty()) {// 等待可用的socketchannel或超时
                    if (nanosTimeout > 0) {
                        nanosTimeout = emptyCondition.awaitNanos(nanosTimeout);
                    } else {
                        logger.error("get socketchannel is timeout,the timeout:" + waitSocketFromPoolTime + "ms");
                        return null;
                    }
                }
                if (!channelQueue.isEmpty()) {// 队列中有可用的
                    // 判断池中已经存在的连接是否可用，有可能句柄原来可用，但还回来后对方断开使连接失效。
                    // 此处只是替换了原来的连接，对池的计数没有影响
                    SocketChannel ch = channelQueue.poll();
                    if (!ch.isOpen() || !ch.isConnected()) {
                        ch = closeAndCreateChannel(ch);
                    }
                    return ch;
                } else {// 没达到上限
                    channelsTotal++;
                    createNew = true;
                }
            } else {
                if (!channelQueue.isEmpty()) {
                    SocketChannel ch = channelQueue.poll();
                    if (!ch.isOpen() || !ch.isConnected()) {
                        ch = closeAndCreateChannel(ch);
                    }
                    return ch;
                } else {
                    channelsTotal++;
                    createNew = true;
                }
            }
        } catch (InterruptedException e) {
            logger.error("current thread is Interrupted,the error message:" + e.getMessage());
            return null;
        } finally {
            lock.unlock();
        }
        if (createNew) {
            return createChannel();
        }
        return null;
    }

    private SocketChannel createChannel() throws IOException {
        if (logger.isInfoEnabled()) {
            logger.info("create new socketChannel for address:" + address);
        }
        try {
            SocketChannel newChannel = SocketChannel.open();
            //newChannel.socket().setReuseAddress(true);
            //newChannel.socket().setTcpNoDelay(true);
            newChannel.socket().setSendBufferSize(2048);
            newChannel.socket().setReceiveBufferSize(16348);
            //这个参数没有测试，可能有影响 
            newChannel.socket().setSoTimeout(soTimeout);

            Integer hostScTimeout = socketTimeoutByHost.get(hostPortKey);
            int time = (hostScTimeout != null) ? hostScTimeout : socketTimeout;

            newChannel.socket().connect(address, time);
            newChannel.configureBlocking(false);
            return newChannel;
        } catch (IOException e) {
            logger.error("create new socketchannel error:" + e.getMessage());
            lock.lock();
            try {
                channelsTotal--;
            } finally {
                lock.unlock();
            }
            throw e;
        }
    }

    /**
     * 创建一个新连接来代替原来的
     *
     * @param channel
     * @return
     * @throws IOException
     */
    public SocketChannel closeAndCreateChannel(SocketChannel channel) throws IOException {
        try {
            channel.close();
        } catch (IOException e) {
        }
        return createChannel();
    }

    public void releaseChannel(SocketChannel channel, boolean forceClose) {
        if (channel == null) {
            return;
        }
        if (channel.isOpen() && channel.isConnected()) {
            if (forceClose || closed) {
                if (logger.isInfoEnabled()) {
                    logger.info(" force close connection:" + channel);
                }
                try {
                    channel.close();
                } catch (IOException e) {
                }
                lock.lock();
                try {
                    if (channelsTotal > 0) {
                        channelsTotal--;
                    }
                } finally {
                    lock.unlock();
                }
            } else {
                lock.lock();
                try {
                    if (!channelQueue.contains(channel)) {
                        boolean success = channelQueue.offer(channel);
                        if (success) {
                            emptyCondition.signal();
                        }
                    }
                } finally {
                    lock.unlock();
                }

            }
        }

    }

    public void destory() {
        lock.lock();
        try {
            for (SocketChannel channel : channelQueue) {
                try {
                    channel.close();
                } catch (IOException e) {
                }
            }
            channelsTotal = 0;
            closed = true;
        } finally {
            lock.unlock();
        }
    }

}
