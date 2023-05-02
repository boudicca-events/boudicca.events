FROM openjdk:17-alpine

COPY build/libs/boudicca-eventcollectors.jar /opt/boudicca-eventcollectors/
WORKDIR /opt/boudicca-eventcollectors/
CMD ["java", "-jar", "boudicca-eventcollectors.jar"]