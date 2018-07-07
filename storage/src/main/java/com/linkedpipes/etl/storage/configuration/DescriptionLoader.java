package com.linkedpipes.etl.storage.configuration;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.rdf.PojoLoader;
import org.eclipse.rdf4j.model.Statement;

import java.util.Collection;

class DescriptionLoader {

    Description load(Collection<Statement> descriptionRdf)
            throws BaseException {
        Description description = new Description();
        PojoLoader.loadOfType(descriptionRdf, Description.TYPE, description);
        if (description.getType() == null) {
            throw new BaseException("Missing configuration type.");
        }
        return description;
    }

}
