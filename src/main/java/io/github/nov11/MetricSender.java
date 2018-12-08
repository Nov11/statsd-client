package io.github.nov11;

public interface MetricSender {
    void send(String msg);
    void shutdown();
}
