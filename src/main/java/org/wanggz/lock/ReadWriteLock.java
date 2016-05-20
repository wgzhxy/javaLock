package org.wanggz.lock;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by wanggz on 16/3/30.
 */
public class ReadWriteLock {

    private ReentrantLock reentrantLock = new ReentrantLock();

    private Condition noFull = reentrantLock.newCondition();

    private Condition notEmpty = reentrantLock.newCondition();

    private Object[] queen = new Object[20];

    private int totalSize = 0;


    public Object take() throws InterruptedException {
        Object o = null;
        try {
            reentrantLock.lock();
            if (totalSize <= 0) {
                notEmpty.await();
            }
            o = queen[--totalSize];
            queen[totalSize] = null;
            noFull.signal();
            return o;

        } finally {
            reentrantLock.unlock();
        }
    }

    public void push(Object o) throws InterruptedException {

        try {
            reentrantLock.lock();
            if (totalSize >= 20) {
                noFull.await();
            }
            queen[totalSize++] = o;
            notEmpty.signal();
        } finally {
            reentrantLock.unlock();
        }
    }


    public static void main(String[] args) {
        final ReadWriteLock obj = new ReadWriteLock();
        try {
                for(int i=0; i<500; i++) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                System.out.println(obj.take());
                            } catch (Throwable e) {

                            }
                        }
                    }).start();
                }
                /*try {
                    Thread.sleep(1000 * 2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                for(int i=0; i<1000; i++) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                obj.push("String is good");
                            }catch (Throwable e) {
                        }
                    }
                }).start();
                }
            //ReentrantLock
        } catch (Throwable e){
             e.printStackTrace();
        }
    }
}
