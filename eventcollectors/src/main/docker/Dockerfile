FROM openjdk:17-alpine

EXPOSE 8080

COPY build/libs/boudicca-eventcollectors.jar /opt/boudicca-eventcollectors/
WORKDIR /opt/boudicca-eventcollectors/
CMD ["java", "-jar", "boudicca-eventcollectors.jar"]