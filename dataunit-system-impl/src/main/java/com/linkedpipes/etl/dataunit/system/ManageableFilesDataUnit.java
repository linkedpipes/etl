package com.linkedpipes.etl.dataunit.system;

import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;

/**
 * @author Petr Škoda
 */
public interface ManageableFilesDataUnit
        extends ManageableDataUnit, WritableFilesDataUnit {

}
