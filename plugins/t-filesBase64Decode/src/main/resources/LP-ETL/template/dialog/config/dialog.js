define([], function () {
    function controller($scope, rdfService) {

        $scope.dialog = {
            'skipOnError': false
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/t-fileDecode#');

        $scope.setConfiguration = function (inConfig) {
            rdf.setData(inConfig);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.skipOnError = rdf.getBoolean(resource, 'skipOnError');
        };

        $scope.getConfiguration = function () {
            var resource = rdf.secureByType('Configuration');

            rdf.setBoolean(resource, 'skipOnError', $scope.dialog.skipOnError);

            return rdf.getData();
        };
    }
    //
    controller.$inject = ['$scope', 'services.rdf.0.0.0'];
    return controller;
});