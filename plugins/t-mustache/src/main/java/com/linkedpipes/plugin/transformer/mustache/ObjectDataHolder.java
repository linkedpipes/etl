package com.linkedpipes.plugin.transformer.mustache;

class ObjectDataHolder {

    /**
     * If true object is used to generate output.
     */
    boolean output;

    /**
     * Used for object ordering.
     */
    Integer order = null;

    /**
     * If {@link #output} is true then specify name of the output file.
     * If null some name is generated and used.
     */
    String fileName;

    /**
     * If {@link #output} is true then contains data.
     */
    Object data;

}
