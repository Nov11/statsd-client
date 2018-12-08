package io.github.nov11.udp;

import io.github.nov11.MetricSender;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class UdpPipelineClient extends UdpClient{
    public UdpPipelineClient(String host, int port) {
        super(host, port, Collections.singletonList(new MetricAggregationHandler()));
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
