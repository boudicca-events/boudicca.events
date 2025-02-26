FROM docker.io/eclipse-temurin:21-alpine

COPY build/libs/eventcollectors*.jar /opt/boudicca-eventcollectors/boudicca-eventcollectors.jar
WORKDIR /opt/boudicca-eventcollectors/
EXPOSE 8083
CMD ["java", "-jar", "boudicca-eventcollectors.jar"]