package cz.skodape.hdt.core;

import cz.skodape.hdt.model.ArrayTransformation;
import cz.skodape.hdt.model.BaseTransformation;
import cz.skodape.hdt.model.ObjectTransformation;
import cz.skodape.hdt.model.PrimitiveTransformation;
import cz.skodape.hdt.model.SelectorConfiguration;
import cz.skodape.hdt.model.TransformationFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Transform {

    private static final Logger LOG = LoggerFactory.getLogger(Transform.class);

    protected final TransformationFile definition;

    protected final SelectorContext context;

    protected final Output output;

    public Transform(
            TransformationFile definition, SelectorContext context,
            Output output) {
        this.definition = definition;
        this.context = context;
        this.output = output;
    }

    public void apply() throws OperationFailed, IOException {
        openSources();
        PropertySource rootSource = getRootSource();
        BaseTransformation rootTransformation = definition.transformation;
        transformRoot(rootTransformation, rootSource.roots());
        closeSources();
        output.onTransformationFinished();
    }

    protected void openSources() throws OperationFailed {
        for (var entry : context.sources.entrySet()) {
            LOG.info("Opening source: {}", entry.getKey());
            entry.getValue().open();
        }
        LOG.info("Opening sources ... done");
    }

    protected PropertySource getRootSource() {
        return context.sources.get(definition.rootSource);
    }

    /**
     * We can not use split on root source, as some sources may not support
     * it. As a result we have special limitations and functions to deal with
     * roots.
     */
    protected void transformRoot(
            BaseTransformation definition, ReferenceSource source)
            throws OperationFailed, IOException {
        ReferenceSource filteredSource = applySelectors(definition, source);
        if (definition instanceof ArrayTransformation) {
            transformRootArray(
                    (ArrayTransformation) definition, filteredSource);
        } else if (definition instanceof ObjectTransformation) {
            transformRootObject(
                    (ObjectTransformation) definition, filteredSource);
        } else {
            throw new OperationFailed("Unsupported root definition.");
        }
    }

    protected void transformRootArray(
            ArrayTransformation arrayDefinition, ReferenceSource source)
            throws OperationFailed, IOException {
        output.openNextArray();
        Reference next;
        while ((next = source.next()) != null) {
            for (BaseTransformation itemDefinition : arrayDefinition.items) {
                transform(itemDefinition, asSource(next));
            }
        }
        output.closeLastArray();
    }

    protected ReferenceSource asSource(Reference reference) {
        return new MemoryReferenceSource<>(reference);
    }

    /**
     * We can utilize only one Resource for object root. We allow the root
     * object to consists of more object.
     */
    protected void transformRootObject(
            ObjectTransformation objectDefinition, ReferenceSource source)
            throws OperationFailed, IOException {
        output.openNextObject();
        Reference next;
        while ((next = source.next()) != null) {
            for (var entry : objectDefinition.properties.entrySet()) {
                output.setNextKey(entry.getKey());
                transform(entry.getValue(), asSource(next));
            }
        }
        output.closeLastObject();
    }

    protected void transform(
            BaseTransformation definition, ReferenceSource source)
            throws OperationFailed, IOException {
        if (definition instanceof ArrayTransformation) {
            transformArray(
                    (ArrayTransformation) definition, source);
        } else if (definition instanceof ObjectTransformation) {
            transformObject(
                    (ObjectTransformation) definition, source);
        } else if (definition instanceof PrimitiveTransformation) {
            transformPrimitive(
                    (PrimitiveTransformation) definition, source);
        } else {
            throw new OperationFailed("Unknown transformation definition.");
        }
    }

    protected void transformArray(
            ArrayTransformation definition, ReferenceSource source)
            throws OperationFailed, IOException {
        ReferenceSource filteredSource = applySelectors(definition, source);
        output.openNextArray();
        Reference next;
        while ((next = filteredSource.next()) != null) {
            for (BaseTransformation itemDefinition : definition.items) {
                try {
                    transform(itemDefinition, asSource(next));
                } catch (OperationFailed ex) {
                    throw new OperationFailed(
                            "Error processing: {}", next.asDebugString(), ex);
                }
            }
        }
        output.closeLastArray();
    }

    private ReferenceSource applySelectors(
            BaseTransformation definition, ReferenceSource source)
            throws OperationFailed {
        ReferenceSource result = source;
        for (SelectorConfiguration configuration : definition.selectors) {
            Selector selector = configuration.createSelector();
            selector.initialize(context, result);
            result = selector;
        }
        return result;
    }

    protected void transformObject(
            ObjectTransformation definition, ReferenceSource source)
            throws OperationFailed, IOException {
        ReferenceSource filteredSource = applySelectors(definition, source);
        output.openNextObject();
        for (var entry : definition.properties.entrySet()) {
            output.setNextKey(entry.getKey());
            transform(entry.getValue(), filteredSource.split());
        }
        output.closeLastObject();
    }

    protected void transformPrimitive(
            PrimitiveTransformation definition, ReferenceSource source)
            throws OperationFailed, IOException {
        String value = getValueForPrimitive(definition, source);
        if (value == null) {
            return;
        }
        output.writeValue(definition.outputConfiguration, value);
    }

    protected String getValueForPrimitive(
            PrimitiveTransformation definition, ReferenceSource source)
            throws OperationFailed {
        if (definition.constantValue != null) {
            return definition.constantValue;
        }
        ReferenceSource filteredSource = applySelectors(definition, source);
        Reference reference = filteredSource.next();
        if (reference == null) {
            return definition.defaultValue;
        }
        if (!(reference instanceof PrimitiveReference)) {
            throw new OperationFailed("Reference must be PrimitiveReference.");
        }
        String result = ((PrimitiveReference) reference).getValue();
        // Check there is no next value.
        Reference next = filteredSource.next();
        if (next != null) {
            String content = collectToString(reference, next, filteredSource);
            throw new OperationFailed(
                    "Multiple values detected for primitive: " + content);
        }
        return result;
    }

    protected String collectToString(
            Reference head, Reference next, ReferenceSource source)
            throws OperationFailed {
        StringBuilder result = new StringBuilder();
        result.append("\n  ");
        result.append(head.asDebugString());
        result.append("\n  ");
        result.append(next.asDebugString());
        Reference rest;
        while ((rest = source.next()) != null) {
            result.append("\n  ");
            result.append(rest.asDebugString());
        }
        return result.toString();
    }

    protected void closeSources() {
        LOG.info("Closing sources ...");
        for (PropertySource source : context.sources.values()) {
            source.close();
        }
        LOG.info("Closing sources ... done");
    }

}
