((definition) => {
    if (typeof define === "function" && define.amd) {
        define(["vocabulary"], definition);
    }
})((vocabulary) => {

    const LP = vocabulary.LP;

    function executePipeline($http, pipeline) {
        const config = createExecutionConfiguration(true, false);
        return postPipelineExecution($http, pipeline, config);
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

    function postPipelineExecution($http, pipeline, config) {
        const url = "/resources/executions?pipeline=" + pipeline.iri;
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
            "@type": "http://linkedpipes.com/ontology/UpdateOptions"
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
            "@id": "http://localhost/options",
            "@type": "http://linkedpipes.com/ontology/UpdateOptions",
            "http://etl.linkedpipes.com/ontology/local": true
        };
    }

    return {
        "execute": executePipeline,
        "executeWithoutDebugData": executeWithoutDebugData,
        "create": createPipeline,
        "copy": copyPipeline
    }

});
