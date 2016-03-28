package org.wanggz.security;


/**
 * Created by guangzhong.wgz on 2016/3/23.
 */
public class MySecurityManager extends SecurityManager {

    @Override
    public void checkRead(String file) {
        if (file != null && file.contains("good")) {
            throw new SecurityException("can't read file");
        }
    }
}
