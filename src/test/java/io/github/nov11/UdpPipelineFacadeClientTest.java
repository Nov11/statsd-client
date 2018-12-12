package io.github.nov11;

import io.netty.util.ResourceLeakDetector;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingDeque;

import static io.github.nov11.Util.getRandomPort;

public class UdpPipelineFacadeClientTest {
    private static final Logger logger = LoggerFactory.getLogger(UdpPipelineFacadeClientTest.class);
    private static final String METRIC = "test-METRIC";
    private static final int VALUE = 10;
    private static com.timgroup.statsd.StatsDClient client;
    private static DumbUdpServer server;
    private static BlockingDeque<String> blockingDeque;

    @BeforeClass
    public static void beforeClass() throws Exception {
        int port = getRandomPort();
        logger.info("PORT: {}", port);
        client = UdpStatsDClient.buildTimGroupStatsDClient("prefix", "localhost", port);
        server = new DumbUdpServer(port);
        blockingDeque = server.getMessage();
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
    }

    @AfterClass
    public static void afterClass() {
        client.stop();
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
    public void incrementCounter() throws InterruptedException {
        client.incrementCounter(METRIC);
        Thread.sleep(500);
        Assert.assertNull(blockingDeque.poll());
        String recv = blockingDeque.take();
        Assert.assertEquals("prefix.test-METRIC:1|c", recv);
    }

    @Test
    public void decrement() throws InterruptedException {
        client.decrement(METRIC);
        Thread.sleep(500);
        Assert.assertNull(blockingDeque.poll());
        String recv = blockingDeque.take();
        Assert.assertEquals("prefix.test-METRIC:-1|c", recv);
    }

    @Test
    public void decrementCounter() throws InterruptedException {
        client.decrementCounter(METRIC);
        Thread.sleep(500);
        Assert.assertNull(blockingDeque.poll());
        String recv = blockingDeque.take();
        Assert.assertEquals("prefix.test-METRIC:-1|c", recv);
    }

    @Test
    public void set() throws InterruptedException {
        client.set(METRIC, String.valueOf(VALUE));
        Thread.sleep(500);
        Assert.assertNull(blockingDeque.poll());
        String recv = blockingDeque.take();
        Assert.assertEquals("prefix.test-METRIC:10|s", recv);
    }

    @Test
    public void recordSetEvent() throws InterruptedException {
        client.recordSetEvent(METRIC, String.valueOf(VALUE));
        Thread.sleep(500);
        Assert.assertNull(blockingDeque.poll());
        String recv = blockingDeque.take();
        Assert.assertEquals("prefix.test-METRIC:10|s", recv);
    }

    @Test
    public void gaugePositive() throws InterruptedException {
        String metric = "test-METRIC";
        client.recordGaugeValue(metric, 10);
        Thread.sleep(500);
        Assert.assertNull(blockingDeque.poll());
        String recv = blockingDeque.take();
        Assert.assertEquals("prefix.test-METRIC:10|g", recv);
    }

    @Test
    public void gaugeNegative() throws InterruptedException {
        String metric = "test-METRIC";
        client.recordGaugeValue(metric, -10);
        Thread.sleep(500);
        Assert.assertNull(blockingDeque.poll());
        String recv = blockingDeque.take();
        Assert.assertEquals("prefix.test-METRIC:0|g\nprefix.test-METRIC:-10|g", recv);
    }

    @Test
    public void gaugeLong() throws InterruptedException {
        String metric = "test-METRIC";
        client.recordGaugeValue(metric, 10L);
        Thread.sleep(500);
        Assert.assertNull(blockingDeque.poll());
        String recv = blockingDeque.take();
        Assert.assertEquals("prefix.test-METRIC:10|g", recv);
    }

    @Test
    public void gaugeDouble() throws InterruptedException {
        String metric = "test-METRIC";
        client.recordGaugeValue(metric, 1.1);
        Thread.sleep(500);
        Assert.assertNull(blockingDeque.poll());
        String recv = blockingDeque.take();
        Assert.assertEquals("prefix.test-METRIC:1.1|g", recv);
    }

    @Test
    public void gaugeDeltaNegative() throws InterruptedException {
        client.recordGaugeDelta(METRIC, -10);
        Thread.sleep(500);
        Assert.assertNull(blockingDeque.poll());
        String recv = blockingDeque.take();
        Assert.assertEquals("prefix.test-METRIC:-10|g", recv);
    }

    @Test
    public void gaugeDeltaPositive() throws InterruptedException {
        client.recordGaugeDelta(METRIC, 10);
        Thread.sleep(500);
        Assert.assertNull(blockingDeque.poll());
        String recv = blockingDeque.take();
        Assert.assertEquals("prefix.test-METRIC:+10|g", recv);
    }

    @Test
    public void sampling() throws InterruptedException {
        client.count(METRIC, 20, 1.2);
        Thread.sleep(500);
        Assert.assertNull(blockingDeque.poll());
        String recv = blockingDeque.take();
        Assert.assertEquals("prefix.test-METRIC:20|c|@1.2", recv);
    }

    @Test
    public void time() throws InterruptedException {
        client.time(METRIC, 10);
        Thread.sleep(500);
        Assert.assertNull(blockingDeque.poll());
        String recv = blockingDeque.take();
        Assert.assertEquals("prefix.test-METRIC:10|ms", recv);
    }

    @Test
    public void recordExecutionTime() throws InterruptedException {
        client.recordExecutionTime(METRIC, 10);
        Thread.sleep(500);
        Assert.assertNull(blockingDeque.poll());
        String recv = blockingDeque.take();
        Assert.assertEquals("prefix.test-METRIC:10|ms", recv);
    }

    @Test
    public void recordExecutionTimeWithSampleRate() throws InterruptedException {
        client.recordExecutionTime(METRIC, 10, 2.1);
        Thread.sleep(500);
        Assert.assertNull(blockingDeque.poll());
        String recv = blockingDeque.take();
        Assert.assertEquals("prefix.test-METRIC:10|ms|@2.1", recv);
    }
}