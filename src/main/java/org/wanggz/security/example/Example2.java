package org.wanggz.security.example;

public class Example2 {

    public static void main(String[] args) {

        System.setSecurityManager(new SecurityManager());

        TextFileDisplayer tfd = new TextFileDisplayer("d:/question.txt");

        Friend friend = new Friend(tfd, true);

        Stranger stranger = new Stranger(tfd, true);

        stranger.doYourThing();
    }
}

