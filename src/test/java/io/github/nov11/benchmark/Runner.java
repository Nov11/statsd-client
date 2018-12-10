package io.github.nov11.benchmark;

import io.github.nov11.StatsDClient;
import io.github.nov11.UdpStatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Runner {
    private static final Logger logger = LoggerFactory.getLogger(Runner.class);
    private static final String host = "192.168.1.25";
    private static final int port = 60000;

    public static void main(String[] args) throws InterruptedException {
        int messageCount = 10000;
//        com.timgroup.statsd.StatsDClient client = new NonBlockingStatsDClient("prefix", host, 60000,
//                exception -> logger.error("ex:", exception));


        StatsDClient client = UdpStatsDClient.buildClientSupportPipeline("prefix", host, port);
        long start = System.currentTimeMillis();
        for (int i = 0; i < messageCount; i++) {
            client.count("test-METRIC", 1);
        }
        long end = System.currentTimeMillis();
        logger.info("called NonBlockingStatsDClient.count {} times, cost: {} ms. blocking before gathering status", messageCount, end - start);
        Thread.sleep(10000);
//        client.stop();
        client.shutdown();
    }
}
