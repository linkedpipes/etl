define([], function () {
    function controller($scope, rdfService) {

        $scope.dialog = {
            'hardRedirect': false
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/e-httpGetFile#');

        $scope.setConfiguration = function (inConfig) {
            rdf.setData(inConfig);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.hardRedirect = rdf.getBoolean(resource, 'hardRedirect');
        };

        $scope.getConfiguration = function () {
            var resource = rdf.secureByType('Configuration');

            rdf.setBoolean(resource, 'hardRedirect', $scope.dialog.hardRedirect);

            return rdf.getData();
        };
    }
    //
    controller.$inject = ['$scope', 'services.rdf.0.0.0'];
    return controller;
});