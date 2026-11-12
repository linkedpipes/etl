FROM node:26.13.1 AS build
WORKDIR /opt/lp-etl/frontend

# Install dependencies first, so the layer is cached until the lock file changes.
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci

# The git directory is used to resolve the commit hash shown in the application.
COPY .git /opt/lp-etl/.git

COPY frontend/ ./
RUN npm run build

FROM node:26.13.1-slim

ARG LP_ETL_USER=5987

RUN addgroup --gid $LP_ETL_USER "linkedpipes" \
    && useradd --gid "linkedpipes" --uid $LP_ETL_USER "linkedpipes" --create-home

RUN mkdir -p /data/lp-etl/logs \
    && chown -R $LP_ETL_USER:$LP_ETL_USER /data/lp-etl

# This is the default configuration.
COPY --chown=linkedpipes deploy/configuration.docker.properties /opt/lp-etl/configuration/configuration.properties

# We start by installing runtime dependecies.
WORKDIR /opt/lp-etl/frontend
COPY --chown=linkedpipes frontend/package.json frontend/package-lock.json ./
RUN npm ci --omit=dev

# Copy server files and build.
COPY --chown=linkedpipes frontend/server ./server
COPY --from=build --chown=linkedpipes /opt/lp-etl/frontend/dist ./dist

USER $LP_ETL_USER

EXPOSE 8080

ENV configFileLocation="/opt/lp-etl/configuration/configuration.properties"

CMD ["npm", "run", "start"]
