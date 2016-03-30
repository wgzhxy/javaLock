package org.wanggz.nio.nioexam;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class SearchConnectionExecutor {

    protected static final Log logger = LogFactory.getLog(SearchConnectionExecutor.class);
    private static final int BUFFER_SIZE = 9216;
    private SelectorFactory selectorFactory;
    private SocketChannelFactory socketChannelFactory;
    private ConnectionConfig connectionConfig;

    //    private int                  selectKeyWaitTimes = 100;                                          // 一次selector.select()所等待的最长时间

    private SearchConnectionExecutor() {
        this.selectorFactory = SelectorFactory.getInstance();
        this.socketChannelFactory = SocketChannelFactory.getInstance();
        this.connectionConfig = ConnectionConfig.getInstance();
    }

    public static SearchConnectionExecutor getInstance() {
        return SingletonHolder.EXECUTOR;
    }

    protected Selector getSelector() throws IOException {
        Selector selector = selectorFactory.getSelector();
        return selector;
    }

    protected void releaseSelector(Selector selector) {
        selectorFactory.returnSelector(selector);
    }

    protected SocketChannel getSocketChannel(String host, int port) {
        try {
            return socketChannelFactory.getSocketChannel(host, port);
        } catch (IOException e) {
            logger.error("get socketChannel  error:" + e.getMessage() + ",the host:" + host
                    + ",port:" + port);
        }
        return null;

    }

    protected void releaseSocketChannel(SocketChannel socketChannel, boolean forceClose,
                                        String host, int port) {
        socketChannelFactory.releaseSocketChannel(socketChannel, forceClose, host, port);
    }

    protected SocketChannel closeAndCreateChannel(SocketChannel socketChannel, String host, int port) {
        try {
            return socketChannelFactory.closeAndCreateChannel(socketChannel, host, port);
        } catch (Exception e) {
            logger.error("change socketChannel  error:" + e.getMessage() + ",the host:" + host
                    + ",port:" + port);
            return null;
        }
    }

    /**
     * 单请求执行
     *
     * @param request
     * @return HttpResponse ,如果出错返回null
     */
    public SearchResponse execute(SearchRequest request) {

        SearchResponse[] responses = execute(new SearchRequest[]{request});
        if (responses == null || responses.length == 0) {
            return null;
        } else {
            return responses[0];
        }

    }

    /**
     * 多请求并发
     *
     * @param requests ,数组中元素可以为null(对应的请求执行出错时)
     * @return 数组中元素可以为null(对应的请求执行出错时)
     */
    public SearchResponse[] execute(SearchRequest[] requests) {

        SearchResponse[] responses = finalExecute(requests);
        return responses;

    }

    /**
     * 多个查询同时进行的最终方法
     *
     * @param requests
     * @return
     */
    private SearchResponse[] finalExecute(SearchRequest[] requests) {

        // 检查请求是否有效
        if (requests == null || requests.length <= 0) {
            throw new NullPointerException("the requests is null!");
        }

        for (int i = 0; i < requests.length; i++) {
            if (requests[i] != null && !requests[i].isValid()) {
                throw new IllegalArgumentException("the requests:" + requests[i] + "is illegal!");
            }

        }

        if (logger.isInfoEnabled()) {
            StringBuilder log = new StringBuilder(512);
            for (int i = 0; i < requests.length; i++) {
                log.append("\nthe request[").append(i).append("]:\n").append(requests[i]);
            }
            logger.info(log);
        }

        ByteBuffer writeByteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        ByteBuffer readByteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        SearchResponse[] responses = new SearchResponse[requests.length];
        Selector selector = null;
        SocketChannel[] socketChannels = new SocketChannel[requests.length];

        try {
            selector = getSelector();

            if (selector == null) {
                throw new RuntimeException("can't get usable  selector,time out:"
                        + connectionConfig.getWaitSelectorFromPoolTime() + " ms");
            }
            // 有效请求数
            int validRequets = sendRequest(requests, writeByteBuffer, responses, selector,
                    socketChannels);

            int selectTimeout = connectionConfig.getSelectTimeout();
            int allProcessMaxTime = connectionConfig.getAllProcessMaxTime();
            //selector在实现时有bug,epool底层可能会发送一过错误的信号导致select方法提前返回，但没有
            //返回注册的事件，而且不断循环造成CPU100％
            int slelectZeroCount = 0;
            int maxZeroCount = 20;
            int fixed = 0;

            while (selector.isOpen() && selector.keys().size() != 0 && allProcessMaxTime > 0
                    && validRequets > 0) {
                long start = System.currentTimeMillis();
                // 查询看是否有已经准备好的通道,指定超时时间
                int count = selector.select(selectTimeout);

                if (count == 0) {
                    slelectZeroCount++;
                } else {
                    slelectZeroCount = 0;
                    //保证是连续的count==0时才将slelectZeroCount++,如果其中有一次返回注册事件测已经正常
                }
                if (slelectZeroCount > maxZeroCount && fixed == 0) {
                    //没有尝试修复动作，则修复
                    for (SelectionKey key : selector.keys()) {
                        if (key.isValid() && key.interestOps() == 0) {
                            key.cancel();
                        }
                    }
                    fixed = 1;
                } else if (slelectZeroCount > maxZeroCount && fixed == 1) {
                    //如果已经干预过仍然连续返回0，注意如果不返回0的话slelectZeroCount就被置0.
                    //重新获取一个selector,将当前事件重新注册到新的selector上。并销毁当前selector
                    Selector newSelector = this.getSelector();
                    this.changeSelector(selector, newSelector);
                    selector = newSelector;
                }

                //每次select后就有可能超时，所以要检查
                validRequets = checkTimeout(requests, responses, selector, socketChannels, validRequets);

                Iterator<SelectionKey> it = selector.selectedKeys().iterator();

                while (it.hasNext()) {
                    SelectionKey selectionKey = (SelectionKey) it.next();
                    it.remove();
                    validRequets = handleSelectionKey(requests, readByteBuffer, responses,
                            socketChannels, validRequets, selectionKey);
                }

                long end = System.currentTimeMillis();
                // 计算剩余时间
                allProcessMaxTime = allProcessMaxTime - (int) (end - start);
            }

            if (logger.isWarnEnabled()) {
                logErrorResponse(requests, responses);
            }

        } catch (IOException e) {
            StringBuilder log = new StringBuilder(512);
            for (int i = 0; i < requests.length; i++) {
                log.append("the error http request[").append(i).append("]:\n").append(requests[i]);
            }
            logger.error(log.append("\ncause by :").append(e.getMessage()));
            throw new RuntimeException(e);
        } finally {
            if (selector != null) {
                releaseSelector(selector);// 这个先执行
                for (int i = 0; i < socketChannels.length; i++) {
                    if (socketChannels[i] != null) {
                        releaseSocketChannel(socketChannels[i], responses[i].isForceClose(),
                                requests[i].getHost(), requests[i].getPort());
                    }
                }

            }
        }
        if (logger.isInfoEnabled()) {
            StringBuilder log = new StringBuilder(512);
            for (int i = 0; i < responses.length; i++) {
                if (responses[i] != null) {
                    log.append("\nthe http responses[").append(i).append("]:\n").append(
                            responses[i].getHeaders());
                }
            }
            logger.info(log);
        }
        return responses;
    }

    /**
     * 发送http请求
     *
     * @param requests
     * @param byteBuffer
     * @param responses
     * @param selector
     * @param socketChannels
     * @return
     */
    private int sendRequest(SearchRequest[] requests, ByteBuffer byteBuffer,
                            SearchResponse[] responses, Selector selector,
                            SocketChannel[] socketChannels) {
        int validRequets = 0;
        for (int i = 0; i < requests.length; i++) {
            if (requests[i] != null) {
                SocketChannel socketChannel = getSocketChannel(requests[i].getHost(), requests[i].getPort());

                if (socketChannel != null && socketChannel.isOpen() && socketChannel.isConnected()) {
                    try {
                        // 做一个读的测试，看socketchannel是否还可用
                        // If the remote host properly closed the connection,
                        // read() will return -1.
                        // If the connection was not terminated normally, read()
                        // and write() will throw an exception.
                        byteBuffer.clear();
                        int readNumber = socketChannel.read(byteBuffer);
                        if (readNumber == -1) {
                            if (logger.isInfoEnabled()) {
                                logger.info("the socketchannel:" + socketChannel
                                        + " properly  was closed by the remote host!");
                            }
                            socketChannel = closeAndCreateChannel(socketChannel, requests[i]
                                    .getHost(), requests[i].getPort());
                        }
                    } catch (IOException e) {
                        if (logger.isInfoEnabled()) {
                            logger.info("the socketchannel:" + socketChannel
                                    + " was not terminated normally!");
                        }
                        socketChannel = closeAndCreateChannel(socketChannel, requests[i].getHost(),
                                requests[i].getPort());

                    }
                    if (socketChannel == null) {
                        break;
                    }
                    try {
                        // 设置开始处理的时间
                        long start = System.currentTimeMillis();
                        if (requests[i].getLastAccess() == -1L) {
                            requests[i].setLastAccess(start);
                        }
                        //2010-10-12 add:
                        byte[] data = requests[i].toBytes();
                        if (data.length > byteBuffer.capacity()) {
                            //如果当前数据长度超过byteBuffer的最大值，则重新生成一个临时的byteBuffer.
                            //注意这个临时的byteBuffer会被使用到本次循环结束，这种机率极少发生
                            byteBuffer = ByteBuffer.allocate(data.length);
                            logger.error("too long url:" + requests[i].getPathInfo());
                        }
                        //end add
                        byteBuffer.clear();
                        byteBuffer.put(data);
                        byteBuffer.flip();

                        // socketChannel.write(byteBuffer);

                        // 2010-04-21 axman.wangjh modifies.
                        // 此处不能保证数据完全写出，因为socketChannel.write是异步的，在还没有完全写出的时候，
                        // byteBuffer有可能被循环的下一次覆盖。
                        flushData(selector, socketChannel, byteBuffer);

                        // 将本次处理的时间和最大处处理时设置给key以便控制超时

                        socketChannel.register(selector, SelectionKey.OP_READ, requests[i]);
                        // key.attach(requests[i]);

                        socketChannels[i] = socketChannel;//此句应该提前
                        responses[i] = new SearchResponse();
                        validRequets++;
                    } catch (IOException e) {
                        logger.error("socketChannel register error:" + e.getMessage()
                                + ",the request:" + requests[i], e);
                        // 出错关闭socketChannel
                        releaseSocketChannel(socketChannel, true, requests[i].getHost(),
                                requests[i].getPort());
                    }
                } else {
                    logger.warn("can't get usable socketChannel for request:" + requests[i]);
                }
            }
        }
        return validRequets;
    }

    /**
     * @param socketChannel
     * @param byteBuffer
     * @throws IOException
     * @author axman.wangjh add at 2010-04-21
     */
    private void flushData(Selector selector, SocketChannel socketChannel, ByteBuffer byteBuffer)
            throws IOException {

        int count = 0;
        int maxCount = 20;

        while (byteBuffer.hasRemaining()) {
            int len = socketChannel.write(byteBuffer);
            if (len < 0) {
                throw new EOFException("write channel is closed.");
            }
            // 如果不对len==0(即当前网络不可用)的情况处理，则while(byteBuffer.hasRemaining())可能一直
            // 循环下去而消耗大量的CPU.
            if (len == 0) {
                count++;
            } else {
                count = 0;
            }
            if (count > maxCount) {
                throw new IOException("can't connect to target.");
            }
        }

    }

    /**
     * 处理http响应数据
     *
     * @param requests
     * @param byteBuffer
     * @param responses
     * @param socketChannels
     * @param validRequets
     * @param selectionKey
     * @return
     */
    private int handleSelectionKey(SearchRequest[] requests, ByteBuffer byteBuffer,
                                   SearchResponse[] responses, SocketChannel[] socketChannels,
                                   int validRequets, SelectionKey selectionKey) {
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        for (int i = 0; i < socketChannels.length; i++) {
            if (socketChannels[i] == channel) {
                try {
                    handleIO(selectionKey, byteBuffer, channel, requests[i], responses[i]);
                } catch (IOException e) {// 读数据出错时
                    logger.error("socketChannel read error:" + e.getMessage() + ",the request:"
                            + requests[i]);
                    selectionKey.cancel();
                    // 强行关闭socketChannel
                    releaseSocketChannel(channel, true, requests[i].getHost(), requests[i]
                            .getPort());
                    validRequets--;
                    continue;
                }
                if (responses[i].isCompleted()) {// 当前请求已经结束
                    validRequets--;
                    selectionKey.cancel();
                }
            }
        }
        return validRequets;
    }

    private void logErrorResponse(SearchRequest[] requests, SearchResponse[] responses) {
        StringBuilder log = new StringBuilder(256);
        for (int i = 0; i < responses.length; i++) {
            if (responses[i] == null) {
                log.append("the http request[").append(i).append("]<").append(requests[i]);
                log.append(">'s response is null.\n");
            }
            if (responses[i] != null && !responses[i].isCompleted()) {
                log.append("the http request[").append(i).append("]<").append(requests[i]);

                log.append("> is timeout,the default http connection response timeout:");
                log.append(connectionConfig.getSocketTimeout());

                String socketCfgKey = requests[i].getHost() + ":" + requests[i].getPort();
                Integer v = connectionConfig.getSocketTimeoutByHost().get(socketCfgKey);
                if (v != null) {
                    log.append("the host's connection timeout:" + v);
                    log.append("the request's read timeout:" + requests[i].getMaxRequestTime());
                }
                log.append(" milliseconds. \n");
            }
        }
        if (log.length() > 0) {
            logger.warn(log);
        }
    }

    private void handleIO(SelectionKey selectionKey, ByteBuffer byteBuffer, SocketChannel channel,
                          SearchRequest request, SearchResponse response) throws IOException {

        if (selectionKey.isReadable()) {
            int count = 0;
            byteBuffer.clear();
            while ((count = channel.read(byteBuffer)) > 0) {
                byteBuffer.flip();
                byte bytes[] = new byte[byteBuffer.limit()];
                byteBuffer.get(bytes);
                response.appendBytes(bytes);
                byteBuffer.clear();
            }
            // count == 0 or -1
            if (count == -1) {
                response.setCompleted(true);// 设置已经结束，
            }

            // if (channel.read(byteBuffer) == -1) {// 已经读到了尽头
            // response.setCompleted(true);// 设置已经结束，
            // return;
            // }
            // byteBuffer.flip();
            // if (byteBuffer.limit() == 0) {// 没读出内容来
            // return;
            // }
            //
            // byte bytes[] = new byte[byteBuffer.limit()];
            // byteBuffer.get(bytes);
            // response.appendBytes(bytes);

        }

    }

    private void changeSelector(Selector oldSelector, Selector newSelector) {
        for (SelectionKey key : oldSelector.keys()) {
            if (!key.isValid() || key.interestOps() == 0) {
                continue;
            }
            Object att = key.attachment();
            try {
                if (att == null) {
                    key.channel().register(newSelector, key.interestOps());
                } else {
                    key.channel().register(newSelector, key.interestOps(), att);
                }
            } catch (ClosedChannelException e) {
                SocketChannel sc = (SocketChannel) key.channel();
                if (att == null) {
                    String host = sc.socket().getInetAddress().getHostName();
                    int port = sc.socket().getPort();
                    releaseSocketChannel(sc, true, host, port);
                    logger
                            .error("con't get attachment request...... releaseSocketChannel will fail.");
                    return;
                }
                SearchRequest sr = (SearchRequest) att;
                releaseSocketChannel(sc, true, sr.getHost(), sr.getPort());
            }
        }
        try {
            oldSelector.close();
            this.releaseSelector(oldSelector);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

    }

    private int checkTimeout(SearchRequest[] requests, SearchResponse[] responses,
                             Selector selector, SocketChannel[] socketChannels, int validRequets) {
        for (int i = 0; i < requests.length; i++) {
            SearchRequest request = requests[i];
            if (request == null) {
                continue;
            }
            if (socketChannels[i] == null) {
                continue;
            }
            if (responses[i] == null) {
                continue;
            }
            if (!responses[i].isCompleted()
                    && request.getLastAccess() != -1L
                    && ((System.currentTimeMillis() - request.getLastAccess()) > request
                    .getMaxRequestTime())) {

                //对应的连接已经超时，key先取消
                socketChannels[i].keyFor(selector).cancel();
                //然后归还channel.
                releaseSocketChannel(socketChannels[i], true, request.getHost(), request.getPort());
                logger.error("process request timeout:" + request.getMaxRequestTime()
                        + " ms,the request is " + request.getPathInfo() + ".");
                responses[i].setCompleted(true);
                socketChannels[i] = null;
                requests[i] = null;
                validRequets--;
            }
        }
        try {
            selector.selectNow();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return validRequets;
    }

    public void destory() {
        if (selectorFactory != null) {
            selectorFactory.destroy();
        }
        if (socketChannelFactory != null) {
            socketChannelFactory.destory();
        }

    }

    private static class SingletonHolder {

        static SearchConnectionExecutor EXECUTOR = new SearchConnectionExecutor();
    }


}
