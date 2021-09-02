FROM openjdk:8u141-jre
ADD stock-app/target/stock-app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]