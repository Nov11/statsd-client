package io.github.nov11;

import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import io.github.nov11.udp.UdpPipelineClient;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class PacketLossTest {
    private static Logger logger = LoggerFactory.getLogger(PacketLossTest.class);

    //test  直接copy的
    private static String makePrefix() {
        String name = "mac";
        logger.info("metric prefix:{}", name);
        return name;
    }

    private static int getPort() {
        return Integer.valueOf(System.getenv("PORT"));
    }

    private static String getHost() {
        return System.getenv("HOST");
    }

    private static int count() {
        return Integer.valueOf(System.getenv("COUNT"));
    }

    private static void work() {
        NonBlockingStatsDClient nonBlockingStatsDClient =
                new NonBlockingStatsDClient(makePrefix(), getHost(), getPort());
        StatsDClient statsDClient = new TimGroupStatsDClient(makePrefix(), new UdpPipelineClient(getHost(), getPort()));

        int count = count();

        HashedWheelTimer hashedWheelTimer = new HashedWheelTimer();
        hashedWheelTimer.newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                logger.info("task start");
                for (int i = 0; i < count; i++) {
                    statsDClient.count("test1_aggregate", 1, 0.5);
                    statsDClient.count("test2_aggregate", 1, 0.5);
                    nonBlockingStatsDClient.count("test3_single", 1, 0.5);
                    nonBlockingStatsDClient.count("test4_single", 1, 0.5);
                }
//                for (int i = 0; i < 10000; i++) {
//                }
                logger.info("task done");

                timeout.timer().newTimeout(this, 1, TimeUnit.SECONDS);
            }
        }, 1, TimeUnit.SECONDS);
    }

    public static void main(String[] args) {
        work();
    }
}
