package org.wanggz.container;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by guangzhong.wgz on 2016/3/8.
 */
public class ConcurrentSkipMap {

    public static void main(String args[]) {
        ConcurrentSkipListMap<Integer, String> listMap = new ConcurrentSkipListMap<Integer, String>();
        for (int i = 0; i < 20; i++) {
            listMap.put(i, "goood one" + i);
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (Map.Entry<Integer, String> obj : listMap.entrySet()) {
            System.out.println("key : " + obj.getKey() + " ==== value:" + obj.getValue());
        }
        System.out.println("=========================================");
        for (Object tb : listMap.values()) {
            System.out.println("=================" + tb);
        }
    }
}
