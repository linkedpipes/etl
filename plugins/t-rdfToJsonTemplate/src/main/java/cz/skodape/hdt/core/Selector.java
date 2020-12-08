package cz.skodape.hdt.core;

/**
 * Holds transformation unit functionality.
 */
public interface Selector extends ReferenceSource {

    /**
     * Set source, must be called before any of the iterator methods.
     */
    void initialize(SelectorContext context, ReferenceSource input)
        throws OperationFailed;

}
