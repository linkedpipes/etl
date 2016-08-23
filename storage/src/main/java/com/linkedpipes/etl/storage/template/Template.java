package com.linkedpipes.etl.storage.template;


/**
 * Public interface for a template typed component.
 *
 * @author Petr Škoda
 */
public interface Template {

    String INTERFACE_FILE = "interface.trig";

    String DEFINITION_FILE = "definition.trig";

    String CONFIG_FILE = "configuration.trig";

    String CONFIG_DESC_FILE = "configuration-description.trig";

    String getIri();

}
