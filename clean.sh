#!/bin/bash
set -e

APP="$(echo -e 'setns x=http://maven.apache.org/POM/4.0.0\ncat /x:project/x:artifactId/text()' | xmllint --shell pom.xml | grep -v /)"

docker stop $(docker ps -aq --filter "name=$APP")
docker rm $(docker ps -aq --filter "name=$APP")
docker rmi -f $(docker images $APP -aq)