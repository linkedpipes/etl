package com.linkedpipes.etl.storage.pipeline.info;

import com.linkedpipes.etl.storage.Configuration;
import com.linkedpipes.etl.storage.pipeline.Pipeline;
import org.openrdf.model.*;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Provide existing pipeline based knowledge to improve used experience
 * with pipeline design.
 *
 * @author Petr Škoda
 */
@Service
public class InfoFacade {

    private static class TemplateInfo {

        private Map<Resource, Integer> followup = new HashMap<>();

        /**
         * Merge information from given info to this instance.
         *
         * @param info
         */
        private void merge(TemplateInfo info) {
            for (Map.Entry<Resource, Integer> entry :
                    info.followup.entrySet()) {
                followup.put(entry.getKey(), followup.getOrDefault(
                        entry.getKey(), 0) +entry.getValue());
            }
        }

    }


    /**
     * Holds information about a single pipeline.
     */
    private static class PipelineInfo {

        /**
         * List of used tags.
         */
        private Collection<Value> tags = new HashSet<>();

        /**
         * Templates by type.
         */
        private Map<Resource, TemplateInfo> templates = new HashMap<>();

        /**
         * Merge information from given info to this instance.
         *
         * @param info
         */
        private void merge(PipelineInfo info) {
            tags.addAll(info.tags);
            info.templates.entrySet().forEach((entry) -> {
                TemplateInfo templateInfo = templates.get(entry.getKey());
                if (templateInfo == null) {
                    templateInfo = new TemplateInfo();
                    templates.put(entry.getKey(), templateInfo);
                }
                templateInfo.merge(entry.getValue());
            });
        }

    }

    /**
     * Holds information about connection.
     */
    private static class Connection {

        /**
         * Set by loader to indicate that the connection has
         * required class.
         */
        private boolean isValid = false;

        private Resource source;

        private Resource target;

        private void add(Map<Resource, Resource> instanceToTemplate,
                PipelineInfo info) {
            final Resource sourceTemplate = instanceToTemplate.get(source);
            final Resource targetTemplate = instanceToTemplate.get(target);
            if (sourceTemplate == null || targetTemplate == null) {
                return;
            }
            //
            TemplateInfo templateInfo = info.templates.get(sourceTemplate);
            if (templateInfo == null) {
                templateInfo = new TemplateInfo();
                info.templates.put(sourceTemplate, templateInfo);
            }
            templateInfo.followup.put(target,
                    templateInfo.followup.getOrDefault(target, 0) + 1);
        }

    }

    private final static IRI TYPE;

    static {
        TYPE = SimpleValueFactory.getInstance().createIRI(
                "http://linkedpipes.com/ontology/PipelineInformation");
    }

    @Autowired
    private Configuration configuration;

    /**
     * Cached RDF form of information.
     */
    private List<Statement> information = new LinkedList<>();

    private Map<String, PipelineInfo> pipelineInfo = new HashMap<>();

    public Collection<Statement> getInformation() {
        return information;
    }

    public void onPipelineCreate(Pipeline pipeline,
            Collection<Statement> pipelineRdf) {
        final PipelineInfo info = createInfo(pipeline, pipelineRdf);
        pipelineInfo.put(pipeline.getIri(), info);
        regenerate();
    }

    public void onPipelineUpdate(Pipeline pipeline,
            Collection<Statement> pipelineRdf) {
        final PipelineInfo info = createInfo(pipeline, pipelineRdf);
        pipelineInfo.put(pipeline.getIri(), info);
        regenerate();
    }

    public void onPipelineDelete(Pipeline pipeline) {
        pipelineInfo.remove(pipeline.getIri());
    }

    /**
     * Regenerate {@link #information} from {@link #pipelineInfo}.
     */
    private void regenerate() {
        // Prepare data.
        final PipelineInfo globalInfo = new PipelineInfo();
        pipelineInfo.values().forEach((item) -> globalInfo.merge(item));
        // Write data.
        final ValueFactory vf = SimpleValueFactory.getInstance();
        final List<Statement> statements = new LinkedList<>();
        final IRI graphIri = vf.createIRI(
                configuration.getDomainName() + "/pipelines/info");
        final IRI rootResource = vf.createIRI(
                configuration.getDomainName() + "/resources/pipelines/info");
        statements.add(vf.createStatement(
                rootResource, RDF.TYPE, TYPE, graphIri
        ));
        for (Value tag : globalInfo.tags) {
            statements.add(vf.createStatement(rootResource,
                    vf.createIRI("http://etl.linkedpipes.com/ontology/tag"),
                    tag, graphIri));
        }
        Integer counter = 0;
        for (Map.Entry<Resource, TemplateInfo> template :
                globalInfo.templates.entrySet()) {
            for (Map.Entry<Resource, Integer> followup
                    : template.getValue().followup.entrySet()) {
                final Resource followupResource = vf.createIRI(
                        configuration.getDomainName() +
                                "/resources/pipelines/info/followup/" +
                                (++counter));
                //
                statements
                        .add(vf.createStatement(rootResource, vf.createIRI(
                                "http://etl.linkedpipes.com/ontology/followup"),
                                followupResource, graphIri));
                statements.add(vf.createStatement(followupResource,
                        vf.createIRI(
                                "http://etl.linkedpipes.com/ontology/source"),
                        template.getKey(), graphIri));
                statements.add(vf.createStatement(followupResource,
                        vf.createIRI(
                                "http://etl.linkedpipes.com/ontology/target"),
                        followup.getKey(), graphIri));
                statements.add(vf.createStatement(followupResource,
                        vf.createIRI(
                                "http://etl.linkedpipes.com/ontology/frequency"),
                        vf.createLiteral(followup.getValue()),
                        graphIri));
            }
        }
        //
        information = statements;
    }

    private static PipelineInfo createInfo(Pipeline pipeline,
            Collection<Statement> pipelineRdf) {
        final PipelineInfo info = new PipelineInfo();
        //
        final Map<Resource, Resource> componentTypes = new HashMap<>();
        final Map<Resource, Connection> connections = new HashMap<>();
        for (Statement statement : pipelineRdf) {
            if (!statement.getContext().stringValue().equals(
                    pipeline.getIri())) {
                continue;
            }
            if (statement.getPredicate().equals(RDF.TYPE)) {
                if (statement.getObject().stringValue().equals(
                        "http://linkedpipes.com/ontology/Connection")) {
                    final Connection connection = getConnection(connections,
                            statement.getSubject());
                    connection.isValid = true;
                }
                continue;
            }
            // We assume that the predicates are used only of the
            // right resources. So we can just parse based on predicate
            // value.
            switch (statement.getPredicate().stringValue()) {
                case "http://etl.linkedpipes.com/ontology/tag":
                    info.tags.add(statement.getObject());
                    break;
                case "http://linkedpipes.com/ontology/template":
                    componentTypes.put(statement.getSubject(),
                            (Resource)statement.getObject());
                    break;
                case "http://linkedpipes.com/ontology/sourceComponent":
                    getConnection(connections, statement.getSubject()).source =
                            (Resource)statement.getObject();
                    break;
                case "http://linkedpipes.com/ontology/targetComponent":
                    getConnection(connections, statement.getSubject()).target =
                            (Resource)statement.getObject();
                    break;
            }
        }
        // Update connections with componentTypes and add them
        // to pipeline info.
        for (Map.Entry<Resource, Connection> entry
                : connections.entrySet()) {
            final Connection connection = entry.getValue();
            connection.source = componentTypes.get(connection.source);
            connection.target = componentTypes.get(connection.target);
            if (!connection.isValid || connection.source == null ||
                    connection.target == null) {
                continue;
            }
            //
            TemplateInfo templateInfo = info.templates.get(connection.source);
            if (templateInfo == null) {
                templateInfo = new TemplateInfo();
                info.templates.put(connection.source, templateInfo);
            }
            templateInfo.followup.put(connection.target,
                    templateInfo.followup.getOrDefault(connection.target, 0)
                    + 1);
        }
        return info;
    }

    /**
     *
     * @param connections
     * @param resource
     * @return Connection for given resource.
     */
    private static Connection getConnection(
            Map<Resource, Connection> connections, Resource resource) {
        Connection connection = connections.get(resource);
        if (connection == null) {
            connection = new Connection();
            connections.put(resource, connection);
        }
        return connection;
    }

}
