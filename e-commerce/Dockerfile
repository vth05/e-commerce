# stage 1: build
# start with a Maven image that includes JDK 21
FROM maven:3.9.14-amazoncorretto-21 AS build

WORKDIR /app
# copy contents of src folder and pom.xml file to the app folder
# /app/pom.xml
COPY pom.xml .
# /app/src/contents of src folder
COPY src ./src

# build with Maven
RUN mvn package -DskipTests

# stage 2: create image
# start with Amazon Corretto JDK 21
FROM amazoncorretto:21.0.10

WORKDIR /app
# copy compiled jar from build stage and rename it to app.jar
COPY --from=build /app/target/*.jar app.jar

# run the application like java -jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]