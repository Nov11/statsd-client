package io.github.nov11;

public class Util {
    public static int getRandomPort() {
        return (int) (Math.random() * 60000) + 1024;
    }
}
