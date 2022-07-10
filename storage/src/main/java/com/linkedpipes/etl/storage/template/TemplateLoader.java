package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.rdf.PojoLoader;
import com.linkedpipes.etl.storage.template.repository.RepositoryReference;
import com.linkedpipes.etl.storage.template.repository.TemplateRepository;
import org.eclipse.rdf4j.model.Statement;

import java.util.Collection;

/**
 * Load templates into {@link TemplateService}.
 */
public class TemplateLoader {

    private final TemplateRepository repository;

    public TemplateLoader(TemplateRepository repository) {
        this.repository = repository;
    }

    public Template loadTemplate(RepositoryReference reference)
            throws StorageException {
        switch (reference.getType()) {
            case JAR_TEMPLATE:
                return loadJarTemplate(reference);
            case REFERENCE_TEMPLATE:
                return loadReferenceTemplate(reference);
            default:
                throw new StorageException("No template find for: {}",
                        reference.getId());
        }
    }

    public JarTemplateRef loadJarTemplate(
            RepositoryReference reference) throws StorageException {
        Collection<Statement> definition =
                repository.getDefinition(reference);
        JarTemplateRef template = new JarTemplateRef();
        template.setId(reference.getId());
        PojoLoader.loadOfType(definition, JarTemplateRef.TYPE, template);
        return template;
    }

    public ReferenceTemplateRef loadReferenceTemplate(
            RepositoryReference reference) throws StorageException {
        Collection<Statement> definition =
                repository.getDefinition(reference);
        ReferenceTemplateRef template = new ReferenceTemplateRef();
        template.setId(reference.getId());
        PojoLoader.loadOfType(definition, ReferenceTemplateRef.TYPE, template);
        return template;
    }

}
