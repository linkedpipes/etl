((definition) => {
  if (typeof define === "function" && define.amd) {
    define([
      "../../app-service/vocabulary",
      "../../app-service/repository/repository-infinite-scroll",
      "../../app-service/jsonld/jsonld-source",
      "../execution-status-to-icon",
      "../../personalization/personalization"
    ], definition);
  }
})((vocab, repositoryService, jsonLdSource, statusToIcon, _personalization) => {
  "use strict";

  const LP = vocab.LP;
  const SKOS = vocab.SKOS;

  // TODO Export to navigation module.
  const PIPELINE_EDIT_URL = "#/pipelines/edit/canvas";

  // TODO Move predicates to vocabulary.
  const REPOSITORY_TEMPLATE = {
    "iri": {
      "$resource": null
    },
    "start": {
      "$property": "http://etl.linkedpipes.com/ontology/execution/start",
      "$type": "date"
    },
    "end": {
      "$property": "http://etl.linkedpipes.com/ontology/execution/end",
      "$type": "date"
    },
    "status": {
      "$property": LP.HAS_STATUS,
      "$type": "iri"
    },
    "status-monitor": {
      "$property": "http://etl.linkedpipes.com/ontology/statusMonitor",
      "$type": "iri"
    },
    "size": {
      "$property": "http://etl.linkedpipes.com/ontology/execution/size",
      "$type": "plain-string"
    },
    "progress": {
      "current": {
        "$property": "http://etl.linkedpipes.com/ontology/execution/componentFinished",
        "$type": "plain-string"
      },
      "total": {
        "$property": "http://etl.linkedpipes.com/ontology/execution/componentToExecute",
        "$type": "plain-string"
      }
    },
    "pipeline": {
      "_": {
        "$property": LP.HAS_PIPELINE,
        "$oneToOne": {
          "iri": {
            "$resource": null
          },
          "label": {
            "$property": SKOS.PREF_LABEL,
            "$type": "plain-string"
          }
        }
      }
    },
    // TODO Merge with pipeline object ?
    "metadata": {
      "_": {
        "$property": LP.HAS_PIPELINE,
        "$oneToOne": {
          "_": {
            "$property": "http://linkedpipes.com/ontology/executionMetadata",
            "$oneToOne": {
              "executionType": {
                "$property": "http://linkedpipes.com/ontology/execution/type",
                "$type": "iri"
              },
              "saveDebugData": {
                "$property": LP.SAVE_DEBUG,
                "$type": "plain-string"
              },
              "deleteWorkingData": {
                "$property": LP.DELETE_WORKING,
                "$type": "plain-string"
              },
              "_": {
                "$property": "http://linkedpipes.com/ontology/execution/targetComponent",
                "$oneToOne": {
                  "targetComponent": {
                    "label": {
                      "$property": SKOS.PREF_LABEL,
                      "$type": "plain-string"
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
    item["onClickUrl"] = PIPELINE_EDIT_URL +
      "?pipeline=" + encodeURIComponent(item["pipeline"]["iri"]) +
      "&execution=" + encodeURIComponent(item["iri"]);
    item["label"] = getExecutionLabel(item);
    item["duration"] = getExecutionDuration(item);
    item["progress"]["value"] = getProgressValue(item["progress"]);
    updateExecutionStatus(item);
    updateExecutionMetadata(item);
    item["searchLabel"] = item["label"].toLowerCase();
    item["filterLabel"] = true;
    if (item["size"] === undefined) {
      item["size"] = "?";
    } else {
      // Use * 100 / 100 to round to two places.
      item["size"] = Math.ceil((item["size"] / 1048576 * 100)) / 100;
    }
  }

  function getExecutionLabel(execution) {
    const pipeline = execution["pipeline"];
    if (pipeline["label"]) {
      return pipeline["label"];
    } else {
      return execution["iri"];
    }
  }

  function getExecutionDuration(execution) {
    if (!execution.end) {
      return "";
    }
    const start = Date.parse(execution.start);
    const end = Date.parse(execution.end);
    const duration = (end - start) / 1000;
    const seconds = Math.ceil((duration) % 60);
    const minutes = Math.floor((duration / (60)) % 60);
    const hours = Math.floor(duration / (60 * 60));
    return (hours < 10 ? "0" + hours : hours) +
      ":" + (minutes < 10 ? "0" + minutes : minutes) +
      ":" + (seconds < 10 ? "0" + seconds : seconds);
  }

  function getProgressValue(progress) {
    const current = progress.current;
    const total = progress.total;
    if (current === undefined || total === undefined ||
      parseInt(total) === 0) {
      return 0;
    } else {
      return 100.0 * (current / total);
    }
  }

  function updateExecutionStatus(execution) {
    execution.icon = statusToIcon(execution.status);
    switch (execution.status) {
      case LP.EXEC_CANCELLED:
        execution.canDelete = true;
        execution.canCancel = false;
        execution.detailType = "FULL";
        execution.canDelete = true;
        break;
      case LP.EXEC_QUEUED:
        execution.canDelete = true;
        execution.canCancel = false;
        execution.detailType = "NONE";
        break;
      case LP.EXEC_INITIALIZING:
      case LP.EXEC_RUNNING:
        execution.canDelete = false;
        execution.canCancel = true;
        execution.detailType = "PROGRESS";
        break;
      case LP.EXEC_FINISHED:
        execution.canDelete = true;
        execution.canCancel = false;
        execution.detailType = "FULL";
        break;
      case LP.EXEC_FAILED:
        execution.canDelete = true;
        execution.canCancel = false;
        execution.detailType = "FULL";
        break;
      case LP.EXEC_CANCELLING:
        execution.canDelete = false;
        execution.canCancel = false;
        execution.detailType = "PROGRESS";
        break;
      case LP.EXEC_UNRESPONSIVE:
        execution.canDelete = false;
        execution.canCancel = false;
        break;
      case LP.EXEC_INVALID:
      case LP.EXEC_DANGLING:
        execution.canDelete = true;
        execution.canCancel = false;
        break;
      default:
        execution.detailType = "NONE";
        break;
    }
  }

  // TODO Move IRIs to vocabulary.
  function updateExecutionMetadata(execution) {
    switch (execution.metadata.executionType) {
      case "http://linkedpipes.com/resources/executionType/Full":
        execution.metadata.executionTypeLabel =
          "Full execution";
        break;
      case "http://linkedpipes.com/resources/executionType/DebugFrom":
        execution.metadata.executionTypeLabel =
          "Partial execution (debug from)";
        break;
      case "http://linkedpipes.com/resources/executionType/DebugTo":
        execution.metadata.executionTypeLabel =
          "Partial execution (debug to: '" +
          execution.metadata.targetComponent.label +
          "')";
        break;
      case "http://linkedpipes.com/resources/executionType/DebugFromTo":
        execution.metadata.executionTypeLabel =
          "Partial execution (debug from & to: '" +
          execution.metadata.targetComponent.label +
          "')";
        break;
      default:
        // Can happen for older executions.
        execution.metadata.executionTypeLabel = "";
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

  function filter(item, filters, options) {
    if (options === "label") {
      filterSearchLabel(item, filters.labelSearch);
    } else if (options === "status") {
      filterState(item, filters.status);
    } else {
      // Else we need to filter all.
      filterSearchLabel(item, filters.labelSearch);
      filterState(item, filters.status);
    }
    return item["filterLabel"] && item["filterState"];
  }

  function filterSearchLabel(item, value) {
    if (value === "") {
      item["filterLabel"] = true;
      return;
    }
    const query = value.toLowerCase();
    item["filterLabel"] = item["searchLabel"].indexOf(query) !== -1;
  }

  function filterState(item, values) {
    if (values.length === 0) {
      item["filterState"] = true;
      return;
    }
    for (const value of values) {
      if (value.filter.includes(item.status)) {
        item["filterState"] = true;
        return;
      }
    }
    item["filterState"] = false;
  }

  function deleteExecution(execution, repository) {
    return repositoryService.deleteItem(repository, execution)
      .then(() => repositoryService.update(repository));
  }

  function increaseVisibleItemLimit(repository) {
    repositoryService.increaseVisibleItemsLimit(repository, 10);
  }

  function service($personalization) {

    function createRepository(filters) {
      const builder = jsonLdSource.createBuilder();
      builder.url("resources/executions");
      builder.itemType(LP.EXECUTION);
      builder.tombstoneType(LP.TOMBSTONE);
      builder.itemTemplate(REPOSITORY_TEMPLATE);
      builder.supportIncrementalUpdate();
      return repositoryService.createWithInfiniteScroll({
        "itemSource": builder.build(),
        "newItemDecorator": decorateItem,
        "filter": (item, options) => filter(item, filters, options),
        "order": compareExecutions,
        "visibleItemLimit": $personalization.getListSize(),
        "id": (item) => item["iri"]
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

    return {
      "create": createRepository,
      "update": repositoryService.update,
      "delete": deleteExecution,
      "onFilterChanged": repositoryService.onFilterChange,
      "load": repositoryService.initialFetch,
      "increaseVisibleItemLimit": increaseVisibleItemLimit
    };
  }

  service.$inject = ["personalization"];

  let initialized = false;
  return function init(app) {
    if (initialized) {
      return;
    }
    initialized = true;
    _personalization(app);
    app.service("execution.list.repository", service);
  }

});
