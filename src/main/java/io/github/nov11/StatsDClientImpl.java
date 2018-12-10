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
        String FORMAT_COUNT = "%s:%d|c";
        sender.send(buildMessage(FORMAT_COUNT, metric, value));
    }

    @Override
    public void increment(String metric, long value) {
        count(metric, value);
    }

    @Override
    public void set(String metric, long value) {
        String FORMAT_SET = "%s:%d|s";
        sender.send(buildMessage(FORMAT_SET, metric, value));
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
        String FORMAT_GAUGE = delta ? "%s:+%d|g" : "%s:%d|g";
        sender.send(buildMessage(FORMAT_GAUGE, metric, value));
    }

    //gorets:1|c|@0.1
    @Override
    public void sampling(String metric, long value, double delta) {
        String FORMAT_SAMPLING = "%s:%d|c|@%s";
        sender.send(buildSamplingMessage(FORMAT_SAMPLING, metric, value, delta));
    }

    //glork:320|ms|@0.1
    @Override
    public void time(String metric, long time, double rate) {
        String FORMAT_TIME = "%s:%d|ms|@%s";
        sender.send(buildSamplingMessage(FORMAT_TIME, metric, time, rate));
    }

    private String buildMessage(String format, String metric, long value) {
        return String.format(format, prefix + metric, value);
    }

    private String buildSamplingMessage(String format, String metric, long value, double sampleRate) {
        DecimalFormat decimalFormat = new DecimalFormat("0.#");
        decimalFormat.setGroupingUsed(false);
        decimalFormat.setMaximumFractionDigits(15);
        return String.format(format, prefix + metric, value, decimalFormat.format(sampleRate));
    }

    @Override
    public void shutdown() {
        sender.shutdown();
    }
}
