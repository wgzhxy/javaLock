package org.wanggz.security.example;

public class Example3 {

    public static void main(String[] args) {

        TextFileDisplayer tfd = new TextFileDisplayer("d:/answer.txt");
        Friend friend = new Friend(tfd, false);
        Stranger stranger = new Stranger(friend, true);
        stranger.doYourThing();
    }
}
