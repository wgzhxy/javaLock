package javaLock;

import org.junit.Test;
import org.wanggz.lock.AtomicIntegerWithLock;

/**
 * TODO Comment of LockPerformTest
 * 
 * @author guangzhong.wgz
 */
public class LockPerformTest {

    static int staticValue = 0;

    @Test
    public void test() throws InterruptedException {
        // Lock锁性能测试
        final int max = 10;
        final int loopCount = 100000;
        
        long costTime = 0;
        for (int m = 0; m < max; m++) {
            long start1 = System.currentTimeMillis();
            final AtomicIntegerWithLock value1 = new AtomicIntegerWithLock(0);
            Thread[] ts = new Thread[max];
            for (int i = 0; i < max; i++) {
                ts[i] = new Thread() {
                    public void run() {
                        for (int i = 0; i < loopCount; i++) {
                            value1.incrementAndGet();
                        }
                    }
                };
            }
            for (Thread t : ts) {
                t.start();
            }
            for (Thread t : ts) {
                t.join();
            }
            long end1 = System.currentTimeMillis();
            costTime += (end1 - start1);
        }
        System.out.println("cost1: " + (costTime));
        //
        System.out.println();
        costTime = 0;
        
        
        // synchronized 内存锁性测试
        final Object lock = new Object();
        for (int m = 0; m < max; m++) {
            staticValue = 0;
            long start1 = System.currentTimeMillis();
            Thread[] ts = new Thread[max];
            for (int i = 0; i < max; i++) {
                ts[i] = new Thread() {
                    public void run() {
                        for (int i = 0; i < loopCount; i++) {
                            synchronized (lock) {
                                ++staticValue;
                            }
                        }
                    }
                };
            }
            for (Thread t : ts) {
                t.start();
            }
            for (Thread t : ts) {
                t.join();
            }
            long end1 = System.currentTimeMillis();
            costTime += (end1 - start1);
        }
        //
        System.out.println("cost2: " + (costTime));
    }

}
