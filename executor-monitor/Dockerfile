FROM openjdk:17.0.1-jdk-slim-bullseye as lp-etl-build
ARG LP_ETL_BUILD=-DskipTests

WORKDIR /opt/lp-etl/
COPY ./ ./
RUN ./mvnw install  $LP_ETL_BUILD -P "install-executor-monitor"

FROM openjdk:17.0.1-slim-bullseye
ARG LP_ETL_USER=5987
RUN addgroup --gid $LP_ETL_USER "linkedpipes" \
    && useradd --gid "linkedpipes" --uid $LP_ETL_USER "linkedpipes"
RUN mkdir -p /data/lp-etl/executor \
    && mkdir -p /data/lp-etl/logs \
    && chown -R $LP_ETL_USER:$LP_ETL_USER /data/lp-etl
COPY --from=lp-etl-build --chown=linkedpipes /opt/lp-etl/deploy/configuration.docker.properties /opt/lp-etl/configuration/configuration.properties
COPY --from=lp-etl-build --chown=linkedpipes /opt/lp-etl/deploy/executor-monitor /opt/lp-etl/executor-monitor

WORKDIR /opt/lp-etl/executor-monitor
USER $LP_ETL_USER
EXPOSE 8081
CMD ["java", "-DconfigFileLocation=/opt/lp-etl/configuration/configuration.properties", "-jar" ,"./executor-monitor.jar"]
