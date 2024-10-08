FROM eclipse-temurin:21-noble as lp-etl-build
ARG LP_ETL_BUILD=""

WORKDIR /opt/lp-etl/
COPY ./ ./
# Copy Docker specific configuration file as we need it for install-plugins.
RUN cp ./deploy/configuration.docker.properties ./deploy/configuration.properties.sample
RUN ./mvnw install  $LP_ETL_BUILD -P "install-storage,install-plugins"

FROM eclipse-temurin:21-noble
ARG LP_ETL_USER=5987
RUN groupadd --gid $LP_ETL_USER "linkedpipes" \
    && useradd --gid "linkedpipes" --uid $LP_ETL_USER "linkedpipes"
RUN mkdir -p /data/lp-etl/storage \
    && mkdir -p /data/lp-etl/logs \
    && chown -R $LP_ETL_USER:$LP_ETL_USER /data/lp-etl
COPY --from=lp-etl-build --chown=linkedpipes /opt/lp-etl/deploy/configuration.docker.properties /opt/lp-etl/configuration/configuration.properties
COPY --from=lp-etl-build --chown=linkedpipes /opt/lp-etl/deploy/storage /opt/lp-etl/storage
COPY --from=lp-etl-build --chown=linkedpipes /opt/lp-etl/deploy/jars /opt/lp-etl/components

WORKDIR /opt/lp-etl/storage
USER $LP_ETL_USER
EXPOSE 8083
CMD ["java", "-DconfigFileLocation=/opt/lp-etl/configuration/configuration.properties","-jar" ,"./storage.jar"]
