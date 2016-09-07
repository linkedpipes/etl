define([], function () {
    function controler($scope, $mdDialog, data, jsonldService) {

        var jsonld = jsonldService.jsonld();

        $scope.detail = {
            'uri': data.definition['@id'],
            'label' : jsonld.getString(data.definition, 'http://www.w3.org/2004/02/skos/core#prefLabel'),
            'tags' : jsonld.getValues(data.definition, 'http://etl.linkedpipes.com/ontrology/tag')
        };

        $scope.onCancel = function () {
            $mdDialog.cancel();
        };

        $scope.onSave = function () {
            // Save changes.
            data.definition['http://www.w3.org/2004/02/skos/core#prefLabel'] = $scope.detail.label;
            jsonld.setValues(data.definition, 'http://etl.linkedpipes.com/ontrology/tag', undefined, $scope.detail.tags)
            // Close the dialog.
            $mdDialog.hide();
        };

    }
    controler.$inject = ['$scope', '$mdDialog', 'data', 'services.jsonld'];
    //
    function init(app) {
        app.controller('components.pipelines.detail.dialog', controler);
    }
    //
    return init;
});
