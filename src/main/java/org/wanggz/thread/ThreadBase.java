package org.wanggz.thread;

/**
 * Created by guangzhong.wgz on 2016/3/8.
 */
public class ThreadBase {

    public static void main(String args[]) {
        ThreadGroup threadGroup = new ThreadGroup("wanggz-hsf");
        Thread t1 = new Thread(threadGroup, new Runnable() {
            public void run() {
                try {

                    System.out.println("running begin ~~~~~~~~~!");
                    Thread.sleep(2000);
                    System.out.println("running end ~~~~~~~~~!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        Thread t2 = new Thread(threadGroup, new Runnable() {
            public void run() {
                try {
                    System.out.println("running2 begin ~~~~~~~~~!");
                    Thread.sleep(2000);
                    System.out.println("running2 end ~~~~~~~~~!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        try {
            t1.start();
            t2.start();
            Thread.sleep(1000);
            threadGroup.stop();
            System.out.println("t1.status " + t1.getState() + " t1.alive " + t1.isAlive() + " ThreadName " + t1.getName());
            Thread.yield();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
