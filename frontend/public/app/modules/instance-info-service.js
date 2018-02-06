/**
 * Provide access to information about the server.
 *
 * TODO Made data available via API.
 */
((definition) => {
    if (typeof define === "function" && define.amd) {
        define([], definition);
    }
})(() => {
    "use strict";

    const cache = {
        "ready": false,
        "data": {}
    };

    function fetch() {
        if (cache.ready) {
            return Promise.resolve(cache.data);
        } else {
            return fetchFromRemote();
        }
    }

    function fetchFromRemote() {
        return $http.get("api/v1/info").then((response) => {
            cache.data = response.data;
            cache.ready = true;
            return cache.data;
        });
    }

    //
    //
    //

    let $http;

    function service(_$http) {
        $http = _$http;
        this.fetch = fetch;
    }

    service.$inject = ["$http"];

    let _initialized = false;
    return function init(app) {
        if (_initialized) {
            return;
        }
        _initialized = true;
        app.service("service.info", service);
    };

});
