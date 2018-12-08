package io.github.nov11;

import io.github.nov11.udp.UdpClient;

public interface UdpStatsDClient {
    static StatsDClient build(String prefix, String host, int port) {
        return new StatsDClientImpl(prefix, new UdpClient(host, port));
    }
}
