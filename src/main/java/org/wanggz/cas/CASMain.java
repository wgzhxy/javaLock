package org.wanggz.cas;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by wanggz on 16/5/20.
 */
public class CASMain {

    private static AtomicInteger count = new AtomicInteger(0);

    public static void main(String args[]) {
        System.out.println("===========");

        int i = 0;

        while(true) {
            int currentValue = count.get();
            if(count.compareAndSet(currentValue, i)) {
                break;
            }
        }
        System.out.println(count.get());
    }
}
