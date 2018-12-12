package io.github.nov11;

import com.timgroup.statsd.ConvenienceMethodProvidingStatsDClient;

import java.text.NumberFormat;
import java.util.Locale;

public class TimGroupStatsDClient extends ConvenienceMethodProvidingStatsDClient {

    private final String prefix;
    private final MetricSender sender;

    TimGroupStatsDClient(String prefix, MetricSender sender) {
        if (prefix == null || prefix.equals("")) {
            this.prefix = "";
        } else {
            this.prefix = prefix + '.';
        }
        this.sender = sender;
    }

    @Override
    public void stop() {
        sender.shutdown();
    }

    /**
     * these implementations are copied from com.timgroup.statsd.NonBlockingStatsDClient
     */

    /**
     * Adjusts the specified counter by a given delta.
     *
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * @param aspect     the name of the counter to adjust
     * @param delta      the amount to adjust the counter by
     * @param sampleRate the sampling rate being employed. For example, a rate of 0.1 would tell StatsD that this counter is being sent
     *                   sampled every 1/10th of the time.
     */
    @Override
    public void count(String aspect, long delta, double sampleRate) {
        send(messageFor(aspect, Long.toString(delta), "c", sampleRate));
    }

    /**
     * Records the latest fixed value for the specified named gauge.
     *
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * @param aspect the name of the gauge
     * @param value  the new reading of the gauge
     */
    @Override
    public void recordGaugeValue(String aspect, long value) {
        recordGaugeCommon(aspect, Long.toString(value), value < 0, false);
    }

    @Override
    public void recordGaugeValue(String aspect, double value) {
        recordGaugeCommon(aspect, stringValueOf(value), value < 0, false);
    }

    @Override
    public void recordGaugeDelta(String aspect, long value) {
        recordGaugeCommon(aspect, Long.toString(value), value < 0, true);
    }

    @Override
    public void recordGaugeDelta(String aspect, double value) {
        recordGaugeCommon(aspect, stringValueOf(value), value < 0, true);
    }

    private void recordGaugeCommon(String aspect, String value, boolean negative, boolean delta) {
        final StringBuilder message = new StringBuilder();
        if (!delta && negative) {
            message.append(messageFor(aspect, "0", "g")).append('\n');
        }
        message.append(messageFor(aspect, (delta && !negative) ? ("+" + value) : value, "g"));
        send(message.toString());
    }

    /**
     * StatsD supports counting unique occurrences of events between flushes, Call this method to records an occurrence
     * of the specified named event.
     *
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * @param aspect    the name of the set
     * @param eventName the value to be added to the set
     */
    @Override
    public void recordSetEvent(String aspect, String eventName) {
        send(messageFor(aspect, eventName, "s"));
    }

    /**
     * Records an execution time in milliseconds for the specified named operation.
     *
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * @param aspect   the name of the timed operation
     * @param timeInMs the time in milliseconds
     */
    @Override
    public void recordExecutionTime(String aspect, long timeInMs, double sampleRate) {
        send(messageFor(aspect, Long.toString(timeInMs), "ms", sampleRate));
    }

    private String messageFor(String aspect, String value, String type) {
        return messageFor(aspect, value, type, 1.0);
    }

    private String messageFor(String aspect, String value, String type, double sampleRate) {
        final String message = prefix + aspect + ':' + value + '|' + type;
        return (sampleRate == 1.0)
                ? message
                : (message + "|@" + stringValueOf(sampleRate));
    }

    private void send(final String message) {
        sender.send(message);
    }

    private String stringValueOf(double value) {
        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        formatter.setGroupingUsed(false);
        formatter.setMaximumFractionDigits(19);
        return formatter.format(value);
    }
}
