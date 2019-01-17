package io.github.nov11.benchmark;

import io.github.nov11.StatsDClient;
import io.github.nov11.UdpStatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Runner {
    private static final Logger logger = LoggerFactory.getLogger(Runner.class);
    private static final String host = "localhost";
    private static final int port = 60000;

    public static void main(String[] args) throws InterruptedException {
        int messageCount = 10000;
        StatsDClient client = UdpStatsDClient.buildPipelineClient("prefix", host, port);
        long start = System.currentTimeMillis();
        for (int i = 0; i < messageCount; i++) {
            for (int j = 0; j < 500; j++) {
                client.count("test-METRIC", 1);
            }
        }
        long end = System.currentTimeMillis();
        logger.info("called count {} times, cost: {} ms. blocking before gathering status", messageCount, end - start);
        Thread.sleep(10000);
        client.shutdown();
    }
}
