package org.wanggz.lock.atomic;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class AtomicReferenceFieldUpdaterDemo {

    class DemoDataOne {

        Integer value1 = 12;
        Integer value2 = 12;
        Integer value3 = 13;
        Integer value4 = 14;
    }

    class DemoDataTow {
        public Integer value1 = 21;
        Integer        value2 = 22;
        Integer        value3 = 23;
        Integer        value4 = 24;
    }

    AtomicReferenceFieldUpdater<DemoDataOne, DemoDataTow> getUpdater(String fieldName) {
        return AtomicReferenceFieldUpdater.newUpdater(DemoDataOne.class, DemoDataTow.class, fieldName);
    }

    void doit() {
        DemoDataOne oneData = new DemoDataOne();
        DemoDataTow update = new DemoDataTow();
        DemoDataTow expect = new DemoDataTow();
        print("1 ==> " + getUpdater("value1").getAndSet(oneData, update) + " " + getUpdater("value1").get(oneData));
        print("2 ==> " + getUpdater("value3").compareAndSet(oneData, expect, update) + " "
                + getUpdater("value1").get(oneData));
    }

    static void print(String str) {
        System.out.println(str);
    }

    public static void main(String[] args) {
        AtomicReferenceFieldUpdaterDemo demo = new AtomicReferenceFieldUpdaterDemo();
        demo.doit();
    }

}
