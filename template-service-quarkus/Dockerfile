FROM quay.io/quarkus/ubi-quarkus-mandrel-builder-image:jdk-25 AS build

WORKDIR /app
COPY . .

RUN ./mvnw package -Dnative -DskipTests

FROM quay.io/quarkus/quarkus-micro-image:2.0
WORKDIR /work/
COPY --from=build /app/target/template-service-1.0.0-runner /work/application

EXPOSE 9090
USER 1001

ENTRYPOINT ["./application", "-Dquarkus.http.host=0.0.0.0", "-Dquarkus.http.port=9090"]
