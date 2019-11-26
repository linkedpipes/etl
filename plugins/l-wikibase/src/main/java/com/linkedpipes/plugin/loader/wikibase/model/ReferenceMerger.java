package com.linkedpipes.plugin.loader.wikibase.model;

import org.wikidata.wdtk.datamodel.helpers.ReferenceBuilder;
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;
import org.wikidata.wdtk.datamodel.interfaces.Reference;
import org.wikidata.wdtk.datamodel.interfaces.Snak;
import org.wikidata.wdtk.datamodel.interfaces.SnakGroup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class ReferenceMerger {

    private static class SimilarityPair {

        final Reference local;

        final Reference remote;

        final Double similarity;

        final boolean exactMatch;

        public SimilarityPair(
                Reference local, Reference remote, Double similarity,
                boolean exactMatch) {
            this.local = local;
            this.remote = remote;
            this.similarity = similarity;
            this.exactMatch = exactMatch;
        }

    }

    private final Map<Object, MergeStrategy> mergeStrategy;

    private ArrayList<SimilarityPair> similarities;

    private List<Reference> results;

    private final SnakEqual snakEqualStrategy;

    public ReferenceMerger(
            Map<Object, MergeStrategy> mergeStrategy,
            SnakEqual snakEqualStrategy) {
        this.mergeStrategy = mergeStrategy;
        this.snakEqualStrategy = snakEqualStrategy;
    }

    public List<Reference> merge(
            List<Reference> locals, List<Reference> remotes) {
        // Make copy of remove as we perform update, and we do not know
        // if would not change some internal state.
        locals = new ArrayList<>(locals);
        remotes = new ArrayList<>(remotes);
        computeSimilarities(locals, remotes);
        results = new ArrayList<>();
        // We start with locals, as they describe what need to be changed.
        for (int index = 0; index < similarities.size(); ++index) {
            SimilarityPair pair = similarities.get(index);
            if (pair == null) {
                // Item is no longer valid.
                continue;
            }
            // Now we have the best match of local and remote.
            MergeStrategy mergeStrategy = getMergeStrategy(pair.local);
            if (MergeStrategy.EXACT.equals(mergeStrategy)) {
                // We handle this here as we need to call
                // removeSimilarityPairs with different arguments.
                if (pair.exactMatch) {
                    // Data are same, so just add local data.
                    results.add(pair.local);
                    removeSimilarityPairs(pair.local, pair.remote);
                    locals.remove(pair.local);
                    remotes.remove(pair.remote);
                } else {
                    // Data are similar but not same,
                    // so we add the local value and leave the remote
                    // value untouched.
                    results.add(pair.local);
                    removeSimilarityPairs(pair.local, null);
                    locals.remove(pair.local);
                }
                continue;
            }
            // Remove from locals and remotes.
            locals.remove(pair.local);
            remotes.remove(pair.remote);
            switch (mergeStrategy) {
                case DELETE:
                    // We already removed the reference.
                    break;
                case MERGE:
                    results.add(mergeReferences(pair.local, pair.remote));
                    break;
                case REPLACE:
                default:
                    throwUnsupportedStrategy(mergeStrategy);
            }
            removeSimilarityPairs(pair.local, pair.remote);
        }
        // We may be left with some locals that were not processed.
        for (Reference local : locals) {
            mergeRemainingLocal(local);
        }
        // Add what is left as it is unchanged.
        results.addAll(remotes);
        return results;
    }

    private void computeSimilarities(
            List<Reference> local, List<Reference> remote) {
        similarities = new ArrayList<>(local.size() * remote.size());
        for (Reference localRef : local) {
            for (Reference remoteRef : remote) {
                int shared = countShared(localRef, remoteRef);
                int total =
                        Math.max(countValues(localRef), countValues(remoteRef));
                similarities.add(new SimilarityPair(
                        localRef, remoteRef,
                        (double) shared / total,
                        shared == total
                ));
            }
        }
        // Sort in decreasing order and put exactMatch first.
        similarities.sort((left, right) -> {
            int compare = Double.compare(left.similarity, right.similarity);
            if (compare == 0) {
                if (left.exactMatch && right.exactMatch) {
                    return 0;
                } else if (left.exactMatch) {
                    // Left has exactMatch and right does not, put left first.
                    return -1;
                } else {
                    return 1;
                }
            }
            // Reverse order.
            return -compare;
        });
    }

    private int countShared(
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
                        if (snakEqualStrategy.equal(leftSnak, rightSnak)) {
                            shared += 1;
                        }
                    }
                }
            }
        }
        return shared;
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

    /**
     * Remove all records mentioning local or remote reference.
     */
    private void removeSimilarityPairs(Reference local, Reference remote) {
        for (int index = 0; index < similarities.size(); ++index) {
            SimilarityPair pair = similarities.get(index);
            if (pair == null) {
                continue;
            }
            if (pair.local.equals(local) || pair.remote.equals(remote)) {
                similarities.set(index, null);
            }

        }
    }

    private MergeStrategy getMergeStrategy(Object object) {
        return mergeStrategy.getOrDefault(object, MergeStrategy.MERGE);
    }

    private void throwUnsupportedStrategy(MergeStrategy strategy) {
        throw new RuntimeException("Unsupported strategy: " + strategy);
    }

    private Reference mergeReferences(Reference local, Reference remote) {
        ReferenceBuilder builder = ReferenceBuilder.newInstance();
        SnakMerger snakMerger = new SnakMerger(snakEqualStrategy);
        snakMerger.merge(local.getSnakGroups(), remote.getSnakGroups())
                .entrySet()
                .forEach((entry) -> {
                    entry.getValue().forEach((value) -> {
                        builder.withPropertyValue(entry.getKey(), value);
                    });
                });
        Reference result = builder.build();
        return result;
    }

    private void mergeRemainingLocal(Reference local) {
        MergeStrategy mergeStrategy = getMergeStrategy(local);
        switch (mergeStrategy) {
            case DELETE:
                // There is no match so we have nothing to delete.
                break;
            case NEW:
            case EXACT:
                // As there is no match we can just add the new one.
                results.add(local);
                break;
            case REPLACE:
            default:
                throwUnsupportedStrategy(mergeStrategy);
        }
    }

}
