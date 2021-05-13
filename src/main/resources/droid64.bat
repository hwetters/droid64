@echo off
set CPATH=lib\@batchJarName@.jar;@classpath@
java -classpath %CPATH% droid64.DroiD64 -Xmx1024m %1
