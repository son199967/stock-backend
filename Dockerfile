FROM openjdk:11.0.10
ADD stock-app/target/stock-app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]