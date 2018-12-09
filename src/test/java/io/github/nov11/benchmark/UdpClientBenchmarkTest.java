package io.github.nov11.benchmark;

import com.timgroup.statsd.NonBlockingStatsDClient;
import io.github.nov11.StatsDClient;
import io.github.nov11.UdpStatsDClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdpClientBenchmarkTest {
    private static final Logger logger = LoggerFactory.getLogger(UdpClientBenchmarkTest.class);
    private static final int port = 60000;
    private static final int messageCount = 1000000;
    private static final int oneKilo = 1000;
    private UdpBenchmarkServer server;

    @Before
    public void setUp() throws Exception {
        server = new UdpBenchmarkServer(port);
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    public void udpPipelineClient() throws InterruptedException {
        StatsDClient client = UdpStatsDClient.buildClientSupportPipeline("prefix", "localhost", port);
        for (int i = 0; i < messageCount; i++) {
            client.count("test-METRIC", 1);
        }
        logger.info("called client.count {} times", messageCount);
        Thread.sleep(3000);
        server.printStats();
        client.shutdown();
    }

    @Test
    public void timGroupNonblockingStatsDClient() throws InterruptedException {
        com.timgroup.statsd.StatsDClient client = new NonBlockingStatsDClient("prefix", "localhost", port);
        for (int i = 0; i < messageCount; i++) {
            client.count("test-METRIC", 1);
        }
        logger.info("called client.count {} times", messageCount);
        Thread.sleep(3000);
        server.printStats();
        client.stop();
    }
}
