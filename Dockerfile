FROM ${CI_DEPENDENCY_PROXY_GROUP_IMAGE_PREFIX_SLASH}eclipse-temurin:25-jdk

WORKDIR /backend

EXPOSE 8080

# copy the jar from the build/libs dir into /backend
COPY build/libs/lab02-spring-0.0.1-SNAPSHOT.jar /backend/app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]