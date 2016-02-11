/**
 * Define service that should be used for periodic update tasks. The service
 * reset on the page change.
 */
define([], function () {
    function factoryFunction($rootScope, $interval) {
        var service = {
            'current': null, // Store function callback.
            'interval': 5,
            'counter': 0
        };

        service.reset = function () {
            service.current = null;
        };

        service.set = function (callback) {
            service.current = callback;
            service.counter = 0;
        };

        service.callback = function () {
            if (service.current === null) {
                return;
            }
            service.counter += 1;
            if (service.counter >= service.interval) {
                service.counter = 0;
                if (service.current !== null) {
                    service.current();
                }
            }
        };

        // Reset refresher on page update.
        $rootScope.$on('$locationChangeStart', function (event) {
            service.reset();
        });

        // Refresh once per second, we use service.interval to set interval.
        $interval(service.callback, 500);
        return service;
    }
    //
    factoryFunction.$inject = ['$rootScope', '$interval'];
    //
    function init(app) {
        app.factory('service.refresh', factoryFunction);
    }
    return init;
});