FROM docker.io/eclipse-temurin:21-alpine

EXPOSE 8080

COPY build/libs/publisher-event-html*.jar /opt/boudicca-html/publisher-event-html.jar
WORKDIR /opt/boudicca-html/
CMD ["java", "-Dserver.port=8080", "-jar", "publisher-event-html.jar"]