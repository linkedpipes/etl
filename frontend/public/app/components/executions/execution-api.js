((definition) => {
    if (typeof define === "function" && define.amd) {
        define([], definition);
    }
})(() => {

    function cancelExecution($http, id, message) {
        const body = {"reason": message};
        const url = '/resources/executions/cancel?id=' + id;
        return $http.post(url, body);
    }

    return {
        "cancel": cancelExecution
    }

});
