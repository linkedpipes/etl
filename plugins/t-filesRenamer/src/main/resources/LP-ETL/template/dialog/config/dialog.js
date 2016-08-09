define([], function () {
    function controller($scope, $service, rdfService) {

        $scope.dialog = {
            'pattern': '',
            'replaceWith': ''
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/t-filesRenamer#');


        function loadDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.pattern = rdf.getString(resource, 'pattern');
            $scope.dialog.replaceWith = rdf.getString(resource, 'replaceWith');

        };

        function saveDialog() {
            var resource = rdf.secureByType('Configuration');

            rdf.setString(resource, 'pattern', $scope.dialog.pattern);
            rdf.setString(resource, 'replaceWith', $scope.dialog.replaceWith);

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
