define([], function () {
    function controler($scope, $cookies, statusService) {

        // Set expiration for today + 10 years.
        var expires = new Date();
        expires.setYear(expires.getFullYear() + 10);

        // Available options.
        $scope.landingPage = '';

        /**
         * Load values from cookie.
         */
        function load() {
            $scope.landingPage = $cookies.get('lp-landing');
            if ($scope.landingPage === undefined) {
                $scope.landingPage = '/executions';
            }
        }

        $scope.onDiscard = function () {
            load();
        };

        $scope.onSave = function () {
            $cookies.put('lp-landing', $scope.landingPage, {
                'expires': expires
            });

            statusService.success({
                'title': "Changes saved."
            });
        };

        // Load values from cookies.
        load();
    }
    //
    controler.$inject = ['$scope', '$cookies', 'services.status'];
    //
    function init(app) {
        app.controller('personalization', controler);
    }
    return init;
});
