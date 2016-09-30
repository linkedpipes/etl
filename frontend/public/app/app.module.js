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
    'app/components/personalizationView/personalizationCtrl',
    'app/services/rdfService',
    'app/services/refreshService',
    'app/services/repositoryService',
    'app/services/statusService',
    'app/services/jsonldService',
    'app/services/infoService',
    'app/services/designService',
    'app/components/inputs/localizedTextInput/localizedTextInput',
    'app/components/inputs/iriList/iriList',
    'app/components/templates/listView/templateListView',
    'app/components/templates/detailView/templateDetailView',
    'app/components/templates/templateService',
    'app/components/templates/generalTab/templateGeneralTab',
    'app/components/templates/hierarchyTab/templateHierarchyTab',
    'angular',
    'angular-route',
    'angular-resource',
    'angular-messages',
    'angular-cookies',
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
        personalizationCtrl,
        rdfService,
        refreshService,
        repositoryService,
        statusService,
        jsonldService,
        infoService,
        designService,
        localizedTextInputInit,
        iriListInit,
        templateListInit,
        templateDetailInit,
        templateService,
        templateGeneralTab,
        templateHierarchyTab,
        angular
        ) {
    var app = angular.module('angularApp', [
        'ngRoute',
        'ngResource',
        'ngMaterial',
        'ngMessages', // Support for ng-messages directive used in dialogs.
        'ngFileUpload',
        'ngCookies',
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
    designService(app);
    localizedTextInputInit(app);
    iriListInit(app);
    templateListInit(app);
    templateDetailInit(app);
    templateService(app);
    templateGeneralTab(app);
    templateHierarchyTab(app);
    //
    executionModel(app);
    pipelineListCtrlInit(app);
    pipelineEditCanvasCtrlInit(app);
    pipelineUploadCtrlInit(app);
    pipelineExportDialogCtrlInit(app);
    executionListCtrlInit(app);
    executionDetailCtrlInit(app);
    componentExecutionDetailCtrl(app);
    personalizationCtrl(app);
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

        $scope.$on('$routeChangeSuccess', function (event, current, previous) {
            if (current.$$route && current.$$route.pageTitle) {
                $scope.index.title = current.$$route.pageTitle;
                if (current.$$route.color !== undefined) {
                    $scope.index.color = current.$$route.color;
                }
            }
        });

        var sidenavId = 'left';
        $scope.toggleSidenav = function () {
            $mdSidenav(sidenavId).toggle();
        };

        $scope.closeSidenav = function () {
            $mdSidenav(sidenavId).close();
        };

        $scope.onPipelines = function () {
            $scope.closeSidenav();
            $location.path('/pipelines').search({});
        };

        $scope.onExecutions = function () {
            $scope.closeSidenav();
            $location.path('/executions').search({});
        };

        $scope.onTemplates = function () {
            $scope.closeSidenav();
            $location.path('/templates').search({});
        };

        $scope.onPersonalization = function () {
            $scope.closeSidenav();
            $location.path('/personalization').search({});
        };

    });
    // Execute after the application is loaded, we can take care about
    // some initialization: redirect
    app.run(function ($location, $cookies) {
        // If user acess the 'root' page redirect to the
        // configured landing page.
        if ($location.path() === '') {
            var landingPage = $cookies.get('lp-landing');
            if (landingPage === undefined || landingPage === '') {
                landingPage = '/executions';
            }
            $location.path(landingPage);
        }
    });
    //
    return app;
});
