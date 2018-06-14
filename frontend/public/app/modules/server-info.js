// TODO Add support for refresh.
((definition) => {
    if (typeof define === "function" && define.amd) {
        define(["./http"], definition);
    }
})((http) => {
    "use strict";

    const cache = {
        "ready": false,
        "data": {}
    };

    function load() {
        if (cache.ready) {
            return Promise.resolve(cache.data);
        } else {
            return fetchFromRemote();
        }
    }

    function fetchFromRemote() {
        return http.getJson("api/v1/info").then((response) => {
            cache.data = response.json;
            cache.ready = true;
            return cache.data;
        });
    }

    function getCommit() {
        return cache.data["version"]["commit"];
    }

    function getFtpUrl() {
        return cache.data["path"]["ftp"];
    }

    return {
        "load": load,
        "getCommit": getCommit,
        "getFtpUrl": getFtpUrl
    };

});
