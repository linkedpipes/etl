define([
    "app/app-config",
    "app/components/pipelines/list-view/pipeline-list-ctrl",
    "app/components/pipelines/edit-view/pipeline-edit-ctrl",
    "app/components/pipelines/upload-view/pipeline-upload-ctrl",
    "app/components/executions/list-view/execution-list-ctrl",
    "app/components/pipelines/edit-view/execution-mode/detail-dialog/component-execution-ctrl",
    "app/components/personalization/personalization-ctrl",
    "app/components/help/help-ctrl",
    "app/services/rdfService",
    "app/modules/refresh",
    "app/modules/status-service",
    "app/services/jsonldService",
    "app/services/designService",
    "app/components/dialogs/ui/localizedTextInput/localizedTextInput",
    "app/components/dialogs/ui/iriList/iriList",
    "app/components/templates/listView/templateListView",
    "app/components/templates/detail-view/template-detail-ctrl",
    "app/components/templates/template-service",
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

    // We cannot inject personalization here as they are not yet available.
    function redirectFromHomePage($rootScope, $route, $location, $cookies) {
        // Update path.
        if ($location.path() === "") {
            let landingPage = $cookies.get("lp-landing");
            if (landingPage === undefined || landingPage === "") {
                landingPage = "/executions";
            }
            $location.path(landingPage);
        }
        //
        preventReloadOnPathChange($rootScope, $route, $location);
    }

    function preventReloadOnPathChange($rootScope, $route, $location) {
        const originalPath = $location.path;
        $location.path = (path, reload) => {
            if (reload === false) {
                const lastRoute = $route.current;
                const un = $rootScope.$on("$locationChangeSuccess", () => {
                    $route.current = lastRoute;
                    un();
                });
            }
            return originalPath.apply($location, [path]);
        };
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
