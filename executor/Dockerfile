FROM openjdk:17.0.1-jdk-slim-bullseye as lp-etl-build
ARG LP_ETL_BUILD=-DskipTests

WORKDIR /opt/lp-etl/
COPY ./ ./
RUN ./mvnw install  $LP_ETL_BUILD -P "install-executor,install-plugins"

FROM openjdk:17.0.1-slim-bullseye
ARG LP_ETL_USER=5987
RUN addgroup --gid $LP_ETL_USER "linkedpipes" \
    && useradd --gid "linkedpipes" --uid $LP_ETL_USER "linkedpipes"
RUN mkdir -p /data/lp-etl/executor \
    && mkdir -p /data/lp-etl/logs \
    && chown -R $LP_ETL_USER:$LP_ETL_USER /data/lp-etl
COPY --from=lp-etl-build --chown=linkedpipes /opt/lp-etl/deploy/configuration.docker.properties /opt/lp-etl/configuration/configuration.properties
COPY --from=lp-etl-build --chown=linkedpipes /opt/lp-etl/deploy/executor /opt/lp-etl/executor
COPY --from=lp-etl-build --chown=linkedpipes /opt/lp-etl/deploy/jars /opt/lp-etl/components
COPY --from=lp-etl-build --chown=linkedpipes /opt/lp-etl/deploy/osgi /opt/lp-etl/osgi

WORKDIR /opt/lp-etl
USER $LP_ETL_USER
EXPOSE 8085
CMD ["java", "-DconfigFileLocation=/opt/lp-etl/configuration/configuration.properties", "-jar" ,"./executor/executor.jar"]
