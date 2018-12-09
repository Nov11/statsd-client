#### StatsD client based on Netty
[![Build Status](https://travis-ci.org/Nov11/statsd-client.svg?branch=master)](https://travis-ci.org/Nov11/statsd-client)
[![Coverage Status](https://coveralls.io/repos/github/Nov11/statsd-client/badge.svg?branch=master)](https://coveralls.io/github/Nov11/statsd-client?branch=master)


#### test by using nc to establish a localhost udp server

nc -lu port will receive the first package only.

use `nc -kluvw 100 localhost 9000` instead

actually `w` option is ignored. so it should be `nc -kluv localhost port`.
```
-k      Forces nc to stay listening for another connection after its current connection is completed.  It is an error to use this option without the -l option.  When used together with the -u
             option, the server socket is not connected and it can receive UDP datagrams from multiple hosts.

-l      Used to specify that nc should listen for an incoming connection rather than initiate a connection to a remote host.  The destination and port to listen on can be specified either as
             non-optional arguments, or with options -s and -p respctively.  It is an error to use -l in conjunction with the -z option.  Additionally, any timeouts specified with the -w option are
             ignored.
             
-u      Use UDP instead of the default option of TCP.  For UNIX-domain sockets, use a datagram socket instead of a stream socket.  If a UNIX-domain socket is used, a temporary receiving socket
             is created in /tmp unless the -s flag is given             

-v      Have nc give more verbose output.

-w timeout
             Connections which cannot be established or are idle timeout after timeout seconds.  The -w flag has no effect on the -l option, i.e. nc will listen forever for a connection, with or
             without the -w flag.  The default is no timeout.
```
#### udp socket defaults & packet loss
default values on Ubuntu 18.04.1 LTS:
net.core.rmem_max = 212992
net.core.rmem_default = 212992

when running UdpClientBenchmarkTest sending 100000 messages, some packets are dropped.

after changing above configs to 25MB(26214400), no packet is lost.
 
#### performance

given:
net.core.rmem_max = 26214400
net.core.rmem_default = 26214400

udp with pipe line
```
[INFO  20:30:16.437 [thread:main] i.g.n.b.UdpClientBenchmarkTest:35] - called client.count 1000000 times 
[INFO  20:30:19.443 [thread:main] i.g.n.benchmark.UdpBenchmarkServer:50] - Stats:
time:
	[received first packet]: 2018-12-09 20:30:14.770
	[processed last packet]: 2018-12-09 20:30:17.441
time consumption: 2671 ms
packet received:	1000000
packet valid:		1000000
packet rate:		374.4 packets / million second 
```

timgroup nonblocking stasd client

```
[INFO  20:32:15.637 [thread:main] i.g.n.b.UdpClientBenchmarkTest:47] - called client.count 1000000 times 
[INFO  20:32:18.643 [thread:main] i.g.n.benchmark.UdpBenchmarkServer:50] - Stats:
time:
	[received first packet]: 2018-12-09 20:32:13.995
	[processed last packet]: 2018-12-09 20:32:16.362
time consumption: 2367 ms
packet received:	742491
packet valid:		742491
packet rate:		313.7 packets / million second 
```