define([], function () {
    function controller($scope, $service, rdfService) {

        $scope.dialog = {
            'regexp': '',
            'source': '',
            'preserveMetadata': false,
            'binding': []
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/t-valueParser#');

        function loadDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.regexp = rdf.getString(resource, 'regexp');
            $scope.dialog.source = rdf.getString(resource, 'source');
            $scope.dialog.preserveMetadata = rdf.getBoolean(resource,
                'preserveMetadata');

            var bindings = rdf.getObjects(resource, 'binding');
            bindings.forEach(function(item) {
                var record = {
                    '@id' : item['@id'],
                    'group': rdf.getString(item, 'group'),
                    'target': rdf.getString(item, 'target')
                };
                $scope.dialog.binding.push(record);
            });

        };

        function saveDialog() {
            var resource = rdf.secureByType('Configuration');

            rdf.setString(resource, 'regexp', $scope.dialog.regexp);
            rdf.setString(resource, 'source', $scope.dialog.source);
            rdf.setBoolean(resource, 'preserveMetadata',
                $scope.dialog.preserveMetadata);

            var bindings = [];
            $scope.dialog.binding.forEach(function(item) {
                // Check values.
                if (item['group'] === '' && item['target'] === '') {
                    return;
                }
                //
                var id = item['@id'];
                var record;
                if (id) {
                    // Try to read existing object.
                    record = rdf.filterSingle(rdf.findByUri(id));
                }
                if (!id || !record) {
                    // New object.
                    record = rdf.createObject('Binding');
                }
                // Now we have object to store data into.
                rdf.setString(record, 'group', item['group']);
                rdf.setString(record, 'target', item['target']);
                bindings.push(record);
            });
            // Do not add new object, as we have already add them.
            rdf.updateObjects(resource, 'binding', bindings, false);

            return rdf.getData();
        };

        $scope.onAdd = function () {
            $scope.dialog.binding.push({
                'group': '',
                'target': ''
            });
        };

        $scope.onDelete = function (index) {
            $scope.dialog.binding.splice(index, 1);
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
