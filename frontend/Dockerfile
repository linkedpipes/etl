FROM node:12-alpine as lp-etl-frontend-build
RUN apk add --no-cache git
WORKDIR /opt/lp/etl
ARG BRANCH=master
RUN git clone --single-branch --branch $BRANCH https://github.com/linkedpipes/etl.git .
WORKDIR /opt/lp/etl/frontend
RUN npm run build

FROM node:12-alpine
LABEL component="frontend"
ENV LP.ETL.DOMAIN="http://localhost:8080"
WORKDIR /opt/lp/etl
COPY --from=lp-etl-build /opt/lp/etl/deploy/frontend /opt/lp/etl/frontend
COPY --from=lp-etl-build /opt/lp/etl/deploy/configuration.docker.properties /data/lp/etl/
EXPOSE 8085
VOLUME ["/data/lp/etl/"]
WORKDIR /opt/lp/etl/frontend
CMD ["npm", "run", "start"]

