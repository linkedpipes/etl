package com.linkedpipes.etl.storage.vocabulary;

import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;

/**
 * Contains used LinkedPipes vocabulary.
 *
 * @author Petr Škoda
 */
public final class LP {

    private LP() {

    }

    static {
        final ValueFactory vf = SimpleValueFactory.getInstance();
    }

}
