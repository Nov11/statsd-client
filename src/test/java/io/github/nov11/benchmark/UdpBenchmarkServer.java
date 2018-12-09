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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class UdpBenchmarkServer {
    private static final Logger logger = LoggerFactory.getLogger(UdpBenchmarkServer.class);
    private final static DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private final static DecimalFormat decimalFormat = new DecimalFormat("0.#");
    private final EventLoopGroup worker = new NioEventLoopGroup(1);
    private final TestHandler handler;
    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    UdpBenchmarkServer(int port) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        handler = new TestHandler(countDownLatch);

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

    void printStats() {
        logger.info(handler.getStats());
    }

    void drainPackets() throws InterruptedException {
        countDownLatch.await();
    }

    void shutdown() throws InterruptedException {
        worker.shutdownGracefully().sync();
    }

    static class TestHandler extends SimpleChannelInboundHandler<DatagramPacket> {
        private long receivedMetric = 0;
        private long validMetric = 0;
        private long receivedPacket = 0;
        private long firstReceived = 0;
        private long lastProcessed = 0;
        private long contentOfByte = 0;
        private final CountDownLatch countDownLatch;
        private AtomicInteger messageOfLastSecond = new AtomicInteger(0);
        private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        TestHandler(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
            receivedPacket++;
            messageOfLastSecond.getAndAdd(1);
            if (firstReceived == 0) {
                scheduledExecutorService.schedule(this::releaseIfIdleForOneSecond, 2, TimeUnit.SECONDS);
                firstReceived = System.currentTimeMillis();
            }
            ByteBuf byteBuf = msg.content();
            contentOfByte += byteBuf.readableBytes();
            String s = byteBuf.toString(StandardCharsets.UTF_8);
            String[] metrics = s.split("\n");
            for (String item : metrics) {
                if (item.equals("prefix.test-METRIC:1|c")) {
                    validMetric++;
                }
            }
            receivedMetric += metrics.length;
            lastProcessed = System.currentTimeMillis();
        }

        void releaseIfIdleForOneSecond() {
            if (messageOfLastSecond.get() == 0) {
                countDownLatch.countDown();
                scheduledExecutorService.shutdownNow();
            } else {
                scheduledExecutorService.schedule(this::releaseIfIdleForOneSecond, 2, TimeUnit.SECONDS);
                messageOfLastSecond.set(0);
            }
        }

        String getStats() {
            double time = ((lastProcessed - firstReceived) * 1.0 / 1);

            StringBuilder builder = new StringBuilder("Stats record in server:\n");
            builder.append("time:\n\t[received first packet]: ").append(formatter.format(new Date(firstReceived)))
                    .append("\n")
                    .append("\t[processed last packet]: ").append(formatter.format(new Date(lastProcessed)))
                    .append("\n")
                    .append("time consumption: \t").append(lastProcessed - firstReceived).append(" ms")
                    .append("\n");
            builder.append("packet received:\t").append(receivedPacket).append("\n");
            builder.append("packet rate:\t\t").append(decimalFormat.format(receivedPacket / time))
                    .append(" packet / million second")
                    .append("\n");
            builder.append("metric received:\t").append(receivedMetric).append("\n");
            builder.append("metric valid:\t\t").append(validMetric).append("\n");
            builder.append("metric rate:\t\t")
                    .append(decimalFormat.format((validMetric * 1.0 / time)))
                    .append(" metric / million second")
                    .append("\n");
            builder.append("data received:\t\t").append(contentOfByte).append(" bytes")
                    .append("\n");
            builder.append("data rate:\t\t")
                    .append(decimalFormat.format((1.0 * contentOfByte) / time))
                    .append(" byte / million second")
                    .append("\n");
            return builder.toString();
        }
    }
}
