#!/bin/bash

CP="$CLASSPATH"
for i in lib/jars/*.jar 
do   
    CP="$i:$CP"
done

for i in lib/jars/external/*.jar 
do   
    CP="$i:$CP"
done

export CP

OPTS=
#OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,address=8003,suspend=n,server=y $OPTS"
export OPTS


java $OPTS -cp "$CP" org.redoubt.application.Application
