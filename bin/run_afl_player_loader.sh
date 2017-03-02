#!/bin/bash
export CLASSPATH=/app/target/dflmngr.jar:/app/target/dependency/*
java -classpath $CLASSPATH -Dnetworkaddress.cache.ttl=20 -Dnetworkaddress.cache.negative.ttl=0 net.dflmngr.handlers.AflPlayerLoaderHandler
