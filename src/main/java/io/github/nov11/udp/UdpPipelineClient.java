package io.github.nov11.udp;

import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class UdpPipelineClient extends UdpClient {
    public UdpPipelineClient(String host, int port) {
        super(host, port, Arrays.asList(
                new StringEncoder(StandardCharsets.UTF_8),
                new IdleStateHandler(0, 1, 0, TimeUnit.SECONDS),
                new MetricAggregationHandler()));
    }
}
