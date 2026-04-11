FROM gradle:jdk21-alpine AS builder

ARG APP_HOME=/home/gradle/src
ARG SERVICE=service
ARG CI_ARGS='--no-daemon --stacktrace'
ARG GIT_COMMIT=e85c61a

WORKDIR $APP_HOME
# don't copy the settings.gradle file because it includes all submodules and gradle will require all of them to be present in order to run any task.
COPY --chown=gradle:gradle ["./gradlew", "build.gradle", "lombok.config", "./"]
COPY --chown=gradle:gradle ["/gradle/", "./gradle/"]
COPY --chown=gradle:gradle ["/$SERVICE/build.gradle", "./$SERVICE/build.gradle"]
RUN sed -i 's/\r$//' ./gradlew && chmod +x ./gradlew
RUN echo "include ':$SERVICE'" >> settings.gradle

RUN ./gradlew :$SERVICE:dependencies $CI_ARGS || true

COPY --chown=gradle:gradle ["/$SERVICE/src/main/", "./$SERVICE/src/main/"]

RUN ./gradlew :$SERVICE:bootJar -x test $CI_ARGS -PgitCommit=$GIT_COMMIT

WORKDIR $APP_HOME/layers
RUN java -Djarmode=tools -jar $APP_HOME/$SERVICE/build/libs/*.jar extract --layers --launcher --destination extracted

FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /home/gradle/src/layers/extracted/dependencies/ ./
COPY --from=builder /home/gradle/src/layers/extracted/spring-boot-loader/ ./
COPY --from=builder /home/gradle/src/layers/extracted/snapshot-dependencies/ ./
COPY --from=builder /home/gradle/src/layers/extracted/application/ ./

EXPOSE 8080

#CMD ["tail","-f","/dev/null"] // for debugging
CMD ["java","org.springframework.boot.loader.launch.JarLauncher"]