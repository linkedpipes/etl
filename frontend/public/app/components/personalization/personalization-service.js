((definition) => {
    if (typeof define === "function" && define.amd) {
        define([], definition);
    }
})(() => {

    function factory($cookies, statusService) {

        let $scope;

        function initialize(scope) {
            $scope = scope;
            $scope.landingPage = "";
            $scope.initialListSize = "";
        }

        function load() {
            // TODO Extract default so that can be used in the application.

            $scope.landingPage = $cookies.get("lp-landing");
            if ($scope.landingPage === undefined) {
                $scope.landingPage = "/executions";
            }

            $scope.initialListSize =
                parseInt($cookies.get("lp-initial-list-size"));
            if ($scope.initialListSize === undefined) {
                $scope.initialListSize = 15;
            }
        }

        function save() {
            const expires = getExpiration();
            const cookiesOptions = {"expires": expires};

            $cookies.put("lp-landing",
                $scope.landingPage, cookiesOptions);

            const initListSize = parseInt($scope.initialListSize);
            if (isNaN(initListSize)) {
                $cookies.remove("lp-initial-list-size");
            } else {
                $cookies.put("lp-initial-list-size",
                    initListSize, cookiesOptions);
            }

            statusService.success({
                "title": "Changes saved."
            });
        }

        // TODO Move to cookies module.
        function getExpiration() {
            const expires = new Date();
            expires.setYear(expires.getFullYear() + 10);
            return expires;
        }

        return {
            "initialize": initialize,
            "load": load,
            "save": save
        };
    }

    factory.$inject = [
        "$cookies",
        "services.status"
    ];

    let initialized = false;
    return function init(app) {
        if (initialized) {
            return;
        }
        initialized = true;
        app.factory("personalization.service", factory);
    }
});





