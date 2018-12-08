package io.github.nov11.udp;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class PipelineHandlerTest {
    private static final Logger logger = LoggerFactory.getLogger(PipelineHandlerTest.class);
    private static EmbeddedChannel embeddedChannel;
    private final String number = "0123456789";
    private final String five = "12345";
    private final int packetSize = 10;
    private MetricAggregationHandler metricAggregationHandler;
    private IdleStateHandler idleStateHandler;

    @Before
    public void setUp() {
        idleStateHandler = new IdleStateHandler(0, 10, 0, TimeUnit.MILLISECONDS);

        metricAggregationHandler = new MetricAggregationHandler(packetSize);

        embeddedChannel = new EmbeddedChannel(idleStateHandler, metricAggregationHandler);
    }

    private void waitForIdleHandler() throws InterruptedException {
        logger.info("runScheduledPendingTasks");
        long ret = embeddedChannel.runScheduledPendingTasks();
        long milli = Duration.ofNanos(ret).toMillis();
        logger.info("next job: {}", milli);
        Thread.sleep(10);
        logger.info("runScheduledPendingTasks");
        embeddedChannel.runScheduledPendingTasks();
    }

    @Test
    public void writeMsgLargerThanPacketSizeTest() throws InterruptedException {
        Assert.assertFalse(embeddedChannel.writeOutbound(five));
        Assert.assertEquals(5, metricAggregationHandler.remainMsgLength());
        Assert.assertEquals(0, embeddedChannel.outboundMessages().size());
        Assert.assertNull(embeddedChannel.readOutbound());

        waitForIdleHandler();

        Assert.assertEquals(five, embeddedChannel.readOutbound());
        Assert.assertEquals(0, embeddedChannel.outboundMessages().size());
        Assert.assertEquals(0, metricAggregationHandler.remainMsgLength());
    }

    @Test
    public void writeTest() throws InterruptedException {
        Assert.assertFalse(embeddedChannel.writeOutbound(five));
        Assert.assertTrue(embeddedChannel.writeOutbound(five));
        Assert.assertEquals(five, embeddedChannel.readOutbound());
        Assert.assertEquals(0, embeddedChannel.outboundMessages().size());
        Assert.assertEquals(5, metricAggregationHandler.remainMsgLength());
        Assert.assertNull(embeddedChannel.readOutbound());

        waitForIdleHandler();

        Assert.assertEquals(1, embeddedChannel.outboundMessages().size());
        Assert.assertEquals(five, embeddedChannel.readOutbound());
        Assert.assertEquals(0, embeddedChannel.outboundMessages().size());
        Assert.assertEquals(0, metricAggregationHandler.remainMsgLength());

    }
}
