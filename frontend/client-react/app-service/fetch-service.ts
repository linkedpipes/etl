//
// Interface for fetch API used in the whole application. We use this
// to abstract from any particular fetch implementation.
//

export type FetchFunction =
  (input: RequestInfo, init?: RequestInit) => Promise<Response>;

export const getFetch =
  (): FetchFunction => fetch;

/**
 * Fetch that can be cancelled using given controller.
 */
export const getFetchWithController =
  (controller: AbortController): FetchFunction => {
    return (input: RequestInfo, init?: RequestInit) => {
      return fetch(input, {
        ...(init ?? {}),
        "signal": controller.signal,
      });
    };
  };

export async function fetchJsonLd(
  fetchFunction: FetchFunction, url: string, method = "GET"
) {
  const request = {
    "method": method,
    "headers": {
      "Accept": "application/ld+json"
    }
  };
  const response = await fetchFunction(url, request)
  return await response.json();
}

export async function fetchJson(
  fetchFunction: FetchFunction, url: string, method = "GET"
) {
  const request = {
    "method": method,
    "headers": {
      "Accept": "application/json",
    },
  };
  const response = await fetchFunction(url, request)
  return await response.json();
}

export async function fetchContent(
  fetchFunction: FetchFunction, url: string, method = "GET"
) {
  const request = {
    "method": method,
  };
  const response = await fetchFunction(url, request)
  return await response.text();
}
