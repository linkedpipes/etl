import React from "react";
import {useSearchParams} from "react-router-dom";
import {useDebugMetadata} from "./debug-view-service";
import {FetchState} from "../app-service/react";
import {isDebugFileMetadata} from "../app-service/execution-debug";
import ListEntryComponent from "./list-entry";
import FileEntryComponent from "./file-entry";
import LoadingIndicator from "../app-service/component/loading-indicator";
import ErrorMessage from "../app-service/component/error-message";

const PAGE_SIZE = 50;

export default function DebugView() {
  const [searchParams,] = useSearchParams();

  const execution = searchParams.get("execution");
  const path = searchParams.get("path") ?? "";

  const page = parseInt(searchParams.get("page") ?? "1") - 1;
  const data = useDebugMetadata(
    execution,
    path,
    searchParams.get("source") ?? undefined,
    page * PAGE_SIZE,
    PAGE_SIZE
  );

  if (data.status === FetchState.Ready || data.status === FetchState.Updating) {
    if (isDebugFileMetadata(data.content?.metadata ?? null)) {
      return (
        <FileEntryComponent entry={data.content}/>
      );
    } else {
      return (
        <ListEntryComponent
          page={page}
          pageSize={PAGE_SIZE}
          entry={data.content}
        />
      );
    }
  } else if (data.status === FetchState.Error) {
    return (
      <ErrorMessage/>
    )
  } else {
    return (
      <LoadingIndicator/>
    );
  }
}
