((definition) => {
    if (typeof define === "function" && define.amd) {
        define(["../canvas-edit-mode-service"], definition);
    }
})((_service) => {

    function directive($service) {
        return {
            "restrict": "E",
            "templateUrl": "app/components/pipelines/edit-view/edit-mode/edit-directive/canvas-edit.html",
            "link": function ($scope, element, attrs) {
                $scope.onEditComponent = $service.ui.onEditComponent;
                $scope.onDeleteComponent = $service.ui.onDeleteComponent;
                $scope.onCopyComponent = $service.ui.onCopyComponent;
                $scope.onEnableDisable = $service.ui.onEnableDisable;
                $scope.onPrerequisiteComponent = $service.ui.onPrerequisiteComponent;
                $scope.onDebugToComponent = $service.ui.onDebugToComponent;
                $scope.onMappingComponent = $service.ui.onMappingComponent;
                $scope.onCreateTemplate = $service.ui.onCreateTemplate;
                //
                $scope.onAddComponent = $service.ui.onAddComponent;
                $scope.onImportPipeline = $service.ui.onImportPipeline;
            }
        };
    }

    directive.$inject = ["canvas.edit-mode.service"];

    let initialized = false;
    return function (app) {
        if (initialized) {
            return;
        }
        initialized = true;
        _service(app);
        app.directive("lpCanvasEdit", directive);
    };

});
