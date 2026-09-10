# ============================================================
# STAGE 1: BUILD
# We use a JDK image here (not JRE) because compiling code needs
# the full toolchain (javac, etc.) — the JDK. This stage produces
# the .jar file, but it will NOT be part of our final image.
# ============================================================
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

# Copy only what's needed to resolve dependencies FIRST, before copying source code.
# Why order matters: Docker caches each instruction as a "layer". If pom.xml hasn't
# changed, Docker reuses the cached dependency-download layer instead of re-downloading
# everything from Maven Central every single build — this is the single biggest thing
# that makes repeated Docker builds fast instead of slow.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Downloads all dependencies into the image's local Maven repo cache.
# go-offline lets later commands run without network if pom.xml didn't change.
RUN ./mvnw dependency:go-offline -B

# NOW copy the actual source code (this changes far more often than pom.xml,
# so it's placed AFTER dependency resolution to maximize cache reuse).
COPY src ./src

# Compile and package into a jar, skipping tests here (tests should run in CI,
# not block/duplicate work inside every image build).
RUN ./mvnw clean package -DskipTests

# ============================================================
# STAGE 2: RUNTIME
# This is the actual image that ships. Using a JRE (not JDK) image means
# no compiler, no build tools — just what's needed to RUN a compiled jar.
# Smaller image = faster pulls, smaller attack surface.
# ============================================================
FROM eclipse-temurin:25-jre

WORKDIR /app

# Copy ONLY the built jar from the "build" stage above — none of the source code,
# Maven cache, or build tools make it into this final image. This is the core benefit
# of a multi-stage build: a lean production image built by a fat build environment.
COPY --from=build /app/target/*.jar app.jar

# Documents that this container listens on 8081 (matches application.yml).
# NOTE: EXPOSE is documentation/metadata only — it does not actually publish the port.
# That happens at `docker run -p` time, which we'll do next.
EXPOSE 8081

# ENTRYPOINT defines the fixed command that always runs when the container starts.
# (We use ENTRYPOINT over CMD here because this container's only job is "run this jar" —
# CMD is better suited when you want the default to be easily overridden at `docker run` time.)
ENTRYPOINT ["java", "-jar", "app.jar"]