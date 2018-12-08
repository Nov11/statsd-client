package io.github.nov11.udp;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricAggregationHandler extends ChannelDuplexHandler {
    private static final Logger logger = LoggerFactory.getLogger(MetricAggregationHandler.class);
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
    public void write(ChannelHandlerContext ctx, Object input, ChannelPromise promise) {
        String msg = (String) input;
        //if msg is bigger than PACKET_SIZE, send buffered message first, then msg
        if (msg.length() >= PACKET_SIZE) {
            writeBufferedData(ctx);
            ctx.writeAndFlush(msg);

            return;
        }
        //if msg can be buffered, add it to the buffer then return
        if (builder.length() + 1 + msg.length() <= PACKET_SIZE) {
            if (builder.length() != 0) {
                builder.append('\n');
            }
            builder.append(msg);

            return;
        }

        //send buffered data and make msg the new buffered one
        writeBufferedData(ctx);
        builder.append(msg);
    }

    private void writeBufferedData(ChannelHandlerContext ctx) {
        if (builder.length() > 0) {
            ctx.writeAndFlush(builder.toString());
            builder.setLength(0);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (!(evt instanceof IdleStateEvent)) {
            return;
        }

        IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
        if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
            writeBufferedData(ctx);
        }
    }

    int remainMsgLength() {
        return builder.length();
    }
}
