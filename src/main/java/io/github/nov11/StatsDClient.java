package io.github.nov11;

public interface StatsDClient {
    void count(String metric, long delta);

    void increment(String metric, long delta);

    void gauge(String metric, long value);

    void time(String metric, long time);

    void shutdown();
}
