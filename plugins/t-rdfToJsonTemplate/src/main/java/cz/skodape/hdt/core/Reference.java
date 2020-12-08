package cz.skodape.hdt.core;

/**
 * Base reference instance.
 */
public interface Reference {

    String asDebugString();

    boolean isObjectReference();

    boolean isArrayReference();

    boolean isPrimitiveReference();

}
