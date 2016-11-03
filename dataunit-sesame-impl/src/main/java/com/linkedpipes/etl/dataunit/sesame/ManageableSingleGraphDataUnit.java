package com.linkedpipes.etl.dataunit.sesame;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.SesameDataUnit;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;
import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;

/**
 * Interface for a single graph RDF data unit.
 */
public interface ManageableSingleGraphDataUnit extends SesameDataUnit,
        ManageableDataUnit, WritableSingleGraphDataUnit, SparqlSelect {

}
