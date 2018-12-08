package io.github.nov11;

import io.github.nov11.udp.UdpClient;
import io.github.nov11.udp.UdpPipelineClient;

public interface UdpStatsDClient {
    static StatsDClient build(String prefix, String host, int port) {
        return new StatsDClientImpl(prefix, new UdpClient(host, port));
    }

    static StatsDClient buildClientSupportPipeline(String prefix, String host, int port) {
        return new StatsDClientImpl(prefix, new UdpPipelineClient(host, port));
    }
}
