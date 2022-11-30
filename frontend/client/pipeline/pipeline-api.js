((definition) => {
  if (typeof define === "function" && define.amd) {
    define([
      "@client/app-service/vocabulary"
    ], definition);
  }
})((vocabulary) => {

  const LP = vocabulary.LP;

  /**
   * Create execution for local pipeline.
   */
  function executeLocalPipeline($http, iri, options) {
    const config = createExecutionConfiguration(options);
    return postLocalPipelineExecution($http, iri, config);
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

  function postLocalPipelineExecution($http, iri, config) {
    const url = "./api/v1/executions?local-iri=" + iri;
    return $http.post(url, config);
  }

  function createPipeline($http) {
    const data = createPipelineCreatePostData();
    const config = createPostConfigWithJsonLd();
    const url = "./api/v1/pipelines";
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
    const url = "./api/v1/pipelines-copy?local-iri=" +
      encodeURIComponent(pipeline.iri);
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
      "@type": LP.UPDATE_OPTIONS,
      "http://www.w3.org/2004/02/skos/core#prefLabel":
        "Copy of " + pipeline.label,
      "http://etl.linkedpipes.com/ontology/updateExistingTemplates": false,
      "http://etl.linkedpipes.com/ontology/importNewTemplates": false,
      "http://etl.linkedpipes.com/ontology/importPipeline": true,
      "http://etl.linkedpipes.com/ontology/pipeline": {
        "@id": pipeline.iri,
      },
    };
  }

  function asLocalFromIri($http, pipelineIri, updateTemplates) {
    const formData = new FormData();
    addTransformOptions(formData, true, updateTemplates);
    const iri = "./api/v1/localize?iri=" +
      encodeURIComponent(pipelineIri);
    return $http.post(iri, formData, noTransformConfiguration())
      .then((data) => data["data"]);
  }

  function asLocalFromFile($http, fileWithPipeline, updateTemplates) {
    const formData = new FormData();
    formData.append("pipeline", fileWithPipeline);
    addTransformOptions(formData, true, updateTemplates);
    const iri = "./api/v1/localize";
    return $http.post(iri, formData, noTransformConfiguration())
      .then((data) => data["data"]);
  }

  function addTransformOptions(formData, importTemplates, updateTemplates) {
    const options = {
      "@id": "http://localhost/pipelineImportOptions",
      "@type": LP.UPDATE_OPTIONS,
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
    const url = "./api/v1/pipelines?" +
      "templates=false&mappings=false&removePrivateConfig=false" +
      "&iri=" + encodeURIComponent(iri);
    return $http({
      "method": "GET",
      "url": url,
      "headers": {
        "Accept": "application/ld+json",
      },
    }).then(response => response.data);
  }

  function deletePipeline($http, iri) {
    const url = "./api/v1/pipelines?iri=" + encodeURIComponent(iri);
    return $http({
      "method": "DELETE",
      "url": url
    });
  }

  function savePipeline($http, iri, jsonld, unchecked) {
    const url = "./api/v1/pipelines?iri=" + encodeURIComponent(iri);
    return $http({
      "method": "PUT",
      "url": url,
      "params": {"unchecked": unchecked},
      "headers": {
        "Content-Type": "application/json",
        "Accept": "application/ld+json",
      },
      "data": jsonld
    });
  }

  function createPipelineFromData($http, pipelineIri, pipeline, label) {
    const form = new FormData();
    form.append("pipeline",
      new Blob([JSON.stringify(pipeline)], {
        "type": "application/ld+json"
      }), "pipeline.jsonld");
    const options = {
      "@id": "http://localhost/options",
      "@type": "http://linkedpipes.com/ontology/UpdateOptions",
      "http://www.w3.org/2004/02/skos/core#prefLabel": label,
      "http://etl.linkedpipes.com/ontology/importPipeline": true,
      "http://etl.linkedpipes.com/ontology/pipeline": {
        "@id": pipelineIri,
      },
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
    return $http.post("./api/v1/import", form, config)
      .then((response) => {
        const jsonld = response.data;
        let iri = null;
        for (const item of jsonld) {
          if (!item["@type"]?.includes("http://linkedpipes.com/ontology/Pipeline")) {
            continue;
          }
          iri = item[ "http://etl.linkedpipes.com/ontology/localResource"][0]["@id"];
          break;
        }
        if (iri == null) {
          throw new Error("Can't read pipeline IRI from response.");
        }
        return iri;
      });
  }

  return {
    "executePipeline": executeLocalPipeline,
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
