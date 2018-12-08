package io.github.nov11.udp;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class UdpPipelineClient extends UdpClient {
    private static final List<ChannelHandler> handlers = Arrays.asList(
            new StringEncoder(StandardCharsets.UTF_8),
            new IdleStateHandler(0, 1, 0, TimeUnit.SECONDS),
            new MetricAggregationHandler());

    public UdpPipelineClient(String host, int port) {
        super(host, port, handlers);
    }

    public static void main(String[] args) {
        UdpClient udpClient = new UdpPipelineClient("localhost", 1455);
        udpClient.send("1234567890");
        udpClient.send("1234567890");
        Scanner scanner = new Scanner(System.in);
        scanner.nextInt();
        udpClient.shutdown();
    }
}
