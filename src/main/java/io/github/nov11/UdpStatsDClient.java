package io.github.nov11;

import io.github.nov11.udp.UdpClient;
import io.github.nov11.udp.UdpPipelineClient;

public interface UdpStatsDClient {
    static StatsDClient buildNormalClient(String prefix, String host, int port) {
        return new StatsDClientImpl(prefix, new UdpClient(host, port));
    }

    static StatsDClient buildPipelineClient(String prefix, String host, int port) {
        return new StatsDClientImpl(prefix, new UdpPipelineClient(host, port));
    }

    static com.timgroup.statsd.StatsDClient buildTimGroupStatsDClient(String prefix, String host, int port) {
        return new TimGroupStatsDClient(prefix, new UdpPipelineClient(host, port));
    }
}
