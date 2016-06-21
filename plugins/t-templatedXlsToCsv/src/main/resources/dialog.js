define([], function () {
    function controller($scope, rdfService) {

        $scope.dialog = {
            'prefix': ''
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/t-templatedXlsToCsv#');

        $scope.setConfiguration = function (inConfig) {
            rdf.setData(inConfig);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.prefix = rdf.getString(resource, 'prefix');
        };

        $scope.getConfiguration = function () {
            var resource = rdf.secureByType('Configuration');

            rdf.setString(resource, 'prefix', $scope.dialog.prefix);

            return rdf.getData();
        };
    }
    //
    controller.$inject = ['$scope', 'services.rdf.0.0.0'];
    return controller;
});