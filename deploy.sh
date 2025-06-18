#!/bin/bash
set -e

APP="$(echo -e 'setns x=http://maven.apache.org/POM/4.0.0\ncat /x:project/x:artifactId/text()' | xmllint --shell pom.xml | grep -v /)"
VERSION="$(echo -e 'setns x=http://maven.apache.org/POM/4.0.0\ncat /x:project/x:version/text()' | xmllint --shell pom.xml | grep -v /)"

# Clean up
if [ ! -z $(docker ps -q --filter "name=$APP") ]; then
  echo "Stopping: $(docker stop $(docker ps -q --filter "name=$APP"))"
  echo "Removing: $(docker rm $(docker ps -aq --filter "name=$APP"))"
fi
if [ ! -z $(docker images $APP -aq) ]; then
  docker rmi -f $(docker images $APP -aq)
fi

if [ "$1" == "x" ]; then
  # Do nothing
else
  docker build -t $APP:$VERSION .
fi