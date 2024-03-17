FROM docker.io/eclipse-temurin:21-alpine

EXPOSE 8080

COPY build/libs/publisher-html-diabolical*.jar /opt/boudicca-html/publisher-html-diabolical.jar
WORKDIR /opt/boudicca-html/
CMD ["java", "-Dserver.port=8080", "-jar", "publisher-html-diabolical.jar"]