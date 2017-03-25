#!/bin/bash
export CLASSPATH=/app/target/dflmngr.jar:/app/target/dependency/*
java -classpath $CLASSPATH net.dflmngr.scheduler.generators.EndRoundJobGenerator
