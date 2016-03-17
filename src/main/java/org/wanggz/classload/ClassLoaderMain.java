package org.wanggz.classload;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guangzhong.wgz on 2016/3/16.
 */
public class ClassLoaderMain {

    public static void main(String args[]) {

        //自定义加载器 myloader 加载类
        String fullClassPath = "org.wanggz.example.CouterServiceImpl";
        try {
            MyClassLoader myClassLoader = new MyClassLoader();
            myClassLoader.addUrl(new URL("file:///E:/aliwork_android/git/javaLock/target/classes/"));
            Class<?> obj = myClassLoader.loadClass(fullClassPath);
            System.out.println(obj);

            // jar:file:/home/user/a/b/c/foo.jar!/com/example/stuff/config.txt
            URL url = new URL("file:///E:/aliwork_android/git/javaLock/target/classes/");
            List<URL> urls = new ArrayList<URL>();
            urls.add(url);

            URL[] a = new URL[]{};
            MyUrlClassLoader urlClassLoader = new MyUrlClassLoader(urls.toArray(a), null);
            Class<?> obj2 = urlClassLoader.loadClass(fullClassPath);

            System.out.println(obj.toString());
            System.out.println(obj2.toString());

            if (obj.isAssignableFrom(obj2) || obj.isInstance(obj2)) {
                System.out.println("=====================true=========================");
            } else {
                System.out.println("obj != obj2 ======================================");
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }

        System.out.println(Thread.currentThread().getContextClassLoader());
        System.out.println(Thread.currentThread().getContextClassLoader().getParent());
        System.out.println(Thread.currentThread().getContextClassLoader().getParent().getParent());

    }
}
