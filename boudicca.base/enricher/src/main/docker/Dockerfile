FROM docker.io/eclipse-temurin:21-alpine

EXPOSE 8080

COPY build/libs/enricher.jar /opt/boudicca-enricher/
WORKDIR /opt/boudicca-enricher/
CMD ["java", "-Dserver.port=8080", "-jar", "enricher.jar"]