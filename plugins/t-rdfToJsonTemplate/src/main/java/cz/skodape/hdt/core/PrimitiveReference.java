package cz.skodape.hdt.core;

/**
 * Reference to a primitive value. Unlike other this reference must provide
 * access to it's value. As a reason we do not need to remember a source
 * that was used to create this reference.
 */
public interface PrimitiveReference extends Reference {

    String getValue() throws OperationFailed;

}
