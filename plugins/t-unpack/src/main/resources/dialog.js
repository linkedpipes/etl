define([], function () {
    function controller($scope, rdfService) {

        $scope.dialog = {
            'usePrefix': false,
            'format': ''
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/t-unpack#');

        $scope.setConfiguration = function (inConfig) {
            rdf.setData(inConfig);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.usePrefix = rdf.getBoolean(resource, 'usePrefix');
            $scope.dialog.format = rdf.getString(resource, 'format');
        };

        $scope.getConfiguration = function () {
            var resource = rdf.secureByType('Configuration');

            rdf.setBoolean(resource, 'usePrefix', $scope.dialog.usePrefix);
            rdf.setString(resource, 'format', $scope.dialog.format);

            return rdf.getData();
        };
    }
    //
    controller.$inject = ['$scope', 'services.rdf.0.0.0'];
    return controller;
});