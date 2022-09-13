
export type PipelineList = {
  items: PipelineListItem[],
};

export type PipelineListItem = {
  iri: string,
  label: string,
  tags: string[],
};
