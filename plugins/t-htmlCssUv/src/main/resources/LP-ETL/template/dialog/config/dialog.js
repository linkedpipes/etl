define([], function () {
    "use-prefix";

    const PREFIX = 'http://plugins.linkedpipes.com/ontology/t-htmlCssUv#';

    function controller($scope, $service, rdfService) {

        // Obtain the RDF service.
        const RDF = rdfService.create('');

        // Store configuration of the dialog.
        $scope.dialog = {
            'class': '',
            'predicate': '',
            'includeSourceInformation': undefined,
            'records': []
        };

        $scope.onDelete = function (index) {
            $scope.dialog.records.splice(index, 1);
        };

        $scope.onAdd = function () {
            $scope.dialog.records.push({
                'name': '',
                'type': 'QUERY'
            });
            //
            console.log('onAdd', $scope.dialog.records);
        };

        function loadDialog() {
            RDF.setData($service.config.instance);
            var resource = RDF.secureByType(PREFIX + 'Configuration');
            //
            $scope.dialog.class = RDF.getString(resource, PREFIX + 'class');
            $scope.dialog.predicate = RDF.getString(resource,
                PREFIX + 'predicate');
            $scope.dialog.includeSourceInformation = RDF.getBoolean(resource,
                PREFIX + 'includeSourceInformation');
            //
            var actions = RDF.getObjects(resource, PREFIX + 'action');
            actions.forEach(function (action) {
                var record = {
                    '@id': action['@id'],
                    'name': RDF.getString(action, PREFIX + 'name'),
                    'type': RDF.getString(action, PREFIX + 'type'),
                    'data': RDF.getString(action, PREFIX + 'data'),
                    'output': RDF.getString(action, PREFIX + 'output')
                };
                $scope.dialog.records.push(record);
            });
        }

        function saveDialog() {
            RDF.setData($service.config.instance);
            var resource = RDF.secureByType(PREFIX + 'Configuration');
            //
            RDF.setString(resource, PREFIX + 'class', $scope.dialog.class);
            RDF.setString(resource, PREFIX + 'predicate',
                $scope.dialog.predicate);
            RDF.setBoolean(resource, PREFIX + 'includeSourceInformation',
                $scope.dialog.includeSourceInformation);
            //
            var actions = [];
            $scope.dialog.records.forEach(function (record) {
                var id = record['@id'];
                var action;
                if (id) {
                    // Try to read existing object.
                    action = RDF.filterSingle(RDF.findByUri(id));
                }
                if (!id || !action) {
                    // New object.
                    action = RDF.createObject(PREFIX + 'Column');
                }
                // Now we have object to store data into.
                RDF.setString(action, PREFIX + 'name', record.name);
                RDF.setString(action, PREFIX + 'type', record.type);
                RDF.setString(action, PREFIX + 'data', record.data);
                RDF.setString(action, PREFIX + 'output', record.output);
                //
                actions.push(action);
            });
            // Do not add new object, as we have already add them.
            RDF.updateObjects(resource, PREFIX + 'action', actions, false);
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
