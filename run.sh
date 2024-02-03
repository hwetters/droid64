#!/bin/sh
PROJDIR=$HOME/git/droid64
#JAVA_HOME=/opt/jdk1.8.0
#JAVA_HOME=/opt/jdk-11
#JAVA_HOME=/opt/jdk-17
JAVA_HOME=/opt/jdk-21
export JAVA_HOME

M2REPO=$HOME/.m2/repository

error()
{
  echo "$*" >&2
  zenity --error --text="$*" --title='DroiD64 Failed!'
  exit 1
}

cd "$PROJDIR" || error "Failed to move to dir $PROJDIR"
[ -f "pom.xml" ] || error "No pom.xml file"

VER=`sed -n 's/.*[<]version[>]\([0-9][.][-0-9a-z.A-Z]*\)[<][/]version[>].*/\1/p' pom.xml | head -1`
DROID64_JAR="$PROJDIR/target/droid64-$VER.jar"

if [ ! -f  "$DROID64_JAR" ]; then
  echo "[build]"
  mvn package -DskipTests || error "Failed to build"
fi

CP=`mvn dependency:build-classpath -Dmdep.includeScope=runtime -Dmdep.outputFile=/dev/stdout -q`
[ -n "$CP" ] || error "Failed to get dependencies"

if [ -r "$DROID64_JAR" ]; then
  [ -x "$JAVA_HOME/bin/java" ] || error "No JVM in $JAVA_HOME"
  echo "[run]"
  exec "$JAVA_HOME/bin/java" -Xmx4096m -classpath $DROID64_JAR:$CP droid64.DroiD64 $*
else
  error "Failed to find jar $DROID64_JAR"
fi
