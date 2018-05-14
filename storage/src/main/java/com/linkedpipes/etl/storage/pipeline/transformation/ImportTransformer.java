package com.linkedpipes.etl.storage.pipeline.transformation;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.pipeline.Pipeline;
import com.linkedpipes.etl.storage.pipeline.PipelineInfo;
import com.linkedpipes.etl.storage.rdf.PojoLoader;
import com.linkedpipes.etl.storage.template.TemplateFacade;
import com.linkedpipes.etl.storage.template.mapping.MappingFacade;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class ImportTransformer {

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private ImportOptions options;

    private ImportTemplates importTemplates;

    public ImportTransformer(
            TemplateFacade templatesFacade,
            MappingFacade mappingFacade) {
        this.importTemplates = new ImportTemplates(
                templatesFacade, mappingFacade);
    }

    public Collection<Statement> localizePipeline(
            Collection<Statement> pipelineRdf,
            Collection<Statement> optionsRdf,
            PipelineInfo pipelineInfo,
            IRI pipelineIri)
            throws TransformationFailed {
        if (pipelineRdf.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        this.loadOptions(optionsRdf);
        this.loadPipelineInfo(pipelineRdf);
        if (!options.isLocal()) {
            pipelineRdf = this.importTemplates(
                    pipelineRdf, pipelineInfo.getVersion());
        }
        if (pipelineIri != null) {
            String targetIri = pipelineIri.stringValue();
            pipelineRdf = this.updateResources(pipelineRdf, targetIri);
        } else {
            // We have not translate pipeline IRI, so just use the current one.
            pipelineIri = valueFactory.createIRI(pipelineInfo.getIri());
        }
        pipelineRdf = this.replacePipelineLabel(pipelineRdf, pipelineIri);
        return pipelineRdf;
    }

    private void loadOptions(Collection<Statement> optionsRdf)
            throws TransformationFailed {
        this.options = new ImportOptions();
        try {
            PojoLoader.loadOfType(optionsRdf, ImportOptions.TYPE, options);
        } catch (PojoLoader.CantLoadException ex) {
            throw new TransformationFailed("Can't load options.", ex);
        }
    }

    private void loadPipelineInfo(Collection<Statement> pipelineRdf)
            throws TransformationFailed {
        PipelineInfo info = new PipelineInfo();
        try {
            PojoLoader.loadOfType(pipelineRdf, Pipeline.TYPE, info);
        } catch (PojoLoader.CantLoadException ex) {
            throw new TransformationFailed(
                    "Can't createMappingFromStatements pipeline.", ex);
        }
    }

    private Collection<Statement> importTemplates(
            Collection<Statement> pipelineRdf, int pipelineVersion)
            throws TransformationFailed {
        try {
            importTemplates.setImportMissing(options.isImportTemplates());
            importTemplates.setUpdateExisting(options.isUpdateTemplates());
            importTemplates.setMigrateConfigurations(pipelineVersion < 2);
            return importTemplates.importTemplates(pipelineRdf);
        } catch (BaseException ex) {
            throw new TransformationFailed(
                    "Can't import templates.", ex);
        }
    }

    /**
     * Update resource/graph IRIs.
     *
     * @param pipelineRdf
     * @param baseIri
     * @return
     */
    private Collection<Statement> updateResources(
            Collection<Statement> pipelineRdf, String baseIri) {
        Map<Resource, Resource> mapping =
                this.createIriMapping(pipelineRdf, baseIri);
        return mapPipelineIris(pipelineRdf, mapping);
    }

    private Map<Resource, Resource> createIriMapping(
            Collection<Statement> pipelineRdf, String baseIri) {
        Map<Resource, Resource> mapping = new HashMap<>();
        for (Statement s : pipelineRdf) {
            // Create mapping for all typed resources.
            if (s.getPredicate().equals(RDF.TYPE)) {
                if (s.getObject().equals(Pipeline.TYPE)) {
                    // For pipeline we the IRI as it is, event if
                    // we should overide existing from the graph.
                    mapping.put(s.getSubject(), valueFactory.createIRI(
                            baseIri));
                } else if (!mapping.containsKey(s.getSubject())) {
                    // Only if the mapping is missing.
                    mapping.put(s.getSubject(), valueFactory.createIRI(
                            baseIri + "/" + (mapping.size() + 1)));
                }
            }
            // And for all graphs. This is needed as names
            // of configuration graphs may not be same as the name of
            // configuration resource.
            if (!mapping.containsKey(s.getContext())) {
                mapping.put(s.getContext(), valueFactory.createIRI(
                        baseIri + "/graph/" + (mapping.size() + 1)));
            }
        }
        return mapping;
    }

    private Collection<Statement> mapPipelineIris(
            Collection<Statement> pipelineRdf,
            Map<Resource, Resource> mapping) {
        List<Statement> result = new ArrayList<>(pipelineRdf.size());
        for (Statement s : pipelineRdf) {
            final Resource subject = mapping.getOrDefault(
                    s.getSubject(), s.getSubject());
            Value object = mapping.get(s.getObject());
            if (object == null) {
                object = s.getObject();
            }
            final Resource context = mapping.getOrDefault(
                    s.getContext(), s.getContext());
            //
            result.add(valueFactory.createStatement(
                    subject, s.getPredicate(), object, context));
        }
        return result;
    }

    /**
     * Upon import the options can specify, that a pipeline labels should
     * be updated.
     */
    private Collection<Statement> replacePipelineLabel(
            Collection<Statement> pipelineRdf, IRI pipelineIri) {

        if (options.getLabels() != null || options.getLabels().isEmpty()) {
            return pipelineRdf;
        }

        Predicate<Statement> filterOutPipelineLabel = st ->
                !(st.getSubject().equals(pipelineIri) &&
                        SKOS.PREF_LABEL.equals(st.getPredicate()));

        List<Statement> result = pipelineRdf.stream()
                .filter(filterOutPipelineLabel)
                .collect(Collectors.toList());

        List<Statement> newLabels = options.getLabels().stream().map(
                value -> valueFactory.createStatement(pipelineIri,
                        SKOS.PREF_LABEL, value, pipelineIri)
        ).collect(Collectors.toList());

        result.addAll(newLabels);
        return result;
    }

}
