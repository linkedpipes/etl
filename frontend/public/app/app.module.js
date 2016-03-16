define([
    'app/app.config',
    'app/components/pipelines/listView/pipelineListCtrl',
    'app/components/pipelines/canvasView/pipelineEditCanvasCtrl',
    'app/components/pipelines/uploadView/pipelineUploadCtrl',
    'app/components/executions/listView/executionListCtrl',
    'app/components/executions/detailView/executionDetailCtrl',
    'app/services/rdfService',
    'app/services/refreshService',
    'app/services/repositoryService',
    'app/services/statusService',
    'app/components/inputs/localizedTextInput/localizedTextInput',
    'angular',
    'angular-route',
    'angular-resource',
    'angular-messages',
    'angular-material',
    'angular-file-upload',
    'angular-ui-notification'
], function (
        config,
        pipelineListCtrlInit,
        pipelineEditCanvasCtrlInit,
        pipelineUploadCtrlInit,
        executionListCtrlInit,
        executionDetailCtrlInit,
        rdfService,
        refreshService,
        repositoryService,
        statusService,
        localizedTextInputInit,
        angular
        ) {
    var app = angular.module('angularApp', [
        'ngRoute',
        'ngResource',
        'ngMaterial',
        'ngMessages', // Support for ng-messages directive used in dialogs.
        'ngFileUpload',
        'ui-notification' // Notification
    ]);
    app.config(config);
    //
    rdfService(app);
    refreshService(app);
    repositoryService(app);
    statusService(app);
    localizedTextInputInit(app);
    //
    pipelineListCtrlInit(app);
    pipelineEditCanvasCtrlInit(app);
    pipelineUploadCtrlInit(app);
    executionListCtrlInit(app);
    executionDetailCtrlInit(app);
    //
    app.bootstrap = function () {
        angular.bootstrap(document, ['angularApp']);
    };
    // Root controller.
    function controler($scope, $mdSidenav, $route, $location) {

        $scope.route = $route;
        var sidenavId = 'left';

        $scope.toggleSidenav = function () {
            $mdSidenav(sidenavId).toggle();
        };

        $scope.closeSidenav = function() {
            $mdSidenav(sidenavId).close();
        };

        $scope.onPipelines = function() {
            $scope.closeSidenav();
            $location.path('/pipelines').search({});
        };

        $scope.onExecutions = function() {
            $scope.closeSidenav();
            $location.path('/executions').search({});
        };

    }
    //
    controler.$inject = ['$scope', '$mdSidenav', '$route', '$location'];
    app.controller('index.ctrl', controler);
    return app;
});
