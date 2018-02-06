define([], function () {

    function getExpiration() {
        const expires = new Date();
        expires.setYear(expires.getFullYear() + 10);
        return expires;
    }

    function initializeScope($scope) {
        $scope.landingPage = '';
        $scope.pipelineInitialSize = '';
    }

    function controller($scope, $cookies, statusService) {

        function loadFromCookies() {

            // TODO Extract default so that can be used in the application.

            $scope.landingPage = $cookies.get('lp-landing');
            if ($scope.landingPage === undefined) {
                $scope.landingPage = '/executions';
            }

            $scope.pipelineInitialSize = parseInt($cookies.get('lp-pipelines-initial-size'));
            if ($scope.pipelineInitialSize === undefined) {
                $scope.pipelineInitialSize = 15;
            }
        }

        $scope.onDiscard = function () {
            loadFromCookies();
        };

        $scope.onSave = function () {
            const pplInitPageSize = parseInt($scope.pipelineInitialSize);
            if (isNaN(pplInitPageSize)) {
                statusService.error({
                    'title': "Invalid input."
                });
            }


            const expires = getExpiration();
            const cookiesOptions = {'expires': expires};

            $cookies.put('lp-landing',
                $scope.landingPage, cookiesOptions);

            $cookies.put('lp-pipelines-initial-size',
                pplInitPageSize, cookiesOptions);

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
