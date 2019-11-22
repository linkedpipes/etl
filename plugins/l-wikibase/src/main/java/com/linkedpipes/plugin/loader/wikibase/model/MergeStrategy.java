package com.linkedpipes.plugin.loader.wikibase.model;

import com.linkedpipes.plugin.loader.wikibase.WikibaseLoaderVocabulary;

import java.util.List;

public enum MergeStrategy {
    NEW,
    REPLACE,
    DELETE,
    MERGE,
    EXACT;

    public static MergeStrategy fromTypesOrDefault(
            List<String> types, MergeStrategy defaultStrategy) {
        if (types.contains(WikibaseLoaderVocabulary.NEW_STRATEGY)) {
            return NEW;
        } else if (types.contains(WikibaseLoaderVocabulary.REPLACE_STRATEGY)) {
            return REPLACE;
        } else if (types.contains(WikibaseLoaderVocabulary.DELETE_STRATEGY)) {
            return DELETE;
        } else if (types.contains(WikibaseLoaderVocabulary.MERGE_STRATEGY)) {
            return MERGE;
        } else if (types.contains(WikibaseLoaderVocabulary.EXACT_STRATEGY)) {
            return EXACT;
        }
        return defaultStrategy;
    }

}
