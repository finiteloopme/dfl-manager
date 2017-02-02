#!/bin/bash
export CLASSPATH=target/dflmngr.jar:target/dependency/*
java -classpath $CLASSPATH net.dflmngr.handlers.AflFixtureLoaderHandler
