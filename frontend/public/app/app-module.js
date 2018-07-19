define([
    "app/app-config",
    "app/components/pipelines/list-view/pipeline-list-ctrl",
    "app/components/pipelines/canvasView/pipelineCanvasCtrl",
    "app/components/pipelines/uploadView/pipelineUploadCtrl",
    "app/components/executions/list-view/execution-list-ctrl",
    "app/components/componentExecutionDetail/componentExecutionDetailCtrl",
    "app/components/personalization/personalization-ctrl",
    "app/components/help/help-ctrl",
    "app/services/rdfService",
    "app/services/refreshService",
    "app/modules/status-service",
    "app/services/jsonldService",
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
    "app/components/dialogs/sparqlEditor/yasqe-editor",
    "angular",
    "angular-route",
    "angular-resource",
    "angular-messages",
    "angular-cookies",
    "angular-material",
    "angular-file-upload",
    "angular-ui-notification",
    "angular-clipboard",
    "app/modules/scroll-watch"
], function (
        config,
        pipelineListCtrlInit,
        pipelineEditCanvasCtrlInit,
        pipelineUploadCtrlInit,
        executionListCtrlInit,
        componentExecutionDetailCtrl,
        personalizationCtrl,
        helpCtrl,
        rdfService,
        refreshService,
        statusService,
        jsonldService,
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
        yasqe,
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
            "angular-clipboard",
            "lp-scroll-watch"
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
        statusService(app);
        jsonldService(app);
        designService(app);
        localizedTextInputInit(app);
        iriListInit(app);
        templateListInit(app);
        templateDetailInit(app);
        templateService(app);
        templateGeneralTab(app);
        templateHierarchyTab(app);
        indexPageService(app);
        yasqe(app);
    }

    function initializeComponents(app) {
        pipelineListCtrlInit(app);
        pipelineEditCanvasCtrlInit(app);
        pipelineUploadCtrlInit(app);
        executionListCtrlInit(app);
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
