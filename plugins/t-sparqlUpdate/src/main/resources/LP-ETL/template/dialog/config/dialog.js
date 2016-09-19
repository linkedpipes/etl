define([], function () {

    const PREFIX = 'http://plugins.linkedpipes.com/ontology/t-sparqlUpdate#';

    function controller($scope, $service, rdfService) {

        $scope.dialog = {};

        if ($scope.control === undefined) {
            $scope.control = {};
        }

        var rdf = rdfService.create('');

        function loadDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType(PREFIX + 'Configuration');
            //
            $scope.dialog.query = rdf.getString(resource, PREFIX + 'query');
            //
            $scope.control.query = $service.control.fromIri(
                rdf.getIri(resource, PREFIX + 'queryControl'));

            console.log('load:', resource);
            console.log('$scope.dialog:', $scope.dialog);
        }

        function saveDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType(PREFIX + 'Configuration');
            //
            if (!$scope.control.query.forced) {
                rdf.setString(resource, PREFIX + 'query',
                    $scope.dialog.query);
            }
            //
            rdf.setIri(resource, PREFIX + 'queryControl',
                $service.control.toIri($scope.control.query));

            console.log('save:', resource)
        }

        $service.onStore = function () {
            saveDialog();
        }

        loadDialog();
    }

    controller.$inject = ['$scope', '$service', 'services.rdf.0.0.0'];
    return controller;
});
