package com.linkedpipes.plugin.loader.wikibase.model;

import com.linkedpipes.plugin.loader.wikibase.WikibaseLoaderVocabulary;

import java.util.List;

public enum MergeStrategy {
    NEW,
    REPLACE,
    DELETE,
    MERGE;

    public static MergeStrategy fromTypes(List<String> types) {
        if (types.contains(WikibaseLoaderVocabulary.NEW_STRATEGY)) {
            return NEW;
        } else if (types.contains(WikibaseLoaderVocabulary.REPLACE_STRATEGY)) {
            return REPLACE;
        } else if (types.contains(WikibaseLoaderVocabulary.DELETE_STRATEGY)) {
            return DELETE;
        } else if (types.contains(WikibaseLoaderVocabulary.MERGE_STRATEGY)) {
            return MERGE;
        }
        return MERGE;
    }

}
