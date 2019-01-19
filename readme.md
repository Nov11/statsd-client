#### StatsD client based on Netty
[![Build Status](https://travis-ci.org/Nov11/statsd-client.svg?branch=master)](https://travis-ci.org/Nov11/statsd-client)
[![Coverage Status](https://coveralls.io/repos/github/Nov11/statsd-client/badge.svg?branch=master)](https://coveralls.io/github/Nov11/statsd-client?branch=master)

#### features
* Supports Udp connection only
* Automatically buffers & builds largest packet as possible to lower packet loss rate
* Compatible with Timgroup's StatsD client interface 

##### pick one client:
- buildNormalClient: udp client on top of Netty which send individual metric in one packet
- buildPipelineClient: udp client combines adjacent packets and send them together. buffer flushes itself if not more packets arrived in 1 second.

##### maven dependency:
```
<dependency>
    <groupId>io.github.nov11</groupId>
    <artifactId>statsd-client</artifactId>
    <version>1.0.4</version>
</dependency>

```

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

travis(net.core.rmem_max = 212992, net.core.rmem_default = 212992):
```
Running io.github.nov11.benchmark.UdpClientBenchmarkTest
[INFO  18:10:51.746 [thread:main] i.g.n.b.UdpClientBenchmarkTest:125] - called NonBlockingStatsDClient.count 1000000 times, cost: 7445 ms. blocking before gathering status 
[INFO  18:10:54.322 [thread:main] i.g.n.benchmark.UdpBenchmarkServer:51] - Stats record in server:
time:
	[received first packet]: 2018-12-09 18:10:44.319
	[processed last packet]: 2018-12-09 18:10:51.838
time consumption: 	7519 ms
packet received:	14162
packet rate:		1.9 packet / million second
metric received:	14162
metric valid:		14162
metric rate:		1.9 metric / million second
data received:		311564 bytes
data rate:		41.4 byte / million second
 
[INFO  18:10:57.376 [thread:main] i.g.n.b.UdpClientBenchmarkTest:125] - called NonBlockingStatsDClient.count 100000 times, cost: 946 ms. blocking before gathering status 
[INFO  18:11:00.441 [thread:main] i.g.n.benchmark.UdpBenchmarkServer:51] - Stats record in server:
time:
	[received first packet]: 2018-12-09 18:10:56.441
	[processed last packet]: 2018-12-09 18:10:57.495
time consumption: 	1054 ms
packet received:	2271
packet rate:		2.2 packet / million second
metric received:	2271
metric valid:		2271
metric rate:		2.2 metric / million second
data received:		49962 bytes
data rate:		47.4 byte / million second
 
[INFO  18:11:02.615 [thread:main] i.g.n.b.UdpClientBenchmarkTest:125] - called NonBlockingStatsDClient.count 10000 times, cost: 61 ms. blocking before gathering status 
[INFO  18:11:06.569 [thread:main] i.g.n.benchmark.UdpBenchmarkServer:51] - Stats record in server:
time:
	[received first packet]: 2018-12-09 18:11:02.574
	[processed last packet]: 2018-12-09 18:11:02.697
time consumption: 	123 ms
packet received:	355
packet rate:		2.9 packet / million second
metric received:	355
metric valid:		355
metric rate:		2.9 metric / million second
data received:		7810 bytes
data rate:		63.5 byte / million second
 
[INFO  18:11:14.971 [thread:main] i.g.n.b.UdpClientBenchmarkTest:96] - called udpPipelineClient.count 1000000 times, cost: 6286 ms. blocking before gathering status 
[INFO  18:11:36.693 [thread:main] i.g.n.benchmark.UdpBenchmarkServer:51] - Stats record in server:
time:
	[received first packet]: 2018-12-09 18:11:08.689
	[processed last packet]: 2018-12-09 18:11:33.423
time consumption: 	24734 ms
packet received:	45455
packet rate:		1.8 packet / million second
metric received:	1000000
metric valid:		1000000
metric rate:		40.4 metric / million second
data received:		22954545 bytes
data rate:		928.1 byte / million second
 
[INFO  18:11:41.361 [thread:main] i.g.n.b.UdpClientBenchmarkTest:96] - called udpPipelineClient.count 100000 times, cost: 452 ms. blocking before gathering status 
[INFO  18:11:46.925 [thread:main] i.g.n.benchmark.UdpBenchmarkServer:51] - Stats record in server:
time:
	[received first packet]: 2018-12-09 18:11:40.940
	[processed last packet]: 2018-12-09 18:11:44.097
time consumption: 	3157 ms
packet received:	4546
packet rate:		1.4 packet / million second
metric received:	100000
metric valid:		100000
metric rate:		31.7 metric / million second
data received:		2295454 bytes
data rate:		727.1 byte / million second
 
[INFO  18:11:51.205 [thread:main] i.g.n.b.UdpClientBenchmarkTest:96] - called udpPipelineClient.count 10000 times, cost: 58 ms. blocking before gathering status 
[INFO  18:11:55.149 [thread:main] i.g.n.benchmark.UdpBenchmarkServer:51] - Stats record in server:
time:
	[received first packet]: 2018-12-09 18:11:51.154
	[processed last packet]: 2018-12-09 18:11:52.400
time consumption: 	1246 ms
packet received:	455
packet rate:		0.4 packet / million second
metric received:	10000
metric valid:		10000
metric rate:		8 metric / million second
data received:		229545 bytes
data rate:		184.2 byte / million second
 
[INFO  18:12:03.006 [thread:main] i.g.n.b.UdpClientBenchmarkTest:110] - called udpNettyClient.count 1000000 times, cost: 3640 ms. blocking before gathering status 
[INFO  18:16:47.665 [thread:main] i.g.n.benchmark.UdpBenchmarkServer:51] - Stats record in server:
time:
	[received first packet]: 2018-12-09 18:11:59.387
	[processed last packet]: 2018-12-09 18:16:44.522
time consumption: 	285135 ms
packet received:	999903
packet rate:		3.5 packet / million second
metric received:	999903
metric valid:		999903
metric rate:		3.5 metric / million second
data received:		21997866 bytes
data rate:		77.1 byte / million second
 
[INFO  18:16:52.203 [thread:main] i.g.n.b.UdpClientBenchmarkTest:110] - called udpNettyClient.count 100000 times, cost: 322 ms. blocking before gathering status 
[INFO  18:17:23.886 [thread:main] i.g.n.benchmark.UdpBenchmarkServer:51] - Stats record in server:
time:
	[received first packet]: 2018-12-09 18:16:51.891
	[processed last packet]: 2018-12-09 18:17:20.313
time consumption: 	28422 ms
packet received:	100000
packet rate:		3.5 packet / million second
metric received:	100000
metric valid:		100000
metric rate:		3.5 metric / million second
data received:		2200000 bytes
data rate:		77.4 byte / million second
 
[INFO  18:17:28.129 [thread:main] i.g.n.b.UdpClientBenchmarkTest:110] - called udpNettyClient.count 10000 times, cost: 27 ms. blocking before gathering status 
[INFO  18:17:34.105 [thread:main] i.g.n.benchmark.UdpBenchmarkServer:51] - Stats record in server:
time:
	[received first packet]: 2018-12-09 18:17:28.111
	[processed last packet]: 2018-12-09 18:17:31.127
time consumption: 	3016 ms
packet received:	10000
packet rate:		3.3 packet / million second
metric received:	10000
metric valid:		10000
metric rate:		3.3 metric / million second
data received:		220000 bytes
data rate:		72.9 byte / million second
 
Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 414.057 sec
Results :
Tests run: 32, Failures: 0, Errors: 0, Skipped: 0
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 07:12 min
[INFO] Finished at: 2018-12-09T18:17:38Z
[INFO] Final Memory: 19M/254M
[INFO] ------------------------------------------------------------------------
The command "mvn test -B" exited with 0.

```



on my laptop given:
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
----------

#### udp packet loss
Start UdpBenchmarkServer on one machine. And run the three client on another machine within the same LAN with 10000 count request.
- Receive buffer on server is set to 25MB.
- Scheduler task in 20 sec instead of 2 to guarantee that server drained incoming packets.
- On server side machine, reported packet counts do not deviate from that of wireshark.

- nonblocking statsD client:
```
[INFO  22:10:49.113 [thread:main] i.g.n.benchmark.UdpBenchmarkServer:51] - Stats record in server:
time:
	[received first packet]: 2018-12-10 22:10:29.101
	[processed last packet]: 2018-12-10 22:10:29.296
time consumption: 	195 ms
packet received:	1551
packet rate:		8 packet / million second
metric received:	1551
metric valid:		1551
metric rate:		8 metric / million second
data received:		34122 bytes
data rate:		175 byte / million second


Process finished with exit code 0

```

- udp netty client
```
[INFO  22:13:55.767 [thread:main] i.g.n.benchmark.UdpBenchmarkServer:51] - Stats record in server:
time:
	[received first packet]: 2018-12-10 22:13:35.755
	[processed last packet]: 2018-12-10 22:13:35.838
time consumption: 	83 ms
packet received:	1549
packet rate:		18.7 packet / million second
metric received:	1549
metric valid:		1549
metric rate:		18.7 metric / million second
data received:		34078 bytes
data rate:		410.6 byte / million second


Process finished with exit code 0
```

- udp pipeline client

```
[INFO  22:16:02.144 [thread:main] i.g.n.benchmark.UdpBenchmarkServer:51] - Stats record in server:
time:
	[received first packet]: 2018-12-10 22:15:42.132
	[processed last packet]: 2018-12-10 22:15:42.841
time consumption: 	709 ms
packet received:	449
packet rate:		0.6 packet / million second
metric received:	9868
metric valid:		9868
metric rate:		13.9 metric / million second
data received:		226515 bytes
data rate:		319.5 byte / million second


Process finished with exit code 0
```

#### todo:
1.discard packets if the event loop is overwhelmed.
2.add statistics to provide performance insight.
3.optional client side load balancing.
