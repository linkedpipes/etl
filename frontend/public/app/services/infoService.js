/**
 * Provide access to information about the server.
 */
define([], function () {
    "use strict";

    function factory($http, $q) {

        const data = {
            "ready": false,
            "info": {}
        };

        function load() {
            return $http.get("api/v1/info").then(function (response) {
                data.info = response.data;
                data.ready = true;
                return data.info;
            });
        }

        /**
         * If the data are missing load them.
         * In every case return promise with the info object.
         */
        function fetch() {
            if (data.ready) {
                return $q.when(data.info);
            } else {
                return load();
            }
        }

        return {
            "fetch": fetch
        };
    }

    let _initialized = false;
    return function init(app) {
        if (_initialized) {
            return;
        }
        _initialized = true;
        app.factory("service.info", ["$http", "$q", factory]);
    };

});
