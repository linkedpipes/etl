package com.linkedpipes.etl.library.pipeline.adapter;

import com.linkedpipes.etl.library.pipeline.vocabulary.LP_V1;
import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.rdf.StatementsSelector;
import com.linkedpipes.etl.library.pipeline.model.DataRetentionPolicy;
import com.linkedpipes.etl.library.pipeline.model.Pipeline;
import com.linkedpipes.etl.library.pipeline.model.PipelineComponent;
import com.linkedpipes.etl.library.pipeline.model.PipelineConnection;
import com.linkedpipes.etl.library.pipeline.model.PipelineDataFlow;
import com.linkedpipes.etl.library.pipeline.model.PipelineExecutionFlow;
import com.linkedpipes.etl.library.pipeline.model.PipelineExecutionProfile;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RdfToPipeline {

    private static final String PIPELINE = LP_V1.PIPELINE;

    public static List<Pipeline> asPipelines(Statements statements) {
        StatementsSelector selector = statements.selector();
        return selector.selectByType(PIPELINE)
                .stream().map(statement -> loadPipeline(
                        selector,
                        statement.getSubject(),
                        statement.getContext()))
                .collect(Collectors.toList());
    }

    private static Pipeline loadPipeline(
            StatementsSelector statements,
            Resource resource, Resource graph) {
        Resource publicResource = null;
        Literal label = null, version = null, note = null;
        List<Literal> tags = new ArrayList<>();
        PipelineExecutionProfile profile = null;
        LocalDateTime created = null, lastUpdate= null;

        for (Statement statement : statements.withSubject(resource)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case LP_V1.PREF_LABEL:
                    if (value.isLiteral()) {
                        label = (Literal) value;
                    }
                    break;
                case LP_V1.HAS_VERSION:
                    if (value.isLiteral()) {
                        version = (Literal) value;
                    }
                    break;
                case LP_V1.NOTE:
                    if (value.isLiteral()) {
                        note = (Literal) value;
                    }
                    break;
                case LP_V1.HAS_TAG:
                    if (value.isLiteral()) {
                        tags.add((Literal) value);
                    }
                    break;
                case LP_V1.HAS_PROFILE:
                    if (value.isResource()) {
                        profile = loadExecutionProfile(
                                statements, (Resource) value);
                    }
                    break;
                default:
                    break;
            }
        }
        // There is no connection from pipeline to components and
        // connections, so we load that by type.
        StatementsSelector pipelineGraph =
                statements.selectByGraph(graph).selector();
        List<PipelineComponent> components = new ArrayList<>();
        for (Resource subject : pipelineGraph.selectByType(
                LP_V1.COMPONENT).subjects()) {
            components.add(loadComponent(statements, subject));
        }
        List<PipelineConnection> connections = new ArrayList<>();
        for (Resource subject : pipelineGraph.selectByType(
                LP_V1.RUN_AFTER).subjects()) {
            connections.add(loadExecutionFlow(statements, subject));
        }
        for (Resource subject : pipelineGraph.selectByType(
                LP_V1.CONNECTION).subjects()) {
            connections.add(loadDataFlow(statements, subject));
        }

        profile = sanitizeProfile(profile, resource);

        return new Pipeline(
                resource, publicResource,
                created, lastUpdate, label, version, note, tags,
                profile, components, connections
        );
    }

    private static PipelineExecutionProfile loadExecutionProfile(
            StatementsSelector statements, Resource profileResource) {
        Resource rdfRepositoryPolicy = null, rdfRepositoryType = null;
        DataRetentionPolicy logRetentionPolicy = DataRetentionPolicy.DEFAULT,
                debugDataRetentionPolicy = DataRetentionPolicy.DEFAULT;
        Integer failedExecutionLimit = null, successfulExecutionLimit = null;
        for (Statement statement : statements.withSubject(profileResource)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case LP_V1.HAS_RDF_REPOSITORY_POLICY:
                    if (value.isResource()) {
                        rdfRepositoryPolicy = (Resource) value;
                    }
                    break;
                case LP_V1.HAS_RDF_REPOSITORY_TYPE:
                    if (value.isResource()) {
                        rdfRepositoryType = (Resource) value;
                    }
                    break;

                case LP_V1.HAS_LOG_RETENTION:
                    if (value.isIRI()) {
                        logRetentionPolicy = DataRetentionPolicy
                                .fromIri(value.stringValue());
                    }
                    break;
                case LP_V1.HAS_DATA_RETENTION:
                    if (value.isIRI()) {
                        debugDataRetentionPolicy = DataRetentionPolicy
                                .fromIri(value.stringValue());
                    }
                    break;
                case LP_V1.HAS_FAILED_EXECUTION_LIMIT:
                    if (value.isLiteral()) {
                        failedExecutionLimit = ((Literal) value).intValue();
                    }
                    break;
                case LP_V1.HAS_SUCCESSFUL_EXECUTION_LIMIT:
                    if (value.isResource()) {
                        successfulExecutionLimit = ((Literal) value).intValue();
                    }
                    break;
                default:
                    break;
            }
        }
        return new PipelineExecutionProfile(
                profileResource,
                rdfRepositoryPolicy, rdfRepositoryType,
                logRetentionPolicy, debugDataRetentionPolicy,
                failedExecutionLimit, successfulExecutionLimit);
    }

    private static PipelineComponent loadComponent(
            StatementsSelector statements, Resource componentResource) {
        Literal label = null, description = null, note = null,
                x = null, y = null, disabled = null;
        Value color = null;
        Resource template = null;
        IRI configurationGraph = null;

        for (Statement statement : statements.withSubject(componentResource)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case LP_V1.PREF_LABEL:
                    if (value.isLiteral()) {
                        label = (Literal) value;
                    }
                    break;
                case LP_V1.HAS_DESCRIPTION:
                    if (value.isLiteral()) {
                        description = (Literal) value;
                    }
                    break;
                case LP_V1.NOTE:
                    if (value.isLiteral()) {
                        note = (Literal) value;
                    }
                    break;
                case LP_V1.HAS_COLOR:
                    color = value;
                    break;
                case LP_V1.HAS_CONFIGURATION_GRAPH:
                    if (value.isIRI()) {
                        configurationGraph = (IRI) value;
                    }
                    break;
                case LP_V1.HAS_X:
                    if (value.isLiteral()) {
                        x = (Literal) value;
                    }
                    break;
                case LP_V1.HAS_Y:
                    if (value.isLiteral()) {
                        y = (Literal) value;
                    }
                case LP_V1.HAS_TEMPLATE:
                    if (value.isResource()) {
                        template = (Resource) value;
                    }
                    break;
                case LP_V1.HAS_DISABLED:
                    if (value.isLiteral()) {
                        disabled = (Literal) value;
                    }
                default:
                    break;
            }
        }
        return new PipelineComponent(
                componentResource, label, description, note, color,
                x, y, template, disabled, configurationGraph,
                statements.selectByGraph(configurationGraph)
        );
    }

    private static PipelineDataFlow loadDataFlow(
            StatementsSelector statements, Resource flowResource) {
        Resource source = null, target = null;
        Value sourceBinding = null, targetBinding = null;
        for (Statement statement : statements.withSubject(flowResource)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case LP_V1.HAS_SOURCE_BINDING:
                    sourceBinding = value;
                    break;
                case LP_V1.HAS_TARGET_BINDING:
                    targetBinding = value;
                    break;
                case LP_V1.HAS_SOURCE_COMPONENT:
                    if (value.isResource()) {
                        source = (Resource) value;
                    }
                    break;
                case LP_V1.HAS_TARGET_COMPONENT:
                    if (value.isResource()) {
                        target = (Resource) value;
                    }
                    break;
                default:
                    break;
            }
        }
        return new PipelineDataFlow(
                flowResource, source, target,
                sourceBinding, targetBinding);
    }

    private static PipelineExecutionFlow loadExecutionFlow(
            StatementsSelector statements, Resource flowResource) {
        Resource source = null, target = null;
        for (Statement statement : statements.withSubject(flowResource)) {
            Value value = statement.getObject();
            switch (statement.getPredicate().stringValue()) {
                case LP_V1.HAS_SOURCE_COMPONENT:
                    if (value.isResource()) {
                        source = (Resource) value;
                    }
                    break;
                case LP_V1.HAS_TARGET_COMPONENT:
                    if (value.isResource()) {
                        target = (Resource) value;
                    }
                    break;
                default:
                    break;
            }
        }
        return new PipelineExecutionFlow(flowResource, source, target);
    }

    /**
     * Make sure execution profile exists. In addition, profile
     * resource is determined by pipeline resource is that is not blank node.
     */
    private static PipelineExecutionProfile sanitizeProfile(
            PipelineExecutionProfile profile, Resource pipeline) {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        Resource resource;
        if (pipeline.isBNode()) {
            resource = valueFactory.createBNode();
        } else {
            resource = valueFactory.createIRI(pipeline + "/profile/default");
        }

        PipelineExecutionProfile result;
        if (profile == null) {
            result = new PipelineExecutionProfile(resource);
        } else {
            result = new PipelineExecutionProfile(
                    resource,
                    profile.rdfRepositoryPolicy(),
                    profile.rdfRepositoryType(),
                    profile.logRetentionPolicy(),
                    profile.debugDataRetentionPolicy(),
                    profile.failedExecutionLimit(),
                    profile.successfulExecutionLimit());
        }
        return result;
    }

}
