package io.github.nov11.udp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public class MetricAggregationHandler extends ChannelOutboundHandlerAdapter {
    private static final int MAX_PACKET_SIZE = 512;
    private final int PACKET_SIZE;
    private StringBuilder builder = new StringBuilder();

    MetricAggregationHandler() {
        PACKET_SIZE = MAX_PACKET_SIZE;
    }

    MetricAggregationHandler(int packetSize) {
        PACKET_SIZE = packetSize;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object input, ChannelPromise promise){
        String msg = (String) input;
        //if msg is bigger than PACKET_SIZE, send buffered message first, then msg
        if (msg.length() >= PACKET_SIZE) {
            if (builder.length() > 0) {
                ctx.writeAndFlush(builder.toString());
                builder = new StringBuilder();
            }
            ctx.writeAndFlush(msg);
            return;
        }
        //if msg can be buffered, add it to the buffer then return
        if (builder.length() + 1 + msg.length() <= PACKET_SIZE) {
            if (builder.length() == 0) {
                builder.append(msg);
            } else {
                builder.append('\n').append(msg);
            }
            return;
        }

        //send buffered data and make msg the new buffered one
        if (builder.length() > 0) {
            ctx.writeAndFlush(builder.toString());
        }
        builder = new StringBuilder(msg);
    }

    int remainMsgLength() {
        return builder.length();
    }
}
