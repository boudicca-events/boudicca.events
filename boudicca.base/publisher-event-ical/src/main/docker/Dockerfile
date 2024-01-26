FROM docker.io/eclipse-temurin:21-alpine

EXPOSE 8080

COPY build/libs/publisher-event-ical*.jar /opt/boudicca-ical/publisher-event-ical.jar
WORKDIR /opt/boudicca-ical/
CMD ["java", "-Dserver.port=8080", "-jar", "publisher-event-ical.jar"]