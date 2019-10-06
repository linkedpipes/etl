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

    private final Map<Object, MergeStrategy> mergeStrategy;

    public ReferenceMerger(Map<Object, MergeStrategy> mergeStrategy) {
        this.mergeStrategy = mergeStrategy;
    }

    public List<Reference> merge(
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

    private MergeStrategy getMergeStrategy(Object object) {
        return mergeStrategy.getOrDefault(object, MergeStrategy.MERGE);
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

    private void throwUnsupportedStrategy(MergeStrategy strategy) {
        throw new RuntimeException("Unsupported strategy: " + strategy);
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
        SnakMerger snakMerger = new SnakMerger();
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

}
