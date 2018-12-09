package io.github.nov11.benchmark;

import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClientErrorHandler;
import io.github.nov11.StatsDClient;
import io.github.nov11.UdpStatsDClient;
import io.netty.util.ResourceLeakDetector;
import org.junit.*;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UdpClientBenchmarkTest {
    private static final Logger logger = LoggerFactory.getLogger(UdpClientBenchmarkTest.class);
    private static final int N_1000K = 1000000;
    private static final int N_100K = 100000;
    private static final int N_10k = 10000;
    private static final int delay1000k = 60000;
    private static final int delay100k = 8000;
    private static final int delay10k = 2000;
    private static int port;
    private UdpBenchmarkServer server;

    @BeforeClass
    public static void beforeClass() throws IOException {
        Runtime.getRuntime().exec("sysctl net.core.rmem_max");
        Runtime.getRuntime().exec("sysctl net.core.rmem_default");
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
        udpPipelineClient(N_1000K, port, delay1000k);
    }

    @Test
    public void pipeline100k() throws InterruptedException {
        udpPipelineClient(N_100K, port, delay100k);
    }

    @Test
    public void pipeline10k() throws InterruptedException {
        udpPipelineClient(N_10k, port, delay10k);
    }

    @Test
    public void udpclient1000k() throws InterruptedException {
        udpNettyClient(N_1000K, port, delay1000k);
    }

    @Test
    public void udpclient100k() throws InterruptedException {
        udpNettyClient(N_100K, port, delay100k);
    }

    @Test
    public void udpclient10k() throws InterruptedException {
        udpNettyClient(N_10k, port, delay10k);
    }

    @Test
    public void nonBlockingStatsDClient1000k() throws InterruptedException {
        timGroupNonblockingStatsDClient(N_1000K, port, delay1000k);
    }

    @Test
    public void nonBlockingStatsDClient100k() throws InterruptedException {
        timGroupNonblockingStatsDClient(N_100K, port, delay100k);
    }

    @Test
    public void nonBlockingStatsDClient10k() throws InterruptedException {
        timGroupNonblockingStatsDClient(N_10k, port, delay10k);
    }

    private void udpPipelineClient(int messageCount, int port, int delay) throws InterruptedException {
        StatsDClient client = UdpStatsDClient.buildClientSupportPipeline("prefix", "localhost", port);
        long start = System.currentTimeMillis();
        for (int i = 0; i < messageCount; i++) {
            client.count("test-METRIC", 1);
        }
        long end = System.currentTimeMillis();
        logger.info("called udpPipelineClient.count {} times, cost: {} ms. wait {} ms before gathering status", messageCount, end - start, delay);
        Thread.sleep(delay);
        server.printStats();
        client.shutdown();
    }


    private void udpNettyClient(int messageCount, int port, int delay) throws InterruptedException {
        StatsDClient client = UdpStatsDClient.build("prefix", "localhost", port);
        long start = System.currentTimeMillis();
        for (int i = 0; i < messageCount; i++) {
            client.count("test-METRIC", 1);
        }
        long end = System.currentTimeMillis();
        logger.info("called udpNettyClient.count {} times, cost: {} ms. wait {} ms before gathering status", messageCount, end - start, delay);
        Thread.sleep(delay);
        server.printStats();
        client.shutdown();
    }


    private void timGroupNonblockingStatsDClient(int messageCount, int port, int delay) throws InterruptedException {
        com.timgroup.statsd.StatsDClient client = new NonBlockingStatsDClient("prefix", "localhost", port,
                new StatsDClientErrorHandler() {
                    @Override
                    public void handle(Exception exception) {
                        logger.error("ex:", exception);
                    }
                });
        long start = System.currentTimeMillis();
        for (int i = 0; i < messageCount; i++) {
            client.count("test-METRIC", 1);
        }
        long end = System.currentTimeMillis();
        logger.info("called NonBlockingStatsDClient.count {} times, cost: {} ms. wait {} ms before gathering status", messageCount, end - start, delay);
        Thread.sleep(delay);
        server.printStats();
        client.stop();
    }
}
