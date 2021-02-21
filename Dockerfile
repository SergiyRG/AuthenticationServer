FROM openjdk:15-alpine
WORKDIR /app
COPY /target/AuthenticationServer-1.0.jar /app

CMD ["java", "-jar", "/app/AuthenticationServer-1.0.jar"]