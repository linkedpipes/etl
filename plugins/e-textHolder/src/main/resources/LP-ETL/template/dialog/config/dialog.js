define([], function () {
    "use-prefix";

    const PREFIX = 'http://plugins.linkedpipes.com/ontology/e-textHolder#';

    function controlToObject(value) {
        switch (value) {
            case "http://plugins.linkedpipes.com/resource/configuration/None":
                return {
                    'inherit': false,
                    'force': false,
                    'forced': false
                }
            case "http://plugins.linkedpipes.com/resource/configuration/Inherit":
                return {
                    'inherit': true,
                    'force': false,
                    'forced': false
                }
            case "http://plugins.linkedpipes.com/resource/configuration/Force":
                // This apply for templates.
                return {
                    'inherit': false,
                    'force': true,
                    'forced': false
                }
            case "http://plugins.linkedpipes.com/resource/configuration/InheritAndForce":
                // This apply for templates.
                return {
                    'inherit': true,
                    'force': true,
                    'forced': false
                }
            case "http://plugins.linkedpipes.com/resource/configuration/Forced":
                return {
                    'forced': true
                }
        }
    }

    function objectToControl(value) {
        if (value.forced) {
            return 'http://plugins.linkedpipes.com/resource/configuration/Forced';
        }
        if (value.inherit) {
            if (value.force) {
                return "http://plugins.linkedpipes.com/resource/configuration/InheritAndForce";
            } else {
                return "http://plugins.linkedpipes.com/resource/configuration/Inherit";
            }
        } else {
            if (value.force) {
                return "http://plugins.linkedpipes.com/resource/configuration/Force";
            } else {
                return "http://plugins.linkedpipes.com/resource/configuration/None";
            }
        }
    }

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
            $scope.control.fileName = controlToObject(RDF.getIri(resource,
                PREFIX + 'fileNameControl'));
            $scope.control.content = controlToObject(RDF.getIri(resource,
                PREFIX + 'contentControl'));
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
                objectToControl($scope.control.fileName));
            RDF.setIri(resource, PREFIX + 'contentControl',
                objectToControl($scope.control.content));
        }

        function loadTemplate() {
            RDF.setData($service.config.template);
            var resource = RDF.secureByType(PREFIX + 'Control');
            $scope.template.fileNameForce = RDF.getBoolean(resource,
                PREFIX + 'fileNameForce');
        }

        // Define the save function.
        $service.onStore = function () {
            saveDialog();
        }

        // Load data.
        loadDialog();
        loadTemplate();

    }

    controller.$inject = ['$scope', '$service', 'services.rdf.0.0.0'];
    return controller;
});
