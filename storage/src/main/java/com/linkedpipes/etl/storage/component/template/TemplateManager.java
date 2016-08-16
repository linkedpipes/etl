package com.linkedpipes.etl.storage.component.template;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.Configuration;
import com.linkedpipes.etl.storage.component.jar.JarComponent;
import com.linkedpipes.etl.storage.component.jar.JarFacade;
import com.linkedpipes.etl.storage.configuration.ConfigurationFacade;
import com.linkedpipes.etl.storage.rdf.PojoLoader;
import com.linkedpipes.etl.storage.rdf.RdfUtils;
import org.apache.commons.io.FileUtils;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.*;

/**
 * @author Petr Škoda
 */
@Service
class TemplateManager {

    private static final Logger LOG =
            LoggerFactory.getLogger(TemplateManager.class);

    @Autowired
    private JarFacade jarFacade;

    @Autowired
    private Configuration configuration;

    /**
     * List of templates referenced by IRI.
     */
    private final Map<String, BaseTemplate> templates = new HashMap<>();

    @PostConstruct
    public void initialize() {
        // Create template directory if not exists.
        final File templatesDirectory = configuration.getTemplatesDirectory();
        if (!templatesDirectory.exists()) {
            templatesDirectory.mkdir();
        }
        // Create templates from JAR files.
        for (JarComponent item : jarFacade.getJarComponents()) {
            final File destination = new File(templatesDirectory,
                    "jar-" + item.getFile().getName());
            // TODO Do not re-create existing ?
            try {
                JarImport.create(item, destination);
            } catch (Exception ex) {
                LOG.error("Can't load template from JAR component {}",
                        item.getIri(), ex);
                FileUtils.deleteQuietly(destination);
            }
        }
        // Load templates.
        for (File file : templatesDirectory.listFiles()) {
            if (!file.isDirectory()) {
                continue;
            }
            try {
                final BaseTemplate template = loadTemplate(file);
                templates.put(template.getIri(), template);
            } catch (Exception ex) {
                LOG.error("Can't load template from: {}", file, ex);
            }
        }
    }

    public Map<String, BaseTemplate> getTemplates() {
        return Collections.unmodifiableMap(templates);
    }

    /**
     * Create a new template and return it.
     *
     * @param templateRdf
     * @param configurationRdf
     * @return
     */
    public synchronized Template createTemplate(
            Collection<Statement> templateRdf,
            Collection<Statement> configurationRdf) throws BaseException {
        // Get a new IRI.
        String name = "" + (new Date()).getTime();
        String iri = configuration.getDomainName() +
                "/resources/components/" + name;
        while (templates.containsKey(iri)) {
            name = "" + (new Date()).getTime();
            iri = configuration.getDomainName() +
                    "/resources/components/" + name;
        }
        // Create template.
        final File destination
                = new File(configuration.getTemplatesDirectory(), name);
        try {
            ReferenceFactory.create(templateRdf, configurationRdf,
                    destination, iri, this);
            final BaseTemplate template = loadTemplate(destination);
            templates.put(template.getIri(), template);
            return template;
        } catch (BaseException ex) {
            FileUtils.deleteQuietly(destination);
            throw ex;
        }
    }

    /**
     * Load and return template from given directory.
     *
     * @param directory
     * @return Can't be null.
     */
    private static BaseTemplate loadTemplate(File directory)
            throws BaseException {
        final Collection<Statement> interfaceRdf = RdfUtils.read(
                new File(directory, Template.INTERFACE_FILE));
        // Check for full template.
        final Resource fullTemplateResource
                = RdfUtils.find(interfaceRdf, FullTemplate.TYPE);
        if (fullTemplateResource != null) {
            return loadFullTemplate(
                    fullTemplateResource, interfaceRdf, directory);
        }
        // Check for reference template.
        final Resource referenceTemplateResource
                = RdfUtils.find(interfaceRdf, ReferenceTemplate.TYPE);
        if (referenceTemplateResource != null) {
            return loadReferenceTemplate(
                    referenceTemplateResource, interfaceRdf, directory);
        }
        // Unknown template type.
        throw new BaseException("Missing template resource");
    }

    /**
     * Load a full template.
     *
     * @param resource
     * @param interfaceRdf
     * @param directory
     * @return
     */
    private static BaseTemplate loadFullTemplate(Resource resource,
            Collection<Statement> interfaceRdf, File directory)
            throws BaseException {
        final FullTemplate template = new FullTemplate();
        template.setIri(resource.stringValue());
        template.setInterfaceRdf(interfaceRdf);
        //
        loadBaseTemplate(template, directory);
        // Load dialogs.
        final File dialogDirectory = new File(directory, "dialog");
        if (!dialogDirectory.exists()) {
            // There are no dialogs.
            return template;
        }
        final Map<String, FullTemplate.Dialog> dialogs = new HashMap<>();
        for (File file : dialogDirectory.listFiles()) {
            if (!file.isDirectory()) {
                continue;
            }
            final FullTemplate.Dialog dialog = new FullTemplate.Dialog();
            dialog.setName(file.getName());
            dialog.setRoot(file);
            dialogs.put(file.getName(), dialog);
        }
        template.setDialogs(dialogs);
        //
        return template;
    }

    /**
     * Load a reference template.
     *
     * @param resource
     * @param interfaceRdf
     * @param directory
     * @return
     */
    private static BaseTemplate loadReferenceTemplate(Resource resource,
            Collection<Statement> interfaceRdf, File directory)
            throws BaseException {
        final ReferenceTemplate template = new ReferenceTemplate();
        PojoLoader.loadOfType(interfaceRdf, ReferenceTemplate.TYPE, template);
        template.setInterfaceRdf(interfaceRdf);
        //
        if (template.getIri() == null) {
            throw new BaseException("Missing template resource.");
        }
        //
        loadBaseTemplate(template, directory);
        //
        return template;
    }

    /**
     * Load components of {@link BaseTemplate}.
     *
     * @param template
     * @param directory
     */
    private static void loadBaseTemplate(BaseTemplate template, File directory)
            throws BaseException {
        template.setDirectory(directory);
        //
        template.setDefinitionRdf(RdfUtils.read(
                new File(directory, Template.DEFINITION_FILE)));
        template.setConfigRdf(RdfUtils.read(
                new File(directory, Template.CONFIG_FILE)));
        template.setConfigDescRdf(RdfUtils.read(
                new File(directory, Template.CONFIG_DESC_FILE)));
        // Create configuration for instances.
        final IRI graph = SimpleValueFactory.getInstance().createIRI(
                template.getIri() + "/new");
        template.setConfigForInstanceRdf(
                ConfigurationFacade.createConfigurationTemplate(
                        template.getConfigRdf(),
                        template.getConfigDescRdf(),
                        graph.stringValue(), graph));

    }

}
