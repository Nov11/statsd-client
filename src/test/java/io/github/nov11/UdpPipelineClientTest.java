package io.github.nov11;

import io.netty.util.ResourceLeakDetector;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingDeque;

public class UdpPipelineClientTest {
    private static final Logger logger = LoggerFactory.getLogger(UdpPipelineClientTest.class);
    private static final String METRIC = "test-METRIC";
    private static final int VALUE = 10;
    private static StatsDClient client;
    private static DumbUdpServer server;
    private static BlockingDeque<String> blockingDeque;

    @BeforeClass
    public static void beforeClass() throws Exception {
        int port = (int) (Math.random() * 60000);
        logger.info("PORT: {}", port);
        client = UdpStatsDClient.buildClientSupportPipeline("prefix", "localhost", port);
        server = new DumbUdpServer(port);
        blockingDeque = server.getMessage();
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
    }

    @AfterClass
    public static void afterClass() {
        client.shutdown();
        server.shutdown();
    }

    @After
    public void after() {
        Assert.assertTrue(blockingDeque.isEmpty());
    }

    @Test
    public void count() throws InterruptedException {
        client.count(METRIC, VALUE);
        Thread.sleep(500);
        Assert.assertNull(blockingDeque.poll());
        String recv = blockingDeque.take();
        Assert.assertEquals("prefix.test-METRIC:10|c", recv);
    }

    @Test
    public void increment() throws InterruptedException {
        client.increment(METRIC);
        Thread.sleep(500);
        Assert.assertNull(blockingDeque.poll());
        String recv = blockingDeque.take();
        Assert.assertEquals("prefix.test-METRIC:1|c", recv);
    }

    @Test
    public void incrementWithCount() throws InterruptedException {
        client.increment(METRIC, VALUE);
        Thread.sleep(500);
        Assert.assertNull(blockingDeque.poll());
        String recv = blockingDeque.take();
        Assert.assertEquals("prefix.test-METRIC:10|c", recv);
    }

    @Test
    public void set() throws InterruptedException {
        client.set(METRIC, VALUE);
        Thread.sleep(500);
        Assert.assertNull(blockingDeque.poll());
        String recv = blockingDeque.take();
        Assert.assertEquals("prefix.test-METRIC:10|s", recv);
    }

    @Test
    public void gauge() throws InterruptedException {
        client.gauge(METRIC, VALUE);
        Thread.sleep(500);
        Assert.assertNull(blockingDeque.poll());
        String recv = blockingDeque.take();
        Assert.assertEquals("prefix.test-METRIC:10|g", recv);
    }

    @Test
    public void gaugePositive() throws InterruptedException {
        String metric = "test-METRIC";
        client.gauge(metric, 10, true);
        Thread.sleep(500);
        Assert.assertNull(blockingDeque.poll());
        String recv = blockingDeque.take();
        Assert.assertEquals("prefix.test-METRIC:+10|g", recv);
    }

    @Test
    public void gaugeNegative() throws InterruptedException {
        client.gauge(METRIC, -10, false);
        Thread.sleep(500);
        Assert.assertNull(blockingDeque.poll());
        String recv = blockingDeque.take();
        Assert.assertEquals("prefix.test-METRIC:-10|g", recv);
    }

    @Test
    public void sampling() throws InterruptedException {
        client.sampling(METRIC, 20, 1.2);
        Thread.sleep(500);
        Assert.assertNull(blockingDeque.poll());
        String recv = blockingDeque.take();
        Assert.assertEquals("prefix.test-METRIC:20|c|@1.2", recv);
    }

    @Test
    public void time() throws InterruptedException {
        client.time(METRIC, 10, 2.2);
        Thread.sleep(500);
        Assert.assertNull(blockingDeque.poll());
        String recv = blockingDeque.take();
        Assert.assertEquals("prefix.test-METRIC:10|ms|@2.2", recv);
    }
}