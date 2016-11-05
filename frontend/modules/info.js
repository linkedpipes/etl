/**
 * Holds system information that are available to the client.
 *
 * This module exports info object.
 */
(function() {
    "use strict";
    var gFs = require("fs");
    var info = {};

    // Information about version.
    try {
        var path = __dirname + "/../data/git.json";
        var git = JSON.parse(gFs.readFileSync(path, "utf8"));
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
    var gConfiguration = require('./../modules/configuration');
    info.path = {
        'ftp': gConfiguration.executor.ftp.uri
    };

    module.exports = info;
})();
