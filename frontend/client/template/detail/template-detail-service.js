((definition) => {
    if (typeof define === "function" && define.amd) {
        define([], definition);
    }
})(() => {

    let $scope;
    let $routeParams;
    let $templates;
    let $status;

    function factory(_$routeParams, _$templates, _$status) {
        $routeParams = _$routeParams;
        $templates = _$templates;
        $status = _$status;
        return {
            "initialize": initialize,
            "onSave": onSave,
            "onLoad": onLoad
        };
    }

    factory.$inject = ["$routeParams", "template.service", "status"];

    function initialize(_$scope) {
        $scope = _$scope;
        //
        $scope.api = {};
        $scope.template = undefined;
        $scope.templateToEdit = undefined;
        $scope.configuration = undefined;
    }

    function onSave() {
        $scope.api.save();
        $templates.saveTemplate($scope.templateToEdit,
            $scope.configuration).then(() => {
            $status.success("Template saved.");
        }, (response) => {
            $status.httpError("Can't save template.", response);
        });
    }

    function onLoad() {
        $templates.load().then(() => {
            const template = $templates.getTemplate($routeParams.template);
            $scope.template = template;

            $scope.templateToEdit = $templates.getEditableTemplate(
                $routeParams.template);

            loadConfiguration(template);
        });
    }


    function loadConfiguration(template) {
        $templates.fetchConfig(template.id).then((instance) => {
            $scope.configuration = [...instance];
            // Pass data to the directive.
            $scope.api.store = {
                "template": $scope.template,
                "templateToEdit": $scope.templateToEdit,
                "configuration": $scope.configuration
            };
            if ($scope.api.load !== undefined) {
                $scope.api.load();
            }
        });
    }

    let initialized = false;
    return function init(app) {
        if (initialized) {
            return;
        }
        initialized = true;
        app.factory("template.detail.view.service", factory);
    }
});
