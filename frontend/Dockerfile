FROM node:12-alpine as lp-etl-build
ARG LP_ETL_BUILD_BRANCH=master
ARG LP_ETL_BUILD_JAVA_TEST=-DskipTests
LABEL lp_project=etl
RUN apk add --no-cache maven git
WORKDIR /opt/lp/etl
RUN git clone --single-branch --branch $LP_ETL_BUILD_BRANCH https://github.com/linkedpipes/etl.git .
RUN mvn install $LP_ETL_BUILD_JAVA_TEST -P "install-frontend"

FROM node:12-alpine
ARG LP_ETL_USER=5987
LABEL lp_project=etl
LABEL component=frontend
RUN addgroup --gid "$LP_ETL_USER" "linkedpipes" && adduser -D --home /opt/lp --ingroup "linkedpipes" --uid "$LP_ETL_USER" "etl"
WORKDIR /opt/lp/etl/frontend
COPY --from=lp-etl-build /opt/lp/etl/deploy/configuration.docker.properties /data/lp/etl/configuration/configuration.properties
COPY --from=lp-etl-build /opt/lp/etl/deploy/frontend /opt/lp/etl/frontend
USER $LP_ETL_USER
VOLUME ["/data/lp/elt/configuration"]
EXPOSE 8080
ENV configFileLocation="/data/lp/etl/configuration/configuration.properties"
CMD ["npm", "run", "start"]
