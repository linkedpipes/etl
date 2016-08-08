define([], function () {
    function controller($scope, rdfService) {

        $scope.dialog = {
            'predicate': ''
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/t-filesToStatements#');

        $scope.setConfiguration = function (inConfig) {
            rdf.setData(inConfig);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.predicate = rdf.getString(resource, 'predicate');

        };

        $scope.getConfiguration = function () {
            var resource = rdf.secureByType('Configuration');

            rdf.setString(resource, 'predicate', $scope.dialog.predicate);

            return rdf.getData();
        };
    }
    //
    controller.$inject = ['$scope', 'services.rdf.0.0.0'];
    return controller;
});