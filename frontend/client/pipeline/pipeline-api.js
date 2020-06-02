((definition) => {
  if (typeof define === "function" && define.amd) {
    define([
      "@client/app-service/vocabulary"
    ], definition);
  }
})((vocabulary) => {

  const LP = vocabulary.LP;

  function executePipeline($http, iri, options) {
    const config = createExecutionConfiguration(options);
    return postPipelineExecution($http, iri, config);
  }

  function createExecutionConfiguration(options) {
    const {keepDebugData, debugTo, execution, mapping, resume} = options;
    const configuration = {
      "@id": "",
      "@type": LP.EXEC_OPTIONS,
    };

    if (debugTo) {
      configuration[LP.RUN_TO] = {"@id": debugTo};
    }

    if (keepDebugData) {
      configuration[LP.SAVE_DEBUG] = true;
      configuration[LP.DELETE_WORKING] = false;
    } else {
      configuration[LP.SAVE_DEBUG] = false;
      configuration[LP.DELETE_WORKING] = true;
    }

    if (execution) {
      const executionMapping = {};
      executionMapping[LP.HAS_EXECUTION_ETL] = {"@id": execution};
      if (mapping) {
        executionMapping[LP.MAPPING] = createComponentMapping(mapping);
      }
      if (resume) {
        executionMapping[LP.RESUME] = createComponentMapping(resume);
      }
      configuration[LP.EXECUTION_MAPPING] = executionMapping;
    }

    return configuration;
  }

  function createComponentMapping(mapping) {
    return mapping.map((item) => ({
      [LP.MAPPING_SOURCE]: {"@id": item["source"]},
      [LP.MAPPING_TARGET]: {"@id": item["target"]}
    }));
  }

  function postPipelineExecution($http, iri, config) {
    const url = "./resources/executions?pipeline=" + iri;
    return $http.post(url, config);
  }

  function createPipeline($http) {
    const data = createPipelineCreatePostData();
    const config = createPostConfigWithJsonLd();
    const url = "./resources/pipelines/";
    return $http.post(url, data, config);
  }

  function createPipelineCreatePostData() {
    const data = new FormData();
    const options = createEmptyPipelineCreateOptions();
    addOptionsToData(data, options);
    return data;
  }

  function addOptionsToData(data, options) {
    data.append("options", new Blob([JSON.stringify(options)], {
      "type": "application/ld+json"
    }), "options.jsonld");
  }

  function createEmptyPipelineCreateOptions() {
    return {
      "@id": "http://localhost/options",
      "@type": LP.UPDATE_OPTIONS
    };
  }

  function createPostConfigWithJsonLd() {
    return {
      "transformRequest": angular.identity,
      "headers": {
        // By this angular add Content-Type itself.
        "Content-Type": undefined,
        "accept": "application/ld+json"
      }
    };
  }

  function copyPipeline($http, pipeline) {
    const data = createPipelineCopyPostData(pipeline);
    const config = createPostConfigWithJsonLd();
    const url =
      "./resources/pipelines?fromLocal=true&pipeline=" + pipeline.iri;
    return $http.post(url, data, config);
  }

  function createPipelineCopyPostData(pipeline) {
    const data = new FormData();
    const options = createCopyPipelineOptions(pipeline);
    addOptionsToData(data, options);
    return data;
  }

  function createCopyPipelineOptions(pipeline) {
    return {
      "@id": "http://localhost/pipelineImportOptions",
      "@type": LP.UPDATE_OPTIONS,
      "http://etl.linkedpipes.com/ontology/local": true,
      "http://www.w3.org/2004/02/skos/core#prefLabel":
        "Copy of " + pipeline.label,
    };
  }

  function asLocalFromIri($http, pipelineIri, updateTemplates) {
    const formData = new FormData();
    addTransformOptions(formData, true, updateTemplates);
    const iri = "./resources/pipelines/localize?pipeline=" + pipelineIri;
    return $http.post(iri, formData, noTransformConfiguration())
      .then((data) => data["data"]);
  }

  function asLocalFromFile($http, fileWithPipeline, updateTemplates) {
    const formData = new FormData();
    formData.append("pipeline", fileWithPipeline);
    addTransformOptions(formData, true, updateTemplates);
    const iri = "./resources/pipelines/localize";
    return $http.post(iri, formData, noTransformConfiguration())
      .then((data) => data["data"]);
  }

  function addTransformOptions(formData, importTemplates, updateTemplates) {
    const options = {
      "@id": "http://localhost/pipelineImportOptions",
      "@type": LP.UPDATE_OPTIONS,
      "http://etl.linkedpipes.com/ontology/local": false
    };
    options[LP.HAS_IMPORT_TEMPLATES] = importTemplates;
    options[LP.HAS_UPDATE_TEMPLATES] = updateTemplates;
    formData.append("options", new Blob([JSON.stringify(options)], {
      type: "application/ld+json"
    }), "options.jsonld");
  }

  function noTransformConfiguration() {
    return {
      // Do not transform data.
      "transformRequest": angular.identity,
      "headers": {
        // By this angular add Content-Type itself.
        "Content-Type": undefined,
        "accept": "application/ld+json"
      }
    };
  }

  function loadLocal($http, iri) {
    const serviceUrl =
      iri + "&templates=false&mappings=false&removePrivateConfig=false";
    return $http.get(serviceUrl).then(response => response.data);
  }

  function deletePipeline($http, iri) {
    return $http({
      "method": "DELETE",
      "url": iri
    });
  }

  function savePipeline($http, iri, jsonld, unchecked) {
    return $http({
      "method": "PUT",
      "url": iri,
      "params": {"unchecked": unchecked},
      "headers": {"Content-Type": "application/json"},
      "data": jsonld
    });
  }

  function createPipelineFromData($http, pipeline, label) {
    const form = new FormData();
    form.append("pipeline",
      new Blob([JSON.stringify(pipeline)], {
        "type": "application/ld+json"
      }), "pipeline.jsonld");
    const options = {
      "@id": "http://localhost/options",
      "@type": "http://linkedpipes.com/ontology/UpdateOptions",
      "http://etl.linkedpipes.com/ontology/local": true,
      "http://www.w3.org/2004/02/skos/core#prefLabel": label,
    };
    form.append("options",
      new Blob([JSON.stringify(options)], {
        "type": "application/ld+json"
      }), "options.jsonld");
    const config = {
      "transformRequest": angular.identity,
      "headers": {
        // By this angular add Content-Type itself.
        "Content-Type": undefined,
        "accept": "application/ld+json"
      }
    };
    return $http.post("./resources/pipelines", form, config)
      .then((response) => {
        const jsonld = response.data;
        return jsonld[0]["@graph"][0]["@id"];
      });
  }

  return {
    "executePipeline": executePipeline,
    "create": createPipeline,
    "copy": copyPipeline,
    "asLocalFromIri": asLocalFromIri,
    "asLocalFromFile": asLocalFromFile,
    "loadLocal": loadLocal,
    "deletePipeline": deletePipeline,
    "savePipeline": savePipeline,
    "createPipelineFromData": createPipelineFromData
  }

});
