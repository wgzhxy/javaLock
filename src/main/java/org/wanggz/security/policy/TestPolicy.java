package org.wanggz.security.policy;

import java.io.FileWriter;
import java.io.IOException;

public class TestPolicy {

    public static void main(String[] args) {


        String extDirs = System.getProperty("java.ext.dirs");
        System.out.println(extDirs);

        FileWriter writer = null;
        try {
            writer = new FileWriter("testPolicy.txt");
            writer.write("hello1");

            //java.security.Policy

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
