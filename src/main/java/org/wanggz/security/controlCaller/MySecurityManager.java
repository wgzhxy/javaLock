package org.wanggz.security.controlCaller;

import sun.security.util.SecurityConstants;

import java.io.FilePermission;
import java.security.Permission;

public class MySecurityManager extends SecurityManager {

    private boolean isLoaded = true;

    @Override
    public void checkRead(String file) {
        checkPermission(new FilePermission(file, SecurityConstants.FILE_READ_ACTION));

    }

    @Override
    public void checkPermission(Permission perm) {
        //MyAccessControler.checkPermission(perm);
        if (isLoaded) {
            isLoaded = false;
            System.out.println(MyAccessControler.class.getClassLoader());
        }
    }


}
