package org.wanggz.security.policy;

import sun.security.util.SecurityConstants;

import java.security.Permission;
import java.io.FilePermission;

/**
 * Created by guangzhong.wgz on 2016/3/23.
 */
public class FilePermissionMain {

    public static void main(String[] args) {

        Permission perOne = new FilePermission("d:/tmp/test.txt", SecurityConstants.FILE_READ_ACTION);
        Permission perAll = new FilePermission("d:/tmp/*", SecurityConstants.FILE_READ_ACTION);

        System.out.println(perOne.implies(perAll));
        System.out.println(perAll.implies(perOne));
    }

}
