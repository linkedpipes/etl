FROM openjdk:12-jdk-alpine as lp-etl-build
ARG LP_ETL_BUILD_BRANCH=master
ARG LP_ETL_BUILD_JAVA_TEST=-DskipTests
RUN apk add --no-cache maven git
WORKDIR /opt/lp/etl
RUN git clone --single-branch --branch $LP_ETL_BUILD_BRANCH https://github.com/linkedpipes/etl.git .
RUN mvn install $LP_ETL_BUILD_JAVA_TEST -P "install-executor,install-executor-monitor,install-storage,install-plugins"

FROM openjdk:12-jdk-alpine
LABEL component="executor"
WORKDIR /opt/lp/etl
COPY --from=lp-etl-build /opt/lp/etl/deploy/executor /opt/lp/etl/executor
COPY --from=lp-etl-build /opt/lp/etl/deploy/osgi /opt/lp/etl/osgi
COPY --from=lp-etl-build /opt/lp/etl/deploy/configuration.docker.properties /data/lp/etl/configuration/configuration.properties
VOLUME ["/data/lp/etl/executor", "/data/lp/etl/logs","/data/lp/elt/configuration"]
EXPOSE 8085
CMD ["java", "-DconfigFileLocation=/data/lp/etl/configuration/configuration.properties", "-jar" ,"./executor/executor.jar"]

