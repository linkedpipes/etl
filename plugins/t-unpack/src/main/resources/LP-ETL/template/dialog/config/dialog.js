define([], function () {
    function controller($scope, $service, rdfService) {

        $scope.dialog = {
            'usePrefix': false,
            'format': ''
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/t-unpack#');

        function loadDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.usePrefix = rdf.getBoolean(resource, 'usePrefix');
            $scope.dialog.format = rdf.getString(resource, 'format');
        };

        function saveDialog() {
            var resource = rdf.secureByType('Configuration');

            rdf.setBoolean(resource, 'usePrefix', $scope.dialog.usePrefix);
            rdf.setString(resource, 'format', $scope.dialog.format);

            return rdf.getData();
        };

        // Define the save function.
        $service.onStore = function () {
            saveDialog();
        }

        // Load data.
        loadDialog();
    }
    //
    controller.$inject = ['$scope', '$service', 'services.rdf.0.0.0'];
    return controller;
});
