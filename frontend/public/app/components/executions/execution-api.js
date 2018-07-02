((definition) => {
    if (typeof define === "function" && define.amd) {
        define([], definition);
    }
})(() => {

    function cancelExecution($http, iri, message) {
        const id = iri.substring(iri.lastIndexOf("/") + 1)
        const body = {"reason": message};
        const url = "/resources/executions/" + id + "/cancel";
        return $http.post(url, body);
    }

    return {
        "cancel": cancelExecution
    }

});
