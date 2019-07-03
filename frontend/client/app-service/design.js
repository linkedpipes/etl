/**
 * Provide functionality to enable improve user experience with pipeline
 * designer.
 */
define([
  "./jsonld/jsonld-to-json"
], function (jsonldToJson) {

  const _data = {};

  const _status = {
    "ready": false,
    "loading": false
  };

  const INFO_TYPE = "http://linkedpipes.com/ontology/PipelineInformation";

  const INFO_TEMPLATE = {
    "tags": {
      "$property": "http://etl.linkedpipes.com/ontology/tag",
      "$type": "plain-string"
    },
    "followup": {
      "$property": "http://etl.linkedpipes.com/ontology/followup",
      "$oneToMany": {
        "source": {
          "$property": "http://etl.linkedpipes.com/ontology/source",
          "$type": "iri"
        },
        "target": {
          "$property": "http://etl.linkedpipes.com/ontology/target",
          "$type": "iri"
        },
        "frequency": {
          "$property": "http://etl.linkedpipes.com/ontology/frequency",
          "$type": "plain-string"
        }
      }
    }
  };

  function update(data, $http, callback) {

    if (_status.loading) {
      return;
    } else {
      _status.loading = true;
    }

    $http.get("resources/pipelines/info").then(function (response) {
      const parsedResponse = jsonldToJson(
        response.data, INFO_TYPE, INFO_TEMPLATE)[0];
      if (Array.isArray(parsedResponse.tags)) {
        data.tags = parsedResponse.tags;
      } else {
        data.tags = [parsedResponse.tags];
      }
      data.followup = {};
      if (parsedResponse.followup) {
        parsedResponse.followup.forEach((item) => {
          if (data.followup[item["source"]] === undefined) {
            data.followup[item["source"]] = {};
          }
          data.followup[item["source"]][item["target"]] =
            item["frequency"];
        });
      }
      console.log("design-data", data);
      //
      _status.ready = true;
      _status.loading = false;
      if (callback) {
        callback();
      }
    });
  }

  function getTemplatePriority(data, source, target) {
    if (data.followup[source] === undefined) {
      return 0;
    }
    const priority = data.followup[source][target];
    if (priority === undefined) {
      return 0;
    } else {
      return priority;
    }
  }

  function factoryFunction($http) {

    const service = {
      "update": update.bind(null, _data, $http),
      "getTags": () => _data.tags,
      "getTemplatePriority": getTemplatePriority.bind(null, _data),
      "initialize": function (callback) {
        if (_status.ready) {
          if (callback) {
            callback();
          }
        } else {
          this.update(callback);
        }
      }
    };

    return service;
  }

  factoryFunction.$inject = ["$http"];

  return function init(app) {
    app.factory("service.pipelineDesign", factoryFunction);
  };

});
