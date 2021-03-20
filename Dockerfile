
FROM openjdk:11
RUN addgroup spring && adduser --ingroup spring spring
USER spring:spring
ARG JAR_FILE=build/libs/*jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]