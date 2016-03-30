package org.wanggz.nio.nioexam;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class SelectorFactory {

    private static final Log logger = LogFactory.getLog(SelectorFactory.class);

    private BlockingQueue<Selector> selectors;
    // selector的最大限制数
    private int maxSelectors;
    // 获取可用selector的超时时间,单位为毫秒
    private int waitSelectorTimeout;
    private ConnectionConfig connectionConfig;
    private boolean closed;

    protected SelectorFactory() {
        this.connectionConfig = ConnectionConfig.getInstance();
        this.maxSelectors = connectionConfig.getMaxSelectors();
        this.waitSelectorTimeout = connectionConfig.getWaitSelectorFromPoolTime();
        init();
    }

    public static SelectorFactory getInstance() {
        return SingletonHolder.FACTORY;
    }

    private void init() {
        this.selectors = new ArrayBlockingQueue<Selector>(this.maxSelectors);
        try {
            for (int i = 0; i < this.maxSelectors; i++) {
                Selector selector = Selector.open();
                this.selectors.add(selector);
            }
        } catch (Exception e) {
            throw new RuntimeException(" can't create SelectorFactory ", e);
        }
    }

    public Selector getSelector() {
        if (closed) {
            throw new RuntimeException("the selectorFactory is closed!");
        }
        try {

            if (this.waitSelectorTimeout >= 0) {
                return this.selectors.poll(this.waitSelectorTimeout, TimeUnit.MILLISECONDS);
            } else {
                return this.selectors.take();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(" can't get selector ", e);
        }
    }

    public void returnSelector(Selector selector) {

        try {
            if (closed) {
                if (selector.isOpen()) {
                    selector.close();
                }
                return;
            }
            //从新版修复selector事件错误后，有可能传进来的是已经关闭的selector
            //20100916 add by jinhua.wangjh
            if (!selector.isOpen()) {
                //对于已经关闭的selector要重建
                selector = Selector.open();
            }

            if (selector.keys().size() > 0) {// 如果还有selectionKey,则取消
                Iterator<SelectionKey> it = selector.keys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    key.cancel();
                }
                selector.selectNow();
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            try {// 把出错的selector关闭
                selector.close();
                selector = Selector.open();
            } catch (IOException ioe) {
                logger.error(ioe.getMessage());
            }
        }
        try {
            if (!this.selectors.contains(selector)) {
                // 防止同一线程多次调用returnSelector，重复返回

                if (this.waitSelectorTimeout >= 0) {
                    boolean success = this.selectors.offer(selector, this.waitSelectorTimeout,
                            TimeUnit.MILLISECONDS);
                    if (!success) {
                        logger.error("offer selector to selectors is timeout,the selectors.size():"
                                + this.selectors.size());
                    }
                } else {
                    this.selectors.put(selector);
                }
            }
        } catch (InterruptedException e) {
            logger.error("the thread is interrupted:" + e.getMessage());
            try {
                selector.close();
            } catch (IOException ioe) {
            }
        }

    }

    public void destroy() {
        this.closed = true;
        while (!selectors.isEmpty()) {
            Selector selector = selectors.poll();
            try {
                if (selector != null) {
                    selector.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }

    }

    private static class SingletonHolder {

        static SelectorFactory FACTORY = new SelectorFactory();
    }

}
