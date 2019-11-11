((definition) => {
    if (typeof define === "function" && define.amd) {
        define([
            "../detail/directive/template-detail-directive",
            "../template-service",
            "./invalid-template-directive/invalid-template-directive-ctrl",
            "./template-detail-service"
        ], definition);
    }
})((_templatesDirective, _templates, _invalidDirective, _service) => {
    "use strict";

    function controller($scope, $service) {
        $service.initialize($scope);

        $scope.onSave = $service.onSave;

        $service.onLoad();
    }

    controller.$inject = ["$scope", "template.detail.view.service"];

    let initialized = false;
    return function init(app) {
        if (initialized) {
            return;
        }
        initialized = true;
        _templatesDirective(app);
        _templates(app);
        _invalidDirective(app);
        _service(app);
        app.controller("template.detail.view", controller);
    }

});