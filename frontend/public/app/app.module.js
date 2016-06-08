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
    'angular-ui-notification',
    'angular-clipboard'
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
        'ui-notification', // Notification
        'angular-clipboard'
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
    // Service for index page, used to store variables.
    app.service('indexPage', function () {
        this.color = '#2196f3';
        this.title = 'LinkedPipes ETL';
    });
    // Index controller.
    app.controller('index.ctrl', function ($scope, $mdSidenav, $route,
            $location, indexPage) {

        $scope.route = $route;
        $scope.index = indexPage;

        console.log('indexPage', indexPage);

        $scope.$on('$routeChangeSuccess', function (event, current, previous) {
            if (current.$$route && current.$$route.pageTitle) {
                $scope.index.title = current.$$route.pageTitle;
                $scope.index.color = current.$$route.color;
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

    });
    //
    return app;
});
