FROM openjdk:17-alpine

COPY build/libs/boudicca-eventcollectors.jar /opt/boudicca-eventcollectors/
WORKDIR /opt/boudicca-eventcollectors/
EXPOSE 8083
CMD ["java", "-jar", "boudicca-eventcollectors.jar"]