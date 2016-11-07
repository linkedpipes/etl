define(['jquery'], function (jQuery) {
    "use strict";

    const JAR_TEMPLATE_SELECTOR = {
        'property': '@type',
        'operation': 'in',
        'value': 'http://linkedpipes.com/ontology/JarTemplate'
    };

    const JAR_TEMPLATE_TEMPLATE = {
        'id': {
            '$resource': ''
        },
        'label': {
            '$property': 'http://www.w3.org/2004/02/skos/core#prefLabel'
        },
        'keyword': {
            '$property': 'http://linkedpipes.com/ontology/keyword'
        },
        'color': {
            '$property': 'http://linkedpipes.com/ontology/color'
        },
        'configurationUri': {
            '$property': 'http://linkedpipes.com/ontology/configurationGraph'
        },
        'type': {
            '$property': 'http://linkedpipes.com/ontology/componentType'
        },
        'dialogs': {
            '$property': 'http://linkedpipes.com/ontology/dialog',
            '$oneToMany': {
                'name': {
                    '$property': 'http://linkedpipes.com/ontology/name'
                }
            }
        },
        'ports': {
            '$property': 'http://linkedpipes.com/ontology/port',
            '$oneToMany': {
                'label': {
                    '$property': 'http://www.w3.org/2004/02/skos/core#prefLabel'
                },
                'binding': {
                    '$property': 'http://linkedpipes.com/ontology/binding'
                },
                'types': {
                    '$property': '@type'
                }
            }
        }
    };

    const TEMPLATE_SELECTOR = {
        'property': '@type',
        'operation': 'in',
        'value': 'http://linkedpipes.com/ontology/Template'
    };

    const TEMPLATE_TEMPLATE = {
        'id': {
            '$resource': ''
        },
        'label': {
            '$property': 'http://www.w3.org/2004/02/skos/core#prefLabel'
        },
        'description': {
            '$property': 'http://purl.org/dc/terms/description'
        },
        'template': {
            '$property': 'http://linkedpipes.com/ontology/template'
        }
    };

    /**
     * Prepare and set filtering string for given component.
     * 
     * @param template
     */
    function prepareFilterString(template) {
        template['filterString'] = template.label.toLowerCase();
        if (template['keyword'] === undefined) {
            // We do not update the filter as there are no
            // keywords.
        } else if (jQuery.isArray(template['keyword'])) {
            template['keyword'].forEach(function (word) {
                template['filterString'] += ',' + word.toLowerCase();
            });
        } else {
            // They keyword is a string.
            template['filterString'] +=
                ',' + template['keyword'].toLowerCase();
        }
    }

    /**
     * Update component after raw loading.
     *
     * @param template
     */
    function decorateJarTemplate(template) {
        // Update dialog - we need to add references.
        if (template['dialogs'] === undefined) {
            template['dialogs'] = [];
        }
        template['dialogs'].forEach(function (item) {
            item['iri'] = template['id'];
        });
        // Sort dialogs.
        template['dialogs'].sort(function(left, right) {
            // Config is the left top most.
            // TODO The dialog should provide order of the tabs.
            if (left['name'] === 'config'){
                return false;
            }
            return left['name'] > right['name'];
        });
        // Check ports.
        var inputs = [];
        var outputs = [];
        if (template['ports'] === undefined) {
            template['ports'] = [];
        }
        template['ports'].forEach(function (port) {
            var newPort = {
                'label': port['label'],
                'binding': port['binding'],
                'content': undefined
            };
            port['types'].forEach(function (type) {
                if (type === 'http://linkedpipes.com/ontology/Port') {
                    // Ignore.
                } else if (type === 'http://linkedpipes.com/ontology/Input') {
                    inputs.push(newPort);
                } else if (type === 'http://linkedpipes.com/ontology/Output') {
                    outputs.push(newPort);
                } else {
                    // Add all others to port type.
                    newPort['content'] = type;
                }
            });
        });
        template['inputs'] = inputs;
        template['outputs'] = outputs;
    }

    function factoryFunction($http, jsonldService) {

        var service = {};

        var templates = {
            'list': [],
            'map': {}
        };

        /**
         * Perform full load of templates.
         *
         * @param onSuccess Called on success.
         * @param onFailure Called on failure.
         */
        service.load = function (onSuccess, onFailure) {
            $http.get("./resources/components").then(function (response) {
                    console.time('loading templates');
                    // Load jar templates/.
                    var jarTemplates = jsonldService.toJson(response.data,
                        JAR_TEMPLATE_SELECTOR, JAR_TEMPLATE_TEMPLATE);
                    jarTemplates.forEach(function (template) {
                        // Check if it's new.
                        if (templates.map[template['id']]) {
                            // Item was already stored! This can happen on the
                            // reload (update).
                            return;
                        }
                        // Update definition.
                        prepareFilterString(template);
                        decorateJarTemplate(template);
                        // Store.
                        templates.map[template['id']] = template;
                        templates.list.push(template);
                    });
                    // Load templates.
                    var refTemplates = jsonldService.toJson(response.data,
                        TEMPLATE_SELECTOR, TEMPLATE_TEMPLATE);
                    refTemplates.forEach(function (template) {
                        // Check if it's new.
                        if (templates.map[template['id']]) {
                            // Item was already stored! This can happen on the
                            // reload (update).
                            return;
                        }
                        //
                        var fullTemplate = jQuery.extend({},
                            templates.map[template['template']],
                            template);
                        // Update definition.
                        prepareFilterString(fullTemplate);
                        // Store.
                        templates.map[fullTemplate['id']] = fullTemplate;
                        templates.list.push(fullTemplate);
                    });
                    console.timeEnd('loading templates');
                    if (onSuccess) {
                        onSuccess();
                    }
                }, function (response) {
                    if (onFailure) {
                        onFailure(response);
                    }
                }
            );
        }


        /**
         *
         * @returns List of templates.
         */
        service.getTemplates = function () {
            return templates.list;
        };

        /**
         *
         * @param iri Template IRI.
         * @returns Template object or nothing if URI is invalid.
         */
        service.getTemplate = function (iri) {
            return templates.map[iri];
        };

        /**
         * Try to map given template IRI to the stored templates.
         *
         * @param iri
         * @returns
         */
        service.mapToIri = function (iri) {
            var postfix = iri.substring(iri.indexOf('/resources/components/'));
            for (var key in templates.map) {
                if (key.endsWith(postfix)) {
                    return key;
                }
            }
            console.warn("Can't map template:", iri);
        };

        var fetchEffectiveConfiguration = function (template, onSuccess, onFailure) {
            // Get configuration used for new instances.
            // TODO Add configuration for template, instance, description.
            // TODO Check for updates on configuration.
            var url = '/api/v1/components/effective?iri=' +
                encodeURI(template['id']);
            $http.get(url, {'headers': {'Accept': 'application/ld+json'}}, {
                // Suppress default AngularJS conversion.
                transformResponse: function (data, headersGetter) {
                    return data;
                }
            }).then(function (response) {
                template['effective'] = response.data;
                if (onSuccess) {
                    onSuccess();
                }
            }, function (response) {
                if (onFailure) {
                    onFailure(response);
                }
            });
        };

        /**
         * Make sure that given template has configuration loaded.
         *
         * @param template
         * @param onSuccess Called on success.
         * @param onFailure Called on failure, parameter is a failure message from $http.get.
         */
        service.fetchNewConfig = function (template, onSuccess, onFailure) {
            // Get configuration used for new instances.
            // TODO Add configuration for template, instance, description.
            // TODO Check for updates on configuration.
            var url = '/api/v1/components/configTemplate?iri=' +
                encodeURI(template['id']);
            $http.get(url, {'headers': {'Accept': 'application/ld+json'}}, {
                // Suppress default AngularJS conversion.
                transformResponse: function (data, headersGetter) {
                    return data;
                }
            }).then(function (response) {
                template['configuration'] = response.data;
                fetchEffectiveConfiguration(template, onSuccess, onFailure);
            }, function (response) {
                if (onFailure) {
                    onFailure(response);
                }
            });
        };

        return service;
    }

    factoryFunction.$inject = ['$http', 'services.jsonld'];

    return function register(app) {
        app.factory('components.templates.services.repository', factoryFunction);
    };

})
;
