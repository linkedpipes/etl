import React from "react";
import {Breadcrumbs, Link} from "@mui/material";
import {Link as RouterLink} from "react-router-dom";
import {getMetadataUrlForDirectory} from "./debug-view-service";

const Path = ({metadata}) => {
  const tokens = (metadata.fullPath?.split("/") ?? [])
    .filter(value => value.length > 0);
  const paths = [];
  paths.push([
    metadata.execution,
    getMetadataUrlForDirectory(metadata.execution)
  ]);
  for (let index = 0; index < tokens.length; ++index) {
    const path = "/" + tokens.slice(0, index + 1).join("/");
    const link = getMetadataUrlForDirectory(metadata.execution, path);
    paths.push([tokens[index], link]);
  }
  return (
    <Breadcrumbs>
      {paths.map(([label, link], index) =>
        renderBreadcrumb(paths.length, index, link, label))}
    </Breadcrumbs>
  );
};

export default Path;

function renderBreadcrumb(size, index, link, label) {
  if (size - 1 === index) {
    // Last step.
    return (
      <div key={index}>
        {label}
      </div>
    )
  }
  return (
    <Link component={RouterLink}
          key={index}
          to={link}>
      {label}
    </Link>
  );
}
