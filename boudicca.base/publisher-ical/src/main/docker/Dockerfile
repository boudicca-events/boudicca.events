FROM docker.io/openjdk:17-alpine

EXPOSE 8080

COPY build/libs/publisher-ical.jar /opt/boudicca-ical/
WORKDIR /opt/boudicca-ical/
CMD ["java", "-Dserver.port=8080", "-jar", "publisher-ical.jar"]