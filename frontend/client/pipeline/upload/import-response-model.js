import jsonld from "../../app-service/jsonld/jsonld";

const LP = {
  "REPORT": "http://linkedpipes.com/ontology/ImportReport",
  "HAS_PIPELINE": "http://linkedpipes.com/ontology/hasPipeline",
  "HAS_TEMPLATE": "http://linkedpipes.com/ontology/hasTemplate",
  "PIPELINE": "http://linkedpipes.com/ontology/Pipeline",
  "HAS_LABEL": "http://www.w3.org/2004/02/skos/core#prefLabel",
  "HAS_TAG": "http://etl.linkedpipes.com/ontology/tag",
  "HAS_LOCAL": "http://etl.linkedpipes.com/ontology/localResource",
  "HAS_ERROR": "http://etl.linkedpipes.com/ontology/errorMessage",
  "HAS_STORED": "http://etl.linkedpipes.com/ontology/stored",
  "REFERENCE_TEMPLATE": "http://linkedpipes.com/ontology/Template"
};

export function parseImportResponse(json) {

  const pipelines = [];

  const referenceTemplates = [];

  jsonld.q.iterateResources(json, (resource => {
    const types = jsonld.r.getTypes(resource);
    if (types.includes(LP.REPORT)) {
      // We ignore this.
    }
    if (types.includes(LP.PIPELINE)) {
      pipelines.push(parseShared(json, resource));
    }
    if (types.includes(LP.REFERENCE_TEMPLATE)) {
      referenceTemplates.push(parseShared(json, resource));
    }
  }));

  return {
    "pipelines": pipelines,
    "referenceTemplates": referenceTemplates,
  };

}

function parseShared(json, resource) {
  return {
    "remote": jsonld.r.getId(resource),
    "label": jsonld.r.getPlainString(resource, LP.HAS_LABEL),
    "tags": jsonld.r.getPlainStrings(resource, LP.HAS_TAG),
    "local": jsonld.r.getIRI(resource, LP.HAS_LOCAL),
    "error": jsonld.r.getPlainString(resource, LP.HAS_ERROR),
    "stored": jsonld.r.getBoolean(resource, LP.HAS_STORED),
  };
}
