package com.linkedpipes.etl.storage.pipeline.info;

import com.linkedpipes.etl.storage.Configuration;
import com.linkedpipes.etl.storage.pipeline.PipelineRef;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Provide existing pipeline based knowledge to improve used experience
 * with pipeline design.
 */
@Service
public class InfoFacade {

    private static class TemplateInfo {

        private Map<Resource, Integer> followup = new HashMap<>();

        /**
         * Merge information from given info to this instance.
         */
        private void merge(TemplateInfo info) {
            for (Map.Entry<Resource, Integer> entry :
                    info.followup.entrySet()) {
                followup.put(entry.getKey(), followup.getOrDefault(
                        entry.getKey(), 0) + entry.getValue());
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
         * Followup info for templates by type.
         */
        private Map<Resource, TemplateInfo> followups = new HashMap<>();

        /**
         * Templates used in this pipeline.
         */
        private Set<Resource> templates = new HashSet<>();

        /**
         * Merge information from given info to this instance.
         */
        private void merge(PipelineInfo info) {
            tags.addAll(info.tags);
            info.followups.entrySet().forEach((entry) -> {
                TemplateInfo templateInfo = followups.get(entry.getKey());
                if (templateInfo == null) {
                    templateInfo = new TemplateInfo();
                    followups.put(entry.getKey(), templateInfo);
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

    }

    private static final IRI TYPE;

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

    /**
     * Return names of pipeline where given pipeline is used.
     */
    public Collection<String> getUsage(String templateIriAsString) {
        Resource templateIri = SimpleValueFactory.getInstance().createIRI(
                templateIriAsString);
        List<String> usage = new LinkedList<>();
        for (Map.Entry<String, PipelineInfo> entry : pipelineInfo.entrySet()) {
            PipelineInfo info = entry.getValue();
            if (info.templates.contains(templateIri)) {
                usage.add(entry.getKey());
            }
        }
        return usage;
    }

    public void onPipelineCreate(
            PipelineRef pipeline, Collection<Statement> pipelineRdf) {
        PipelineInfo info = createInfo(pipeline, pipelineRdf);
        pipelineInfo.put(pipeline.getIri(), info);
        regenerate();
    }

    public void onPipelineUpdate(
            PipelineRef pipeline, Collection<Statement> pipelineRdf) {
        PipelineInfo info = createInfo(pipeline, pipelineRdf);
        pipelineInfo.put(pipeline.getIri(), info);
        regenerate();
    }

    public void onPipelineDelete(PipelineRef pipeline) {
        pipelineInfo.remove(pipeline.getIri());
        regenerate();
    }

    /**
     * Regenerate {@link #information} from {@link #pipelineInfo}.
     */
    private void regenerate() {
        // Prepare data.
        PipelineInfo globalInfo = new PipelineInfo();
        pipelineInfo.values().forEach((item) -> globalInfo.merge(item));
        // Write data.
        ValueFactory vf = SimpleValueFactory.getInstance();
        List<Statement> statements = new LinkedList<>();
        IRI graphIri = vf.createIRI(
                configuration.getDomainName() + "/pipelines/info");
        IRI rootResource = vf.createIRI(
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
                globalInfo.followups.entrySet()) {
            for (Map.Entry<Resource, Integer> followup
                    : template.getValue().followup.entrySet()) {
                Resource followupResource = vf.createIRI(
                        configuration.getDomainName()
                                + "/resources/pipelines/info/followup/"
                                + (++counter));
                //
                statements.add(vf.createStatement(rootResource, vf.createIRI(
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

    private static PipelineInfo createInfo(
            PipelineRef pipeline, Collection<Statement> pipelineRdf) {
        PipelineInfo info = new PipelineInfo();
        //
        Map<Resource, Resource> componentTypes = new HashMap<>();
        Map<Resource, Connection> connections = new HashMap<>();
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
                            (Resource) statement.getObject());
                    break;
                case "http://linkedpipes.com/ontology/sourceComponent":
                    getConnection(connections, statement.getSubject()).source =
                            (Resource) statement.getObject();
                    break;
                case "http://linkedpipes.com/ontology/targetComponent":
                    getConnection(connections, statement.getSubject()).target =
                            (Resource) statement.getObject();
                    break;
                default:
                    break;
            }
        }
        // Update connections with componentTypes and add them
        // to pipeline info.
        for (Map.Entry<Resource, Connection> entry
                : connections.entrySet()) {
            Connection connection = entry.getValue();
            connection.source = componentTypes.get(connection.source);
            connection.target = componentTypes.get(connection.target);
            if (!connection.isValid || connection.source == null
                    || connection.target == null) {
                continue;
            }
            //
            TemplateInfo templateInfo = info.followups.get(connection.source);
            if (templateInfo == null) {
                templateInfo = new TemplateInfo();
                info.followups.put(connection.source, templateInfo);
            }
            templateInfo.followup.put(connection.target,
                    templateInfo.followup.getOrDefault(
                            connection.target, 0) + 1);
        }
        info.templates.addAll(componentTypes.values());
        return info;
    }

    /**
     * Return connection for given resource.
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
