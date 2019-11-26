FROM openjdk:13-jdk-alpine as lp-etl-build
ARG LP_ETL_BUILD_BRANCH=master
ARG LP_ETL_BUILD_JAVA_TEST=-DskipTests
LABEL lp_project=etl
RUN apk add --no-cache maven git
WORKDIR /opt/lp/etl
RUN git clone --single-branch --branch $LP_ETL_BUILD_BRANCH https://github.com/linkedpipes/etl.git .
RUN mvn install $LP_ETL_BUILD_JAVA_TEST -P "install-executor,install-executor-monitor,install-storage,install-plugins"

FROM openjdk:13-jdk-alpine
ARG LP_ETL_USER=5987
LABEL lp_project=etl
LABEL component=storage
RUN addgroup --gid "$LP_ETL_USER" "linkedpipes" \
    && adduser -D --home /opt/lp --ingroup "linkedpipes" --uid "$LP_ETL_USER" "etl"
WORKDIR /opt/lp/etl
COPY --from=lp-etl-build /opt/lp/etl/deploy/configuration.docker.properties /data/lp/etl/configuration/configuration.properties
COPY --from=lp-etl-build /opt/lp/etl/deploy/storage /opt/lp/etl/storage
COPY --from=lp-etl-build /opt/lp/etl/deploy/jars /opt/lp/etl/components
RUN mkdir -p /data/lp/etl/storage \
    && mkdir -p /data/lp/etl/logs \
    && chown -R $LP_ETL_USER:$LP_ETL_USER /data/lp/etl
VOLUME ["/data/lp/etl/storage", "/data/lp/etl/logs", "/data/lp/etl/configuration"]
USER $LP_ETL_USER
EXPOSE 8083
CMD ["java", "-DconfigFileLocation=/data/lp/etl/configuration/configuration.properties", "-jar" ,"./storage/storage.jar"]
