#!/bin/bash
PORT=8125 HOST=localhost COUNT=100 mvn  test-compile exec:java -Dexec.mainClass=io.github.nov11.PacketLossTest -Dexec.classpathScope="test"