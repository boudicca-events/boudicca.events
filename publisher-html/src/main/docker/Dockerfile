FROM openjdk:17-alpine

EXPOSE 8080

COPY build/libs/boudicca-html.jar /opt/boudicca-html/
WORKDIR /opt/boudicca-html/
CMD ["java", "-jar", "boudicca-html.jar"]