package io.github.nov11;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class DumbUdpServer {
    private final BlockingDeque<String> blockingDeque = new LinkedBlockingDeque<>();
    private final EventLoopGroup worker = new NioEventLoopGroup(1);

    public DumbUdpServer(int port) throws InterruptedException {

        Bootstrap bootstrap = new Bootstrap();

        bootstrap.group(worker)
                .channel(NioDatagramChannel.class)
                .handler(new ChannelInitializer<DatagramChannel>() {
                    @Override
                    protected void initChannel(DatagramChannel ch) throws Exception {
                        ch.pipeline().addLast(new Handler(blockingDeque));
                    }
                });
        bootstrap.bind(port).sync();
    }

    static class Handler extends SimpleChannelInboundHandler<DatagramPacket> {
        private BlockingDeque<String> blockingDeque;

        public Handler(BlockingDeque<String> blockingDeque) {
            this.blockingDeque = blockingDeque;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
            ByteBuf byteBuf = msg.content();
            String s = byteBuf.toString(StandardCharsets.UTF_8);
            blockingDeque.add(s);
        }
    }

    public void shutdown() {
        worker.shutdownGracefully();
    }

    public BlockingDeque<String> getMessage(){
        return blockingDeque;
    }
}
