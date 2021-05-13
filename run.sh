#!/bin/sh
PROJDIR=$HOME/git/droid64
#JAVA_HOME=/opt/jdk1.8.0
#JAVA_HOME=/opt/jdk-9
#JAVA_HOME=/opt/jdk-10
#JAVA_HOME=/opt/jdk-11
#JAVA_HOME=/opt/jdk-12
#JAVA_HOME=/opt/jdk-13
#JAVA_HOME=/opt/jdk-14
JAVA_HOME=/opt/jdk-16
M2REPO=$HOME/.m2/repository

MYSQLCONNECTJ=$M2REPO/mysql/mysql-connector-java/8.0.22/mysql-connector-java-8.0.22.jar
PSQL=$M2REPO/org/postgresql/postgresql/42.2.18/postgresql-42.2.18.jar
H2JAR=$M2REPO/com/h2database/h2/1.4.200/h2-1.4.200.jar
JAKARTAAPIJAR=$M2REPO/jakarta/xml/bind/jakarta.xml.bind-api/2.3.3/jakarta.xml.bind-api-2.3.3.jar
JAKARTAJAR=$M2REPO/com/sun/activation/jakarta.activation/1.2.2/jakarta.activation-1.2.2.jar
JAXBJAR=$M2REPO/com/sun/xml/bind/jaxb-impl/2.3.3/jaxb-impl-2.3.3.jar

error()
{
  echo "$*" >&2
  exit 1
}

cd $PROJDIR || error "Failed to move to dir $PROJDIR"

VER=`sed -n 's/.*[<]version[>]\([0-9][.][-0-9a-z.A-Z]*\)[<][/]version[>].*/\1/p' pom.xml | head -1`
DROID64_JAR="$PROJDIR/target/droid64-$VER.jar"

if [ ! -f  "$DROID64_JAR" ]; then
  echo "[build]"
  mvn package -DskipTests || error "Failed to build"
fi

if [ -r "$DROID64_JAR" ]; then
  echo "[run]"
  set -x
  exec $JAVA_HOME/bin/java -Xmx4096m -classpath $DROID64_JAR:$MYSQLCONNECTJ:$PSQL:$H2JAR:$JAXBJAR:$JAKARTAJAR:$JAKARTAAPIJAR droid64.DroiD64 $*
else
  error "Failed to find jar $DROID64_JAR"
fi


