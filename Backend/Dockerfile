# syntax=docker/dockerfile:1

FROM maven:3.9.9-amazoncorretto-17 AS build
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app
RUN mvn -f /usr/src/app/pom.xml clean package

FROM amazoncorretto:21-alpine3.16-full
ARG JAR_FILE=/usr/src/app/target/*.jar
COPY --from=build ${JAR_FILE} /usr/app/Ree6-Webinterface.jar
EXPOSE 8888/tcp
ENTRYPOINT ["java","-jar","/usr/app/Ree6-Webinterface.jar"]
