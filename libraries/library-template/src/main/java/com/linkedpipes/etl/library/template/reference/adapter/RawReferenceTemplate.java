package com.linkedpipes.etl.library.template.reference.adapter;

import com.linkedpipes.etl.library.rdf.Statements;
import com.linkedpipes.etl.library.template.reference.model.ReferenceTemplate;
import org.eclipse.rdf4j.model.Resource;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent version less reference template.
 */
public class RawReferenceTemplate{

    /**
     * Resource.
     */
    public Resource resource;

    /**
     * Reference version.
     */
    public int version = 0;

    /**
     * Parent template.
     */
    public Resource template;

    /**
     * IRI of the top template; i.e. the plugin template.
     */
    public Resource plugin;

    /**
     * User given label.
     */
    public String label;

    /**
     * User given description.
     */
    public String description;

    /**
     * User given note.
     */
    public String note;

    /**
     * User given color.
     */
    public String color;

    /**
     * List of tags assigned to given template.
     */
    public List<String> tags = new ArrayList<>();

    /**
     * When imported the original IRI is saved here; this allows us to
     * track a single template among instances.
     */
    public Resource knownAs;

    /**
     * Configuration in form of RDF as there is no fixed structure.
     * Stored without graph.
     */
    public Statements configuration;
    
    /**
     * Graph the configuration is stored in.
     */
    public Resource configurationGraph;

    public ReferenceTemplate toReferenceTemplate() {
        return new ReferenceTemplate(
                resource, version,
                template, plugin,
                label, description, note, color, tags,
                knownAs, configuration, configurationGraph);
    }

}
