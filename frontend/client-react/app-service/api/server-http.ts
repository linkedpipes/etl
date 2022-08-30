import {PipelineList} from "./pipeline-list/pipeline-list-model";
import {jsonLdToPipelineList} from "./pipeline-list/pipeline-list-adapter";
import {jsonToJsonLdEntities} from "../jsonld";

export async function fetchPipelineList(): Promise<PipelineList> {
  const response = await fetch("./api/v1/pipelines-list");
  const json = await response.json();
  const entities = jsonToJsonLdEntities(json);
  return jsonLdToPipelineList(entities);
}

export async function deletePipeline(iri: string): Promise<void> {
  const response = await fetch(
    "./api/v1/pipelines?iri=" + encodeURIComponent(iri), {
      "method": "DELETE",
    });
  return await response.json();
}
