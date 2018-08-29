"use strict";

const propertiesReader = require("properties-reader");
const properties = propertiesReader(process.env.configFileLocation);

module.exports = {
    "frontend": {
        "port": properties.get("frontend.webserver.port")
    },
    "storage": {
        "url": properties.get("storage.uri"),
        "domain": properties.get("domain.uri")
    },
    "executor": {
        "monitor": {
            "url": properties.get("executor-monitor.webserver.uri")
        },
        "ftp": {
            "uri": properties.get("executor-monitor.ftp.uri")
        }
    }
};
