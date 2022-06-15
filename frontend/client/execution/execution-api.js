((definition) => {
  if (typeof define === "function" && define.amd) {
    define([], definition);
  }
})(() => {

  function loadLocal($http, iri) {
    const url = "./api/v1/executions?iri=" + encodeURIComponent(iri);
    return $http.get(url).then(response => response.data);
  }

  function cancelExecution($http, iri, message) {
    const body = {"reason": message};
    const url = "./api/v1/executions-cancel?iri=" + encodeURIComponent(iri);
    return $http.post(url, body);
  }

  return {
    "loadLocal": loadLocal,
    "cancel": cancelExecution
  }

});
