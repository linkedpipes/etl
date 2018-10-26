FROM maven:3-jdk-10-slim
RUN apt-get update; apt-get upgrade -y
RUN apt-get install -y curl software-properties-common gnupg; curl -sL https://deb.nodesource.com/setup_10.x | bash -
RUN apt-get install -y nodejs
RUN node -v; npm -v

WORKDIR /etl
COPY . .
RUN mvn install -DskipTests
RUN chmod a+x start.sh

CMD ["/bin/bash", "/etl/start.sh"]
EXPOSE 8080
