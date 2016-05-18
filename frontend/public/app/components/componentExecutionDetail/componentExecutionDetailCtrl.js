define([], function () {
    function controler($scope, $mdDialog, jsonLdService, component, execution) {

        var jsonld = jsonLdService.jsonld();

        $scope.onCancel = function () {
            $mdDialog.cancel();
        };

        /**
         * For given status IRI return a readable message.
         */
        function statusToMessage(status) {
            switch (status) {
                case 'http://etl.linkedpipes.com/resources/status/queued':
                    return 'Waiting to be executed';
                case 'http://etl.linkedpipes.com/resources/status/initializing':
                case 'http://etl.linkedpipes.com/resources/status/running':
                    return 'Running';
                case 'http://etl.linkedpipes.com/resources/status/finished':
                    return 'Finished';
                case 'http://etl.linkedpipes.com/resources/status/mapped':
                    return 'Was executed in other execution';
                case 'http://etl.linkedpipes.com/resources/status/failed':
                    return 'Failed';
                default:
                    return 'Unknown';
            }
        }

        (function initialize() {
            $scope.label = jsonld.getString(component,
                    'http://www.w3.org/2004/02/skos/core#prefLabel');
            if (execution === undefined) {
                // Not planed for an execution.
                $scope.start = '';
                $scope.end = '';
                $scope.messages = [];
                $scope.status = 'Not planed for execution';
                return;
            }
            $scope.start = execution.start;
            $scope.end = execution.end;
            $scope.messages = execution.messages;
            $scope.status = statusToMessage(execution.status);
            if (execution.failed !== undefined) {
                $scope.failed = true;
                $scope.rootCause = execution.failed.rootCause;
            }
        })();
    }

    controler.$inject = ['$scope', '$mdDialog', 'services.jsonld',
        'component', 'execution'];

    return function init(app) {
        app.controller('components.component.execution.dialog', controler);
    };

});


