package io.github.nov11;

import com.timgroup.statsd.StatsDClient;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class TimGroupSimpleRandomSamplingClientTest {

    @Test(expected = IllegalArgumentException.class)
    public void samplingRateGreaterThanOne(){
        StatsDClient statsDClient = new TimGroupSimpleRandomSamplingClient(null, null, 1.2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void samplingRateEqualToOne(){
        StatsDClient statsDClient = new TimGroupSimpleRandomSamplingClient(null, null, 1.0);
    }

    @Test
    public void samplingRateConstruction(){
        TimGroupSimpleRandomSamplingClient statsDClient = new TimGroupSimpleRandomSamplingClient(null, null, 0.9);
        Assert.assertEquals(statsDClient.samplingRatio, 0.9, 0.0001);
    }


}