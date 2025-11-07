FROM eclipse-temurin:21-jdk-jammy

# ffmpeg 설치 (비디오 썸네일 생성용)
RUN apt-get update && \
    apt-get install -y ffmpeg && \
    rm -rf /var/lib/apt/lists/*

EXPOSE 8080

ADD ./build/libs/whale-api-server-0.0.1-SNAPSHOT.jar app.jar

ENV JAVA_OPTS="-XX:InitialRAMPercentage=50.0 -XX:MaxRAMPercentage=50.0 -XX:MetaspaceSize=64m -XX:MaxMetaspaceSize=256m -XX:InitialCodeCacheSize=16m -XX:ReservedCodeCacheSize=64m"

ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
