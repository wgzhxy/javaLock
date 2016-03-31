package org.wanggz.lock;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by guangzhong.wgz on 2016/3/31.
 */
public class BlockQueen {

    private ArrayBlockingQueue arrayQueue = new ArrayBlockingQueue(100);

    private LinkedBlockingDeque linkedQueue = new LinkedBlockingDeque();

    public static void main(String args[]) {
        System.out.println("=============================");
    }


}
