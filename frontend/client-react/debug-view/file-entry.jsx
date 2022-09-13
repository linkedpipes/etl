import React from "react";
import {Alert, AlertTitle, Box, Button, Container, Link} from "@mui/material";
import Path from "./path";
import {getDownloadUrl, useDebugData} from "./debug-view-service";
import {FetchState} from "../app-service/react";
import {asHumanReadableSize} from "../app-service/format-service";
import LoadingIndicator from "../app-service/component/loading-indicator";
import ErrorMessage from "../app-service/component/error-message";

const FileEntryComponent = ({entry}) => {
  const debugData = useDebugData(entry.metadata);
  const {url, name} = getDownloadUrl(entry.metadata);
  return (
    <>
      <Container sx={{"marginTop": "2rem"}}>
        <Path metadata={entry.metadata}/>
        <Box m="1rem">
          <Button
            component={Link}
            href={url}
            download={name}>
            Download file
          </Button>
          File size: {asHumanReadableSize(entry.metadata.size)}
        </Box>
      </Container>
      <Box m="2rem">
        {renderContent(debugData)}
      </Box>
    </>
  );
};

export default FileEntryComponent;

const renderContent = ({shouldFetch, setShouldFetch, status, content}) => {
  if (!shouldFetch) {
    return (
      <>
        <Alert variant="outlined" severity="warning">
          <AlertTitle>Warning</AlertTitle>
          This file may be too big for preview and may cause browser to crash.
          <br/>
          <Button onClick={setShouldFetch} color="warning">
            Show preview anyway
          </Button>
        </Alert>
      </>
    )
  }
  if (status === FetchState.Ready) {
    return (
      <pre><code>{content}</code></pre>
    );
  } else if (status === FetchState.Error) {
    return (
      <ErrorMessage/>
    )
  } else {
    return (
      <LoadingIndicator/>
    );
  }
}
