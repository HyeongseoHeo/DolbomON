FROM eclipse-temurin:21-jdk

WORKDIR /app

RUN apt-get update \
    && apt-get install -y ffmpeg \
    && rm -rf /var/lib/apt/lists/*

COPY . .

RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test

EXPOSE 8080

CMD ["java", "-jar", "build/libs/careon-0.0.1-SNAPSHOT.jar"]