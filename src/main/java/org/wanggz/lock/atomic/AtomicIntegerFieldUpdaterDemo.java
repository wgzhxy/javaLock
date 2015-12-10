package org.wanggz.lock.atomic;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public class AtomicIntegerFieldUpdaterDemo {

    class DemoData {
        public volatile int value1 = 1;
        volatile int        value2 = 2;
        volatile int        value3 = 3;
        volatile int        value4 = 4;
    }

    AtomicIntegerFieldUpdater<DemoData> getUpdater(String fieldName) {
        return AtomicIntegerFieldUpdater.newUpdater(DemoData.class, fieldName);
    }

    void doit() {
        DemoData data = new DemoData();
        print("1 ==> " + getUpdater("value1").getAndSet(data, 10));
        print("3 ==> " + getUpdater("value2").incrementAndGet(data));
        print("2 ==> " + getUpdater("value3").decrementAndGet(data));
        print("true ==> " + getUpdater("value4").compareAndSet(data, 4, 5) + ", value4 = " + getUpdater("value4").get(data));
        print("true ==> " + getUpdater("value4").compareAndSet(data, 6, 7) + ", value4 = " + getUpdater("value4").get(data));
    }

    static void print(String str) {
        System.out.println(str);
    }

    public static void main(String[] args) {
        AtomicIntegerFieldUpdaterDemo demo = new AtomicIntegerFieldUpdaterDemo();
        demo.doit();
    }
}
