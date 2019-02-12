package io.github.nov11;

import com.timgroup.statsd.StatsDClient;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * this is a copy of ConvenienceMethodProvidingStatsDClient from tim group's statsd client
 * <p>
 * this class is used inside simplerandomsamplingclient.
 * sample ratio in count method is not support intentionally.
 * instead, sample ratio must be set as a constructor argument.
 */
public abstract class BaseClientImpl implements StatsDClient {
    private final MetricSender sender;
    protected String prefix;

    BaseClientImpl(MetricSender sender) {
        this.sender = sender;
    }


    @Override
    public final void count(String aspect, long delta) {
        count(aspect, delta, 1.0);
    }

    /**
     * Convenience method equivalent to {@link #count(String, long)} with a value of 1.
     */
    @Override
    public final void incrementCounter(String aspect) {
        count(aspect, 1);
    }

    /**
     * Convenience method equivalent to {@link #incrementCounter(String)}.
     */
    @Override
    public final void increment(String aspect) {
        incrementCounter(aspect);
    }

    /**
     * Convenience method equivalent to {@link #count(String, long)} with a value of -1.
     */
    @Override
    public final void decrementCounter(String aspect) {
        count(aspect, -1);
    }

    /**
     * Convenience method equivalent to {@link #decrementCounter(String)}.
     */
    @Override
    public final void decrement(String aspect) {
        decrementCounter(aspect);
    }

    /**
     * Convenience method equivalent to {@link #recordGaugeValue(String, long)}.
     */
    @Override
    public final void gauge(String aspect, long value) {
        recordGaugeValue(aspect, value);
    }

    /**
     * Convenience method equivalent to {@link #recordGaugeValue(String, double)}.
     */
    @Override
    public final void gauge(String aspect, double value) {
        recordGaugeValue(aspect, value);
    }

    /**
     * Convenience method equivalent to {@link #recordSetEvent(String, String)}.
     */
    @Override
    public final void set(String aspect, String eventName) {
        recordSetEvent(aspect, eventName);
    }

    /**
     * Convenience method equivalent to {@link #recordExecutionTime(String, long)}.
     */
    @Override
    public final void time(String aspect, long timeInMs) {
        recordExecutionTime(aspect, timeInMs);
    }

    @Override
    public final void recordExecutionTime(String aspect, long timeInMs) {
        recordExecutionTime(aspect, timeInMs, 1.0);
    }

    @Override
    public void recordExecutionTimeToNow(String aspect, long systemTimeMillisAtStart) {
        time(aspect, Math.max(0, System.currentTimeMillis() - systemTimeMillisAtStart));
    }

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

    @Override
    public void stop() {
        sender.shutdown();
    }
}