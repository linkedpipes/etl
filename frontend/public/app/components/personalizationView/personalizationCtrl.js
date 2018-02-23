define([], function () {

    function getExpiration() {
        const expires = new Date();
        expires.setYear(expires.getFullYear() + 10);
        return expires;
    }

    function initializeScope($scope) {
        $scope.landingPage = '';
        $scope.initialListSize = '';
    }

    function controller($scope, $cookies, statusService) {

        function loadFromCookies() {

            // TODO Extract default so that can be used in the application.

            $scope.landingPage = $cookies.get('lp-landing');
            if ($scope.landingPage === undefined) {
                $scope.landingPage = '/executions';
            }

            $scope.initialListSize = parseInt($cookies.get('lp-initial-list-size'));
            if ($scope.initialListSize === undefined) {
                $scope.initialListSize = 15;
            }
        }

        $scope.onDiscard = function () {
            loadFromCookies();
        };

        $scope.onSave = function () {
            const expires = getExpiration();
            const cookiesOptions = {'expires': expires};

            $cookies.put('lp-landing',
                $scope.landingPage, cookiesOptions);

            const initListSize = parseInt($scope.initialListSize);
            if (isNaN(initListSize)) {
                $cookies.remove('lp-initial-list-size');
            } else {
                $cookies.put('lp-initial-list-size',
                    initListSize, cookiesOptions);
            }

            statusService.success({
                'title': "Changes saved."
            });
        };

        (function initialize() {
            initializeScope($scope);
            loadFromCookies();
        })();

    }

    //
    controller.$inject = ['$scope', '$cookies', 'services.status'];
    //
    function init(app) {
        app.controller('personalization', controller);
    }

    return init;
});
