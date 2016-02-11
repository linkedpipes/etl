package com.linkedpipes.etl.dataunit.sesame.rdf;

import com.linkedpipes.etl.dataunit.sesame.api.rdf.SesameDataUnit;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableGraphListDataUnit;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManagableDataUnit;
import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;

/**
 *
 * @author Petr Škoda
 */
public interface ManagableGraphListDataUnit extends SesameDataUnit, ManagableDataUnit, WritableGraphListDataUnit,
        SparqlSelect {

}
