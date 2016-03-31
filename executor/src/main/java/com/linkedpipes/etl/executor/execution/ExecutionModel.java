package com.linkedpipes.etl.executor.execution;

import com.linkedpipes.etl.executor.api.v1.event.ComponentBegin;
import com.linkedpipes.etl.executor.api.v1.event.ComponentFailed;
import com.linkedpipes.etl.executor.api.v1.event.ComponentFinished;
import com.linkedpipes.etl.executor.api.v1.event.ComponentProgress;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LINKEDPIPES;
import com.linkedpipes.etl.executor.event.EventManager;
import com.linkedpipes.etl.executor.event.ExecutionBegin;
import com.linkedpipes.etl.executor.event.ExecutionFailed;
import com.linkedpipes.etl.executor.event.ExecutionFinished;
import com.linkedpipes.etl.executor.pipeline.PipelineModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.openrdf.model.IRI;

import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.JSONLDMode;
import org.openrdf.rio.helpers.JSONLDSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: Extract runtime information from pipeline
 *
 * @author Petr Škoda
 */
public final class ExecutionModel implements EventManager.EventListener {

    static enum ExecutionStatus {
        MAPPED("http://etl.linkedpipes.com/resources/status/mapped"),
        QUEUED("http://etl.linkedpipes.com/resources/status/queued"),
        INITIALIZING("http://etl.linkedpipes.com/resources/status/initializing"),
        RUNNING("http://etl.linkedpipes.com/resources/status/running"),
        FINISHED("http://etl.linkedpipes.com/resources/status/finished"),
        FAILED("http://etl.linkedpipes.com/resources/status/failed");

        private final String iri;

        private ExecutionStatus(String iri) {
            this.iri = iri;
        }

        public String getIri() {
            return iri;
        }

    };

    /**
     * Represent an event created during execution.
     */
    static class Event {

        final int id;

        final String iri;

        final Date created = new Date();

        final List<Statement> statements;

        Event(int id, String iri, List<Statement> statements) {
            this.id = id;
            this.iri = iri;
            this.statements = statements;
        }

        public int getId() {
            return id;
        }

        public Date getCreated() {
            return created;
        }

        public List<Statement> getStatements() {
            return statements;
        }

    };

    public static class DataUnit {

        private final String iri;

        private final String binding;

        /**
         * Path to load content from if {@link #mapped} is true.
         */
        private File loadPath;

        /**
         * Can be null if component is mapped. In such case the loadPath
         * should be used as a save path.
         */
        private File savePath;

        /**
         * Path where to store debug data.
         */
        private File debugPath;

        /**
         * List of used debugging directories as the data unit can save
         * used multiple debug directories. Content of this values
         * should be set
         */
        private Collection<File> debugPaths;

        /**
         * If true content of this data unit should be loaded not computed.
         */
        private boolean mapped;

        /**
         * If false then this data unit is not used for the execution and
         * thus does not have to be loaded.
         */
        private boolean usedForExecution = true;

        public DataUnit(String iri, String binding) {
            this.iri = iri;
            this.binding = binding;
        }

        public String getIri() {
            return iri;
        }

        public String getBinding() {
            return binding;
        }

        public File getLoadPath() {
            return loadPath;
        }

        public File getSavePath() {
            return savePath;
        }

        public File getDebugPath() {
            return debugPath;
        }

        public Collection<File> getDebugPaths() {
            return debugPaths;
        }

        public void setDebugPaths(Collection<File> debugPaths) {
            this.debugPaths = debugPaths;
        }

        public boolean isMapped() {
            return mapped;
        }

        public boolean isUsedForExecution() {
            return usedForExecution;
        }

    }

    public static class Component {

        private final String iri;

        /**
         * If true component is mapped from another
         * execution and should not be executed.
         */
        private final boolean mapped;

        private final List<DataUnit> dataUnits = new LinkedList<>();

        /**
         * If not null then represent current component execution progress.
         */
        private Integer progress = null;

        /**
         * If not null then represents the maximum progress value.
         */
        private Integer progressTotal = null;

        /**
         * Execution status.
         */
        private ExecutionStatus status;

        public Component(String iri, boolean mapped) {
            this.iri = iri;
            this.mapped = mapped;
            if (mapped) {
                status = ExecutionStatus.MAPPED;
            } else {
                status = ExecutionStatus.QUEUED;
            }
        }

        public String getIri() {
            return iri;
        }

        public boolean isMapped() {
            return mapped;
        }

        public List<DataUnit> getDataUnits() {
            return dataUnits;
        }

    }

    private static final Logger LOG
            = LoggerFactory.getLogger(ExecutionModel.class);

    /**
     * IRI of executed pipeline.
     */
    private final String pipeline;

    /**
     * Execution IRI.
     */
    private final String iri;

    /**
     * Execution IRI.
     */
    private final IRI graph;

    private final List<Event> events = new ArrayList<>(32);

    /**
     * List of components, sorted in execution order.
     */
    private final List<Component> components;

    private final ResourceManager resources;

    private ExecutionStatus status;

    /**
     * Store time of last change in this class.
     */
    private Date lastChange = new Date();

    public ExecutionModel(PipelineModel pipeline, String iri,
            ResourceManager resources) {
        this.pipeline = pipeline.getIri();
        this.iri = iri;
        this.components = new ArrayList<>(pipeline.getComponents().size());
        this.resources = resources;
        this.status = status.INITIALIZING;
        //
        final ValueFactory vf = SimpleValueFactory.getInstance();
        this.graph = vf.createIRI(iri);
        //
        initialize(pipeline, resources);
    }

    public List<Component> getComponents() {
        return components;
    }

    public Component getComponent(String iri) {
        for (Component component : components) {
            if (component.iri.equals(iri)) {
                return component;
            }
        }
        return null;
    }

    /**
     *
     * @param iri
     * @return
     */
    public DataUnit getDataUnit(String iri) {
        for (Component component : components) {
            for (DataUnit dataUnit : component.dataUnits) {
                if (dataUnit.iri.equals(iri)) {
                    return dataUnit;
                }
            }
        }
        return null;
    }

    /**
     * Save execution into a file.
     */
    public void save() {
        try (OutputStream stream = new FileOutputStream(
                resources.getExecutionFile())) {
            write(stream, RDFFormat.JSONLD);
        } catch (IOException ex) {
            LOG.error("Can't save execution file.", ex);
        }
    }

    /**
     * Serialize content of {@link ExecutionModel} in form of RDF and write
     * it to the stream.
     *
     * @param stream
     * @param format
     */
    public void write(OutputStream stream, RDFFormat format) {
        LOG.debug("write ({}) ...", format.getDefaultFileExtension());
        //
        final RDFWriter writer = Rio.createWriter(format, stream);
        // Custom settings for JSONLD.
        if (format == RDFFormat.JSONLD) {
            writer.set(JSONLDSettings.JSONLD_MODE, JSONLDMode.COMPACT);
        }
        // We need to enforce custom context.
        writer.startRDF();
        writeModel(writer);
        writer.endRDF();
        //
        LOG.debug("write ({}) ... done", format.getDefaultFileExtension());
    }

    @Override
    public void onEvent(com.linkedpipes.etl.executor.api.v1.event.Event event) {
        final StatementsCollector collector = new StatementsCollector(graph);
        event.write(collector);
        final Event executionEvent = new Event(events.size(),
                event.getResource(), collector.getStatements());
        // Add class for event and order.
        final ValueFactory valueFactory = SimpleValueFactory.getInstance();
        executionEvent.statements.add(valueFactory.createStatement(
                valueFactory.createIRI(executionEvent.iri),
                RDF.TYPE,
                valueFactory.createIRI("http://linkedpipes.com/ontology/Event"),
                graph));
        executionEvent.statements.add(valueFactory.createStatement(
                valueFactory.createIRI(executionEvent.iri),
                valueFactory.createIRI("http://linkedpipes.com/ontology/order"),
                valueFactory.createLiteral(executionEvent.id),
                graph));
        events.add(executionEvent);
        //
        if (event instanceof ComponentProgress) {
            final ComponentProgress e = (ComponentProgress) event;
            final Component component = getComponent(e.getComponentUri());
            if (component == null) {
                LOG.warn("Ignored event ({}) with unknown component.",
                        event.getResource());
            } else {
                component.progress = e.getCurrent();
                component.progressTotal = e.getTotal();
            }
        } else if (event instanceof ComponentBegin) {
            final ComponentBegin e = (ComponentBegin) event;
            final Component component = getComponent(e.getComponentUri());
            if (component == null) {
                LOG.warn("Ignored event ({}) with unknown component.",
                        event.getResource());
            } else {
                component.status = ExecutionStatus.RUNNING;
            }
        } else if (event instanceof ComponentFailed) {
            final ComponentFailed e = (ComponentFailed) event;
            final Component component = getComponent(e.getComponentUri());
            if (component == null) {
                LOG.warn("Ignored event ({}) with unknown component.",
                        event.getResource());
            } else {
                component.status = ExecutionStatus.FAILED;
            }
        } else if (event instanceof ComponentFinished) {
            final ComponentFinished e = (ComponentFinished) event;
            final Component component = getComponent(e.getComponentUri());
            if (component == null) {
                LOG.warn("Ignored event ({}) with unknown component.",
                        event.getResource());
            } else {
                component.status = ExecutionStatus.FINISHED;
            }
        } else if (event instanceof ExecutionBegin) {
            status = ExecutionStatus.RUNNING;
        } else if (event instanceof ExecutionFinished) {
            if (status != ExecutionStatus.FAILED) {
                status = ExecutionStatus.FINISHED;
            }
        } else if (event instanceof ExecutionFailed) {
            status = ExecutionStatus.FAILED;
        }
        //
        lastChange = new Date();
    }

    /**
     * Initialize this class from the pipeline model object.
     *
     * @param pipeline
     * @param resources
     */
    private void initialize(PipelineModel pipeline, ResourceManager resources) {
        // First we copy all components that shall be
        // execute or mapped.
        for (PipelineModel.Component component : pipeline.getComponents()) {
            switch (component.getExecutionType()) {
                case EXECUTE:
                    components.add(createComponent(resources, pipeline,
                            component, false));
                    break;
                case MAP:
                    components.add(createComponent(resources, pipeline,
                            component, true));
                    break;
                case SKIP:
                    break;
                default:
                    LOG.error("Unknown component ({}) execution type. "
                            + "Component ignored.", component.getIri());
            }
        }
        // Sort components.
        components.sort((left, right) -> {
            final Integer leftOrder
                    = pipeline.getComponent(left.iri).getExecutionOrder();
            final Integer rightOrder
                    = pipeline.getComponent(right.iri).getExecutionOrder();
            //
            if (leftOrder < rightOrder) {
                return -1;
            } else if (leftOrder > rightOrder) {
                return 1;
            } else {
                return 0;
            }
        });
        //
        lastChange = new Date();
    }

    /**
     * Serialize into statements. Only and statements to the handler, it's
     * responsibility of called to call {@link RDFHandler#startRDF()}
     * and {@link RDFHandler#endRDF()} methods.
     *
     *
     * @param handler
     */
    private void writeModel(RDFHandler handler) {
        final ValueFactory vf = SimpleValueFactory.getInstance();
        int counter = 0;
        //
        final IRI executionResource = vf.createIRI(iri);
        handler.handleStatement(vf.createStatement(executionResource, RDF.TYPE,
                vf.createIRI("http://etl.linkedpipes.com/ontology/Execution"),
                graph));
        handler.handleStatement(vf.createStatement(executionResource,
                vf.createIRI("http://etl.linkedpipes.com/ontology/pipeline"),
                vf.createIRI(pipeline), graph));
        handler.handleStatement(vf.createStatement(executionResource,
                vf.createIRI("http://etl.linkedpipes.com/ontology/status"),
                vf.createIRI(status.getIri()), graph));
        handler.handleStatement(vf.createStatement(executionResource,
                vf.createIRI("http://etl.linkedpipes.com/ontology/lastChange"),
                vf.createLiteral(lastChange), graph));
        // Save components.
        for (Component component : components) {
            final IRI componentResource = vf.createIRI(component.iri);
            handler.handleStatement(vf.createStatement(componentResource,
                    RDF.TYPE,
                    vf.createIRI(LINKEDPIPES.COMPONENT), graph));
            handler.handleStatement(vf.createStatement(executionResource,
                    vf.createIRI(LINKEDPIPES.HAS_COMPONENT),
                    componentResource, graph));
            handler.handleStatement(vf.createStatement(componentResource,
                    vf.createIRI("http://etl.linkedpipes.com/ontology/status"),
                    vf.createIRI(component.status.getIri()), graph));
            // Sve progress if avalable.
            if (component.progress != null) {
                handler.handleStatement(vf.createStatement(componentResource,
                        vf.createIRI("http://etl.linkedpipes.com/ontology/progress/current"),
                        vf.createLiteral(component.progress), graph));
            }
            if (component.progressTotal != null) {
                handler.handleStatement(vf.createStatement(componentResource,
                        vf.createIRI("http://etl.linkedpipes.com/ontology/progress/total"),
                        vf.createLiteral(component.progressTotal), graph));
            }
            // Save data units.
            for (DataUnit dataUnit : component.dataUnits) {
                final IRI dataUnitResource = vf.createIRI(dataUnit.iri);
                handler.handleStatement(vf.createStatement(dataUnitResource, RDF.TYPE,
                        vf.createIRI("http://etl.linkedpipes.com/ontology/DataUnit"),
                        graph));
                handler.handleStatement(vf.createStatement(componentResource,
                        vf.createIRI("http://etl.linkedpipes.com/ontology/dataUnit"),
                        dataUnitResource, graph));
                //
                handler.handleStatement(vf.createStatement(dataUnitResource,
                        vf.createIRI("http://etl.linkedpipes.com/ontology/binding"),
                        vf.createLiteral(dataUnit.binding), graph));
                handler.handleStatement(vf.createStatement(dataUnitResource,
                        vf.createIRI("http://etl.linkedpipes.com/ontology/dataPath"),
                        vf.createLiteral(resources.relativize(dataUnit.savePath)),
                        graph));
                // Path suffix used to acess debug data.
                handler.handleStatement(vf.createStatement(dataUnitResource,
                        vf.createIRI("http://etl.linkedpipes.com/ontology/debug"),
                        vf.createLiteral(String.format("%03d", ++counter)),
                        graph));
                //
                for (File path : dataUnit.debugPaths) {
                    handler.handleStatement(vf.createStatement(dataUnitResource,
                            vf.createIRI("http://etl.linkedpipes.com/ontology/debugPath"),
                            vf.createLiteral(resources.relativize(path)),
                            graph));
                }
            }
        }
        // Save events.
        for (Event event : events) {
            handler.handleStatement(vf.createStatement(executionResource,
                    vf.createIRI("http://etl.linkdpipes.com/ontology/event"),
                    vf.createIRI(event.iri),
                    graph));
            for (Statement st : event.statements) {
                handler.handleStatement(st);
            }
        }
    }

    /**
     * Create and return execution model for a component from a pipeline
     * component model.
     *
     * @param resources
     * @param pipeline
     * @param component
     * @param isMapped
     * @return
     */
    private static Component createComponent(ResourceManager resources,
            PipelineModel pipeline, PipelineModel.Component component,
            boolean isMapped) {
        final Component executionComponent = new Component(component.getIri(),
                isMapped);
        // Add data units.
        for (PipelineModel.DataUnit dataUnit : component.getDataUnits()) {
            final DataUnit executionDataUnit = new DataUnit(dataUnit.getIri(),
                    dataUnit.getBinding());
            //
            final PipelineModel.DataSource source = dataUnit.getDataSource();
            if (source != null) {
                executionDataUnit.loadPath = resources.resolveExecutionPath(
                        source.getExecution(), source.getLoadPath());
                executionDataUnit.debugPath = null;
                // Resolve path references.
                executionDataUnit.debugPaths = new ArrayList<>(
                        source.getDebugPaths().size());
                for (String path : source.getDebugPaths()) {
                    executionDataUnit.debugPaths.add(
                            resources.resolveExecutionPath(
                                    source.getExecution(),
                                    path));
                }
                executionDataUnit.mapped = true;
                executionDataUnit.savePath = null;
                executionDataUnit.usedForExecution = isDataUnitUsed(pipeline,
                        dataUnit.getIri());
            } else {
                executionDataUnit.loadPath = null;
                executionDataUnit.debugPath = resources.getWorkingDirectory(
                        "debug");
                executionDataUnit.debugPaths = Collections.EMPTY_LIST;
                executionDataUnit.mapped = false;
                executionDataUnit.savePath
                        = resources.getWorkingDirectory("save");
                executionDataUnit.usedForExecution = true;
            }
            //
            executionComponent.dataUnits.add(executionDataUnit);
        }

        //
        return executionComponent;
    }

    /**
     *
     * @param iri
     * @return True if data unit is used during the execution.
     */
    private static boolean isDataUnitUsed(PipelineModel pipeline, String iri) {
        // Component must be used by executing component or be sourceData
        // for such component.
        for (PipelineModel.Component comp : pipeline.getComponents()) {
            if (comp.getExecutionType() != PipelineModel.ExecutionType.EXECUTE) {
                continue;
            }
            for (PipelineModel.DataUnit du : comp.getDataUnits()) {
                if (du.getIri().equals(iri) || du.getSources().contains(iri)) {
                    return true;
                }
            }
        }
        return false;
    }

}
