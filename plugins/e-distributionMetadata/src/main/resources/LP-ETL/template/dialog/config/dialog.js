define([], function () {
    function controller($scope, $service, rdfService) {

        $scope.dialog = {
            'datasetURI': '',
            'distributionURI': '',
            'useDatasetURIfromInput': true,
            'language_orig': '',
            'title_orig':'',
            'title_en': '',
            'desc_orig': '',
            'desc_en': '',
            'license': '',
            'sparqlEndpointUrl' : '',
            'mediaType' : '',
            'downloadURL' : '',
            'accessURL' : '',
            'exampleResources' : [],
            'useNow': true,
            'modified' : '',
            'issued': '',
            'titleFromDataset': true,
            'generateDistroURIFromDataset': true,
            'originalLanguageFromDataset': true,
            'issuedFromDataset': true,
            'descriptionFromDataset' : true,
            'licenseFromDataset': true,
            'schemaFromDataset': true,
            'useTemporal': true,
            'useNowTemporalEnd': false,
            'temporalFromDataset': true,
            'temporalEnd': '',
            'temporalStart': '',
            'schema': '',
            'schemaType': ''
        };

        $scope.control = { };

        var listToString = function(list) {
            return list.join();
        };

        var stringToList = function(list) {
            if (list === '') {
                return [];
            } else {
                var result = [];
                list.split(',').forEach(function (value) {
                    value = value.trim();
                    if (value !== '') {
                        result.push(value);
                    }
                });
                return result;
            }
        };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/e-distributionMetadata#');

        function loadDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.datasetURI = rdf.getString(resource, 'datasetURI');
            $scope.dialog.distributionURI = rdf.getString(resource, 'distributionURI');
            $scope.dialog.language_orig = rdf.getString(resource, 'language_orig');
            $scope.dialog.title_orig = rdf.getString(resource, 'title_orig');
            $scope.dialog.title_en = rdf.getString(resource, 'title_en');
            $scope.dialog.desc_orig = rdf.getString(resource, 'desc_orig');
            $scope.dialog.desc_en = rdf.getString(resource, 'desc_en');
            $scope.dialog.license = rdf.getString(resource, 'license');
            $scope.dialog.sparqlEndpointUrl = rdf.getString(resource, 'sparqlEndpointUrl');
            $scope.dialog.mediaType = rdf.getString(resource, 'mediaType');
            $scope.dialog.downloadURL = rdf.getString(resource, 'downloadURL');
            $scope.dialog.accessURL = rdf.getString(resource, 'accessURL');

            $scope.dialog.modified = rdf.getDate(resource, 'modified');
            $scope.dialog.issued = rdf.getDate(resource, 'issued');
            $scope.dialog.temporalEnd = rdf.getDate(resource, 'temporalEnd');
            $scope.dialog.temporalStart = rdf.getDate(resource, 'temporalStart');

            $scope.dialog.schema = rdf.getString(resource, 'schema');
            $scope.dialog.schemaType = rdf.getString(resource, 'schemaType');


            $scope.dialog.useDatasetURIfromInput = rdf.getBoolean(resource, 'useDatasetURIfromInput');
            $scope.dialog.useNow = rdf.getBoolean(resource, 'useNow');
            $scope.dialog.titleFromDataset = rdf.getBoolean(resource, 'titleFromDataset');
            $scope.dialog.generateDistroURIFromDataset = rdf.getBoolean(resource, 'generateDistroURIFromDataset');
            $scope.dialog.originalLanguageFromDataset = rdf.getBoolean(resource, 'originalLanguageFromDataset');
            $scope.dialog.issuedFromDataset = rdf.getBoolean(resource, 'issuedFromDataset');
            $scope.dialog.descriptionFromDataset = rdf.getBoolean(resource, 'descriptionFromDataset');
            $scope.dialog.licenseFromDataset = rdf.getBoolean(resource, 'licenseFromDataset');
            $scope.dialog.schemaFromDataset = rdf.getBoolean(resource, 'schemaFromDataset');
            $scope.dialog.useTemporal = rdf.getBoolean(resource, 'useTemporal');
            $scope.dialog.useNowTemporalEnd = rdf.getBoolean(resource, 'useNowTemporalEnd');
            $scope.dialog.temporalFromDataset = rdf.getBoolean(resource, 'temporalFromDataset');

            $scope.dialog.exampleResources = listToString(rdf.getValueList(resource, 'exampleResources'));

            $scope.control = $service.control.fromIri(
                rdf.getIri(resource, 'control'));
        };

        function saveDialog() {
            if ($scope.control.forced) {
                return;
            }

            var resource = rdf.secureByType('Configuration');

            rdf.setString(resource, 'datasetURI', $scope.dialog.datasetURI);
            rdf.setString(resource, 'distributionURI', $scope.dialog.distributionURI);
            rdf.setString(resource, 'language_orig', $scope.dialog.language_orig);
            rdf.setString(resource, 'title_orig', $scope.dialog.title_orig);
            rdf.setString(resource, 'title_en', $scope.dialog.title_en);
            rdf.setString(resource, 'desc_orig', $scope.dialog.desc_orig);
            rdf.setString(resource, 'desc_en', $scope.dialog.desc_en);
            rdf.setString(resource, 'license', $scope.dialog.license);
            rdf.setString(resource, 'sparqlEndpointUrl', $scope.dialog.sparqlEndpointUrl);
            rdf.setString(resource, 'mediaType', $scope.dialog.mediaType);
            rdf.setString(resource, 'downloadURL', $scope.dialog.downloadURL);
            rdf.setString(resource, 'accessURL', $scope.dialog.accessURL);

            rdf.setDate(resource, 'modified', $scope.dialog.issued);
            rdf.setDate(resource, 'issued', $scope.dialog.issued);
            rdf.setDate(resource, 'temporalEnd', $scope.dialog.temporalEnd);
            rdf.setDate(resource, 'temporalStart', $scope.dialog.temporalStart);

            rdf.setString(resource, 'schema', $scope.dialog.schema);
            rdf.setString(resource, 'schemaType', $scope.dialog.schemaType);

            rdf.setBoolean(resource, 'useDatasetURIfromInput', $scope.dialog.useDatasetURIfromInput);
            rdf.setBoolean(resource, 'useNow', $scope.dialog.useNow);
            rdf.setBoolean(resource, 'titleFromDataset', $scope.dialog.titleFromDataset);
            rdf.setBoolean(resource, 'generateDistroURIFromDataset', $scope.dialog.generateDistroURIFromDataset);
            rdf.setBoolean(resource, 'originalLanguageFromDataset', $scope.dialog.originalLanguageFromDataset);
            rdf.setBoolean(resource, 'issuedFromDataset', $scope.dialog.issuedFromDataset);
            rdf.setBoolean(resource, 'descriptionFromDataset', $scope.dialog.descriptionFromDataset);
            rdf.setBoolean(resource, 'licenseFromDataset', $scope.dialog.licenseFromDataset);
            rdf.setBoolean(resource, 'schemaFromDataset', $scope.dialog.schemaFromDataset);
            rdf.setBoolean(resource, 'useTemporal', $scope.dialog.useTemporal);
            rdf.setBoolean(resource, 'useNowTemporalEnd', $scope.dialog.useNowTemporalEnd);
            rdf.setBoolean(resource, 'temporalFromDataset', $scope.dialog.temporalFromDataset);

            rdf.setValueList(resource, 'exampleResources', stringToList($scope.dialog.exampleResources));

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
