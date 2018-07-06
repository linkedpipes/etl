package com.linkedpipes.etl.storage.template.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.Configuration;
import com.linkedpipes.etl.storage.rdf.RdfUtils;
import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class WritableTemplateRepository extends TemplateRepository {

    private static final Logger LOG
            = LoggerFactory.getLogger(TemplateRepository.class);

    @Autowired
    public WritableTemplateRepository(Configuration configuration) {
        super(configuration);
    }

    @PostConstruct
    public void initialize() {
        loadRepositoryInfo();
    }

    public String reserveReferenceId() throws BaseException {
        for (int i = 0; i < 32; ++i) {
            String id = createId();
            File path = getDirectory(RepositoryReference.Reference(id));
            if (path.mkdir()) {
                return id;
            }
        }
        throw new BaseException("Can not create directory in: {}",
                this.repositoryRoot);
    }

    private String createId() {
        return Long.toString((new Date()).getTime()) + "-" + UUID.randomUUID();
    }

    public void setInterface(
            RepositoryReference ref, Collection<Statement> statements)
            throws RdfUtils.RdfException {
        File path = new File(getDirectory(ref), "interface.trig");
        RdfUtils.write(path, RDFFormat.TRIG, statements);
    }

    public void setDefinition(
            RepositoryReference ref, Collection<Statement> statements)
            throws RdfUtils.RdfException {
        File path = new File(getDirectory(ref), "definition.trig");
        RdfUtils.write(path, RDFFormat.TRIG, statements);
    }

    public void setConfig(
            RepositoryReference ref, Collection<Statement> statements)
            throws RdfUtils.RdfException {
        File path = new File(getDirectory(ref), "configuration.trig");
        RdfUtils.write(path, RDFFormat.TRIG, statements);
    }

    public void setConfigDescription(
            RepositoryReference ref, Collection<Statement> statements)
            throws RdfUtils.RdfException {
        File path = new File(getDirectory(ref),
                "configuration-description.trig");
        RdfUtils.write(path, RDFFormat.TRIG, statements);
    }

    public void remove(RepositoryReference ref) {
        File dir = getDirectory(ref);
        if (!FileUtils.deleteQuietly(dir)) {
            LOG.warn("Can not delete: {}", dir);
        }
    }

    /**
     * Called once initialization is done, used to save info file.
     */
    public void updateFinished() {
        // TODO Export version to some shared single space.
        this.info.version = RepositoryInfo.LATEST_VERSION;
        try {
            saveRepositoryInfo();
        } catch (IOException ex) {
            LOG.error("Can not save repository info file.", ex);
        }
    }

    private void saveRepositoryInfo() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(repositoryInfoFile(), this.info);
    }

}
