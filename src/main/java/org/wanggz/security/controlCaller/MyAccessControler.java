package org.wanggz.security.controlCaller;

import sun.security.util.SecurityConstants;

import java.io.FilePermission;
import java.security.AccessControlException;
import java.security.Permission;


public class MyAccessControler {

    private MyAccessControler() {
        super();
    }

    public static void checkPermission(Permission perm) throws AccessControlException {
        Permission perAll = new FilePermission("d:/tmp/*", SecurityConstants.FILE_READ_ACTION);
        if (perAll.implies(perm)) {
            System.out.println("你可以读取这个文件哦!");
        } else {
            throw new AccessControlException("你没有读取这个文件的权限");
        }
    }
}
