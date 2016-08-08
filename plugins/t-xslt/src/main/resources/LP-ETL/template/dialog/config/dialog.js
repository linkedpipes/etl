define([], function () {
    function controller($scope, rdfService) {

        $scope.dialog = {
            'xslt': '',
            'extension': ''
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/t-xslt#');

        $scope.setConfiguration = function (inConfig) {
            rdf.setData(inConfig);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.xslt = rdf.getString(resource, 'template');
            $scope.dialog.extension = rdf.getString(resource, 'extension');
        };

        $scope.getConfiguration = function () {
            var resource = rdf.secureByType('Configuration');

            rdf.setString(resource, 'template', $scope.dialog.xslt);
            rdf.setString(resource, 'extension', $scope.dialog.extension);

            return rdf.getData();
        };
    }
    //
    controller.$inject = ['$scope', 'services.rdf.0.0.0'];
    return controller;
});