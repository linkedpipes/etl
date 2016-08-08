define([], function () {
    function controller($scope, rdfService) {

        $scope.dialog = {
            'hardRedirect': true,
            'skipOnError' : false
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/e-httpGetFiles#');

        $scope.setConfiguration = function (inConfig) {
            rdf.setData(inConfig);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.hardRedirect = rdf.getBoolean(resource, 'hardRedirect');
            $scope.dialog.skipOnError = rdf.getBoolean(resource, 'skipOnError');
        };

        $scope.getConfiguration = function () {
            var resource = rdf.secureByType('Configuration');

            rdf.setBoolean(resource, 'hardRedirect', $scope.dialog.hardRedirect);
            rdf.setBoolean(resource, 'skipOnError', $scope.dialog.skipOnError);

            return rdf.getData();
        };
    }
    //
    controller.$inject = ['$scope', 'services.rdf.0.0.0'];
    return controller;
});