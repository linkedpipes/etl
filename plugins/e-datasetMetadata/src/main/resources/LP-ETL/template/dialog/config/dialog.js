define([], function () {
    function controller($scope, $service, rdfService) {

        $scope.dialog = {
            'datasetURI': '',
            'language_orig': '',
            'title_cs': '',
            'title_en': '',
            'desc_cs': '',
            'desc_en': '',
            'authors': [],
            'publisherURI': '',
            'publisherName': '',
            'license': '',
            'sources': [],
            'languages': [],
            'keywords_orig': [],
            'keywords_en': [],
            'themes': [],
            'contactPoint': '',
            'contactPointName': '',
            'periodicity': '',
            'useNow': true,
            'useNowTemporalEnd': false,
            'useTemporal': true,
            'modified': '',
            'issued': '',
            'identifier': '',
            'landingPage': '',
            'temporalEnd': '',
            'temporalStart': '',
            'spatial': '',
            'schema': ''
        };

        $scope.control = { };

        var rdf = rdfService.create('http://plugins.linkedpipes.com/ontology/e-datasetMetadata#');

        var listToString = function(string) {
            return string.join();
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

        function loadDialog() {
            rdf.setData($service.config.instance);
            var resource = rdf.secureByType('Configuration');

            $scope.dialog.datasetURI = rdf.getString(resource, 'datasetURI');
            $scope.dialog.language_orig = rdf.getString(resource, 'language_orig');
            $scope.dialog.title_cs = rdf.getString(resource, 'title_cs');
            $scope.dialog.title_en = rdf.getString(resource, 'title_en');
            $scope.dialog.desc_cs = rdf.getString(resource, 'desc_cs');
            $scope.dialog.desc_en = rdf.getString(resource, 'desc_en');
            $scope.dialog.publisherURI = rdf.getString(resource, 'publisherURI');
            $scope.dialog.publisherName = rdf.getString(resource, 'publisherName');
            $scope.dialog.license = rdf.getString(resource, 'license');
            $scope.dialog.contactPoint = rdf.getString(resource, 'contactPoint');
            $scope.dialog.contactPointName = rdf.getString(resource, 'contactPointName');
            $scope.dialog.periodicity = rdf.getString(resource, 'periodicity');
            $scope.dialog.modified = rdf.getDate(resource, 'modified');
            $scope.dialog.issued = rdf.getDate(resource, 'issued');
            $scope.dialog.identifier = rdf.getString(resource, 'identifier');
            $scope.dialog.landingPage = rdf.getString(resource, 'landingPage');
            $scope.dialog.temporalEnd = rdf.getDate(resource, 'temporalEnd');
            $scope.dialog.temporalStart = rdf.getDate(resource, 'temporalStart');
            $scope.dialog.spatial = rdf.getString(resource, 'spatial');
            $scope.dialog.schema = rdf.getString(resource, 'schema');

            $scope.dialog.useNow = rdf.getBoolean(resource, 'useNow');
            $scope.dialog.useNowTemporalEnd = rdf.getBoolean(resource, 'useNowTemporalEnd');
            $scope.dialog.useTemporal = rdf.getBoolean(resource, 'useTemporal');

            $scope.dialog.authors = listToString(rdf.getValueList(resource, 'authors'));
            $scope.dialog.sources = listToString(rdf.getValueList(resource, 'sources'));
            $scope.dialog.languages = listToString(rdf.getValueList(resource, 'languages'));
            $scope.dialog.keywords_orig = listToString(rdf.getValueList(resource, 'keywords_orig'));
            $scope.dialog.keywords_en = listToString(rdf.getValueList(resource, 'keywords_en'));
            $scope.dialog.themes = listToString(rdf.getValueList(resource, 'themes'));

            $scope.control = $service.control.fromIri(
                rdf.getIri(resource, 'control'));
        };

        function saveDialog() {
            if ($scope.control.forced) {
                return;
            }

            var resource = rdf.secureByType('Configuration');

            rdf.setString(resource, 'datasetURI', $scope.dialog.datasetURI);
            rdf.setString(resource, 'language_orig', $scope.dialog.language_orig);
            rdf.setString(resource, 'title_cs', $scope.dialog.title_cs);
            rdf.setString(resource, 'title_en', $scope.dialog.title_en);
            rdf.setString(resource, 'desc_cs', $scope.dialog.desc_cs);
            rdf.setString(resource, 'desc_en', $scope.dialog.desc_en);
            rdf.setString(resource, 'publisherURI', $scope.dialog.publisherURI);
            rdf.setString(resource, 'publisherName', $scope.dialog.publisherName);
            rdf.setString(resource, 'license', $scope.dialog.license);
            rdf.setString(resource, 'contactPoint', $scope.dialog.contactPoint);
            rdf.setString(resource, 'contactPointName', $scope.dialog.contactPointName);
            rdf.setString(resource, 'periodicity', $scope.dialog.periodicity);
            rdf.setDate(resource, 'modified', $scope.dialog.modified);
            rdf.setDate(resource, 'issued', $scope.dialog.issued);
            rdf.setString(resource, 'identifier', $scope.dialog.identifier);
            rdf.setString(resource, 'landingPage', $scope.dialog.landingPage);
            rdf.setDate(resource, 'temporalEnd', $scope.dialog.temporalEnd);
            rdf.setDate(resource, 'temporalStart', $scope.dialog.temporalStart);
            rdf.setString(resource, 'spatial', $scope.dialog.spatial);
            rdf.setString(resource, 'schema', $scope.dialog.schema);

            rdf.setBoolean(resource, 'useNow', $scope.dialog.useNow);
            rdf.setBoolean(resource, 'useNowTemporalEnd', $scope.dialog.useNowTemporalEnd);
            rdf.setBoolean(resource, 'useTemporal', $scope.dialog.useTemporal);

            rdf.setValueList(resource, 'authors', stringToList($scope.dialog.authors));
            rdf.setValueList(resource, 'sources', stringToList($scope.dialog.sources));
            rdf.setValueList(resource, 'languages', stringToList($scope.dialog.languages));
            rdf.setValueList(resource, 'keywords_orig', stringToList($scope.dialog.keywords_orig));
            rdf.setValueList(resource, 'keywords_en', stringToList($scope.dialog.keywords_en));
            rdf.setValueList(resource, 'themes', stringToList($scope.dialog.themes));

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
