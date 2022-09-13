import {FetchFunction, fetchJson} from "../fetch-service";

export async function deletePipeline(
  fetchFunction: FetchFunction, iri: string
): Promise<void> {
  const url = "./api/v1/pipelines?iri=" + encodeURIComponent(iri);
  return fetchJson(fetchFunction, url, "DELETE");
}
