# syntax=docker/dockerfile:1

FROM maven:3.9.2-amazoncorretto-17 AS build
COPY Backend/src /usr/src/app/src
COPY Backend/pom.xml /usr/src/app
RUN mvn -f /usr/src/app/pom.xml clean package

FROM amazoncorretto:17-alpine3.16-full
ARG JAR_FILE=/usr/src/app/target/*.jar
COPY --from=build ${JAR_FILE} /usr/app/Ree6-Webinterface.jar
EXPOSE 8080/tcp
ENTRYPOINT ["java","-jar","/usr/app/Ree6-Webinterface.jar"]