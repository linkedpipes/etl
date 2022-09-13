import {useState, useEffect} from "react";
import {FetchFunction, getFetchWithController} from "../fetch-service";
import {logger} from "../logger-service";

export enum FetchState {
  /**
   * Loading data for the first time.
   */
  Loading,
  /**
   * Loading data, with old data available.
   */
  Updating,
  /**
   * Data are ready to be used.
   */
  Ready,
  /**
   * Loading failed.
   */
  Error
}

/**
 * We pass the fetch here, so we can control it.
 */
export type FetchConsumer<T> = (
  fetchFunction: FetchFunction
) => Promise<T>;

export type FetchResult<T> = {
  status: FetchState;
  content: T | null;
}

/**
 * Wrap asynchronous producer.
 */
export function useFetch<T>(
  producer: FetchConsumer<T>
): FetchResult<T> {
  const [status, setStatus] = useState(FetchState.Loading);
  const [content, setContent] = useState<T | null>(null);
  useEffect(() => {
    const controller = new AbortController();
    const fetchFunction = getFetchWithController(controller);
    if (content === null) {
      setStatus(FetchState.Loading);
    } else {
      setStatus(FetchState.Updating);
    }
    (async () => {
      try {
        const result = await producer(fetchFunction);
        setStatus(FetchState.Ready);
        setContent(result);
      } catch (er) {
        logger.error("Error", er);
        setStatus(FetchState.Error);
      }
    })();
    return () => controller.abort();
  }, [producer]);
  return {
    "status": status,
    "content": content,
  };
}
