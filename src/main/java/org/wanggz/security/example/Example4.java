package org.wanggz.security.example;

public class Example4 {

    public static void main(String[] args) {

        TextFileDisplayer tfd = new TextFileDisplayer("d:/answer.txt");

        Stranger stranger = new Stranger(tfd, false);
        Friend friend = new Friend(stranger, true);

        stranger.doYourThing();
    }
}
