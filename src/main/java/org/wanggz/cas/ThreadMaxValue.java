package org.wanggz.cas;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by guangzhong.wgz on 2016/4/6.
 */
public class ThreadMaxValue {

    private static ExecutorService pools = Executors.newCachedThreadPool();
    private static AtomicInteger runTimes = new AtomicInteger();

    public static void main(String args[]) throws InterruptedException {

        MyFutureTask myTask = null;
        for (int i = 0; i < 10; i++) {
            int time = new Double(Math.random() * 1000000).intValue();
            myTask = new MyFutureTask(time);
            pools.submit(myTask.getTask());
            System.out.println(time);
        }
        pools.shutdown();
        Thread.sleep(8000);
        System.out.print(runTimes.get());
    }

    public static class MyFutureTask {

        int total = 0;
        FutureTask<Void> task = null;

        public MyFutureTask(final int times) {
            task = new FutureTask<Void>(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    int i = 0;
                    for (; ; ) {
                        i++;
                        if (i > times) {
                            break;
                        }
                        setMax(++total);
                    }
                    return null;
                }
            });
        }

        public FutureTask<Void> getTask() {
            return task;
        }

        private void setMax(int maxValue) {
            for (; ; ) {
                int currentValue = runTimes.get();
                if (maxValue <= currentValue) {
                    return;
                }
                if (runTimes.compareAndSet(currentValue, maxValue)) {
                    break;
                }
            }
        }
    }

}
