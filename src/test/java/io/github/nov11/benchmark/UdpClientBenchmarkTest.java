package io.github.nov11.benchmark;

import io.github.nov11.StatsDClient;
import io.github.nov11.UdpStatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdpClientBenchmarkTest {
    private static final Logger logger = LoggerFactory.getLogger(UdpClientBenchmarkTest.class);
    private static final int port = 60000;
    private static final int messageCount = 1000000;
    private static final int oneKilo = 1000;


    public static void main(String[] args) throws InterruptedException {
        UdpBenchmarkServer server = new UdpBenchmarkServer(port);
        StatsDClient client = UdpStatsDClient.buildClientSupportPipeline("prefix", "localhost", port);
        for (int i = 0; i < messageCount; i++) {
            client.count("test-METRIC", 1);
            if ((i + 1) % oneKilo == 0) {
                logger.info("called 1 kilo client.count");
            }
        }
        logger.info("called client.count {} times", messageCount);
        Thread.sleep(3000);
        server.printStats();
        client.shutdown();
        server.shutdown();
    }
}
