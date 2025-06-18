#!/bin/bash
set -e

APP="$(echo -e 'setns x=http://maven.apache.org/POM/4.0.0\ncat /x:project/x:artifactId/text()' | xmllint --shell pom.xml | grep -v /)"
VERSION="$(echo -e 'setns x=http://maven.apache.org/POM/4.0.0\ncat /x:project/x:version/text()' | xmllint --shell pom.xml | grep -v /)"

if [ "$1" != "" ]; then
  VERSION="$1"
fi

# Clean up
if [ ! -z $(docker ps -q --filter "name=$APP") ]; then
  echo "Stopping: $(docker stop $(docker ps -q --filter "name=$APP"))"
  echo "Removing: $(docker rm $(docker ps -aq --filter "name=$APP"))"
fi

# Run detached, follow logs
echo; echo "Hang on, starting engines..."; echo
CONTAINER=$(docker run --name $APP -p 8100:8100 -td $APP:$VERSION)