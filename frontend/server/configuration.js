const propertiesReader = require("properties-reader");
const logger = require("./logging");

logger.info("Loading configuration from: ", process.env.configFileLocation);
if (process.env.configFileLocation === undefined) {
  throw new Error("Missing configuration file!")
}

const properties = propertiesReader(process.env.configFileLocation);

const domain = process.env["LP_ETL_DOMAIN"]
  || properties.get("domain.uri");
const fptUrl = process.env["LP_ETL_FTP"]
  || properties.get("executor-monitor.ftp.uri");
const storageUrl = process.env["LP_ETL_STORAGE_URL"]
  || properties.get("storage.uri");
const monitorUrl = process.env["LP_ETL_MONITOR_URL"]
  || properties.get("executor-monitor.webserver.uri");
const label = process.env["LP_ETL_LABEL"]
  || properties.get("frontend.instance-label")
  || "LinkedPipes ETL";

module.exports = {
  "port": properties.get("frontend.webserver.port"),
  "storage": {
    "url": storageUrl,
    "domain": domain
  },
  "executor": {
    "monitor": {
      "url": monitorUrl,
    },
    "ftp": {
      "uri": fptUrl
    }
  },
  "instanceLabel": label,
};
