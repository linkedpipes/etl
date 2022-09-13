import {PipelineList} from "./pipeline-list-model";
import {
  JsonLdDocument,
  getId,
  getEntitiesByType,
  getValue,
  getValues,
} from "../jsonld";

const VOCABULARY = {
  "PIPELINE": "http://linkedpipes.com/ontology/Pipeline",
  "LABEL": "http://www.w3.org/2004/02/skos/core#prefLabel",
  "TAG": "http://etl.linkedpipes.com/ontology/tag",
}

export function jsonLdToPipelineList(jsonld: JsonLdDocument): PipelineList {
  const result: PipelineList = {items: []};
  for (const graph of jsonld) {
    const pipelines = getEntitiesByType(graph.entities, VOCABULARY.PIPELINE);
    for (const pipeline of pipelines) {
      const iri = getId(pipeline);
      const label = String(getValue(pipeline, VOCABULARY.LABEL));
      const tags: string[] = getValues(pipeline, VOCABULARY.TAG).map(String);
      result.items.push({
        "iri": iri,
        "label": label,
        "tags": tags
      });
    }
  }
  result.items.reverse();
  return result;
}
