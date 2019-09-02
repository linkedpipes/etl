"use strict";

console.log("Loading configuration from: ", process.env.configFileLocation);
if (process.env.configFileLocation === undefined) {
  throw new Error("Missing configuration file!")
}

const propertiesReader = require("properties-reader");
const properties = propertiesReader(process.env.configFileLocation);

module.exports = {
  "port": properties.get("frontend.webserver.port"),
  "storage": {
    "url": properties.get("storage.uri"),
    "domain": process.env["LP.ETL.DOMAIN"] || properties.get("domain.uri")
  },
  "executor": {
    "monitor": {
      "url": properties.get("executor-monitor.webserver.uri")
    },
    "ftp": {
      "uri": properties.get("executor-monitor.ftp.uri")
    }
  },
  "instanceLabel": properties.get("frontend.instance-label") || "LinkedPipes ETL"
};
