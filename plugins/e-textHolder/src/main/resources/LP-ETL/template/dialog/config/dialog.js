define([], function () {
    "use-prefix";

    const PREFIX = 'http://plugins.linkedpipes.com/ontology/e-textHolder#';

    function controller($scope, $service, rdfService) {

        // Obtain the RDF service.
        const RDF = rdfService.create('');

        // Store configuration of the dialog.
        $scope.dialog = {};

        // Load controls from template.
        $scope.template = {}

        // Define the control object on shared scope.
        if ($scope.control === undefined) {
            $scope.control = {};
        }

        function loadDialog() {
            RDF.setData($service.config.instance);
            var resource = RDF.secureByType(PREFIX + 'Configuration');
                $scope.dialog.fileName = RDF.getString(resource,
                    PREFIX + 'fileName');
                $scope.dialog.content = RDF.getString(resource,
                    PREFIX + 'content');
            //
            $scope.control.fileName = $service.control.fromIri(
                RDF.getIri(resource, PREFIX + 'fileNameControl'));
            $scope.control.content = $service.control.fromIri(
                RDF.getIri(resource, PREFIX + 'contentControl'));
        }

        function saveDialog() {
            RDF.setData($service.config.instance);
            var resource = RDF.secureByType(PREFIX + 'Configuration');
            if (!$scope.control.fileName.forced) {
                RDF.setString(resource, PREFIX + 'fileName', $scope.dialog.fileName);
            }
            if (!$scope.control.content.forced) {
                RDF.setString(resource, PREFIX + 'content', $scope.dialog.content);
            }
            //
            RDF.setIri(resource, PREFIX + 'fileNameControl',
                $service.control.toIri($scope.control.fileName));
            RDF.setIri(resource, PREFIX + 'contentControl',
                $service.control.toIri($scope.control.content));
        }

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
