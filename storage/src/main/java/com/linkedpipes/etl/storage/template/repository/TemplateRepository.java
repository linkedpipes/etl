package com.linkedpipes.etl.storage.template.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkedpipes.etl.storage.Configuration;
import com.linkedpipes.etl.storage.rdf.RdfUtils;
import com.linkedpipes.etl.storage.template.Template;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TemplateRepository {

    private static final Logger LOG
            = LoggerFactory.getLogger(TemplateRepository.class);

    public static final int LATEST_VERSION = 4;

    protected final File repositoryRoot;

    protected RepositoryInfo info = null;

    private int initialVersion;

    public TemplateRepository(Configuration configuration) {
        this.repositoryRoot = configuration.getTemplatesDirectory();
    }

    public int getInitialVersion() {
        return initialVersion;
    }

    // TODO Move to a global version management.
    protected void loadRepositoryInfo() {
        if (!repositoryInfoFile().exists()) {
            info = new RepositoryInfo();
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            info = mapper.readValue(repositoryInfoFile(), RepositoryInfo.class);
        } catch (IOException ex) {
            LOG.warn("Can not read repository info file.", ex);
            info = new RepositoryInfo();
        }
        initialVersion = info.version;
    }

    protected File repositoryInfoFile() {
        return new File(repositoryRoot, "repository-info.json");
    }

    public Collection<Statement> getInterface(RepositoryReference ref)
            throws RdfUtils.RdfException {
        File path = new File(getDirectory(ref), "interface.trig");
        return RdfUtils.read(path, RDFFormat.TRIG);
    }

    public File getDirectory(RepositoryReference ref) {
        switch (ref.getType()) {
            case JAR_TEMPLATE:
                return new File(repositoryRoot, ref.getId());
            case REFERENCE_TEMPLATE:
                return new File(repositoryRoot, ref.getId());
            default:
                throw new RuntimeException("Unknown type: " + ref.getType());
        }
    }

    public Collection<Statement> getDefinition(RepositoryReference ref)
            throws RdfUtils.RdfException {
        File path = new File(getDirectory(ref), "definition.trig");
        return RdfUtils.read(path, RDFFormat.TRIG);
    }

    public Collection<Statement> getConfig(RepositoryReference ref)
            throws RdfUtils.RdfException {
        File path = new File(getDirectory(ref), "configuration.trig");
        return RdfUtils.read(path, RDFFormat.TRIG);
    }

    public Collection<Statement> getConfigDescription(
            RepositoryReference ref) throws RdfUtils.RdfException {
        File path = new File(getDirectory(ref),
                "configuration-description.trig");
        return RdfUtils.read(path, RDFFormat.TRIG);
    }

    public File getDialogFile(
            RepositoryReference ref, String dialog, String path) {
        if (ref.getType() != Template.Type.JAR_TEMPLATE) {
            return null;
        }
        // TODO Check that we are still in the directory!
        String relativePath = "dialog" + File.separator
                + dialog + File.separator + path;
        return new File(getDirectory(ref), relativePath);
    }

    public File getStaticFile(RepositoryReference ref, String path) {
        if (ref.getType() != Template.Type.JAR_TEMPLATE) {
            return null;
        }
        // TODO Check that we are still in the directory!
        String relativePath = "static" + File.separator + path;
        return new File(getDirectory(ref), relativePath);
    }

    public List<RepositoryReference> getReferences() {
        List<RepositoryReference> output = new ArrayList<>();
        File[] files = repositoryRoot.listFiles();
        if (files == null) {
            return output;
        }
        for (File file : files) {
            if (!file.isDirectory()) {
                continue;
            }
            if (file.getName().startsWith("jar-")) {
                output.add(RepositoryReference.createJar(file.getName()));
            } else {
                output.add(RepositoryReference.createReference(file.getName()));
            }
        }
        return output;
    }

}
