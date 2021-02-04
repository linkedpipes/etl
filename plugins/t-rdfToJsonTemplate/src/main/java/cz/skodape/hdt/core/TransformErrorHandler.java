package cz.skodape.hdt.core;

public class TransformErrorHandler {

    public void onInvalidRoot() throws OperationFailed {
        throw new OperationFailed("Unsupported root definition.");
    }

    public void onUnknownTransformation() throws OperationFailed {
        throw new OperationFailed("Unknown transformation definition.");
    }

    public void onReferenceAsPrimitive() throws OperationFailed {
        throw new OperationFailed("Reference must be PrimitiveReference.");
    }

    public void onMultiplePrimitiveValues(
            Reference head, Reference next, ReferenceSource source)
            throws OperationFailed {
        String message = multiplePrimitiveValuesMessage(head, next, source);
        throw new OperationFailed(
                "Multiple values detected for primitive: {}", message);
    }

    protected String multiplePrimitiveValuesMessage(
            Reference head, Reference next, ReferenceSource source
    ) throws OperationFailed {
        StringBuilder content = new StringBuilder();
        content.append("\n  ");
        content.append(head.asDebugString());
        content.append("\n  ");
        content.append(next.asDebugString());
        Reference rest;
        while ((rest = source.next()) != null) {
            content.append("\n  ");
            content.append(rest.asDebugString());
        }
        return content.toString();
    }

}
