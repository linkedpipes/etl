package com.linkedpipes.etl.dataunit.sesame;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.SesameDataUnit;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableGraphListDataUnit;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManagableDataUnit;
import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;

/**
 * Interface for multi-graph RDF data unit.
 *
 * @author Petr Škoda
 */
public interface ManagableGraphListDataUnit extends SesameDataUnit,
        ManagableDataUnit, WritableGraphListDataUnit, SparqlSelect {

}
