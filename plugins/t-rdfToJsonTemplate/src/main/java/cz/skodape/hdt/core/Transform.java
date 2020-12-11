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
import java.util.Stack;

public class Transform {

    private enum StepType {
        Object,
        Array,
        Primitive,
        Reference
    }

    private static class Step {

        public final Reference reference;

        public final BaseTransformation definition;

        public final StepType type;

        public Step(Reference reference) {
            this.reference = reference;
            this.definition = null;
            this.type = StepType.Reference;
        }

        public Step(ObjectTransformation definition) {
            this.reference = null;
            this.definition = definition;
            this.type = StepType.Object;
        }

        public Step(ArrayTransformation definition) {
            this.reference = null;
            this.definition = definition;
            this.type = StepType.Array;
        }

        public Step(PrimitiveTransformation definition) {
            this.reference = null;
            this.definition = definition;
            this.type = StepType.Primitive;
        }

    }

    private static final Logger LOG = LoggerFactory.getLogger(Transform.class);

    protected final TransformationFile definition;

    protected final SelectorContext context;

    protected final Output output;

    /**
     * Store path to currently node the is currently being processed.
     */
    protected final Stack<Step> path = new Stack<>();

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
            ArrayTransformation arrayDefinition =
                    (ArrayTransformation) definition;
            path.add(new Step(arrayDefinition));
            transformArray(arrayDefinition, source);
            path.pop();
        } else if (definition instanceof ObjectTransformation) {
            ObjectTransformation objectDefinition =
                    (ObjectTransformation) definition;
            path.add(new Step(objectDefinition));
            transformObject(objectDefinition, source);
            path.pop();
        } else if (definition instanceof PrimitiveTransformation) {
            PrimitiveTransformation primitiveDefinition =
                    (PrimitiveTransformation) definition;
            path.add(new Step(primitiveDefinition));
            transformPrimitive(primitiveDefinition, source);
            path.pop();
        } else {
            throw createError("Unknown transformation definition.");
        }
    }

    protected OperationFailed createError(String messages, Object... args) {
        return new OperationFailed(messages, args);
    }

    protected void transformArray(
            ArrayTransformation definition, ReferenceSource source)
            throws OperationFailed, IOException {
        ReferenceSource filteredSource = applySelectors(definition, source);
        output.openNextArray();
        Reference next;
        while ((next = filteredSource.next()) != null) {
            path.push(new Step(next));
            for (BaseTransformation itemDefinition : definition.items) {
                transform(itemDefinition, asSource(next));
            }
            path.pop();
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
            throw createError("Reference must be PrimitiveReference.");
        }
        String result = ((PrimitiveReference) reference).getValue();
        // Check there is no next value.
        Reference next = filteredSource.next();
        if (next != null) {
            onMultiplePrimitiveValues(reference, next, filteredSource);
        }
        return result;
    }

    protected void onMultiplePrimitiveValues(
            Reference head, Reference next, ReferenceSource source)
            throws OperationFailed {
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
        throw createError(
                "Multiple values detected for primitive: {}", content);
    }

    protected void closeSources() {
        LOG.info("Closing sources ...");
        for (PropertySource source : context.sources.values()) {
            source.close();
        }
        LOG.info("Closing sources ... done");
    }

}
