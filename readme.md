#### StatsD client build on Netty



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
