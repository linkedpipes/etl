((definition) => {
    if (typeof define === "function" && define.amd) {
        define([
            "vocabulary",
            "jsonld",
            "app/modules/http",
            "./execution-message-loader"
        ], definition);
    }
})((vocabulary, jsonld, http, messageLoader) => {
    "use strict";

    const LP = vocabulary.LP;
    const SKOS = vocabulary.SKOS;

    function service($refresh) {

        let $scope;
        let $mdDialog;
        let component;
        let execution;
        let executionIri;

        function initialize(_scope, _mdDialog,
                            _component, _execution, _executionIri) {
            $scope = _scope;
            $mdDialog = _mdDialog;
            component = _component;
            execution = _execution;
            executionIri = _executionIri;

            $scope.start = "";
            $scope.end = "";
            $scope.duration = "";
            $scope.status = "";
            $scope.messages = [];
            $scope.failed = false;
            $scope.cause = [];
            $scope.rootCause = [];

            loadComponent(component);
            if (execution === undefined) {
                handleMissingExecution();
            } else {
                updateStatus();
                // Register watchers and updates.
                $scope.$watch("execution.status", () => {
                    updateStatus($scope, execution);
                });
                loadMessages();
                $refresh.add("exec-detail", refresh);
                // TODO Add refresh for messages.
            }

            $scope.$on("$destroy", () => {
                $refresh.remove("exec-detail");
            });
        }

        function loadComponent(component) {
            $scope.label = jsonld.r.getPlainString(component, SKOS.PREF_LABEL);
        }

        function handleMissingExecution() {
            $scope.start = "";
            $scope.end = "";
            $scope.duration = "";
            $scope.status = "Not planed for execution.";
        }

        function updateStatus() {
            switch (execution.status) {
                case LP.COMPONENT_QUEUED:
                    $scope.status = "Waiting to be executed";
                    break;
                case LP.COMPONENT_INITIALIZING:
                    $scope.status = "Initializing";
                    break;
                case LP.COMPONENT_RUNNING:
                    $scope.status = "Running";
                    break;
                case LP.COMPONENT_FINISHED:
                    $scope.status = "Finished";
                    break;
                case LP.COMPONENT_MAPPED:
                    $scope.status = "Was executed in other execution";
                    break;
                case LP.COMPONENT_FAILED:
                    $scope.status = "Failed";
                    $scope.failed = true;
                    break;
                default:
                    console.warn("Unknown status:", execution);
                    $scope.status = "Unknown";
                    break;
            }
        }

        function loadMessages() {
            fetchMessages()
                .then(updateFromMessages)
                .catch(onLoadMessagesFail);
        }

        function fetchMessages() {
            let url = getComponentExecution() +
                "/messages/component?iri=" +
                encodeURIComponent(execution.iri);
            return http.getJsonLd(url);
        }

        function getComponentExecution() {
            if (execution.execution) {
                return execution.execution;
            } else {
                return executionIri;
            }
        }

        function updateFromMessages(response) {
            const model = {};
            messageLoader.loadFromJsonLd(
                model, response.payload, getComponentExecution());
            const messages = model[execution.iri];
            if (messages === undefined) {
                // This can happen before the component starts.
                return;
            }
            $scope.start = messages.start;
            $scope.end = messages.end;
            if ($scope.start && $scope.end) {
                $scope.duration =
                    new Date(messages.end) - new Date(messages.start);
            }
            if (messages.failed !== undefined) {
                $scope.cause = messages.failed.cause;
                $scope.rootCause = messages.failed.rootCause;
            }
            $scope.messages = messages.messages;
            //
            $scope.$apply();
        }

        function onLoadMessagesFail(error) {
            console.warn("Can't fetch messages!", error);
        }

        function refresh() {
            switch (execution.status) {
                case LP.COMPONENT_FINISHED:
                case LP.COMPONENT_FAILED:
                case LP.COMPONENT_MAPPED:
                    break;
                default:
                    loadMessages();
                    break
            }
        }

        function onCancel() {
            $mdDialog.cancel();
        }

        this.initialize = initialize;
        this.onCancel = onCancel;
    }

    service.$inject = ["service.refresh"];

    let initialized = false;
    return function init(app) {
        if (initialized) {
            return;
        }
        initialized = true;
        app.service("components.component.execution.service", service);
    }
});


