package com.linkedpipes.plugin.loader.wikibase.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.helpers.ItemDocumentBuilder;
import org.wikidata.wdtk.datamodel.helpers.StatementBuilder;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.MonolingualTextValue;
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;
import org.wikidata.wdtk.datamodel.interfaces.Snak;
import org.wikidata.wdtk.datamodel.interfaces.SnakGroup;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Should be used only if merge strategy is {@link MergeStrategy#MERGE}
 * for the whole document.
 * <p>
 * As for other strategies respective API operation should be used.
 * The statements may use other merging strategy.
 * <p>
 * The final operation may not be merge is some statements should be removed.
 */
public class DocumentMerger {

    private static class StatementId {

        public final String id;

        public final PropertyIdValue property;

        public StatementId(Statement st, PropertyIdValue property) {
            this.id = st.getStatementId();
            this.property = property;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof StatementId)) {
                return false;
            }
            StatementId right = (StatementId) obj;
            return this.id.equals(right.id)
                    && this.property.getId().equals(right.property.getId());
        }

        @Override
        public int hashCode() {
            // We use only property name hash as they can have
            // different prefix.
            return this.id.hashCode() + this.property.getId().hashCode();
        }

    }

    private static final Logger LOG =
            LoggerFactory.getLogger(DocumentMerger.class);

    private final ItemDocument localDocument;

    private final ItemDocument remoteDocument;

    private final Map<Object, MergeStrategy> mergeStrategy;

    private Map<String, String> labelsToAdd = new HashMap<>();

    private Map<String, String> labelsToDelete = new HashMap<>();

    private Map<String, String> labelsToKeep = new HashMap<>();

    private Map<String, String> descriptionsToAdd = new HashMap<>();

    private Map<String, String> descriptionsToDelete = new HashMap<>();

    private Map<String, String> descriptionsToKeep = new HashMap<>();

    private Map<String, Set<String>> aliasesToAdd = new HashMap<>();

    private Map<String, Set<String>> aliasesToDelete = new HashMap<>();

    private Map<String, Set<String>> aliasesToKeep = new HashMap<>();

    private List<Statement> statementsToAdd = new ArrayList<>();

    private List<Statement> statementsToDelete = new ArrayList<>();

    /**
     * Statements that have changed.
     */
    private List<Statement> statementsToUpdate = new ArrayList<>();

    /**
     * Statements that does not change.
     */
    private List<Statement> statementsToKeep = new ArrayList<>();

    private boolean merged = false;

    private final SnakEqual snakEqualStrategy;

    public DocumentMerger(
            ItemDocument local, ItemDocument remote,
            Map<Object, MergeStrategy> mergeStrategy,
            SnakEqual snakEqualStrategy) {
        this.localDocument = local;
        this.remoteDocument = remote;
        this.mergeStrategy = mergeStrategy;
        this.snakEqualStrategy = snakEqualStrategy;
        merge();
    }

    private void merge() {
        if (merged) {
            return;
        }
        merged = true;
        mergeStringMap(
                collectAsMap(localDocument.getLabels()),
                collectAsMap(remoteDocument.getLabels()),
                labelsToAdd,
                labelsToDelete,
                labelsToKeep);
        mergeStringMap(
                collectAsMap(localDocument.getDescriptions()),
                collectAsMap(remoteDocument.getDescriptions()),
                descriptionsToAdd,
                descriptionsToDelete,
                descriptionsToKeep);

        mergeStringListMap(
                collectAsListMap(localDocument.getAliases()),
                collectAsListMap(remoteDocument.getAliases()),
                aliasesToAdd,
                aliasesToDelete,
                aliasesToKeep);

        mergeStatements();
    }

    private void mergeStringMap(
            Map<String, String> localMap,
            Map<String, String> remoteMap,
            Map<String, String> toAdd,
            Map<String, String> toDelete,
            Map<String, String> toKeep) {
        // By default keep all from remote.
        toKeep.putAll(remoteMap);
        // And update with local values.
        for (Map.Entry<String, String> entry : localMap.entrySet()) {
            String key = entry.getKey();
            if (!toKeep.containsKey(key)) {
                // This is a new value, so just add it.
                toAdd.put(key, entry.getValue());
                continue;
            }
            if (toKeep.get(key).equals(entry.getValue())) {
                // The values are the same.
                continue;
            }
            // Values are different so we need to replace them.
            toDelete.put(key, toKeep.get(key));
            toAdd.put(key, entry.getValue());
            toKeep.remove(key);
        }
    }

    private Map<String, String> collectAsMap(
            Map<String, MonolingualTextValue> values) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, MonolingualTextValue> entry :
                values.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getText());
        }
        return result;
    }

    private void mergeStringListMap(
            Map<String, Set<String>> localMap,
            Map<String, Set<String>> remoteMap,
            Map<String, Set<String>> toAdd,
            Map<String, Set<String>> toDelete,
            Map<String, Set<String>> toKeep) {
        // By default keep all from remote.
        toKeep.putAll(remoteMap);
        // And update with local values.
        for (Map.Entry<String, Set<String>> entry : localMap.entrySet()) {
            String key = entry.getKey();
            if (!toKeep.containsKey(key)) {
                // This is a new value, so just add it.
                toAdd.put(key, entry.getValue());
                continue;
            }
            // There are values in local and remote, we need to merge them.
            Set<String> localSet = entry.getValue();
            Set<String> remoteSet = remoteMap.get(key);

            Set<String> deleteSet = new HashSet<>(remoteSet);
            deleteSet.removeAll(localSet);

            Set<String> addSet = new HashSet<>(localSet);
            localSet.removeAll(remoteSet);

            Set<String> keepSet = new HashSet<>(localSet);
            keepSet.retainAll(remoteSet);

            //
            toDelete.put(key, deleteSet);
            toAdd.put(key, addSet);
            if (keepSet.isEmpty()) {
                toKeep.remove(key);
            } else {
                toKeep.put(key, keepSet);
            }
        }
    }

    private Map<String, Set<String>> collectAsListMap(
            Map<String, List<MonolingualTextValue>> values) {
        Map<String, Set<String>> result = new HashMap<>();
        for (Map.Entry<String, List<MonolingualTextValue>> entry :
                values.entrySet()) {
            String lang = entry.getKey();
            Set<String> output =
                    result.computeIfAbsent(lang, (key) -> new HashSet<>());
            for (MonolingualTextValue value : entry.getValue()) {
                if (lang.equals(value.getLanguageCode())) {
                    LOG.error(
                            "Alias language mismatch {} {}",
                            lang, value.getLanguageCode());
                }
                output.add(value.getText());
            }
        }
        return result;
    }

    private void mergeStatements() {
        Map<StatementId, Statement> localSt = statementMap(localDocument);
        Map<StatementId, Statement> remoteSt = statementMap(remoteDocument);
        //
        for (Map.Entry<StatementId, Statement> entry : localSt.entrySet()) {
            MergeStrategy mergeStrategy = getMergeStrategy(entry.getValue());
            switch (mergeStrategy) {
                case DELETE:
                    if (remoteSt.containsKey(entry.getKey())) {
                        remoteSt.remove(entry.getKey());
                        statementsToDelete.add(entry.getValue());
                    }
                    break;
                case NEW:
                    statementsToAdd.add(entry.getValue());
                    break;
                case REPLACE:
                    remoteSt.remove(entry.getKey());
                    statementsToUpdate.add(entry.getValue());
                    break;
                case MERGE:
                    if (remoteSt.containsKey(entry.getKey())) {
                        mergeStatement(
                                entry.getValue(),
                                remoteSt.remove(entry.getKey()),
                                entry.getKey().property);
                    } else {
                        statementsToAdd.add(entry.getValue());
                    }
                    break;
                default:
                    throwUnsupportedStrategy(mergeStrategy);
            }
        }
        // We have some statements in the remoteSt that were not used
        // these should be kept as they are.
        statementsToKeep.addAll(remoteSt.values());
    }

    private void throwUnsupportedStrategy(MergeStrategy strategy) {
        throw new RuntimeException("Unsupported strategy: " + strategy);
    }

    private Map<StatementId, Statement> statementMap(ItemDocument document) {
        Map<StatementId, Statement> results = new HashMap<>();
        for (StatementGroup group : document.getStatementGroups()) {
            PropertyIdValue property = group.getProperty();
            for (Statement statement : group) {
                StatementId key = new StatementId(statement, property);
                if (results.containsKey(key)) {
                    throw new RuntimeException(
                            "Key collision: " + statement.getStatementId());
                }
                results.put(key, statement);
            }
        }
        return results;
    }

    private MergeStrategy getMergeStrategy(Object object) {
        return mergeStrategy.getOrDefault(object, MergeStrategy.MERGE);
    }

    private void mergeStatement(
            Statement local, Statement remote, PropertyIdValue property) {
        if (local.equals(remote)) {
            statementsToKeep.add(remote);
            return;
        }
        StatementBuilder builder = StatementBuilder.forSubjectAndProperty(
                remote.getSubject(), property);
        // Defaults and not supported values.
        builder.withId(remote.getStatementId());
        builder.withRank(remote.getRank());
        if (local.getValue() == null) {
            // If no local value is give, then use remote value to
            // allow user not specify a local value.
            builder.withValue(remote.getValue());
        } else {
            builder.withValue(local.getValue());
        }
        //
        ReferenceMerger referenceMerger =
                new ReferenceMerger(mergeStrategy, snakEqualStrategy);
        builder.withReferences(referenceMerger.merge(
                local.getReferences(), remote.getReferences()));
        builder.withQualifiers(mergeQualifierLists(
                local.getQualifiers(), remote.getQualifiers()));
        //
        Statement result = builder.build();
        statementsToUpdate.add(result);
    }

    private List<SnakGroup> mergeQualifierLists(
            List<SnakGroup> local, List<SnakGroup> remote) {
        List<SnakGroup> result = new ArrayList<>();
        SnakMerger snakMerger = new SnakMerger(snakEqualStrategy);
        snakMerger.merge(local, remote).entrySet().forEach((entry) -> {
            PropertyIdValue property = entry.getKey();
            List<Snak> snaks = entry.getValue().stream()
                    .map((value) -> Datamodel.makeValueSnak(property, value))
                    .collect(Collectors.toList());
            result.add(Datamodel.makeSnakGroup(snaks));
        });
        return result;
    }

    public ItemDocument assembleMergeDocument() {
        ItemDocumentBuilder builder =
                ItemDocumentBuilder.forItemId(localDocument.getEntityId());
        addNewOrChanged(builder);
        addUnsupportedStatementProps(builder);
        return builder.build();
    }

    private void addNewOrChanged(ItemDocumentBuilder builder) {
        // Labels.
        for (Map.Entry<String, String> entry : labelsToAdd.entrySet()) {
            builder.withLabel(entry.getValue(), entry.getKey());
        }
        // Description.
        for (Map.Entry<String, String> entry : descriptionsToAdd.entrySet()) {
            builder.withDescription(entry.getValue(), entry.getKey());
        }
        // Aliases.
        for (Map.Entry<String, Set<String>> entry : aliasesToAdd.entrySet()) {
            for (String value : entry.getValue()) {
                builder.withAlias(value, entry.getKey());
            }
        }
        // Statements - just new or changed.
        for (Statement statement : statementsToAdd) {
            builder.withStatement(statement);
        }
        for (Statement statement : statementsToUpdate) {
            builder.withStatement(statement);
        }
    }

    private void addUnsupportedStatementProps(ItemDocumentBuilder builder) {
        remoteDocument.getSiteLinks().values().forEach(
                (link) -> builder.withSiteLink(link));
        remoteDocument.getAliases().values().forEach(
                (aliases) -> aliases.forEach(
                        (alias) -> builder.withAlias(alias)));
    }

    public ItemDocument assembleReplaceDocument() {
        ItemDocumentBuilder builder =
                ItemDocumentBuilder.forItemId(localDocument.getEntityId());
        addNewOrChanged(builder);
        addToKeep(builder);
        addUnsupportedStatementProps(builder);
        return builder.build();
    }

    private void addToKeep(ItemDocumentBuilder builder) {
        for (Map.Entry<String, String> entry : labelsToKeep.entrySet()) {
            builder.withLabel(entry.getValue(), entry.getKey());
        }
        for (Map.Entry<String, String> entry : descriptionsToKeep.entrySet()) {
            builder.withDescription(entry.getValue(), entry.getKey());
        }
        for (Map.Entry<String, Set<String>> entry : aliasesToKeep.entrySet()) {
            for (String value : entry.getValue()) {
                builder.withAlias(value, entry.getKey());
            }
        }
        for (Statement statement : statementsToKeep) {
            builder.withStatement(statement);
        }
    }

    public boolean canUpdateExisting() {
        return labelsToDelete.isEmpty() && descriptionsToAdd.isEmpty() &&
                aliasesToDelete.isEmpty() && statementsToDelete.isEmpty();
    }

}
