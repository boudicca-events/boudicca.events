FROM docker.io/eclipse-temurin:21-alpine

EXPOSE 8080

COPY build/libs/search*.jar /opt/boudicca-search/search.jar
WORKDIR /opt/boudicca-search/
CMD ["java", "-Dserver.port=8080", "-jar", "search.jar"]