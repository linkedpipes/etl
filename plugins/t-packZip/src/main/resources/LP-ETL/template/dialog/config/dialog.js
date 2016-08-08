define([], function () {
    function controller($scope, rdfService) {

        $scope.dialog = {
            'fileName': ''
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/t-packZip#');

        $scope.setConfiguration = function (inConfig) {
            rdf.setData(inConfig);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.fileName = rdf.getString(resource, 'fileName');
        };

        $scope.getConfiguration = function () {
            var resource = rdf.secureByType('Configuration');

            rdf.setString(resource, 'fileName', $scope.dialog.fileName);

            return rdf.getData();
        };
    }
    //
    controller.$inject = ['$scope', 'services.rdf.0.0.0'];
    return controller;
});