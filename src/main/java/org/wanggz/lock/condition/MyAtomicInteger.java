package org.wanggz.lock.condition;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by guangzhong.wgz on 2016/3/31.
 */
public class MyAtomicInteger {

    private volatile int innerValue = 0;

    private ReentrantLock lock = new ReentrantLock();

    public int increamAndGet() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            innerValue++;
            return innerValue;
        } finally {
            lock.unlock();
        }
    }

    public int descreamAndGet() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            innerValue--;
            return innerValue;
        } finally {
            lock.unlock();
        }
    }
}
