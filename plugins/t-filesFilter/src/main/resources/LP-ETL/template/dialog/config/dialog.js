define([], function () {

    const PREFIX = 'http://plugins.linkedpipes.com/ontology/t-filesFilter#';

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
            $scope.dialog.pattern = rdf.getString(resource,
                PREFIX + 'fileNamePattern');
            //
            $scope.control.fileNamePattern = $service.control.fromIri(
                rdf.getIri(resource, PREFIX + 'fileNamePatternControl'));
        }

        function saveDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType(PREFIX + 'Configuration');
            //
            if (!$scope.control.fileNamePattern.forced) {
                rdf.setString(resource, PREFIX + 'fileNamePattern',
                    $scope.dialog.pattern);
            }
            //
            rdf.setIri(resource, PREFIX + 'fileNamePatternControl',
                $service.control.toIri($scope.control.fileNamePattern));
        }

        $service.onStore = function () {
            saveDialog();
        }

        loadDialog();
    }

    controller.$inject = ['$scope', '$service', 'services.rdf.0.0.0'];
    return controller;
});
