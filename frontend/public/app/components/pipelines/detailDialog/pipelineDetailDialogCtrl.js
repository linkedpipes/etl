define([], function () {
    function controler($scope, $mdDialog, data) {

        $scope.detail = {
            'label' : data.definition['http://www.w3.org/2004/02/skos/core#prefLabel']
        };

        $scope.onCancel = function () {
            $mdDialog.cancel();
        };

        $scope.onSave = function () {
            // Save changes.
            data.definition['http://www.w3.org/2004/02/skos/core#prefLabel'] = $scope.detail.label;
            // Close the dialog.
            $mdDialog.hide();
        };

    }
    controler.$inject = ['$scope', '$mdDialog', 'data'];
    //
    function init(app) {
        app.controller('components.pipelines.detail.dialog', controler);
    }
    //
    return init;
});
