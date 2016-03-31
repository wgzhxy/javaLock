package org.wanggz.lock.condition;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by guangzhong.wgz on 2016/3/31.
 */
public class LockConditionMain {

    private static ExecutorService pools = Executors.newCachedThreadPool();

    public static void main(String args[]) {
        //testMyAtomicInteger();
        System.out.println("============================");
        try {
            Thread.currentThread().join();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static void testBlockArray() {
        final MySecurityBlockArray<String> blockArray = new MySecurityBlockArray<String>();
        for (int i = 0; i < 10000; i++) {
            if (i % 2 == 0) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            System.out.println(blockArray.take());
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            } else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            blockArray.put("I am String, random = " + Math.random());
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
        pools.shutdown();
        try {
            Thread.currentThread().join(1000 * 10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("======================end====================");
    }

    private static void testMyAtomicInteger() {
        final MyAtomicInteger blockArray = new MyAtomicInteger();
        for (int i = 0; i < 10000; i++) {
            if (i % 2 == 0) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            System.out.println(blockArray.descreamAndGet());
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            } else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            System.out.println(blockArray.increamAndGet());
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
        pools.shutdown();
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("======================end====================");
    }
}
