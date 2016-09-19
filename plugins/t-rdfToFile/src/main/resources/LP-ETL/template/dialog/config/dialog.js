define([], function () {

    const PREFIX = 'http://plugins.linkedpipes.com/ontology/t-rdfToFile#';

    function controller($scope, $service, rdfService) {

        $scope.dialog = {};

        if ($scope.control === undefined) {
            $scope.control = {};
        }

        var rdf = rdfService.create();

        function loadDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType(PREFIX + 'Configuration');
            //
            $scope.dialog.fileName = rdf.getString(resource,
                PREFIX + 'fileName');
            $scope.dialog.fileType = rdf.getString(resource,
                PREFIX + 'fileType');
            $scope.dialog.graphUri = rdf.getString(resource,
                PREFIX + 'graphUri');
            //
            $scope.control.fileName = $service.control.fromIri(
                rdf.getIri(resource, PREFIX + 'fileNameControl'));
            $scope.control.fileType = $service.control.fromIri(
                rdf.getIri(resource, PREFIX + 'fileTypeControl'));
            $scope.control.graphUri = $service.control.fromIri(
                rdf.getIri(resource, PREFIX + 'graphUriControl'));
        }

        function saveDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType('Configuration');
            //
            if (!$scope.control.fileName.forced) {
                rdf.setString(resource, PREFIX + 'fileName',
                    $scope.dialog.fileName);
            }
            if (!$scope.control.fileType.forced) {
                rdf.setString(resource, PREFIX + 'fileType',
                    $scope.dialog.fileType);
            }
            if (!$scope.control.graphUri.forced) {
                rdf.setString(resource, PREFIX + 'graphUri',
                    $scope.dialog.graphUri);
            }
            //
            rdf.setIri(resource, PREFIX + 'fileNameControl',
                $service.control.toIri($scope.control.fileName));
            rdf.setIri(resource, PREFIX + 'fileTypeControl',
                $service.control.toIri($scope.control.fileType));
            rdf.setIri(resource, PREFIX + 'graphUriControl',
                $service.control.toIri($scope.control.graphUri));
        }

        $service.onStore = function () {
            saveDialog();
        }

        loadDialog();
    }

    controller.$inject = ['$scope', '$service', 'services.rdf.0.0.0'];
    return controller;
});
