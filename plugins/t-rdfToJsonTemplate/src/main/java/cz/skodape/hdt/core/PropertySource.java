package cz.skodape.hdt.core;

public interface PropertySource {

    /**
     * Load any necessary resources here.
     */
    void open() throws OperationFailed;

    /**
     * Called before object description, the source should close all resources
     * and free used memory.
     */
    void close();

    /**
     * Return source of root references. This method can be called multiple
     * times.
     */
    ReferenceSource roots() throws OperationFailed;

    /**
     * Allow to iterate over source values, if the given reference is not as
     * {@link ArrayReference} then the values are wrapped with an array first.
     */
    ReferenceSource source(Reference reference) throws OperationFailed;

    /**
     * Return values for given object and property.
     */
    ArrayReference property(ObjectReference reference, String property)
            throws OperationFailed;

    /**
     * Return array of all references that have given property with
     * given reference as a value.
     */
    ArrayReference reverseProperty(Reference reference, String property)
            throws OperationFailed;

}
