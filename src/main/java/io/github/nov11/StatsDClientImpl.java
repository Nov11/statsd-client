package io.github.nov11;

import java.text.DecimalFormat;

public class StatsDClientImpl implements StatsDClient {

    private final String prefix;
    private final MetricSender sender;

    StatsDClientImpl(String prefix, MetricSender sender) {
        if (prefix == null || "".equals(prefix)) {
            prefix = "";
        } else {
            prefix += ".";
        }
        this.prefix = prefix;
        this.sender = sender;
    }

    @Override
    public void count(String metric, long value) {
        sender.send(buildMessage(metric, value, 'c'));
    }

    @Override
    public void increment(String metric, long value) {
        count(metric, value);
    }

    @Override
    public void set(String metric, long value) {
        sender.send(buildMessage(metric, value, 's'));
    }

    //example:
    //gaugor:333
    //gaugor:-10|g
    //gaugor:+4|g
    @Override
    public void gauge(String metric, long value) {
        gauge(metric, value, false);
    }

    @Override
    public void gauge(String metric, long value, boolean delta) {
        if (!delta) {
            StringBuilder builder = new StringBuilder();
            if (value < 0) {
                builder.append(buildMessage(metric, 0, 'g')).append('\n');
            }
            builder.append(buildMessage(metric, value, 'g'));
            sender.send(builder.toString());
            return;
        }
        sender.send(buildGaugeDeltaMessage(metric, value));
    }

    //gorets:1|c|@0.1
    @Override
    public void sampling(String metric, long value, double delta) {
        String msg = buildSamplingMessage(metric, value, "c", delta);
        sender.send(msg);
    }

    //glork:320|ms|@0.1
    @Override
    public void time(String metric, long time, double rate) {
        String msg = buildSamplingMessage(metric, time, "ms", rate);
        sender.send(msg);
    }

    private String buildMessage(String metric, long value, char type) {
        StringBuilder builder = new StringBuilder(prefix);
        builder.append(metric);
        builder.append(':');
        builder.append(value);
        builder.append('|');
        builder.append(type);
        return builder.toString();
    }

    private String buildGaugeDeltaMessage(String metric, long value) {
        StringBuilder builder = new StringBuilder(prefix);
        builder.append(metric);
        builder.append(':');
        builder.append(value > 0 ? '+' : '-');
        builder.append(value);
        builder.append("|g");
        return builder.toString();
    }

    private String buildSamplingMessage(String metric, long value, String type, double sampleRate) {
        DecimalFormat decimalFormat = new DecimalFormat("0.#");
        decimalFormat.setGroupingUsed(false);
        decimalFormat.setMaximumFractionDigits(15);
        String rate = decimalFormat.format(sampleRate);

        StringBuilder builder = new StringBuilder(prefix);
        builder.append(metric);
        builder.append(':');
        builder.append(value);
        builder.append('|');
        builder.append(type);
        builder.append("|@");
        builder.append(rate);

        return builder.toString();
    }

    @Override
    public void shutdown() {
        sender.shutdown();
    }
}
