#!/bin/sh
CPATH=lib/@batchJarName@.jar:@classpath@
exec java -classpath $CPATH -Xmx1024m droid64.DroiD64 $*
