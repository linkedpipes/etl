/**
 * Exports info object with system information available for the client.
 */
(function () {
    "use strict";
    const fs = require("fs");
    const info = {};

    // Information about version.
    try {
        const path = __dirname + "/../data/git.json";
        const git = JSON.parse(fs.readFileSync(path, "utf8"));
        if (git !== undefined) {
            info.version = {
                "commit": git["git.commit.id"]
            };
        }
    } catch (e) {
        console.warn("Missing git.json file. " +
            "The information about version will not be available.", e);
    }

    // Information from the configuration class.
    const config = require("./../modules/configuration");
    info.path = {
        "ftp": config.executor.ftp.uri
    };

    module.exports = info;
})();
