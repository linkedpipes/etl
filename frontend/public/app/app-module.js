define([
    "app/app-config",
    "app/models/executionModel",
    "app/components/pipelines/listView/pipelineListCtrl",
    "app/components/pipelines/canvasView/pipelineCanvasCtrl",
    "app/components/pipelines/uploadView/pipelineUploadCtrl",
    "app/components/pipelines/exportDialog/pipelineExportDialogCtrl",
    "app/components/executions/listView/executionListCtrl",
    "app/components/executions/detailView/executionDetailCtrl",
    "app/components/componentExecutionDetail/componentExecutionDetailCtrl",
    "app/components/personalizationView/personalizationCtrl",
    "app/components/help/view/helpView",
    "app/services/rdfService",
    "app/services/refreshService",
    "app/services/repositoryService",
    "app/services/statusService",
    "app/services/jsonldService",
    "app/services/infoService",
    "app/services/designService",
    "app/components/inputs/localizedTextInput/localizedTextInput",
    "app/components/inputs/iriList/iriList",
    "app/components/templates/listView/templateListView",
    "app/components/templates/detailView/templateDetailView",
    "app/components/templates/templateService",
    "app/components/templates/generalTab/templateGeneralTab",
    "app/components/templates/hierarchyTab/templateHierarchyTab",
    "app/components/layout/layout-page",
    "app/components/layout/layout-service",
    "angular",
    "angular-route",
    "angular-resource",
    "angular-messages",
    "angular-cookies",
    "angular-material",
    "angular-file-upload",
    "angular-ui-notification",
    "angular-clipboard"
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
        helpCtrl,
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
        indexPage,
        indexPageService,
        angular
        ) {

    function createApplication() {
        return angular.module("angularApp", [
            "ngRoute",
            "ngResource",
            "ngMaterial",
            "ngMessages", // Support for ng-messages directive used in dialogs.
            "ngFileUpload",
            "ngCookies",
            "ui-notification",
            "angular-clipboard"
        ]);
    }

    function configurePathHashPrefix(app) {
        // https://stackoverflow.com/questions/41211875/angularjs-1-6-0-latest-now-routes-not-working
        app.config(["$locationProvider", function($locationProvider) {
            $locationProvider.hashPrefix("");
        }]);
    }

    function initializeServices(app) {
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
        indexPageService(app);
    }

    function initializeComponents(app) {
        executionModel(app);
        pipelineListCtrlInit(app);
        pipelineEditCanvasCtrlInit(app);
        pipelineUploadCtrlInit(app);
        pipelineExportDialogCtrlInit(app);
        executionListCtrlInit(app);
        executionDetailCtrlInit(app);
        componentExecutionDetailCtrl(app);
        personalizationCtrl(app);
        helpCtrl(app);
        indexPage(app);
    }

    function bootstrapAngularApplication(app) {
        app.bootstrap = function () {
            angular.bootstrap(document, ["angularApp"]);
        };
    }

    function redirectFromHomePage($location, $cookies) {
        if ($location.path() === "") {
            let landingPage = $cookies.get("lp-landing");
            if (landingPage === undefined || landingPage === "") {
                landingPage = "/executions";
            }
            $location.path(landingPage);
        }
    }

    const application = createApplication();
    application.config(config);
    configurePathHashPrefix(application);
    initializeServices(application);
    initializeComponents(application);
    bootstrapAngularApplication(application);
    // Execute after the application is loaded.
    application.run(redirectFromHomePage);

    return application;
});
