define([], function () {
    function controler($scope, $mdDialog, templatesRepository) {

        $scope.data = templatesRepository.getTemplates();

        $scope.onSubmit = function() {
            console.log('Submit');
        };

        $scope.onSelect = function(template) {
            $mdDialog.hide(template);
        };

        $scope.onCancel = function () {
            $mdDialog.cancel();
        };

    }
    controler.$inject = ['$scope', '$mdDialog', 'components.templates.services.repository'];
    //
    function init(app) {
        app.controller('components.templates.select.dialog', controler);
    }
    //
    return init;
});

