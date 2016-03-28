package org.wanggz.classload;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by guangzhong.wgz on 2016/3/16.
 */
public class MyUrlClassLoader extends URLClassLoader {

    public MyUrlClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);





    }

}
