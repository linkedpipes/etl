"use strict";

console.log("Loading configuration from: ", process.env.configFileLocation);
if (process.env.configFileLocation === undefined) {
  throw new Error("Missing configuration file!")
}

const propertiesReader = require("properties-reader");
const properties = propertiesReader(process.env.configFileLocation);
const domain = process.env["LP.ETL.DOMAIN"] || properties.get("domain.uri");
const fptUrl = process.env["LP.ETL.DEBUG.FTP"] || properties.get("executor-monitor.ftp.uri");

module.exports = {
  "port": properties.get("frontend.webserver.port"),
  "storage": {
    "url": properties.get("storage.uri"),
    "domain": domain
  },
  "executor": {
    "monitor": {
      "url": properties.get("executor-monitor.webserver.uri") + "/api/v1/ "
    },
    "ftp": {
      "uri": fptUrl
    }
  },
  "instanceLabel": properties.get("frontend.instance-label") || "LinkedPipes ETL"
};
