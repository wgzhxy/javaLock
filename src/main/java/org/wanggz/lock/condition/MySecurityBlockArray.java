package org.wanggz.lock.condition;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by guangzhong.wgz on 2016/3/31.
 */
public class MySecurityBlockArray<T extends Serializable> {
    //数据大小
    private final int MAX_LENGTH = 5;
    //阻塞数组
    private Serializable[] array = new Serializable[MAX_LENGTH];
    //重入锁
    private ReentrantLock trantLock = new ReentrantLock();
    //数组不为空
    private Condition notEmpty = trantLock.newCondition();
    //数据未满
    private Condition notFull = trantLock.newCondition();
    //当前数组大小
    private int currentCount = 0;
    //
    private int putIndex = 0;
    private int getIndex = 0;

    private AtomicInteger tag = new AtomicInteger(0);

    public T take() throws InterruptedException {
        ReentrantLock trantLock = this.trantLock;
        trantLock.lockInterruptibly();
        try {
            while (currentCount == 0) {
                notEmpty.await();
            }

            T obj = (T) array[getIndex];
            array[getIndex] = null;

            if (++getIndex == MAX_LENGTH) getIndex = 0;
            currentCount--;

            notFull.signal();
            return obj;
        } finally {
            trantLock.unlock();
        }
    }

    public void put(T obj) throws InterruptedException {
        ReentrantLock trantLock = this.trantLock;
        trantLock.lockInterruptibly();
        try {
            while (currentCount == MAX_LENGTH) {
                notFull.await();
            }
            array[putIndex] = "currentCount: " + tag.incrementAndGet() + ", " + obj;
            currentCount++;

            if (++putIndex == MAX_LENGTH) putIndex = 0;
            notEmpty.signal();
        } finally {
            trantLock.unlock();
        }
    }


}
