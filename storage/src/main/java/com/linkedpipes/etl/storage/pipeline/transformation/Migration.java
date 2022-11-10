package com.linkedpipes.etl.storage.pipeline.transformation;

//import com.linkedpipes.etl.library.rdf.Statements;
//import com.linkedpipes.etl.storage.SuppressFBWarnings;
//import com.linkedpipes.etl.storage.migration.MigrateV0ToV1;
//import com.linkedpipes.etl.storage.migration.MigrateV1ToV2;
//import com.linkedpipes.etl.storage.pipeline.PipelineRef;
//import com.linkedpipes.etl.storage.rdf.RdfObjects;
//import com.linkedpipes.etl.storage.rdf.RdfUtils;
//import com.linkedpipes.etl.storage.template.TemplateFacade;
//import org.eclipse.rdf4j.model.Literal;
//import org.eclipse.rdf4j.model.Resource;
//import org.eclipse.rdf4j.model.Statement;
//import org.eclipse.rdf4j.model.Value;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//import java.util.function.Predicate;
//
///**
// * Provide functionality to perform updates on the pipeline.
// */
//class Migration {
//
//    private static final Logger LOG
//            = LoggerFactory.getLogger(Migration.class);
//
//    private final TemplateFacade templateFacade;
//
//    private Statements configurations;
//
//    private RdfObjects.Entity pipelineEntity;
//
//    private Resource pipelineResource;
//
//    private RdfObjects pipelineObject;
//
//    private boolean throwOnWarning = true;
//
//    public Migration(TemplateFacade templateFacade) {
//        this.templateFacade = templateFacade;
//    }
//
//    public void setThrowOnWarning(boolean throwOnWarning) {
//        this.throwOnWarning = throwOnWarning;
//    }
//
//    @SuppressFBWarnings(value = "SF_SWITCH_FALLTHROUGH")
//    public Collection<Statement> migrate(Collection<Statement> pipeline)
//            throws TransformationFailed {
//        this.findPipelineResource(pipeline);
//        if (this.pipelineResource == null) {
//            throw new TransformationFailed("Missing pipeline resource.");
//        }
//        this.parseStatements(pipeline);
//        int version = this.getVersion();
//        LOG.info("Migrating pipeline '{}' version '{}'",
//                this.pipelineResource, version);
//        switch (version) {
//            case 0:
//                v0Tov1();
//            case 1:
//                v1Tov2();
//            case 2: // Current version
//                break;
//            default:
//                throw new TransformationFailed("Invalid version!");
//        }
//        updateVersionToLatest(pipelineEntity);
//        return collect();
//    }
//
//    private void findPipelineResource(Collection<Statement> pipeline) {
//        this.pipelineResource = RdfUtils.find(pipeline, PipelineRef.TYPE);
//    }
//
//    private void parseStatements(Collection<Statement> pipeline) {
//        Statements all = Statements.wrap(pipeline);
//
//        Predicate<Statement> configurationFilter =
//                (s) -> !s.getContext().equals(this.pipelineResource);
//
//        this.configurations = Statements.wrap(
//                all.stream().filter(configurationFilter).toList());
//
//        Predicate<Statement> pipelineObjectFilter =
//                (s) -> s.getContext().equals(this.pipelineResource);
//
//        this.pipelineObject = new RdfObjects(
//                all.stream().filter(pipelineObjectFilter).toList());
//        this.pipelineEntity =
//                this.pipelineObject.getTypeSingle(PipelineRef.TYPE);
//    }
//
//    private int getVersion() {
//        try {
//            Value value = pipelineEntity.getProperty(PipelineRef.HAS_VERSION);
//            return ((Literal) value).intValue();
//        } catch (Exception ex) {
//            return 0;
//        }
//    }
//
//    private void v0Tov1() throws TransformationFailed {
//        MigrateV0ToV1 v0ToV1 = new MigrateV0ToV1(
//                this.templateFacade, this.throwOnWarning);
//        v0ToV1.pipeline(this.pipelineObject);
//    }
//
//    private void v1Tov2() {
//        MigrateV1ToV2 v1ToV2 = new MigrateV1ToV2(this.templateFacade);
//        v1ToV2.pipeline(this.configurations, this.pipelineObject);
//    }
//
//    private void updateVersionToLatest(RdfObjects.Entity pipeline) {
//        pipeline.delete(PipelineRef.HAS_VERSION);
//        pipeline.add(PipelineRef.HAS_VERSION, PipelineRef.VERSION_NUMBER);
//    }
//
//    private List<Statement> collect() {
//        List<Statement> output = new ArrayList<>();
//        output.addAll(pipelineObject.asStatements(pipelineResource));
//        output.addAll(configurations);
//        return output;
//    }
//
//}
