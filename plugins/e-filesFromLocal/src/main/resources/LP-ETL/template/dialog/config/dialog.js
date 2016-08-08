define([], function () {
    function controller($scope, rdfService) {

        $scope.dialog = {
            'path': ''
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/e-filesFromLocal#');

        $scope.setConfiguration = function (inConfig) {
            rdf.setData(inConfig);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.path = rdf.getString(resource, 'path');
        };

        $scope.getConfiguration = function () {
            var resource = rdf.secureByType('Configuration');

            rdf.setString(resource, 'path', $scope.dialog.path);

            return rdf.getData();
        };
    }
    //
    controller.$inject = ['$scope', 'services.rdf.0.0.0'];
    return controller;
});