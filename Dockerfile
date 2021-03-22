FROM openjdk:11-jdk-slim as builder
WORKDIR /server
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
RUN ./gradlew dependencies --refresh-dependencies
COPY . .
COPY ./src/main/resources/application.properties.docker ./src/main/resources/application.properties
RUN ./gradlew assemble


FROM openjdk:11-jre-slim
WORKDIR /server
RUN useradd -m spring
RUN chown -R spring:spring /server
COPY --chmod=spring:spring --from=builder /server/build/libs/server.jar .
USER spring
ENTRYPOINT ["java", "-jar", "server.jar"]