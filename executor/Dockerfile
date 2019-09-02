FROM openjdk:12-jdk-alpine as lp-etl-build
RUN apk add --no-cache maven git
WORKDIR /opt/lp/etl
ARG BRANCH=master
RUN git clone --single-branch --branch BRANCH https://github.com/linkedpipes/etl.git .
RUN mvn install -P install-all

FROM openjdk:12-jdk-alpine
LABEL component="executor"
ENV LP.ETL.DOMAIN="http://localhost:8080"
WORKDIR /opt/lp/etl
COPY --from=lp-etl-build /opt/lp/etl/deploy/executor /opt/lp/etl/executor
COPY --from=lp-etl-build /opt/lp/etl/deploy/configuration.docker.properties /data/lp/etl/
EXPOSE 8085
VOLUME ["/data/lp/etl/"]
CMD ["java", "-DconfigFileLocation=/data/lp/etl/configuration.properties", "-jar" ,"./executor/executor.jar"]
