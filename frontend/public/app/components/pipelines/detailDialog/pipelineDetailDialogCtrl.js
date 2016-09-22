define([], function () {
    function controler($scope, $mdDialog, data, jsonldService, pipelineDesign) {

        var jsonld = jsonldService.jsonld();

        $scope.tags = {
            'searchText' : '',
            'all' : [],
            'querySearch': function(query) {
                query = query.toLowerCase()
                return $scope.tags.all.filter(function (item) {
                    console.log(item, query, item.indexOf(query) !== -1)
                    return item.toLowerCase().indexOf(query) !== -1;
                });
            }
        };

        $scope.detail = {
            'uri': data.definition['@id'],
            'label' : jsonld.getString(data.definition, 'http://www.w3.org/2004/02/skos/core#prefLabel'),
            'tags' : jsonld.getValues(data.definition, 'http://etl.linkedpipes.com/ontology/tag')
        };

        $scope.onCancel = function () {
            $mdDialog.cancel();
        };

        $scope.onSave = function () {
            // Save changes.
            data.definition['http://www.w3.org/2004/02/skos/core#prefLabel'] = $scope.detail.label;
            jsonld.setValues(data.definition, 'http://etl.linkedpipes.com/ontology/tag', undefined, $scope.detail.tags)
            // Close the dialog.
            $mdDialog.hide();
        };

        pipelineDesign.initialize(() => {
            $scope.tags.all = pipelineDesign.getTags();
        });

    }
    controler.$inject = ['$scope', '$mdDialog', 'data', 'services.jsonld',
        'service.pipelineDesign'];
    //
    function init(app) {
        app.controller('components.pipelines.detail.dialog', controler);
    }
    //
    return init;
});
