/**
 * Directive used to display the detail of a component.
 *
 * API must implement methods:
 *  show({name})
 *
 * The content of the instance and instanceConfig must not change unless
 * the onSave method is called.
 *
 */
"use strict";
define(['jquery', 'app/components/configuration/lpComponentReferenceDirective'], function (jQuery, referenceDirective) {

    // Predefined dialog labels and visibility.
    const DIALOG_LABELS = {
        'config': 'Configuration',
        'template': 'Template',
        'instance': 'Inheritance'
    }

    function loadGeneralTab($scope, JSONLD) {
        const instance = $scope.instance;
        const template = $scope.template;
        $scope.general = {
            'label': JSONLD.getString(instance,
                'http://www.w3.org/2004/02/skos/core#prefLabel'),
            'description': JSONLD.getString(instance,
                'http://purl.org/dc/terms/description'),
            'color': JSONLD.getString(instance,
                'http://linkedpipes.com/ontology/color')
        };
        if ($scope.general.color) {
            $scope.general.templateColor = false;
            $scope.general.color =
                instance['http://linkedpipes.com/ontology/color'];
        } else {
            $scope.general.templateColor = true;
            $scope.general.color = template['color'];
        }
    };

    /**
     * Load configurations into given dialog service.
     *
     * @param dialogService
     * @param template
     * @param JSONLD
     * @param pipeline
     */
    function loadConfiguration($scope, dialogService, JSONLD) {
        dialogService.config.instance = $scope.instanceConfig;
        dialogService.config.template = $scope.templateConfig;
    }


    function addDialogs($scope, dialogService, JSONLD, $rootScope) {
        const template = $scope.template;
        // First load configurations, so they are ready
        // to use in the dialogs.
        loadConfiguration($scope, dialogService, JSONLD);
        // Shared scope for all dialogs.
        var dialogScope = $rootScope.$new(true);
        // Create dialog objects.
        for (var index in template['dialogs']) {
            var dialog = template['dialogs'][index];
            // Check visibility
            if (!$scope.api.show(dialog['name'])) {
                continue;
            }
            // Base iri used to get resources.
            var baseUrl = '/api/v1/components/dialog?iri=' +
                encodeURI(dialog['iri']) + '&name=' +
                encodeURI(dialog['name']) +
                '&file=';
            // Add dialog instance.
            $scope.dialogs.push({
                'label': DIALOG_LABELS[dialog['name']],
                'scope': dialogScope,
                'locals': {
                    '$service': dialogService
                },
                'html': baseUrl + 'dialog.html',
                'js': baseUrl + 'dialog.js'
            })
        }
    }

    function saveGeneralTab($scope) {
        const instance = $scope.instance;
        // We can save values directly as the storage,
        // will parse and standardise them.
        instance['http://www.w3.org/2004/02/skos/core#prefLabel']
            = $scope.general.label;
        instance['http://purl.org/dc/terms/description']
            = $scope.general.description;
        if ($scope.general.templateColor) {
            delete instance['http://linkedpipes.com/ontology/color'];
        } else {
            instance['http://linkedpipes.com/ontology/color']
                = $scope.general.color;
        }
    };


    function saveConfiguration($scope, dialogService, JSONLD) {
        // Just save values from dialogs to configuration objects.
        if (dialogService.onStore === undefined) {
            return;
        }
        dialogService.onStore();
    }

    function directive($rootScope, jsonldService) {

        const JSONLD = jsonldService.jsonld();

        function link($scope) {

            // Store list of dialogs.
            $scope.dialogs = [];

            // The services used to communicate with the dialogs.
            $scope.dialogService = {
                /**
                 * Object with the configurations.
                 */
                'config': {
                    'instance': [],
                    'template': []
                },
                /**
                 * A callback function dialog must implement.
                 * Upon call of this function the config.instance must
                 * be updated and ready to be saved as the configuration.
                 */
                'onStore': undefined
            };

            loadGeneralTab($scope, JSONLD);
            addDialogs($scope, $scope.dialogService, JSONLD, $rootScope);
        }

        function controller($scope) {

            /**
             * Update configuration objects with values from dialogs.
             */
            $scope.api.onSave = function () {
                saveGeneralTab($scope);
                saveConfiguration($scope, $scope.dialogService, JSONLD);
            };

        }

        return {
            'restrict': 'E',
            'scope': {
                'api': '=',
                'instance': '=',
                'template': '=',
                'instanceConfig': '=',
                'templateConfig': '='
            },
            'templateUrl': 'app/components/componentDetailDirective/lpComponentDetailView.html',
            'link': link,
            'controller': controller
        };

    };

    var initialized = false;
    return (app) => {
        if (initialized) {
            return;
        }
        initialized = true;
        referenceDirective(app);
        app.directive('lpComponentDetail',
            ['$rootScope', 'services.jsonld', directive]);
    }

})
