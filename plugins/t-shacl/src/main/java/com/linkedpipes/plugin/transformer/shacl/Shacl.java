package com.linkedpipes.plugin.transformer.shacl;

import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.RDF4J;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.shacl.ShaclSail;
import org.eclipse.rdf4j.sail.shacl.ShaclSailValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;

public final class Shacl implements Component, SequentialExecution {

    private static final Logger LOG = LoggerFactory.getLogger(Shacl.class);

    @Component.InputPort(iri = "RulesRdf")
    public SingleGraphDataUnit rulesRdf;

    @Component.InputPort(iri = "InputRdf")
    public SingleGraphDataUnit inputRdf;

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.OutputPort(iri = "ReportRdf")
    public WritableSingleGraphDataUnit reportRdf;

    @Component.Configuration
    public ShaclConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        ShaclSail shaclSail = new ShaclSail(new MemoryStore());
        SailRepository sailRepository = new SailRepository(shaclSail);
        sailRepository.init();
        try {
            loadRules(sailRepository);
            validateData(sailRepository);
        } finally {
            LOG.info("Shutting down repository ...");
            sailRepository.shutDown();
        }
    }

    private void loadRules(SailRepository sailRepository) throws LpException {
        LOG.info("Adding rules to SHACL repository ...");
        try (RepositoryConnection connection =
                     sailRepository.getConnection()) {
            connection.begin();
            addRulesFromInput(connection);
            addRulesFromConfiguration(connection);
            connection.commit();
        } catch (RuntimeException | IOException ex) {
            throw exceptionFactory.failure("Can't load rules.", ex);
        }
    }

    private void addRulesFromInput(RepositoryConnection connection)
            throws LpException {
        rulesRdf.execute((inputConnection) -> {
            RepositoryResult<Statement> statements =
                    inputConnection.getStatements(
                            null, null, null, rulesRdf.getReadGraph());
            connection.add(statements, RDF4J.SHACL_SHAPE_GRAPH);
        });
    }

    private void addRulesFromConfiguration(RepositoryConnection connection)
            throws IOException {
        StringReader reader = new StringReader(configuration.getRule());
        connection.add(
                reader, "", RDFFormat.TURTLE, RDF4J.SHACL_SHAPE_GRAPH);
    }

    private void validateData(SailRepository sailRepository) throws LpException {
        try (SailRepositoryConnection connection =
                     sailRepository.getConnection()) {
            connection.begin();
            LOG.info("Adding content to SHACL repository ...");
            inputRdf.execute((inputConnection) -> {
                RepositoryResult<Statement> statements =
                        inputConnection.getStatements(
                                null, null, null, inputRdf.getReadGraph());
                connection.add(statements);
            });
            LOG.info("Validating ..");
            try {
                connection.commit();
            } catch (RepositoryException ex) {
                Throwable cause = ex.getCause();
                if (cause instanceof ShaclSailValidationException) {
                    onRuleViolation((ShaclSailValidationException) cause);
                } else {
                    throw exceptionFactory.failure("Can't validate data", ex);
                }
            }
        }
    }

    private void onRuleViolation(ShaclSailValidationException ex)
            throws LpException {
        LOG.info("Writing validation report ...");
        Model validationReportModel = ex.validationReportAsModel();
        try (RepositoryConnection connection =
                     reportRdf.getRepository().getConnection()) {
            connection.add(validationReportModel, reportRdf.getWriteGraph());
        }
        if (configuration.isFailOnError()) {
            throw exceptionFactory.failure("Validation failed.");
        }
    }

}
