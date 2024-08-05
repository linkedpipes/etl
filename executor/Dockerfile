FROM eclipse-temurin:21-noble as lp-etl-build
ARG LP_ETL_BUILD=""

WORKDIR /opt/lp-etl/
COPY ./ ./
# Copy Docker specific configuration file as we need it for install-plugins.
RUN cp ./deploy/configuration.docker.properties ./deploy/configuration.properties.sample
RUN ./mvnw install  $LP_ETL_BUILD -P "install-executor,install-plugins"

FROM eclipse-temurin:21-noble
ARG LP_ETL_USER=5987
RUN groupadd --gid $LP_ETL_USER "linkedpipes" \
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
