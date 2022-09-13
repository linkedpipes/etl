import {
  Container,
  IconButton,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Pagination
} from "@mui/material";
import {
  isDebugDirectoryEntry,
  isDebugFileEntry
} from "../app-service/execution-debug";
import {logger} from "../app-service/logger-service";
import React from "react";
import {
  getDownloadUrl,
  useNavigateToEntry,
  useNavigateToPage
} from "./debug-view-service";
import Path from "./path";
import FolderIcon from '@mui/icons-material/Folder';
import InsertDriveFileIcon from '@mui/icons-material/InsertDriveFile';
import {asHumanReadableSize} from "../app-service/format-service";
import FileDownloadIcon from '@mui/icons-material/FileDownload';

const ListEntryComponent = ({page, pageSize, entry}) => {
  const {data, metadata} = entry;

  const onNavigateEntry = useNavigateToEntry();
  const onNavigatePage = useNavigateToPage(metadata);
  const pageCount = Math.ceil(metadata.count / pageSize);
  return (
    <Container sx={{"marginTop": "2rem"}}>
      <Path metadata={metadata}/>
      <List sx={{"width": "100%", "maxWidth": 1024}}>
        {data.map(item => renderListItem(item, onNavigateEntry))}
      </List>
      {pageCount > 1 &&
        <Pagination
          page={page + 1}
          count={pageCount}
          onChange={(event, value) => onNavigatePage(value)}
        />
      }
    </Container>
  );
}

export default ListEntryComponent;

const renderListItem = (entry, onNavigateEntry) => {
  const key = entry.fullPath + entry.source;
  if (isDebugDirectoryEntry(entry)) {
    return (
      <DirectoryItem key={key} entry={entry} onNavigateEntry={onNavigateEntry}/>
    );
  } else if (isDebugFileEntry(entry)) {
    return (
      <FileItem key={key} entry={entry} onNavigateEntry={onNavigateEntry}/>
    );
  } else {
    logger.warning("Unknown debug entry.", {"entry": entry})
    return null;
  }
};

const DirectoryItem = ({entry, onNavigateEntry}) => {
  return (
    <ListItemButton
      onClick={() => onNavigateEntry(entry)}
      alignItems="flex-start">
      <ListItemIcon>
        <FolderIcon/>
      </ListItemIcon>
      <ListItemText primary={entry.name}/>
    </ListItemButton>
  )
};

const FileItem = ({entry, onNavigateEntry}) => {
  const {url, name} = getDownloadUrl(entry);
  return (
    <ListItemButton
      onClick={() => onNavigateEntry(entry)}
      alignItems="flex-start"
    >
      <ListItemIcon>
        <InsertDriveFileIcon/>
      </ListItemIcon>
      <ListItemText
        primary={entry.name}
        secondary={"Size: " + asHumanReadableSize(entry.size)}
      />
      <IconButton
        href={url}
        download={name}
        edge="end"
        aria-label="download"
        onClick={event => event.stopPropagation()}
      >
        <FileDownloadIcon/>
      </IconButton>
    </ListItemButton>
  )
};
