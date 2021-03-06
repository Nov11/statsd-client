package io.github.nov11.benchmark;

import com.timgroup.statsd.NonBlockingStatsDClient;
import io.github.nov11.StatsDClient;
import io.github.nov11.UdpStatsDClient;
import io.netty.util.ResourceLeakDetector;
import org.junit.*;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static io.github.nov11.Util.getRandomPort;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UdpClientBenchmarkTest {
    private static final Logger logger = LoggerFactory.getLogger(UdpClientBenchmarkTest.class);
    private static final int N_1000K = 1000000;
    private static final int N_100K = 100000;
    private static final int N_10k = 10000;

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
        port = getRandomPort();
        server = new UdpBenchmarkServer(port);
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    public void pipeline1000k() throws InterruptedException {
        udpPipelineClient("localhost", N_1000K, port);
    }

    @Test
    public void pipeline100k() throws InterruptedException {
        udpPipelineClient("localhost", N_100K, port);
    }

    @Test
    public void pipeline10k() throws InterruptedException {
        udpPipelineClient("localhost", N_10k, port);
    }

    @Test
    public void udpclient1000k() throws InterruptedException {
        udpNettyClient("localhost", N_1000K, port);
    }

    @Test
    public void udpclient100k() throws InterruptedException {
        udpNettyClient("localhost", N_100K, port);
    }

    @Test
    public void udpclient10k() throws InterruptedException {
        udpNettyClient("localhost", N_10k, port);
    }

    @Test
    public void nonBlockingStatsDClient1000k() throws InterruptedException {
        timGroupNonblockingStatsDClient("localhost", N_1000K, port);
    }

    @Test
    public void nonBlockingStatsDClient100k() throws InterruptedException {
        timGroupNonblockingStatsDClient("localhost", N_100K, port);
    }

    @Test
    public void nonBlockingStatsDClient10k() throws InterruptedException {
        timGroupNonblockingStatsDClient("localhost", N_10k, port);
    }

    @Test
    public void pipelineClientWithFacadeInterface1000k() throws InterruptedException {
        pipelineClientWithFacadeInterface("localhost", N_1000K, port);
    }

    @Test
    public void pipelineClientWithFacadeInterface100k() throws InterruptedException {
        pipelineClientWithFacadeInterface("localhost", N_100K, port);
    }

    @Test
    public void pipelineClientWithFacadeInterface10k() throws InterruptedException {
        pipelineClientWithFacadeInterface("localhost", N_10k, port);
    }

    private void udpPipelineClient(String host, int messageCount, int port) throws InterruptedException {
        StatsDClient client = UdpStatsDClient.buildPipelineClient("prefix", host, port);
        long start = System.currentTimeMillis();
        for (int i = 0; i < messageCount; i++) {
            client.count("test-METRIC", 1);
        }
        long end = System.currentTimeMillis();
        logger.info("called udpPipelineClient.count {} times, cost: {} ms. blocking before gathering status", messageCount, end - start);
        server.drainPackets();
        server.printStats();
        client.shutdown();
    }

    private void udpNettyClient(String host, int messageCount, int port) throws InterruptedException {
        StatsDClient client = UdpStatsDClient.buildNormalClient("prefix", host, port);
        long start = System.currentTimeMillis();
        for (int i = 0; i < messageCount; i++) {
            client.count("test-METRIC", 1);
        }
        long end = System.currentTimeMillis();
        logger.info("called udpNettyClient.count {} times, cost: {} ms. blocking before gathering status", messageCount, end - start);
        server.drainPackets();
        server.printStats();
        client.shutdown();
    }


    private void timGroupNonblockingStatsDClient(String host, int messageCount, int port) throws InterruptedException {
        com.timgroup.statsd.StatsDClient client = new NonBlockingStatsDClient("prefix", host, port,
                exception -> logger.error("ex:", exception));
        long start = System.currentTimeMillis();
        for (int i = 0; i < messageCount; i++) {
            client.count("test-METRIC", 1);
        }
        long end = System.currentTimeMillis();
        logger.info("called NonBlockingStatsDClient.count {} times, cost: {} ms. blocking before gathering status", messageCount, end - start);
        server.drainPackets();
        server.printStats();
        client.stop();
    }

    private void pipelineClientWithFacadeInterface(String host, int messageCount, int port) throws InterruptedException {
        com.timgroup.statsd.StatsDClient client = UdpStatsDClient.buildTimGroupStatsDClient("prefix", host, port);
        long start = System.currentTimeMillis();
        for (int i = 0; i < messageCount; i++) {
            client.count("test-METRIC", 1);
        }
        long end = System.currentTimeMillis();
        logger.info("called buildTimGroupStatsDClient.count {} times, cost: {} ms. blocking before gathering status", messageCount, end - start);
        server.drainPackets();
        server.printStats();
        client.stop();
    }
}
