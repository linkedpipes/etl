FROM node:16.14.2-bullseye as lp-etl-build
ARG LP_ETL_BUILD=-DskipTests
RUN apt-get update && apt-get -y --no-install-recommends install openjdk-17-jdk
WORKDIR /opt/lp-etl/
COPY ./ ./
RUN ./mvnw install  $LP_ETL_BUILD -P "install-frontend"

FROM node:16.14.2-bullseye
ARG LP_ETL_USER=5987
RUN addgroup --gid $LP_ETL_USER "linkedpipes" \
    && useradd --gid "linkedpipes" --uid $LP_ETL_USER "linkedpipes"



COPY --from=lp-etl-build --chown=linkedpipes /opt/lp-etl/deploy/configuration.docker.properties /opt/lp-etl/configuration/configuration.properties
COPY --from=lp-etl-build --chown=linkedpipes /opt/lp-etl/deploy/frontend /opt/lp-etl/frontend


WORKDIR /opt/lp-etl/frontend
USER $LP_ETL_USER
EXPOSE 8080
ENV configFileLocation="/opt/lp-etl/configuration/configuration.properties"
CMD ["npm", "run", "start"]
