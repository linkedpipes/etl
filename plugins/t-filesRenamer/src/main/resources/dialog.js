define([], function () {
    function controller($scope, rdfService) {

        $scope.dialog = {
            'pattern': '',
            'replaceWith': ''
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/t-filesRenamer#');

        $scope.setConfiguration = function (inConfig) {
            rdf.setData(inConfig);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.pattern = rdf.getString(resource, 'pattern');
            $scope.dialog.replaceWith = rdf.getString(resource, 'replaceWith');

        };

        $scope.getConfiguration = function () {
            var resource = rdf.secureByType('Configuration');

            rdf.setString(resource, 'pattern', $scope.dialog.pattern);
            rdf.setString(resource, 'replaceWith', $scope.dialog.replaceWith);

            return rdf.getData();
        };
    }
    //
    controller.$inject = ['$scope', 'services.rdf.0.0.0'];
    return controller;
});
