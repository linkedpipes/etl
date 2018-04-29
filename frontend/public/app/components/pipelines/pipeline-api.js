((definition) => {
    if (typeof define === "function" && define.amd) {
        define(["vocabulary"], definition);
    }
})((vocabulary) => {

    const LP = vocabulary.LP;

    function executePipeline($http, iri) {
        const config = createExecutionConfiguration(true, false);
        return postPipelineExecution($http, iri, config);
    }

    function createExecutionConfiguration(saveDebug, deleteWorking) {
        const configuration = {
            "@id": "",
            "@type": LP.EXEC_OPTIONS,
        };
        configuration[LP.SAVE_DEBUG] = saveDebug;
        configuration[LP.DELETE_WORKING] = deleteWorking;
        return configuration;
    }

    function postPipelineExecution($http, iri, config) {
        const url = "/resources/executions?pipeline=" + iri;
        return $http.post(url, config);
    }

    function executeWithoutDebugData($http, pipeline) {
        const config = createExecutionConfiguration(false, true);
        postPipelineExecution($http, pipeline, config)
    }

    function createPipeline($http) {
        const data = createPipelineCreatePostData();
        const config = createPostConfigWithJsonLd();
        const url = "/resources/pipelines/";
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
        const data = createPipelineCopyPostData();
        const config = createPostConfigWithJsonLd();
        const url = "/resources/pipelines?pipeline=" + pipeline.iri;
        return $http.post(url, data, config);
    }

    function createPipelineCopyPostData() {
        const data = new FormData();
        const options = createCopyPipelineOptions();
        addOptionsToData(data, options);
        return data;
    }

    function createCopyPipelineOptions() {
        return {
            "@id": "http://localhost/pipelineImportOptions",
            "@type": LP.UPDATE_OPTIONS,
            "http://etl.linkedpipes.com/ontology/local": true
        };
    }

    function asLocalFromIri($http, pipelineIri, updateTemplates) {
        const formData = new FormData();
        addTransformOptions(formData, true, updateTemplates);
        const iri = '/resources/localize?pipeline=' + pipelineIri;
        return $http.post(iri, formData, noTransformConfiguration())
            .then((data) => data["data"]);
    }

    function asLocalFromFile($http, fileWithPipeline, updateTemplates) {
        const formData = new FormData();
        formData.append("pipeline", fileWithPipeline);
        addTransformOptions(formData, true, updateTemplates);
        const iri = '/resources/localize';
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
        return $http.get(iri).then(response => response.data);
    }

    return {
        "execute": executePipeline,
        "executeWithoutDebugData": executeWithoutDebugData,
        "create": createPipeline,
        "copy": copyPipeline,
        "asLocalFromIri": asLocalFromIri,
        "asLocalFromFile": asLocalFromFile,
        "loadLocal": loadLocal
    }

});
