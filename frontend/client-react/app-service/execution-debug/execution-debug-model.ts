export enum DebugEntryType {
  DIRECTORY = "dir",
  FILE = "file",
  AMBIGUOUS = "ambiguous",
}

export interface DebugEntry {
  execution: string;
  type: DebugEntryType;
  name: string;
  path: string;
  fullPath: string;
  source?: string;
}

export interface DebugDirectoryEntry extends DebugEntry {
}

export function isDebugDirectoryEntry(
  entry: DebugEntry | null | undefined
): entry is DebugDirectoryEntry {
  return entry?.type === DebugEntryType.DIRECTORY;
}

export interface DebugFileEntry extends DebugEntry {
  size: number;
  mimeType: string;
  publicDataPath?: string;
}

export function isDebugFileEntry(
  entry: DebugEntry | null | undefined
): entry is DebugFileEntry {
  return entry?.type === DebugEntryType.FILE;
}

export interface DebugMetadata {
  execution: string;
  fullPath: string;
  type: DebugEntryType;
  source?: string;
}

export function isDebugAmbiguousMetadata(
  entry: DebugMetadata | null | undefined
): entry is DebugMetadata {
  return entry?.type === DebugEntryType.AMBIGUOUS;
}

export interface DebugDirectoryMetadata extends DebugMetadata {
  count: number;
}

export function isDebugDirectoryMetadata(
  entry: DebugMetadata | null | undefined
): entry is DebugDirectoryMetadata {
  return entry?.type === DebugEntryType.DIRECTORY;
}

export interface DebugFileMetadata extends DebugMetadata {
  size: number;
  mimeType: string;
  publicDataPath?: string;
}

export function isDebugFileMetadata(
  entry: DebugMetadata | null | undefined
): entry is DebugFileMetadata {
  return entry?.type === DebugEntryType.FILE;
}

export type DebugMetadataList = {
  data: DebugEntry[];
  metadata: DebugMetadata;
};
