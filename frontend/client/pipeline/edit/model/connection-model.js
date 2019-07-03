((definition) => {
  if (typeof define === "function" && define.amd) {
    define([
      "@client/app-service/jsonld/jsonld",
      "@client/app-service/vocabulary"
    ], definition);
  }
})((jsonld, vocabulary) => {

  const LP = vocabulary.LP;

  const service = {
    "component": {},
    "connection": {},
    "pipeline": {}
  };

  service.getSource = (connection) => {
    return jsonld.r.getIRI(connection, LP.HAS_SOURCE_COMPONENT);
  };

  service.getSourceBinding = (connection) => {
    return jsonld.r.getPlainString(connection, LP.HAS_SOURCE_BINDING);
  };

  service.getTarget = (connection) => {
    return jsonld.r.getIRI(connection, LP.HAS_TARGET_COMPONENT);
  };

  service.getTargetBinding = (connection) => {
    return jsonld.r.getPlainString(connection, LP.HAS_TARGET_BINDING);
  };

  service.createVertex = (x, y) => {
    return {
      "@id": "",
      "@type": LP.VERTEX,
      [LP.HAS_X]: x,
      [LP.HAS_Y]: y,
      [LP.HAS_ORDER]: ""
    };
  };

  service.setSource = (connection, component, binding) => {
    connection[LP.HAS_SOURCE_COMPONENT] = {
      "@id": component["@id"]
    };
    if (binding) {
      connection[LP.HAS_SOURCE_BINDING] = binding;
    }
  };

  service.setTarget = (connection, component, binding) => {
    connection[LP.HAS_TARGET_COMPONENT] = {
      "@id": component["@id"]
    };
    if (binding) {
      connection[LP.HAS_TARGET_BINDING] = binding;
    }
  };

  service.getVertexX = (vertex) => {
    return jsonld.r.getInteger(vertex, LP.HAS_X);
  };

  service.getVertexY = (vertex) => {
    return jsonld.r.getInteger(vertex, LP.HAS_Y);
  };

  service.getVertexOrder = (vertex) => {
    return jsonld.r.getInteger(vertex, LP.HAS_ORDER);
  };

  return service;
});