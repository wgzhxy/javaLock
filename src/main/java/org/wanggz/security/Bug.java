package org.wanggz.security;

import java.security.Permission;

public class Bug {
    public static class A {
    }

    public static void main(String[] args) throws Exception {

        System.out.println("Setting Security Manager");

        System.setSecurityManager(new SecurityManager() {
            public void checkPermission(Permission p) {
                new A();
            }
        });
        System.out.println("Post set.");
    }
}
