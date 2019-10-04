package com.linkedpipes.plugin.loader.wikibase.model;

import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.helpers.ItemDocumentBuilder;
import org.wikidata.wdtk.datamodel.helpers.ReferenceBuilder;
import org.wikidata.wdtk.datamodel.helpers.StatementBuilder;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.MonolingualTextValue;
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;
import org.wikidata.wdtk.datamodel.interfaces.Reference;
import org.wikidata.wdtk.datamodel.interfaces.Snak;
import org.wikidata.wdtk.datamodel.interfaces.SnakGroup;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;
import org.wikidata.wdtk.datamodel.interfaces.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
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

    private final ItemDocument localDocument;

    private final ItemDocument remoteDocument;

    private final Map<Object, MergeStrategy> mergeStrategy;

    private Map<String, String> labelsToAdd = new HashMap<>();

    private Map<String, String> labelsToDelete = new HashMap<>();

    private Map<String, String> labelsToKeep = new HashMap<>();

    private Map<String, String> descriptionsToAdd = new HashMap<>();

    private Map<String, String> descriptionsToDelete = new HashMap<>();

    private Map<String, String> descriptionsToKeep = new HashMap<>();

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

    public DocumentMerger(
            ItemDocument local, ItemDocument remote,
            Map<Object, MergeStrategy> mergeStrategy) {
        this.localDocument = local;
        this.remoteDocument = remote;
        this.mergeStrategy = mergeStrategy;
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
            if (toKeep.containsKey(key)) {
                if (toKeep.get(key).equals(entry.getValue())) {
                    // The values are the same.
                    continue;
                }
                // Values are different so we need to replace them.
                toDelete.put(key, toKeep.get(key));
                toAdd.put(key, entry.getValue());
                toKeep.remove(key);
            } else {
                // Just add a new one.
                toAdd.put(key, entry.getValue());
            }
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
        builder.withValue(local.getValue());
        //
        builder.withReferences(mergeReferenceLists(
                local.getReferences(), remote.getReferences()));
        builder.withQualifiers(mergeQualifierLists(
                local.getQualifiers(), remote.getQualifiers()));
        //
        Statement result = builder.build();
        statementsToUpdate.add(result);
    }

    private List<Reference> mergeReferenceLists(
            List<Reference> local, List<Reference> remote) {
        // Make copy of remove as we perform update, and we do not know
        // if would not change some internal state.
        remote = new ArrayList<>(remote);
        List<Reference> results = new ArrayList<>();
        // For each from local find best match in remote and merge.
        for (Reference reference : local) {
            MergeStrategy mergeStrategy = getMergeStrategy(local);
            if (mergeStrategy.equals(MergeStrategy.NEW)) {
                // Just add the new one.
                results.add(reference);
                continue;
            }
            Reference bestRemote = findBestMatchReference(reference, remote);
            if (bestRemote == null) {
                // Can happen if there is no best match, ie. all remote
                // references were already consumed.
                switch (mergeStrategy) {
                    case DELETE:
                        // There is no match so we have nothing to delete.
                        break;
                    case REPLACE:
                    case MERGE:
                        // As there is no match we can just add the new one.
                        results.add(reference);
                        break;
                    default:
                        throwUnsupportedStrategy(mergeStrategy);
                }
            } else {
                // Consume best match, so it can not be used anymore.
                remote.remove(bestRemote);
                switch (mergeStrategy) {
                    case DELETE:
                        // We already removed the reference.
                        break;
                    case REPLACE:
                        results.add(reference);
                        break;
                    case MERGE:
                        results.add(mergeReferences(reference, bestRemote));
                        break;
                    default:
                        throwUnsupportedStrategy(mergeStrategy);
                }
            }
        }
        // Add what is left as it is unchanged.
        results.addAll(remote);
        return results;
    }

    /**
     * @return Null if no reference match.
     */
    private Reference findBestMatchReference(
            Reference query, List<Reference> database) {
        Reference bestMath = null;
        double bestMatchScore = -1.0;
        for (Reference item : database) {
            double score = calculateReferenceSimilarity(query, item);
            if (score >= bestMatchScore) {
                bestMatchScore = score;
                bestMath = item;
            }
        }
        return bestMath;
    }

    private double calculateReferenceSimilarity(
            Reference left, Reference right) {
        int shared = 0;
        for (SnakGroup leftSnakGroup : left.getSnakGroups()) {
            PropertyIdValue property = leftSnakGroup.getProperty();
            for (SnakGroup rightSnakGroup : right.getSnakGroups()) {
                if (!property.equals(rightSnakGroup.getProperty())) {
                    continue;
                }
                // We have the same property, now compute the number of shared
                // values.
                for (Snak leftSnak : leftSnakGroup) {
                    for (Snak rightSnak : rightSnakGroup) {
                        if (leftSnak.equals(rightSnak)) {
                            shared += 1;
                        }
                    }
                }
            }
        }
        //
        int total = countValues(left) + countValues(right);
        return (double) shared / (double) total;
    }

    private int countValues(Reference reference) {
        int result = 0;
        Iterator<Snak> iterator = reference.getAllSnaks();
        while (iterator.hasNext()) {
            iterator.next();
            result++;
        }
        return result;
    }

    private Reference mergeReferences(Reference local, Reference remote) {
        ReferenceBuilder builder = ReferenceBuilder.newInstance();

        mergeSnakGroups(local.getSnakGroups(), remote.getSnakGroups())
                .entrySet()
                .forEach((entry) -> {
                    entry.getValue().forEach((value) -> {
                        builder.withPropertyValue(entry.getKey(), value);
                    });
                });

        Reference result = builder.build();
        return result;
    }

    private Map<PropertyIdValue, Set<Value>> mergeSnakGroups(
            List<SnakGroup> local, List<SnakGroup> remote) {
        Map<PropertyIdValue, Set<Value>> result = new HashMap<>();

        local.forEach((group) -> {
            PropertyIdValue property = group.getProperty();
            Set<Value> valuesForProperty = new LinkedHashSet<>();
            result.put(property, valuesForProperty);
            group.getSnaks().forEach((snak) -> {
                valuesForProperty.add(snak.getValue());
            });
        });

        remote.forEach((group) -> {
            PropertyIdValue property =group.getProperty();
            Set<Value> valuesForProperty;
            if (result.containsKey(property)) {
                valuesForProperty = result.get(property);
            } else {
                valuesForProperty = new LinkedHashSet<>();
                result.put(property, valuesForProperty);
            }
            group.getSnaks().forEach((snak) -> {
                valuesForProperty.add(snak.getValue());
            });
        });

        return result;
    }

    private List<SnakGroup> mergeQualifierLists(
            List<SnakGroup> local, List<SnakGroup> remote) {
        List<SnakGroup> result = new ArrayList<>();
        mergeSnakGroups(local, remote).entrySet().forEach((entry) -> {
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
        for (Statement statement : statementsToKeep) {
            builder.withStatement(statement);
        }
    }

    public boolean canUpdateExisting() {
        return labelsToDelete.isEmpty() && descriptionsToAdd.isEmpty() &&
                statementsToDelete.isEmpty();
    }

}
