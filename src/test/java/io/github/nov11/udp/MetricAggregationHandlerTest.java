package io.github.nov11.udp;

import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MetricAggregationHandlerTest {
    private static EmbeddedChannel embeddedChannel;
    private final String number = "0123456789";
    private final String five = "12345";
    private final int packetSize = 10;
    private MetricAggregationHandler handler;

    @Before
    public void setUp() {
        handler = new MetricAggregationHandler(packetSize);
        embeddedChannel = new EmbeddedChannel(handler);
    }

    @Test
    public void writeMsgLargerThanPacketSizeTest() {
        Assert.assertTrue(embeddedChannel.writeOutbound(number));
        Assert.assertTrue(embeddedChannel.writeOutbound(number + number));
        Assert.assertEquals(number, embeddedChannel.readOutbound());
        Assert.assertEquals(number + number, embeddedChannel.readOutbound());
        Assert.assertEquals(0, embeddedChannel.outboundMessages().size());
        Assert.assertEquals(0, handler.remainMsgLength());
    }

    @Test
    public void writeTest() {
        Assert.assertFalse(embeddedChannel.writeOutbound(five));
        Assert.assertTrue(embeddedChannel.writeOutbound(five));
        Assert.assertEquals(five, embeddedChannel.readOutbound());
        Assert.assertEquals(0, embeddedChannel.outboundMessages().size());
        Assert.assertEquals(5, handler.remainMsgLength());
    }
}