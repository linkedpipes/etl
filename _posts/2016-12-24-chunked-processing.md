---
layout: post
icon: fast_forward
title: Components for chunked RDF data processing
---

Recently we developed a number of components for chunked RDF data processing.
They can considerably speed up processing and lower memory requirements for many use cases.
They are suitable for data, which can be split into independent entities processed separately, e.g. list of inspections, list of regions, etc.
What they have in common is the new RDF chunked data unit containing RDF data split into smaller data chunks.
Check out the chunked versions of the original components such as [Tabular](/components/t-tabularchunked), [SPARQL Endpoint extractor](/components/e-sparqlendpointchunked), [SPARQL Endpoint loader](/components/l-sparqlendpointchunked), [SPARQL Construct](/components/t-sparqlconstructchunked), [Files to RDF](/components/t-filestordfchunked), etc. and the new components which use chunks natively - [GeoTools](/components/t-geotools) and [Bing translator](/components/t-bingtranslator).
