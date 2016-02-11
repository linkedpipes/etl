define([], function () {
    function controller($scope, rdfService) {

        $scope.dialog = {
            'fileName': '',
            'content': ''
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/e-textHolder#');

        $scope.setConfiguration = function (inConfig) {
            rdf.setData(inConfig);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.fileName = rdf.getString(resource, 'fileName');
            $scope.dialog.content = rdf.getString(resource, 'content');
        };

        $scope.getConfiguration = function () {
            var resource = rdf.secureByType('Configuration');

            rdf.setString(resource, 'fileName', $scope.dialog.fileName);
            rdf.setString(resource, 'content', $scope.dialog.content);

            return rdf.getData();
        };
    }
    //
    controller.$inject = ['$scope', 'services.rdf.0.0.0'];
    return controller;
});