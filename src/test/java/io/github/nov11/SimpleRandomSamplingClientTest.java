package io.github.nov11;

import io.netty.util.ResourceLeakDetector;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;

import static io.github.nov11.Util.getRandomPort;

public class SimpleRandomSamplingClientTest {
    private static final Logger logger = LoggerFactory.getLogger(SimpleRandomSamplingClientTest.class);
    private static final String METRIC = "test-METRIC";
    private static final int VALUE = 10;
    private static com.timgroup.statsd.StatsDClient client;
    private static DumbUdpServer server;
    private static BlockingDeque<String> blockingDeque;

    @BeforeClass
    public static void beforeClass() throws Exception {
        int port = getRandomPort();
        logger.info("PORT: {}", port);
        client = UdpStatsDClient.buildSimpleRandomSamplingStatsDClient("prefix", "localhost", port, 0.5);
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
    public void after() throws InterruptedException {
        Thread.sleep(1000);
        blockingDeque.clear();
    }

    @Test
    public void count() throws InterruptedException {
        for (int i = 0; i < 1000; i++) {
            client.count(METRIC, VALUE);
        }
        resultChecker("prefix.test-METRIC:10|c|@0.5");
    }

    private void resultChecker(String expectedMetric) throws InterruptedException {
        resultChecker(expectedMetric, 450);
    }

    private void resultChecker(String expectedMetric, int expectedCount) throws InterruptedException {
        int count = 0;
        String value = "";
        while (true) {
            String recv = blockingDeque.poll(100, TimeUnit.MILLISECONDS);
            if (recv == null) {
                break;
            }
            String[] ret = recv.split("\n");
            count += ret.length;
            if (ret.length > 0) {
                value = ret[0];
            }
        }
        Assert.assertEquals(expectedMetric, value);
        Assert.assertTrue(count > expectedCount);
    }

    @Test
    public void increment() throws InterruptedException {
        for (int i = 0; i < 1000; i++) {
            client.increment(METRIC);
        }
        resultChecker("prefix.test-METRIC:1|c|@0.5");
    }

    @Test
    public void incrementCounter() throws InterruptedException {
        for (int i = 0; i < 1000; i++) {
            client.incrementCounter(METRIC);
        }
        resultChecker("prefix.test-METRIC:1|c|@0.5");
    }

    @Test
    public void decrement() throws InterruptedException {
        for (int i = 0; i < 1000; i++) {
            client.decrement(METRIC);
        }
        resultChecker("prefix.test-METRIC:-1|c|@0.5");
    }

    @Test
    public void decrementCounter() throws InterruptedException {
        for (int i = 0; i < 1000; i++) {
            client.decrement(METRIC);
        }
        resultChecker("prefix.test-METRIC:-1|c|@0.5");
    }

    @Test
    public void set() throws InterruptedException {
        client.set(METRIC, String.valueOf(VALUE));
        Assert.assertNull(blockingDeque.poll());
        String recv = blockingDeque.take();
        Assert.assertEquals("prefix.test-METRIC:10|s", recv);
    }

    @Test
    public void recordSetEvent() throws InterruptedException {
        client.recordSetEvent(METRIC, String.valueOf(VALUE));
        Assert.assertNull(blockingDeque.poll());
        String recv = blockingDeque.take();
        Assert.assertEquals("prefix.test-METRIC:10|s", recv);
    }

    @Test
    public void gaugePositive() throws InterruptedException {
        String metric = "test-METRIC";
        client.recordGaugeValue(metric, 10);
        Assert.assertNull(blockingDeque.poll());
        String recv = blockingDeque.take();
        Assert.assertEquals("prefix.test-METRIC:10|g", recv);
    }

    @Test
    public void gaugeNegative() throws InterruptedException {
        String metric = "test-METRIC";
        client.recordGaugeValue(metric, -10);
        Assert.assertNull(blockingDeque.poll());
        String recv = blockingDeque.take();
        Assert.assertEquals("prefix.test-METRIC:0|g\nprefix.test-METRIC:-10|g", recv);
    }

    @Test
    public void gaugeLong() throws InterruptedException {
        String metric = "test-METRIC";
        client.recordGaugeValue(metric, 10L);
        Assert.assertNull(blockingDeque.poll());
        String recv = blockingDeque.take();
        Assert.assertEquals("prefix.test-METRIC:10|g", recv);
    }

    @Test
    public void gaugeDouble() throws InterruptedException {
        String metric = "test-METRIC";
        client.recordGaugeValue(metric, 1.1);
        Assert.assertNull(blockingDeque.poll());
        String recv = blockingDeque.take();
        Assert.assertEquals("prefix.test-METRIC:1.1|g", recv);
    }

    @Test
    public void gaugeDeltaNegative() throws InterruptedException {
        client.recordGaugeDelta(METRIC, -10);
        Assert.assertNull(blockingDeque.poll());
        String recv = blockingDeque.take();
        Assert.assertEquals("prefix.test-METRIC:-10|g", recv);
    }

    @Test
    public void gaugeDeltaPositive() throws InterruptedException {
        client.recordGaugeDelta(METRIC, 10);
        Assert.assertNull(blockingDeque.poll());
        String recv = blockingDeque.take();
        Assert.assertEquals("prefix.test-METRIC:+10|g", recv);
    }

    @Test
    public void sampling() throws InterruptedException {
        for (int i = 0; i < 1000; i++) {
            client.count(METRIC, 20, 1.2);
        }
        resultChecker("prefix.test-METRIC:20|c|@0.5", 450);
    }

    @Test
    public void time() throws InterruptedException {
        client.time(METRIC, 10);
        Assert.assertNull(blockingDeque.poll());
        String recv = blockingDeque.take();
        Assert.assertEquals("prefix.test-METRIC:10|ms", recv);
    }

    @Test
    public void recordExecutionTime() throws InterruptedException {
        client.recordExecutionTime(METRIC, 10);
        Assert.assertNull(blockingDeque.poll());
        String recv = blockingDeque.take();
        Assert.assertEquals("prefix.test-METRIC:10|ms", recv);
    }

    @Test
    public void recordExecutionTimeWithSampleRate() throws InterruptedException {
        client.recordExecutionTime(METRIC, 10, 2.1);
        Assert.assertNull(blockingDeque.poll());
        String recv = blockingDeque.take();
        Assert.assertEquals("prefix.test-METRIC:10|ms|@2.1", recv);
    }
}