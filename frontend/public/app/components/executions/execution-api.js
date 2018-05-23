((definition) => {
    if (typeof define === "function" && define.amd) {
        define([], definition);
    }
})(() => {

    function cancelExecution($http, iri, message) {
        const body = {"reason": message};
        const url = '/resources/executions/cancel?id=' + iri;
        return $http.post(url, body);
    }

    return {
        "cancel": cancelExecution
    }

});
