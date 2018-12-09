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

public class UdpClientBenchmarkTest {
    private static final Logger logger = LoggerFactory.getLogger(UdpClientBenchmarkTest.class);
    private static int port = 60000;
    private static int messageCount = 10000;
    private static final int oneKilo = 1000;
    private static final int delay = 20000;
    private UdpBenchmarkServer server;

    @BeforeClass
    public static void beforeClass() {
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
    }

    @Before
    public void setUp() throws Exception {
        port = (int) (Math.random() * 60000);
        server = new UdpBenchmarkServer(port);
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    public void pipeline1000k() throws InterruptedException {
        messageCount = 1000000;
        udpPipelineClient();
    }

    @Test
    public void pipeline100k() throws InterruptedException {
        messageCount = 100000;
        udpPipelineClient();
    }

    @Test
    public void pipeline10k() throws InterruptedException {
        messageCount = 10000;
        udpPipelineClient();
    }

    private void udpPipelineClient() throws InterruptedException {
        StatsDClient client = UdpStatsDClient.buildClientSupportPipeline("prefix", "localhost", port);
        for (int i = 0; i < messageCount; i++) {
            client.count("test-METRIC", 1);
        }
        logger.info("called udpPipelineClient.count {} times. wait {} ms before gathering status", messageCount, delay);
        Thread.sleep(delay);
        server.printStats();
        client.shutdown();
    }


    private void udpNettyClient() throws InterruptedException {
        StatsDClient client = UdpStatsDClient.build("prefix", "localhost", port);
        for (int i = 0; i < messageCount; i++) {
            client.count("test-METRIC", 1);
        }
        logger.info("called udpNettyClient.count {} times. wait {} ms before gathering status", messageCount, delay);
        Thread.sleep(delay);
        server.printStats();
        client.shutdown();
    }


    private void timGroupNonblockingStatsDClient() throws InterruptedException {
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
