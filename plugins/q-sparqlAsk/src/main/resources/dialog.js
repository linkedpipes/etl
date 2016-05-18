define([], function () {
    function controller($scope, rdfService) {

        $scope.dialog = {
            'query': '',
            'failOnTrue': true
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/q-sparqlAsk#');

        $scope.setConfiguration = function (inConfig) {
            rdf.setData(inConfig);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.query = rdf.getString(resource, 'query');
            $scope.dialog.failOnTrue = rdf.getBoolean(resource, 'failOnTrue');
        };

        $scope.getConfiguration = function () {
            var resource = rdf.secureByType('Configuration');

            rdf.setString(resource, 'query', $scope.dialog.query);
            rdf.setBoolean(resource, 'failOnTrue', $scope.dialog.failOnTrue);

            return rdf.getData();
        };
    }
    //
    controller.$inject = ['$scope', 'services.rdf.0.0.0'];
    return controller;
});