FROM docker.io/eclipse-temurin:21-alpine

EXPOSE 8080

COPY build/libs/eventdb*.jar /opt/boudicca-eventdb/eventdb.jar
WORKDIR /opt/boudicca-eventdb/
CMD ["java", "-Dserver.port=8080", "-jar", "eventdb.jar"]