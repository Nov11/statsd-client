package io.github.nov11.udp;

import io.github.nov11.MetricSender;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.string.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class UdpClient implements MetricSender {
    private static final Logger logger = LoggerFactory.getLogger(UdpClient.class);
    private final EventLoopGroup worker;
    private final Channel channel;

    public UdpClient(String host, int port) {
        this(host, port, Collections.singletonList(new StringEncoder(StandardCharsets.UTF_8)));
    }

    UdpClient(String host, int port, List<ChannelHandler> handlerList) {
        worker = new NioEventLoopGroup(1);
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(worker)
                .channel(NioDatagramChannel.class)
                .handler(new ChannelInitializer<DatagramChannel>() {
                    @Override
                    protected void initChannel(DatagramChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        for (ChannelHandler handler : handlerList) {
                            pipeline.addLast(handler);
                        }
                    }
                });

        Channel tmp = null;
        try {
            ChannelFuture future = bootstrap.connect(host, port).sync();
            tmp = future.channel();
        } catch (InterruptedException e) {
            logger.error("connecting to {}:{} ex:", host, port, e);
        }
        if (tmp == null) {
            System.exit(1);
        }
        channel = tmp;
    }

    public void send(String s) {
        try {
            channel.writeAndFlush(s).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() {
        worker.shutdownGracefully();
    }

    public static void main(String[] args) {
        UdpClient udpClient = new UdpClient("127.0.0.1", 9000);
        udpClient.send("hello");
        udpClient.send("world");
    }
}
