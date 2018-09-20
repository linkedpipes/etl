((definition) => {
    if (typeof define === "function" && define.amd) {
        define([], definition);
    }
})(() => {

    function factory($rootScope, $interval) {
        const service = {
            "callbacks": {},
            "interval": 4,
            "counter": 0
        };

        service.reset = function () {
            service.callbacks = {};
        };

        service.add = function (name, callback) {
            service.callbacks[name] = callback;
        };

        service.remove = function (name) {
            delete service.callbacks[name];
        };

        service.callback = function () {
            service.counter += 1;
            if (service.counter >= service.interval) {
                service.counter = 0;
                Object.values(service.callbacks)
                    .forEach(callback => callback());
            }
        };

        // Reset refresher on page update.
        $rootScope.$on("$locationChangeStart", (event)  => service.reset());

        $interval(service.callback, 500);
        return service;
    }

    factory.$inject = ["$rootScope", "$interval"];

    let initialized = false;
    return function init(app) {
        if (initialized) {
            return;
        }
        initialized = true;
        app.factory("service.refresh", factory);
    }

});