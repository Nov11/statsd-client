package io.github.nov11.benchmark;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UdpBenchmarkServer {
    private static final Logger logger = LoggerFactory.getLogger(UdpBenchmarkServer.class);
    private final static DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private final static DecimalFormat decimalFormat = new DecimalFormat("0.#");
    private final EventLoopGroup worker = new NioEventLoopGroup(1);
    private final TestHandler handler;

    public UdpBenchmarkServer(int port) throws InterruptedException {

        Bootstrap bootstrap = new Bootstrap();
        handler = new TestHandler();

        bootstrap.group(worker)
                .channel(NioDatagramChannel.class)
                .handler(new ChannelInitializer<DatagramChannel>() {
                    @Override
                    protected void initChannel(DatagramChannel ch) throws Exception {
                        ch.pipeline().addLast(handler);
                    }
                });
        bootstrap.bind(port).sync();
    }

    public void shutdown() {
        worker.shutdownGracefully();
    }

    public void printStats() {
        logger.info(handler.getStats());
    }

    static class TestHandler extends SimpleChannelInboundHandler<DatagramPacket> {
        private long received = 0;
        private long valid = 0;
        private long firstReceived = 0;
        private long lastProcessed = 0;

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
            if (firstReceived == 0) {
                firstReceived = System.currentTimeMillis();
            }
            ByteBuf byteBuf = msg.content();
            String s = byteBuf.toString(StandardCharsets.UTF_8);
            String[] metrics = s.split("\n");
            for (String item : metrics) {
                if (item.equals("prefix.test-METRIC:1|c")) {
                    valid++;
                }
            }
            received += metrics.length;
            lastProcessed = System.currentTimeMillis();
        }

        String getStats() {
            StringBuilder builder = new StringBuilder("Stats:\n");
            builder.append("time:\n\t[received first packet]: ").append(formatter.format(new Date(firstReceived)))
                    .append("\n")
                    .append("\t[processed last packet]: ").append(formatter.format(new Date(lastProcessed)))
                    .append("\n")
                    .append("time consumption: ").append(lastProcessed - firstReceived).append(" ms")
                    .append("\n");
            builder.append("packet received:\t").append(received).append("\n");
            builder.append("packet valid:\t\t").append(valid).append("\n");
            builder.append("packet rate:\t\t")
                    .append(decimalFormat.format((received * 1.0 / ((lastProcessed - firstReceived) * 1.0 / 1))))
                    .append(" packets / million second");
            return builder.toString();
        }
    }
}
