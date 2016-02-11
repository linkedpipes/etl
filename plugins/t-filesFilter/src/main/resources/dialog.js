define([], function () {
    function controller($scope, rdfService) {

        $scope.dialog = {
            'pattern': ''
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/t-filesFilter#');

        $scope.setConfiguration = function (inConfig) {
            rdf.setData(inConfig);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.pattern = rdf.getString(resource, 'fileNamePattern');
        };

        $scope.getConfiguration = function () {
            var resource = rdf.secureByType('Configuration');

            rdf.setString(resource, 'fileNamePattern', $scope.dialog.pattern);

            return rdf.getData();
        };
    }
    //
    controller.$inject = ['$scope', 'services.rdf.0.0.0'];
    return controller;
});