define([], function () {
    function controller($scope, rdfService) {

        $scope.dialog = {
            'usePrefix': false
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/t-unpackZip#');

        $scope.setConfiguration = function (inConfig) {
            rdf.setData(inConfig);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.usePrefix = rdf.getBoolean(resource, 'usePrefix');
        };

        $scope.getConfiguration = function () {
            var resource = rdf.secureByType('Configuration');

            rdf.setBoolean(resource, 'usePrefix', $scope.dialog.usePrefix);

            return rdf.getData();
        };
    }
    //
    controller.$inject = ['$scope', 'services.rdf.0.0.0'];
    return controller;
});