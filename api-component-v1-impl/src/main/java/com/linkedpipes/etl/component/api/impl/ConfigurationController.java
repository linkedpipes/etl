package com.linkedpipes.etl.component.api.impl;

import com.linkedpipes.etl.component.api.impl.rdf.RdfReader;
import com.linkedpipes.etl.executor.api.v1.RdfException;
import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;

import java.util.*;

/**
 * Can be used to merge configurations.
 */
class ConfigurationController implements RdfReader.MergeOptionsFactory {

    public class MergerPolicy implements RdfReader.MergeOptions {

        private final Map<String, String> options;

        private MergerPolicy(Map<String, String> options) {
            this.options = options;
        }

        @Override
        public boolean load(String predicate) {
            if (ConfigurationController.this.loadingRuntime) {
                // In case of runtime configuration we are loading all.
                return true;
            }
            if (ConfigurationController.this.objectInherit ||
                    ConfigurationController.this.objectForced) {
                // Object based control.
                return false;
            }
            if (ConfigurationController.this.forced.contains(predicate)) {
                // Was forced by some previous configuration.
                return false;
            }
            final String control = options.get(predicate);
            if (control == null) {
                return true;
            }
            switch (control) {
                case "http://plugins.linkedpipes.com/resource/configuration/Inherit":
                case "http://plugins.linkedpipes.com/resource/configuration/Forced":
                    return false;
                case "http://plugins.linkedpipes.com/resource/configuration/InheritAndForce":
                    // Do not read - ie. preserve from parent and
                    // save as forced.
                    ConfigurationController.this.forced.add(predicate);
                    return false;
                case "http://plugins.linkedpipes.com/resource/configuration/Force":
                    // Load and save as forced.
                    ConfigurationController.this.forced.add(predicate);
                    return true;
                default:
                    // Load by default.
                    return true;
            }
        }
    }

    /**
     * Object with pipeline and configurations definition.
     */
    private final SparqlSelect definition;

    /**
     * Store list of forced properties.
     */
    private final Set<String> forced = new HashSet<>();

    private boolean loadingRuntime = false;

    private boolean objectInherit = false;

    private boolean objectForced = false;

    /**
     * If set to true, next call of create will set objectForced to true.
     *
     * The force predicate works on next, level so this is a way how
     * to propagate this.
     */
    private boolean objectForce = false;

    public ConfigurationController(SparqlSelect definition) {
        this.definition = definition;
    }

    @Override
    public RdfReader.MergeOptions create(String resourceIri, String graph)
            throws RdfException {
        // Check object type force.
        if (objectForce) {
            objectForced = true;
        }
        if (objectForced) {
            return new MergerPolicy(Collections.EMPTY_MAP);
        }
        // Check for object type inheritance.
        for (Map<String, String> record
                : definition.executeSelect(
                getQueryObjectControl(resourceIri, graph))) {
            // There should be only one result.
            switch (record.get("controlValue")) {
                case "http://plugins.linkedpipes.com/resource/configuration/Inherit":
                    objectInherit = true;
                    break;
                case "http://plugins.linkedpipes.com/resource/configuration/Forced":
                    objectInherit = false;
                    break;
                case "http://plugins.linkedpipes.com/resource/configuration/InheritAndForce":
                    objectInherit = true;
                    objectForce = true;
                    break;
                case "http://plugins.linkedpipes.com/resource/configuration/Force":
                    objectInherit = false;
                    objectForce = true;
                    break;
            }
        }

        final Map<String, String> options = new HashMap<>();
        // In case of duplicity we just end up with one of the values,
        // that should really not be an issue. As all the configuration
        // description classes should be the same, only with different
        // resource IRIs.
        for (Map<String, String> record
                : definition.executeSelect(
                getQueryPropertyControl(resourceIri, graph))) {
            options.put(record.get("property"), record.get("controlValue"));
        }
        return new MergerPolicy(options);
    }

    /**
     * Must be called before runtime configuration is
     */
    public void loadingRuntime() {
        loadingRuntime = true;
    }

    /**
     * The output value is "controlValue".
     *
     * @param resourceIri
     * @param graph
     * @return
     */
    protected static String getQueryObjectControl(String resourceIri,
            String graph) {
        return "PREFIX config: <http://plugins.linkedpipes.com/ontology/configuration/>\n" +
                "\n" +
                "SELECT ?controlValue WHERE {\n" +
                " GRAPH <" + graph + "> {\n" +
                "  <" + resourceIri + "> a ?type ;\n" +
                "    ?control ?controlValue .\n" +
                " }\n" +
                "\n" +
                "GRAPH ?g {" +
                "  [] ?a <http://plugins.linkedpipes.com/ontology/ConfigurationDescription> ;\n" +
                "   config:type ?type ;\n" +
                "   config:control ?control .\n" +
                " }\n" +
                "}";
    }

    /**
     * The output values are "property" and "controlValue".
     *
     * @param resourceIri
     * @param graph
     * @return
     */
    protected static String getQueryPropertyControl(String resourceIri,
            String graph) {
        return "PREFIX config: <http://plugins.linkedpipes.com/ontology/configuration/>\n" +
                "\n" +
                "SELECT ?property ?controlValue WHERE {\n" +
                " GRAPH <" + graph + "> {\n" +
                "  <" + resourceIri + "> a ?type ;\n" +
                "    ?property [] ;\n" +
                "    ?control ?controlValue .\n" +
                " }\n" +
                "\n" +
                "GRAPH ?g {" +
                "  [] ?a <http://plugins.linkedpipes.com/ontology/ConfigurationDescription> ;\n" +
                "   config:type ?type ;\n" +
                "   config:member ?member .\n" +
                "\n" +
                "  ?member a <http://plugins.linkedpipes.com/ontology/configuration/ConfigurationMember> ;\n" +
                "    config:property ?property ;\n" +
                "    config:control ?control .\n" +
                " }\n" +
                "}";
    }

}
