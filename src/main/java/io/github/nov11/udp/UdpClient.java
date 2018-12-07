package io.github.nov11.udp;

import io.github.nov11.StatsDClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class UdpClient implements StatsDClient {
    private static final Logger logger = LoggerFactory.getLogger(UdpClient.class);
    private final EventLoopGroup worker;
    private final Channel channel;

    public UdpClient(String host, int port) {
        worker = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(worker)
                .channel(NioDatagramChannel.class)
                .handler(new ChannelInitializer<DatagramChannel>() {
                    @Override
                    protected void initChannel(DatagramChannel ch) throws Exception {
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

    public void count(String metric, long delta) {

    }

    public void increment(String metric, long delta) {

    }

    public void gauge(String metric, long value) {

    }

    public void time(String metric, long time) {

    }

    public void send(String s){
        try {
            ByteBuf byteBuf = channel.alloc().buffer();
            byteBuf.writeCharSequence(s, StandardCharsets.UTF_8);
            channel.writeAndFlush(byteBuf).sync();
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
