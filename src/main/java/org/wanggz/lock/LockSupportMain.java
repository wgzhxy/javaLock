package org.wanggz.lock;

import java.util.concurrent.locks.LockSupport;

/**
 * Created by guangzhong.wgz on 2016/3/10.
 */
public class LockSupportMain {

    public static void main(String args[]) {

        parkUtil();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                System.out.println("jvm stoping ...............");
            }
        }));
    }

    public static void parkUtil() {

        final Thread t1 = new Thread(new Runnable() {
            public void run() {
                LockSupport.parkUntil(new Object(), 1);
                System.out.println("parkUtil ending ....................");
                System.out.println(LockSupport.getBlocker(Thread.currentThread()));
            }
        });
        t1.start();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    public static void parkInterrupt() {

        final Thread t1 = new Thread(new Runnable() {
            public void run() {
                LockSupport.park();
                System.out.println("park ending ....................");
            }
        });

        final Thread t2 = new Thread(new Runnable() {
            public void run() {
                try {

                    Thread.sleep(5000);
                    t1.interrupt();
                    System.out.println("interrupting ....................");

                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });

        t1.start();
        t2.start();
    }
}
