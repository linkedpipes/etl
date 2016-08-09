define([], function () {
    function controller($scope, $service, rdfService) {

        $scope.dialog = {
            'pattern': ''
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/t-filesFilter#');

        function loadDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.pattern = rdf.getString(resource, 'fileNamePattern');
        };

        function saveDialog() {
            var resource = rdf.secureByType('Configuration');

            rdf.setString(resource, 'fileNamePattern', $scope.dialog.pattern);

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
