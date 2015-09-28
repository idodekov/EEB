#!/bin/bash
APPLICATION_PATH=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
CP="$CLASSPATH"
for i in $APPLICATION_PATH/lib/jars/*.jar 
do   
    CP="$i:$CP"
done

for i in $APPLICATION_PATH/lib/jars/external/*.jar 
do   
    CP="$i:$CP"
done

export CP

OPTS=
#OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,address=8003,suspend=n,server=y $OPTS"
export OPTS


java $OPTS -cp "$CP" org.redoubt.application.StopApplication > $APPLICATION_PATH/logs/shutdown.log 2>&1
