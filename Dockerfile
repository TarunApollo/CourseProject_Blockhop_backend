# GitLab exposes dependency proxy vars in CI, but Dockerfile FROM can only use
# values passed via a pre FROM ARG, so CI forwards this prefix with --build-arg
# This might not be needed but according to the Docker docs it is.
ARG CI_DEPENDENCY_PROXY_GROUP_IMAGE_PREFIX_SLASH=
FROM ${CI_DEPENDENCY_PROXY_GROUP_IMAGE_PREFIX_SLASH}eclipse-temurin:25-jdk

WORKDIR /backend

EXPOSE 8080

# copy the jar from the build/libs dir into /backend
COPY build/libs/lab02-spring-0.0.1-SNAPSHOT.jar /backend/app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
