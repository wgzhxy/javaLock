package org.wanggz.security;

import java.io.FileInputStream;
import java.io.IOException;

public class TestMySecurityManager {

    public static void main(String[] args) {
        try {
            //
            System.setSecurityManager(new MySecurityManager());

            try {
                FileInputStream fis = new FileInputStream("test");
                System.out.println(fis.read());
            } catch (IOException e) {
                e.printStackTrace();
            }
            //子线程是否有效
            new Thread(new Runnable() {
                public void run() {
                    try {
                        FileInputStream fis = new FileInputStream("good");


                        System.out.println(fis.read());
                    } catch (Throwable e) {
                        System.out.println("Thread Throwable : " + e.getLocalizedMessage());
                    }
                }
            }).start();
            //睡 5s
            Thread.sleep(5000);
        } catch (Throwable e) {
            System.out.println("TestMySecurityManager Exception : " + e.getLocalizedMessage());
        }
        System.out.println("========== end =============");
    }
}
