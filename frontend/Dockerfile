FROM node:12-alpine
LABEL component="frontend"
ARG LP_ETL_BUILD_BRANCH=master
RUN apk add --no-cache git
WORKDIR /opt/lp/etl
RUN git clone --single-branch --branch $LP_ETL_BUILD_BRANCH https://github.com/linkedpipes/etl.git .
WORKDIR /opt/lp/etl/frontend
RUN npm install && npm run build
RUN mkdir --parents /data/lp/etl/configuration && cp /opt/lp/etl/configuration.docker.properties /data/lp/etl/configuration/configuration.properties
ENV configFileLocation="/data/lp/etl/configuration/configuration.properties"
VOLUME ["/data/lp/elt/configuration"]
EXPOSE 8080
CMD ["npm", "run", "start"]
