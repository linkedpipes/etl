/**
 * Provide access to information about the server.
 */
(function () {
    "use strict";

    const data = {
        "ready": false,
        "info": {}
    };

    function fetchFromRemote() {
        return $http.get("api/v1/info").then((response) => {
            data.info = response.data;
            data.ready = true;
            return data.info;
        });
    }

    function fetch() {
        if (data.ready) {
            return $q.when(data.info);
        } else {
            return fetchFromRemote();
        }
    }

    const service = {
        "fetch": fetch
    };

    //
    //

    let $http, $q;

    function factory(_$http, _$q) {
        $http = _$http;
        $q = _$q;
        return service;
    }

    factory.$inject = ["$http", "$q"];

    let _initialized = false;
    function init(app) {
        if (_initialized) {
            return;
        }
        _initialized = true;
        app.factory("service.info", factory);
    }

    if (typeof define === "function" && define.amd) {
        define([], () => init);
    }

})();
