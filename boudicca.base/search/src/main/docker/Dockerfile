FROM docker.io/eclipse-temurin:17-alpine

EXPOSE 8080

COPY build/libs/search.jar /opt/boudicca-search/
WORKDIR /opt/boudicca-search/
CMD ["java", "-Dserver.port=8080", "-jar", "search.jar"]