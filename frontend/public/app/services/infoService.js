/**
 *
 * Load and provide acess to information from backend.
 */
define([], function () {

    function factoryFunction($http) {

        var service = {
            'data': {},
            'ready': false,
            'get': function () {
                return service.data;
            },
            'wait': function (callback) {
                if (service.ready) {
                    callback();
                } else {
                    // TODO Add error handler if loading fail
                    $http.get('api/v1/info').then(function (response) {
                        service.data = response.data;
                        service.ready = true;
                        //
                        callback();
                    });
                }
            }
        };

        return service;
    }

    factoryFunction.$inject = ['$http'];

    return function init(app) {
        app.factory('service.info', factoryFunction);
    };

});
