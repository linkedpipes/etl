package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.library.template.plugin.model.JavaPlugin;
import com.linkedpipes.etl.library.template.plugin.model.PluginTemplate;
import com.linkedpipes.etl.storage.StorageException;
import com.linkedpipes.etl.storage.plugin.JavaPluginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemplateService {

    private static final Logger LOG = LoggerFactory.getLogger(TemplateService.class);

    private final TemplateRepository repository;

    private final JavaPluginService pluginService;

    public TemplateService(
            TemplateRepository repository,
            JavaPluginService pluginService) {
        this.repository = repository;
        this.pluginService = pluginService;
    }

    public void initialize() throws StorageException {
        LOG.debug("Initializing template service ... ");
        importJavaPlugins();
        LOG.info("Initializing template service ... done");
    }

    private void importJavaPlugins() throws StorageException {
        for (JavaPlugin plugin : pluginService.getJavaPlugins()) {
            for (PluginTemplate template : plugin.templates()) {
                repository.storePluginTemplate(template);
            }
        }
    }


//    @PostConstruct
//    public void initialize() throws StorageException {
//        try {
//            importJarFiles();
//            importTemplates();
//            if (repository.getInitialVersion()
//                    != LegacyTemplateRepository.LATEST_VERSION) {
//                migrate();
//            }
//            repository.updateFinished();
//        } catch (Exception ex) {
//            LOG.error("Initialization failed.", ex);
//            throw ex;
//        }
//    }
//
//    private void importJarFiles() {
//        ImportFromJarFile copyJarTemplates =
//                new ImportFromJarFile(repository);
//        for (JavaPlugin plugin : jarFacade.getJavaPlugins()) {
//            copyJarTemplates.importJavaPlugin(plugin);
//        }
//    }
//
//    private void importTemplates() {
//        List<ReferenceTemplateRef> referenceTemplates = new ArrayList<>();
//        for (RepositoryReference reference : repository.getReferences()) {
//            try {
//                Template template = loader.loadTemplate(reference);
//                if (template.getIri() == null) {
//                    LOG.error("Invalid template ignored: {}",
//                            reference.getId());
//                    continue;
//                }
//                if (template instanceof ReferenceTemplateRef) {
//                    referenceTemplates.add((ReferenceTemplateRef) template);
//                }
//                templates.put(template.getIri(), template);
//            } catch (Exception ex) {
//                LOG.error("Can't load template: {}", reference.getId(), ex);
//            }
//        }
//        setTemplateCoreReferences(referenceTemplates);
//    }
//
//    private void setTemplateCoreReferences(List<ReferenceTemplateRef> templates) {
//        for (ReferenceTemplateRef template : templates) {
//            template.setCoreTemplate(findCoreTemplate(template));
//        }
//    }
//
//    private JarTemplateRef findCoreTemplate(ReferenceTemplateRef template) {
//        while (true) {
//            Template parent = templates.get(template.getTemplate());
//            if (parent == null) {
//                LOG.error("Missing parent for: {}", template.getIri());
//                return null;
//            }
//            switch (parent.getType()) {
//                case JAR_TEMPLATE:
//                    return (JarTemplateRef) parent;
//                case REFERENCE_TEMPLATE:
//                    template = (ReferenceTemplateRef) parent;
//                    break;
//                default:
//                    LOG.error("Invalid template type: {}",
//                            parent.getIri());
//                    return null;
//            }
//        }
//    }
//
//    @SuppressFBWarnings(value = "SF_SWITCH_FALLTHROUGH")
//    private void migrate() throws StorageException {
//        // CHECKSTYLE.OFF: MagicNumber
//        switch (this.repository.getInitialVersion()) {
//            case 0:
//            case 1:
//                migrateV1ToV2();
//                reloadTemplates();
//            case 2:
//                migrateV2ToV3();
//                reloadTemplates();
//            case 3:
//                // There is no need to reload as updated information is
//                // not stored in memory.
//                migrateV3ToV4();
//            case 4: // Current version
//                break;
//            default:
//                break;
//        }
//        // CHECKSTYLE.ON: MagicNumber
//    }
//
//    private void migrateV1ToV2() throws StorageException {
//        LOG.info("Migrating to version 2");
//        TemplateV1ToV2 v1Tov2 = new TemplateV1ToV2(this, repository);
//        boolean migrationFailed = migrateTemplates(v1Tov2::migrate);
//        if (migrationFailed) {
//            throw new StorageException("Migration failed");
//        }
//    }
//
//    private boolean migrateTemplates(CheckedFunction<Template> callback) {
//        boolean migrationFailed = false;
//        for (Template template : templates.values()) {
//            try {
//                callback.apply(template);
//            } catch (Throwable ex) {
//                LOG.error("Migration of component '{}' failed",
//                        template.getIri(), ex);
//                migrationFailed = true;
//            }
//        }
//        return migrationFailed;
//    }
//
//    private void reloadTemplates() {
//        LOG.info("Reloading templates ...");
//        this.templates.clear();
//        this.importTemplates();
//    }
//
//    private void migrateV2ToV3() throws StorageException {
//        LOG.info("Migrating to version 3");
//        TemplateV2ToV3 v2Tov3 = new TemplateV2ToV3(repository);
//        boolean migrationFailed = migrateTemplates(v2Tov3::migrate);
//        if (migrationFailed) {
//            throw new StorageException("Migration failed");
//        }
//    }
//
//    private void migrateV3ToV4() throws StorageException {
//        LOG.info("Migrating to version 4");
//        TemplateV3ToV4 v3ToV4 = new TemplateV3ToV4(repository);
//        boolean migrationFailed = migrateTemplates(v3ToV4::migrate);
//        if (migrationFailed) {
//            throw new StorageException("Migration failed");
//        }
//    }
//
//    public Map<String, Template> getTemplates() {
//        return Collections.unmodifiableMap(templates);
//    }
//
//    public Template createTemplate(
//            Collection<Statement> interfaceRdf,
//            Collection<Statement> configurationRdf,
//            Collection<Statement> descriptionRdf)
//            throws StorageException {
//        String id = repository.reserveReferenceId();
//        String iri = configuration.getDomainName()
//                + "/resources/components/" + id;
//        ReferenceFactory factory = new ReferenceFactory(repository);
//        try {
//            ReferenceTemplateRef template = factory.create(
//                    interfaceRdf, configurationRdf,
//                    descriptionRdf, iri);
//            template.setCoreTemplate(findCoreTemplate(template));
//            templates.put(template.getIri(), template);
//            return template;
//        } catch (StorageException ex) {
//            repository.remove(RepositoryReference.createReference(iri));
//            throw ex;
//        }
//    }
//
//    public void updateTemplateInterface(
//            Template template, Collection<Statement> diff)
//            throws StorageException {
//        if (template.getType() != Template.Type.REFERENCE_TEMPLATE) {
//            throw new StorageException("Only reference templates can be updated");
//        }
//        diff = RdfUtils.forceContext(diff, template.getIri());
//        Collection<Statement> newInterface =
//                update(repository.getInterface(template), diff);
//        repository.setInterface(template, newInterface);
//    }
//
//    private List<Statement> update(
//            Collection<Statement> data, Collection<Statement> diff) {
//        Map<Resource, Map<IRI, List<Value>>> toReplace = new HashMap<>();
//        diff.forEach((s) ->
//                toReplace.computeIfAbsent(s.getSubject(),
//                        (key) -> new HashMap<>())
//                        .computeIfAbsent(s.getPredicate(),
//                                (key) -> new ArrayList<>())
//                        .add(s.getObject()));
//        List<Statement> output = new ArrayList<>(diff);
//        List<Statement> leftFromOriginal =
//                removeWithSubjectAndPredicate(data, diff);
//        output.addAll(leftFromOriginal);
//        return output;
//    }
//
//    private List<Statement> removeWithSubjectAndPredicate(
//            Collection<Statement> data, Collection<Statement> toRemove) {
//        Map<Resource, Set<IRI>> toDelete = new HashMap<>();
//        toRemove.forEach((s) -> {
//            toDelete.computeIfAbsent(s.getSubject(), (key) -> new HashSet<>())
//                    .add(s.getPredicate());
//        });
//        // Remove all that are not in the toDelete map.
//        return data.stream().filter((s) ->
//                !toDelete.getOrDefault(s.getSubject(), Collections.EMPTY_SET)
//                        .contains(s.getPredicate())
//        ).collect(Collectors.toList());
//    }
//
//    public void updateConfig(
//            Template template, Collection<Statement> statements)
//            throws StorageException {
//        ValueFactory valueFactory = SimpleValueFactory.getInstance();
//        IRI graph = valueFactory.createIRI(
//                template.getIri() + "/configuration");
//        statements = RdfUtils.forceContext(statements, graph);
//        repository.setConfig(template, statements);
//    }
//
//    public void remove(Template template) throws StorageException {
//        if (template.getType() != Template.Type.REFERENCE_TEMPLATE) {
//            throw new StorageException("Can't delete non-reference template: {}",
//                    template.getIri());
//        }
//        templates.remove(template.getIri());
//        repository.remove(template);
//    }
//
//    public void reload() {
//        reloadTemplates();
//    }

}
