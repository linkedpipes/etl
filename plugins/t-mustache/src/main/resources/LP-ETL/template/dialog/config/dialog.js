define([], function () {
    function controller($scope, rdfService) {

        $scope.dialog = {
            'class': '',
            'template': ''
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/t-mustache#');

        $scope.setConfiguration = function (inConfig) {
            rdf.setData(inConfig);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.class = rdf.getString(resource, 'class');
            $scope.dialog.template = rdf.getString(resource, 'template');
        };

        $scope.getConfiguration = function () {
            var resource = rdf.secureByType('Configuration');

            rdf.setString(resource, 'class', $scope.dialog.class);
            rdf.setString(resource, 'template', $scope.dialog.template);

            return rdf.getData();
        };
    }
    //
    controller.$inject = ['$scope', 'services.rdf.0.0.0'];
    return controller;
});