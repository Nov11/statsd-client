package io.github.nov11.benchmark;

import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClientErrorHandler;
import io.github.nov11.StatsDClient;
import io.github.nov11.UdpStatsDClient;
import io.netty.util.ResourceLeakDetector;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdpClientBenchmark10KiloTest {
    private static final Logger logger = LoggerFactory.getLogger(UdpClientBenchmark10KiloTest.class);
    private static final int port = 60000;
    private static final int messageCount = 1000000;
    private static final int oneKilo = 1000;
    private static final int delay = 20000;
    private UdpBenchmarkServer server;

    @BeforeClass
    public static void beforeClass() {
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
    }

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
        logger.info("called udpPipelineClient.count {} times. wait {} ms before gathering status", messageCount, delay);
        Thread.sleep(delay);
        server.printStats();
        client.shutdown();
    }

    @Test
    public void udpNettyClient() throws InterruptedException {
        StatsDClient client = UdpStatsDClient.build("prefix", "localhost", port);
        for (int i = 0; i < messageCount; i++) {
            client.count("test-METRIC", 1);
        }
        logger.info("called udpNettyClient.count {} times. wait {} ms before gathering status", messageCount, delay);
        Thread.sleep(delay);
        server.printStats();
        client.shutdown();
    }

    @Test
    public void timGroupNonblockingStatsDClient() throws InterruptedException {
        com.timgroup.statsd.StatsDClient client = new NonBlockingStatsDClient("prefix", "localhost", port,
                new StatsDClientErrorHandler() {
                    @Override
                    public void handle(Exception exception) {
                        logger.error("ex:", exception);
                    }
                });
        for (int i = 0; i < messageCount; i++) {
            client.count("test-METRIC", 1);
        }
        logger.info("called NonBlockingStatsDClient.count {} times. wait {} ms before gathering status", messageCount, delay);
        Thread.sleep(delay);
        server.printStats();
        client.stop();
    }
}
