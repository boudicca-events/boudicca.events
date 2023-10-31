FROM docker.io/eclipse-temurin:17-alpine

EXPOSE 8080

COPY build/libs/eventdb.jar /opt/boudicca-eventdb/
WORKDIR /opt/boudicca-eventdb/
CMD ["java", "-Dserver.port=8080", "-jar", "eventdb.jar"]