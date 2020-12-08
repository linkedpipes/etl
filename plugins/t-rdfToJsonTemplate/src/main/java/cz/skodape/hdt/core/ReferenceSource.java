package cz.skodape.hdt.core;

/**
 * We use custom interface to allow for exceptions and more importantly to
 * allow to split the iterator into two while preserving the state.
 */
public interface ReferenceSource {

    ReferenceSource split() throws OperationFailed;

    /**
     * Next can be called only when processing of the last returned object
     * has been completely finished.
     * @return Null if there is no other object.
     */
    Reference next() throws OperationFailed;

}
