package com.linkedpipes.etl.library.template.plugin.model;

import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.template.configuration.model.ConfigurationDescription;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.List;
import java.util.Map;

/**
 * Full definition of a plugin template data structure.
 */
public record PluginTemplate(
        /*
         * Plugin identification.
         */
        IRI resource,
        /*
         * Plugin label.
         */
        String label,
        /*
         * Plugin default color.
         */
        String color,
        /*
         * Plugin type.
         */
        PluginType type,
        /*
         * True if plugin support configuration inheritance.
         */
        boolean supportControl,
        /*
         * List of plugin tags.
         */
        List<String> tags,
        /*
         * Link to a documentation.
         */
        String documentation,
        /*
         * The map is for a dialog in given directory, where the directory
         * dictates the name. For each dialog it contains map or relative
         * final name to full name in the plugin.
         */
        Map<String, Map<String, String>> dialogs,
        /*
         * List of associated ports.
         */
        List<Port> ports,
        /*
         * Configuration statements without graph as the graph is pre-determined
         * for a plugin template.
         */
        Statements configuration,
        /*
         * Graph where the configuration is stored.
         */
        IRI configurationGraph,
        /*
         * Configuration description, each plugin have only one as otherwise
         * it would not be clear where we should start merging resources.
         */
        ConfigurationDescription configurationDescription,
        /*
         * Graph where the configuration description is stored.
         */
        IRI configurationDescriptionGraph
) {

    public record Port(
            /*
             * Name of binding.
             */
            String binding,
            /*
             * Component label.
             */
            String label,
            /*
             * Types.
             */
            List<String> types
    ) {

    }

    public static IRI defaultConfigurationGraph(IRI resource) {
        return SimpleValueFactory.getInstance().createIRI(
                resource.stringValue() + "/configuration");
    }

    public static IRI defaultConfigurationDescriptionGraph(IRI resource) {
        return SimpleValueFactory.getInstance().createIRI(
                resource.stringValue() + "/configuration-description");
    }

}
