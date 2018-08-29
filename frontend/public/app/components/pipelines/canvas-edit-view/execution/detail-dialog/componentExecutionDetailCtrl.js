define([], function () {

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

    function controller($scope, $mdDialog, jsonLdService, component, execution) {
        var jsonld = jsonLdService.jsonld();

        $scope.onCancel = function () {
            $mdDialog.cancel();
        };

        (function initialize() {
            $scope.label = jsonld.getString(component,
                'http://www.w3.org/2004/02/skos/core#prefLabel');
            if (execution === undefined) {
                handleMissing($scope);
                return;
            }
            $scope.execution = execution;
            updateStatus($scope, execution);
            $scope.$watch("execution.status", () => {
                updateStatus($scope, execution);
            });
            $scope.messages = execution.messages;
        })();
    }

    function updateStatus(scope, execution) {
        switch (execution.status) {
            case 'http://etl.linkedpipes.com/resources/status/queued':
                handleQueued(scope);
                break;
            case 'http://etl.linkedpipes.com/resources/status/initializing':
                handleInitializing(scope, execution);
                break;
            case 'http://etl.linkedpipes.com/resources/status/running':
                handleRunning(scope, execution);
                break;
            case 'http://etl.linkedpipes.com/resources/status/finished':
                handleFinished(scope, execution);
                break;
            case 'http://etl.linkedpipes.com/resources/status/mapped':
                handleMapped(scope, execution);
                break;
            case 'http://etl.linkedpipes.com/resources/status/failed':
                handleFailed(scope, execution);
                break;
            default:
                handleUnknown(scope);
                break;
        }
        updateFailMessage(scope, execution);
    }

    function handleMissing(scope) {
        scope.start = '-';
        scope.end = '-';
        scope.duration = '-';
        scope.messages = [];
        scope.status = 'Not planed for execution';
    }

    function handleQueued(scope) {
        scope.start = '-';
        scope.end = '-';
        scope.duration = '-';
        scope.status = 'Waiting to be executed';
    }

    function handleInitializing(scope, execution) {
        scope.start = new Date(execution.start);
        scope.end = '-';
        scope.duration = '-';
        scope.status = 'Initializing';
    }

    function handleRunning(scope, execution) {
        scope.start = new Date(execution.start);
        scope.end = '-';
        scope.duration = '-';
        scope.status = 'Running';
    }

    function handleFinished(scope, execution) {
        scope.start = execution.start;
        scope.end = execution.end;
        scope.duration = new Date(scope.end) - new Date(scope.start);
        scope.status = 'Finished';
    }

    function handleMapped(scope, execution) {
        scope.start = '-';
        scope.end = '-';
        scope.duration = '-';
        scope.status = 'Was executed in other execution';
    }

    function handleFailed(scope, execution) {
        scope.start = execution.start;
        scope.end = execution.end;
        scope.duration = new Date(scope.end) - new Date(scope.start);
        scope.status = 'Failed';
    }

    function handleUnknown(scope) {
        scope.start = '-';
        scope.end = '-';
        scope.duration = '-';
        scope.status = 'Unknown';
    }

    function updateFailMessage(scope, execution) {
        if (execution.failed !== undefined) {
            scope.failed = true;
            scope.cause = execution.failed.cause;
            scope.rootCause = execution.failed.rootCause;
        }
    }

    controller.$inject = ['$scope', '$mdDialog', 'services.jsonld',
        'component', 'execution'];

    return function init(app) {
        app.controller('components.component.execution.dialog', controller);
    };

});


