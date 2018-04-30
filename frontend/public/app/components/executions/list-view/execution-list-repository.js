((definition) => {
    if (typeof define === "function" && define.amd) {
        define([
            "vocabulary",
            "app/modules/repository-infinite-scroll",
            "app/modules/jsonld-source"
        ], definition);
    }
})((vocab, repositoryService, jsonLdSource) => {
    "use strict";

    const LP = vocab.LP;
    const SKOS = vocab.SKOS;

    // TODO Export to navigation module.
    const PIPELINE_EDIT_URL = "#/pipelines/edit/canvas";

    // TODO Merge id and iri into one value.
    // TODO Move predicates to vocabulary.
    const REPOSITORY_TEMPLATE = {
        'id': {
            '$resource': null
        },
        'iri': {
            '$resource': null
        },
        'start': {
            '$property': 'http://etl.linkedpipes.com/ontology/execution/start',
            '$type': 'date'
        },
        'end': {
            '$property': 'http://etl.linkedpipes.com/ontology/execution/end',
            '$type': 'date'
        },
        'status': {
            '$property': LP.HAS_STATUS,
            '$type': 'iri'
        },
        'status-monitor': {
            '$property': 'http://etl.linkedpipes.com/ontology/statusMonitor',
            '$type': 'iri'
        },
        'size': {
            '$property': 'http://etl.linkedpipes.com/ontology/execution/size',
            '$type': 'plain-string'
        },
        'progress': {
            'current': {
                '$property': 'http://etl.linkedpipes.com/ontology/execution/componentFinished',
                '$type': 'plain-string'
            },
            'total': {
                '$property': 'http://etl.linkedpipes.com/ontology/execution/componentToExecute',
                '$type': 'plain-string'
            }
        },
        'pipeline': {
            '_': {
                '$property': LP.HAS_PIPELINE,
                '$oneToOne': {
                    'iri': {
                        '$resource': null
                    },
                    'label': {
                        '$property': SKOS.PREF_LABEL,
                        '$type': 'plain-string'
                    }
                }
            }
        },
        // TODO Merge with pipeline object ?
        'metadata': {
            '_': {
                '$property': LP.HAS_PIPELINE,
                '$oneToOne': {
                    '_': {
                        '$property': 'http://linkedpipes.com/ontology/executionMetadata',
                        '$oneToOne': {
                            'executionType': {
                                '$property': 'http://linkedpipes.com/ontology/execution/type',
                                '$type': 'iri'
                            },
                            'saveDebugData': {
                                '$property': LP.SAVE_DEBUG,
                                '$type': 'plain-string'
                            },
                            'deleteWorkingData': {
                                '$property': LP.DELETE_WORKING,
                                '$type': 'plain-string'
                            },
                            '_': {
                                '$property': 'http://linkedpipes.com/ontology/execution/targetComponent',
                                '$oneToOne': {
                                    'targetComponent': {
                                        'label': {
                                            '$property': SKOS.PREF_LABEL,
                                            '$type': 'plain-string'
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    };

    function decorateItem(item) {
        item['onClickUrl'] = PIPELINE_EDIT_URL +
            "?pipeline=" + encodeURIComponent(item["pipeline"]["iri"]) +
            "&execution=" + encodeURIComponent(item["iri"]);
        item["label"] = getExecutionLabel(item);
        item["duration"] = getExecutionDuration(item);
        item["progress"]["value"] = getProgressValue(item["progress"]);
        updateExecutionStatus(item);
        updateExecutionMetadata(item);
        item["searchLabel"] = item["label"].toLowerCase();
        item["filterLabel"] = true;
    }

    function getExecutionLabel(execution) {
        const pipeline = execution["pipeline"];
        if (pipeline["label"]) {
            return pipeline["label"];
        } else {
            return execution["id"];
        }
    }

    function getExecutionDuration(execution) {
        if (!execution.end) {
            return '';
        }
        const start = Date.parse(execution.start);
        const end = Date.parse(execution.end);
        const duration = (end - start) / 1000;
        const seconds = Math.ceil((duration) % 60);
        const minutes = Math.floor((duration / (60)) % 60);
        const hours = Math.floor(duration / (60 * 60));
        return (hours < 10 ? '0' + hours : hours) +
            ':' + (minutes < 10 ? '0' + minutes : minutes) +
            ':' + (seconds < 10 ? '0' + seconds : seconds);
    }

    function getProgressValue(progress) {
        const current = progress.current;
        const total = progress.total;
        if (current === undefined || total === undefined ||
            parseInt(total) === 0) {
            return 0;
        } else {
            return 100 * (current / total);
        }
    }

    // TODO Move IRIs to vocabulary.
    function updateExecutionStatus(execution) {
        switch (execution.status) {
            case 'http://etl.linkedpipes.com/resources/status/cancelled':
                execution.canDelete = true;
                execution.canCancel = false;
                execution.icon = {
                    'name': 'done',
                    'style': {
                        'color': '#ff9900'
                    }
                };
                execution.detailType = 'FULL';
                execution.canDelete = true;
                break;
            case 'http://etl.linkedpipes.com/resources/status/queued':
                execution.canDelete = true;
                execution.canCancel = false;
                execution.icon = {
                    'name': 'hourglass',
                    'style': {
                        'color': 'black'
                    }
                };
                execution.detailType = 'NONE';
                break;
            case 'http://etl.linkedpipes.com/resources/status/initializing':
            case 'http://etl.linkedpipes.com/resources/status/running':
                execution.canDelete = false;
                execution.canCancel = true;
                execution.icon = {
                    'name': 'run',
                    'style': {
                        'color': 'blue'
                    }
                };
                execution.detailType = 'PROGRESS';
                break;
            case 'http://etl.linkedpipes.com/resources/status/finished':
                execution.canDelete = true;
                execution.canCancel = false;
                execution.icon = {
                    'name': 'done',
                    'style': {
                        'color': 'green'
                    }
                };
                execution.detailType = 'FULL';
                break;
            case 'http://etl.linkedpipes.com/resources/status/failed':
                execution.canDelete = true;
                execution.canCancel = false;
                execution.icon = {
                    'name': 'error',
                    'style': {
                        'color': 'red'
                    }
                };
                execution.detailType = 'FULL';
                break;
            case 'http://etl.linkedpipes.com/resources/status/cancelling':
                execution.canDelete = false;
                execution.canCancel = false;
                execution.icon = {
                    'name': 'run',
                    'style': {
                        'color': '#ff9900'
                    }
                };
                execution.detailType = 'PROGRESS';
                break;
            default:
                execution.detailType = 'NONE';
                break;
        }
        // The status above can be override by the status-monitor.
        switch (execution['status-monitor']) {
            case 'http://etl.linkedpipes.com/resources/status/unresponsive':
                execution.canDelete = false;
                execution.canCancel = false;
                execution.icon = {
                    'name': 'help_outline',
                    'style': {
                        'color': 'orange'
                    }
                };
                break;
            case 'http://etl.linkedpipes.com/resources/status/dangling':
                execution.canDelete = true;
                execution.canCancel = false;
                execution.icon = {
                    'name': 'help_outline',
                    'style': {
                        'color': 'red'
                    }
                };
                break;
            default:
                break;
        }
    }

    // TODO Move IRIs to vocabulary.
    function updateExecutionMetadata(execution) {
        switch (execution.metadata.executionType) {
            case 'http://linkedpipes.com/resources/executionType/Full':
                execution.metadata.executionTypeLabel =
                    'Full execution';
                break;
            case 'http://linkedpipes.com/resources/executionType/DebugFrom':
                execution.metadata.executionTypeLabel =
                    'Partial execution (debug from)';
                break;
            case 'http://linkedpipes.com/resources/executionType/DebugTo':
                execution.metadata.executionTypeLabel =
                    'Partial execution (debug to: "' +
                    execution.metadata.targetComponent.label +
                    '")';
                break;
            case 'http://linkedpipes.com/resources/executionType/DebugFromTo':
                execution.metadata.executionTypeLabel =
                    'Partial execution (debug from & to: "' +
                    execution.metadata.targetComponent.label +
                    '")';
                break;
            default:
                // Can happen for older executions.
                execution.metadata.executionTypeLabel = '';
                break;
        }
        if (execution.metadata.deleteWorkingData === "true") {
            execution.metadata.executionTypeLabel += " (No working data)";
        } else {
            if (execution.metadata.saveDebugData === "false") {
                execution.metadata.executionTypeLabel += " (No debug data)";
            }
        }
    }

    function filter(item, filters) {
        filterSearchLabel(item, filters.labelSearch);
        return item["filterLabel"];
    }

    function filterSearchLabel(item, value) {
        if (value === "") {
            item["filterLabel"] = true;
            return;
        }
        const query = value.toLowerCase();
        item["filterLabel"] = item["searchLabel"].indexOf(query) !== -1;
    }

    function deleteExecution(execution, repository) {
        repositoryService.delete(repository, execution.id)
            .then(() => repositoryService.update(repository));
    }

    function increaseVisibleItemLimit(repository) {
        repositoryService.increaseVisibleItemsLimit(repository, 10);
    }

    function service($cookies) {

        function createRepository(filters) {
            const builder = jsonLdSource.createBuilder();
            builder.url("/resources/executions");
            builder.itemType(LP.EXECUTION);
            builder.tombstoneType(LP.DELETED);
            builder.itemTemplate(REPOSITORY_TEMPLATE);
            builder.supportIncrementalUpdate();
            return repositoryService.createWithInfiniteScroll({
                "itemSource": builder.build(),
                "newItemDecorator": decorateItem,
                "filter": (item, options) => filter(item, filters, options),
                "order": compareExecutions,
                "visibleItemLimit": getVisibleItemLimit()
            });
        }

        function compareExecutions(left, right) {
            if (left.start === undefined && right.start === undefined) {
                return left["id"] - right["id"];
            } else if (left.start === undefined) {
                return 1;
            } else if (right.start === undefined) {
                return -1;
            } else {
                return left.start - right.start;
            }
        }

        // TODO Move to "cookies" module.
        function getVisibleItemLimit() {
            const initialLimit = $cookies.get("lp-initial-list-size");
            if (initialLimit === undefined) {
                return 15;
            } else {
                return parseInt(initialLimit);
            }
        }

        return {
            "create": createRepository,
            "update": repositoryService.update,
            "delete": deleteExecution,
            "onFilterChanged": repositoryService.onFilterChange,
            "load": repositoryService.initialFetch,
            "increaseVisibleItemLimit": increaseVisibleItemLimit
        };
    }

    service.$inject = ["$cookies"];

    let initialized = false;
    return function init(app) {
        if (initialized) {
            return;
        }
        initialized = true;
        app.service("execution.list.repository", service);
    }

});
