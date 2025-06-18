# ----- Build stage
FROM maven:3.8.3-openjdk-17-slim AS build
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app  
RUN mvn -f /usr/src/app/pom.xml clean package


# ----- Final stage
FROM tomcat:jre17-temurin-jammy

# Add user 'spring' and copy keymaster.jar into home folder
RUN groupadd -r -g 3000 spring && useradd -m -d /home/spring/ -s /bin/bash -u 2000 -r -g spring spring
COPY --from=build /usr/src/app/target/*.jar /home/spring/keymaster.jar
WORKDIR /home/spring
USER spring

ENTRYPOINT ["java","-jar","keymaster.jar"]
EXPOSE 8100