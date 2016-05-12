define([
    'app/app.config',
    'app/models/executionModel',
    'app/components/pipelines/listView/pipelineListCtrl',
    'app/components/pipelines/canvasView/pipelineCanvasCtrl',
    'app/components/pipelines/uploadView/pipelineUploadCtrl',
    'app/components/pipelines/exportDialog/pipelineExportDialogCtrl',
    'app/components/executions/listView/executionListCtrl',
    'app/components/executions/detailView/executionDetailCtrl',
    'app/components/componentExecutionDetail/componentExecutionDetailCtrl',
    'app/services/rdfService',
    'app/services/refreshService',
    'app/services/repositoryService',
    'app/services/statusService',
    'app/services/jsonldService',
    'app/services/infoService',
    'app/components/inputs/localizedTextInput/localizedTextInput',
    'app/components/inputs/iriList/iriList',
    'angular',
    'angular-route',
    'angular-resource',
    'angular-messages',
    'angular-material',
    'angular-file-upload',
    'angular-ui-notification'
], function (
        config,
        executionModel,
        pipelineListCtrlInit,
        pipelineEditCanvasCtrlInit,
        pipelineUploadCtrlInit,
        pipelineExportDialogCtrlInit,
        executionListCtrlInit,
        executionDetailCtrlInit,
        componentExecutionDetailCtrl,
        rdfService,
        refreshService,
        repositoryService,
        statusService,
        jsonldService,
        infoService,
        localizedTextInputInit,
        iriListInit,
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
    jsonldService(app);
    infoService(app);
    localizedTextInputInit(app);
    iriListInit(app);
    //
    executionModel(app);
    pipelineListCtrlInit(app);
    pipelineEditCanvasCtrlInit(app);
    pipelineUploadCtrlInit(app);
    pipelineExportDialogCtrlInit(app);
    executionListCtrlInit(app);
    executionDetailCtrlInit(app);
    componentExecutionDetailCtrl(app);
    //
    app.bootstrap = function () {
        angular.bootstrap(document, ['angularApp']);
    };
    // Root controller.
    function controler($scope, $mdSidenav, $route, $location) {

        $scope.route = $route;
        $scope.title = 'LinkedPipes ETL';

        $scope.$on('$routeChangeSuccess', function (event, current, previous) {
            if (current.$$route && current.$$route.pageTitle) {
                $scope.title = current.$$route.pageTitle;
            }
        });

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
