define([], function () {

    const PREFIX = 'http://plugins.linkedpipes.com/ontology/t-sparqlSelect#';

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
            $scope.dialog.query = rdf.getString(resource,
                PREFIX + 'query');
            $scope.dialog.fileName = rdf.getString(resource,
                PREFIX + 'fileName');
            //
            $scope.control.query = $service.control.fromIri(
                rdf.getIri(resource, PREFIX + 'queryControl'));
            $scope.control.fileName = $service.control.fromIri(
                rdf.getIri(resource, PREFIX + 'fileNameControl'));
        }

        function saveDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType(PREFIX + 'Configuration');
            //
            if (!$scope.control.query.forced) {
                rdf.setString(resource, PREFIX + 'query',
                    $scope.dialog.query);
            }
            if (!$scope.control.fileName.forced) {
                rdf.setString(resource, PREFIX + 'fileName',
                    $scope.dialog.fileName);
            }
            //
            rdf.setIri(resource, PREFIX + 'queryControl',
                $service.control.toIri($scope.control.query));
            rdf.setIri(resource, PREFIX + 'fileNameControl',
                $service.control.toIri($scope.control.fileName));
        }

        $service.onStore = function () {
            saveDialog();
        }

        loadDialog();
    }

    controller.$inject = ['$scope', '$service', 'services.rdf.0.0.0'];
    return controller;
});
