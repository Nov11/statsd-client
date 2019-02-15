package io.github.nov11;

import java.util.concurrent.ThreadLocalRandom;

/**
 * very much alike when comparing this with timgroupsratsdclient
 * but this supports simple random sampling on count metrics
 * which means this client ignores
 */
public class TimGroupSimpleRandomSamplingClient extends BaseClientImpl {
    final double samplingRatio;

    public TimGroupSimpleRandomSamplingClient(String prefix, MetricSender sender, double samplingRatio) {
        super(sender);
        if (Double.compare(samplingRatio, 1.0) >= 0) {
            throw new IllegalArgumentException("invalid samplingRatio: must be lower than 1.0");
        }
        if (prefix == null || prefix.equals("")) {
            this.prefix = "";
        } else {
            this.prefix = prefix + '.';
        }
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
        //explicitly ignore 'sampleRate'
        if (ThreadLocalRandom.current().nextDouble() < samplingRatio) {
            super.count(aspect, delta, samplingRatio);
        }
    }
}
