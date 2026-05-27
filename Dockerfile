# GitLab exposes dependency proxy vars in CI, but Dockerfile FROM can only use
# values passed via a pre FROM ARG, so CI forwards this prefix with --build-arg
# This might not be needed but according to the Docker docs it is.
ARG CI_DEPENDENCY_PROXY_GROUP_IMAGE_PREFIX_SLASH=
FROM ${CI_DEPENDENCY_PROXY_GROUP_IMAGE_PREFIX_SLASH}eclipse-temurin:25-jdk

WORKDIR /backend

RUN apt-get update \
    && apt-get install -y --no-install-recommends nodejs \
    && rm -rf /var/lib/apt/lists/*

EXPOSE 8080

# copy the bundled anti-cheat replay runtime into the image
COPY anticheat/replay.bundle.mjs /backend/anticheat/replay.bundle.mjs

# copy the jar from the build/libs dir into /backend
COPY build/libs/lab02-spring-0.0.1-SNAPSHOT.jar /backend/app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
