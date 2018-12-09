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

performance(UdpClientBenchmarkTest):

- calling `client.count` 1,000,000 times
```
[INFO  23:54:25.347 [thread:main] i.g.n.b.UdpClientBenchmarkTest:56] - called udpNettyClient.count 1000000 times. wait 20000 ms before gathering status 
[INFO  23:54:45.351 [thread:main] i.g.n.benchmark.UdpBenchmarkServer:50] - Stats record in server:
time:
	[received first packet]: 2018-12-09 23:54:23.851
	[processed last packet]: 2018-12-09 23:54:45.350
time consumption: 	21499 ms
packet received:	182506
packet rate:		8.5 packet / million second
metric received:	182517
metric valid:		182517
metric rate:		8.5 metric / million second
data received:		4015396 bytes
data rate:			186.8 byte / million second
 
[INFO  23:54:48.774 [thread:main] i.g.n.b.UdpClientBenchmarkTest:74] - called NonBlockingStatsDClient.count 1000000 times. wait 20000 ms before gathering status 
[INFO  23:55:08.775 [thread:main] i.g.n.benchmark.UdpBenchmarkServer:50] - Stats record in server:
time:
	[received first packet]: 2018-12-09 23:54:47.472
	[processed last packet]: 2018-12-09 23:54:51.696
time consumption: 	4224 ms
packet received:	42668
packet rate:		10.1 packet / million second
metric received:	42668
metric valid:		42668
metric rate:		10.1 metric / million second
data received:		938696 bytes
data rate:			222.2 byte / million second
 
[INFO  23:55:12.237 [thread:main] i.g.n.b.UdpClientBenchmarkTest:44] - called udpPipelineClient.count 1000000 times. wait 20000 ms before gathering status 
[INFO  23:55:32.237 [thread:main] i.g.n.benchmark.UdpBenchmarkServer:50] - Stats record in server:
time:
	[received first packet]: 2018-12-09 23:55:10.884
	[processed last packet]: 2018-12-09 23:55:20.376
time consumption: 	9492 ms
packet received:	45455
packet rate:		4.8 packet / million second
metric received:	1000000
metric valid:		1000000
metric rate:		105.4 metric / million second
data received:		22954545 bytes
data rate:			2418.3 byte / million second
 

Process finished with exit code 0

```



- calling count 100,000 times 

```
[INFO  23:50:39.556 [thread:main] i.g.n.b.UdpClientBenchmarkTest:44] - called udpPipelineClient.count 100000 times. wait 20000 ms before gathering status 
[INFO  23:50:59.564 [thread:main] i.g.n.benchmark.UdpBenchmarkServer:50] - Stats record in server:
time:
	[received first packet]: 2018-12-09 23:50:39.146
	[processed last packet]: 2018-12-09 23:50:41.371
time consumption: 	2225 ms
packet received:	4546
packet rate:		2 packet / million second
metric received:	100000
metric valid:		100000
metric rate:		44.9 metric / million second
data received:		2295454 bytes
data rate:			1031.7 byte / million second
 
[INFO  23:51:01.833 [thread:main] i.g.n.b.UdpClientBenchmarkTest:56] - called udpNettyClient.count 100000 times. wait 20000 ms before gathering status 
[INFO  23:51:21.834 [thread:main] i.g.n.benchmark.UdpBenchmarkServer:50] - Stats record in server:
time:
	[received first packet]: 2018-12-09 23:51:01.687
	[processed last packet]: 2018-12-09 23:51:13.255
time consumption: 	11568 ms
packet received:	100000
packet rate:		8.6 packet / million second
metric received:	100000
metric valid:		100000
metric rate:		8.6 metric / million second
data received:		2200000 bytes
data rate:			190.2 byte / million second
 
[INFO  23:51:24.147 [thread:main] i.g.n.b.UdpClientBenchmarkTest:74] - called NonBlockingStatsDClient.count 100000 times. wait 20000 ms before gathering status 
[INFO  23:51:44.148 [thread:main] i.g.n.benchmark.UdpBenchmarkServer:50] - Stats record in server:
time:
	[received first packet]: 2018-12-09 23:51:23.957
	[processed last packet]: 2018-12-09 23:51:27.048
time consumption: 	3091 ms
packet received:	34134
packet rate:		11 packet / million second
metric received:	34134
metric valid:		34134
metric rate:		11 metric / million second
data received:		750948 bytes
data rate:			242.9 byte / million second
 

Process finished with exit code 0


```

- call count 10,000 times. 
- three client are all correct. 
- nonblocking client from timgroup performance the best in packet rate & byte rate.
```
[INFO  23:52:40.537 [thread:main] i.g.n.b.UdpClientBenchmarkTest:44] - called udpPipelineClient.count 10000 times. wait 20000 ms before gathering status 
[INFO  23:53:00.554 [thread:main] i.g.n.benchmark.UdpBenchmarkServer:50] - Stats record in server:
time:
	[received first packet]: 2018-12-09 23:52:40.420
	[processed last packet]: 2018-12-09 23:52:41.683
time consumption: 	1263 ms
packet received:	455
packet rate:		0.4 packet / million second
metric received:	10000
metric valid:		10000
metric rate:		7.9 metric / million second
data received:		229545 bytes
data rate:			181.7 byte / million second
 
[INFO  23:53:02.747 [thread:main] i.g.n.b.UdpClientBenchmarkTest:56] - called udpNettyClient.count 10000 times. wait 20000 ms before gathering status 
[INFO  23:53:22.747 [thread:main] i.g.n.benchmark.UdpBenchmarkServer:50] - Stats record in server:
time:
	[received first packet]: 2018-12-09 23:53:02.682
	[processed last packet]: 2018-12-09 23:53:04.036
time consumption: 	1354 ms
packet received:	10000
packet rate:		7.4 packet / million second
metric received:	10000
metric valid:		10000
metric rate:		7.4 metric / million second
data received:		220000 bytes
data rate:			162.5 byte / million second
 
[INFO  23:53:24.924 [thread:main] i.g.n.b.UdpClientBenchmarkTest:74] - called NonBlockingStatsDClient.count 10000 times. wait 20000 ms before gathering status 
[INFO  23:53:44.924 [thread:main] i.g.n.benchmark.UdpBenchmarkServer:50] - Stats record in server:
time:
	[received first packet]: 2018-12-09 23:53:24.872
	[processed last packet]: 2018-12-09 23:53:25.760
time consumption: 	888 ms
packet received:	10000
packet rate:		11.3 packet / million second
metric received:	10000
metric valid:		10000
metric rate:		11.3 metric / million second
data received:		220000 bytes
data rate:			247.7 byte / million second
 

Process finished with exit code 0


```

---------
old tests

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