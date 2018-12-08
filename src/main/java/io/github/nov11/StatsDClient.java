package io.github.nov11;

public interface StatsDClient {
    void count(String metric, long value);

    default void increment(String metric){
        increment(metric, 1);
    }

    void increment(String metric, long value);

    void set(String metric, long value);

    void gauge(String metric, long value);

    void gauge(String metric, long value, boolean delta);

    void sampling(String metric, long value, double rate);

    void time(String metric, long time, double rate);

    void shutdown();
}
