package org.wanggz.thread;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by guangzhong.wgz on 2016/3/4.
 */
public class WaitNodityMain {

    private final static AtomicInteger threadCount = new AtomicInteger(0);

    private static Executor pools = Executors.newScheduledThreadPool(5, new ThreadFactory() {
        public Thread newThread(Runnable r) {

            return new Thread(r, "my-poll-thread " + threadCount.incrementAndGet());
        }
    });

    public static void main(String args[]) {

        final Object lock = new Object();

        pools.execute(new Runnable() {
            public void run() {
                //while (true) {
                synchronized (lock) {
                    try {
                        //System.out.println(this);
                        System.out.println("===============wait==================");
                        Thread.currentThread().sleep(200);
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //}
            }
        });

        pools.execute(new Runnable() {
            public void run() {
                //while (true) {
                //System.out.println(this);
                synchronized (lock) {
                    System.out.println("===============nofify==================");
                    try {
                        Thread.currentThread().sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    lock.notify();
                }
                //}
            }
        });

        for (int i = 0; i < 100; i++) {
            pools.execute(new Runnable() {
                public void run() {
                    System.out.println(Thread.currentThread().getName() + " : " + Thread.currentThread().getId());
                }
            });
        }
        System.out.println("pool 执行 总线程数" + threadCount.get());
        try {
            Thread.currentThread().join();
            System.out.println("==========Main函数结束==========");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}