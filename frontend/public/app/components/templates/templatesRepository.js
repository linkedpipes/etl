define(['jquery'], function (jQuery) {
    function factoryFunction($http, jsonldService) {
        var service = {};

        var templates = {
            'list': [],
            'map': {}
        };

        /**
         * Perform full load of templates.
         *
         * @param onSucess Called on sucess.
         * @param onFailure Called on failure.
         */
        service.load = function (onSucess, onFailure) {
            $http.get("./resources/components").then(function (response) {
                console.time('loading templates');
                var newTemplates = jsonldService.toJson(response.data, {
                    'property': '@type',
                    'operation': 'in',
                    'value': 'http://linkedpipes.com/ontology/Component'
                }, {
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
                    'dialog': {
                        '_dialog': {
                            '$property': 'http://linkedpipes.com/ontology/dialog',
                            '$oneToOne': {
                                'js': {
                                    '$property': 'http://linkedpipes.com/ontology/js'
                                },
                                'html': {
                                    '$property': 'http://linkedpipes.com/ontology/html'
                                }
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
                    },
                    'followups': {
                        '$property': 'http://linkedpipes.com/ontology/followup',
                        '$oneToMany': {
                            'id': {
                                '$property': 'http://linkedpipes.com/ontology/reference'
                            },
                            'order': {
                                '$property': 'http://linkedpipes.com/ontology/followUpCount'
                            }
                        }
                    }
                });

                newTemplates.forEach(function (template) {
                    if (templates.map[template['id']]) {
                        // Item was already stored! This can hapen on the
                        // reload (update).
                        return;
                    }
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
                    // Create followup
                    template['followup'] = {};
                    if (template['followups']) {
                        template['followups'].forEach(function (item) {
                            template['followup'][item['id']] = item['order'];
                        });
                    }
                    // Check ports.
                    var inputs = [];
                    var outputs = [];
                    template['ports'].forEach(function (port) {
                        var newPort = {
                            'label': port['label'],
                            'binding': port['binding'],
                            'type': []
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
                                newPort['type'].push(type);
                            }
                        });
                    });
                    template['inputs'] = inputs;
                    template['outputs'] = outputs;
                    // Check dialog - remove the option if we do not have full
                    // dialog (ie. JavaScript and HTML).
                    if (template['dialog']['js'] === undefined ||
                            template['dialog']['html'] === undefined) {
                        delete template['dialog'];
                    }
                    // Store.
                    templates.map[template['id']] = template;
                    templates.list.push(template);
                });
                console.timeEnd('loading templates');
                if (onSucess) {
                    onSucess();
                }
            }, function (response) {
                if (onFailure) {
                    onFailure(response);
                }
            });
        };

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

        /**
         * Make sure that given template has configuration loaded.
         *
         * @param template
         * @param onSucess Called on sucess.
         * @param onFailure Called on failure, parameter is a failure message from $http.get.
         */
        service.fetchTemplateConfiguration = function (template, onSucess, onFailure) {
            $http.get(template['configurationUri'], {
                // Supress default AngularJS conversion.
                transformResponse: function (data, headersGetter) {
                    return data;
                }
            }).then(function (response) {
                template['configuration'] = JSON.parse(response.data);
                if (onSucess) {
                    onSucess();
                }
                ;
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

});