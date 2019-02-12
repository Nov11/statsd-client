package io.github.nov11;

import java.util.concurrent.ThreadLocalRandom;

/**
 * very much alike when comparing this with timgroupsratsdclient
 * but this supports simple random sampling on count metrics
 * which means this client ignores
 */
public class TimGroupSimpleRandomSamplingClient extends BaseClientImpl {
    private final double samplingRatio;
    private final boolean simpleRandomSamplingEnabled;

    public TimGroupSimpleRandomSamplingClient(String prefix, MetricSender sender, double samplingRatio) {
        this(prefix, sender, true, samplingRatio);
        if (Double.compare(samplingRatio, 1.0) >= 0) {
            throw new IllegalArgumentException("invalid samplingRatio: must be lower than 1.0");
        }
    }

    public TimGroupSimpleRandomSamplingClient(String prefix, MetricSender sender, boolean simpleRandomSamplingEnabled, double samplingRatio) {
        super(sender);
        if (prefix == null || prefix.equals("")) {
            this.prefix = "";
        } else {
            this.prefix = prefix + '.';
        }
        this.simpleRandomSamplingEnabled = simpleRandomSamplingEnabled;
        this.samplingRatio = samplingRatio;
    }

    /**
     * Adjusts the specified counter by a given delta.
     * <p>
     * when simpleRandomSamplingEnabled == true, this method will behave differently from timgroup's count method.
     * in that case, random sampling is made inside this function which means all count metrics will be sampled by ratio
     * given in constructor.
     * <p>
     * if simpleRandomSamplingEnabled == false, this is the same as timgroup's count method.
     *
     * @param aspect     the name of the counter to adjust
     * @param delta      the amount to adjust the counter by
     * @param sampleRate the sampling rate being employed. For example, a rate of 0.1 would tell StatsD that this counter is being sent
     *                   sampled every 1/10th of the time.
     */
    @Override
    public void count(String aspect, long delta, double sampleRate) {
        if (!simpleRandomSamplingEnabled) {
            super.count(aspect, delta, sampleRate);
            return;
        }
        //explicitly ignore 'sampleRate'
        sampleRate = 1.0;
        if (ThreadLocalRandom.current().nextDouble() < samplingRatio) {
            super.count(aspect, delta, samplingRatio);
        }
    }
}
