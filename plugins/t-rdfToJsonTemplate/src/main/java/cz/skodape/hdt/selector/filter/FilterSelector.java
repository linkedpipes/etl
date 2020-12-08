package cz.skodape.hdt.selector.filter;

import cz.skodape.hdt.core.OperationFailed;
import cz.skodape.hdt.core.PrimitiveReference;
import cz.skodape.hdt.core.PropertySource;
import cz.skodape.hdt.core.Reference;
import cz.skodape.hdt.core.ReferenceSource;
import cz.skodape.hdt.core.Selector;
import cz.skodape.hdt.core.SelectorContext;

public class FilterSelector implements Selector {

    private final FilterSelectorConfiguration configuration;

    private ReferenceSource input;

    private PropertySource propertySource;

    private SelectorContext context = null;

    public FilterSelector(FilterSelectorConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void initialize(SelectorContext context, ReferenceSource input) {
        this.input = input;
        this.propertySource = context.defaultSource;
        this.context = context;
    }

    @Override
    public ReferenceSource split() throws OperationFailed {
        FilterSelector result = new FilterSelector(configuration);
        result.input = input.split();
        result.propertySource = propertySource;
        result.context = context;
        return result;
    }

    @Override
    public Reference next() throws OperationFailed {
        Reference result;
        while ((result = input.next()) != null) {
            if (evaluate(result)) {
                return result;
            }
        }
        return null;
    }


    private boolean evaluate(Reference reference) throws OperationFailed {
        Selector selector = configuration.path.createSelector();
        selector.initialize(context, propertySource.source(reference));
        switch (configuration.condition) {
            case Contain:
                return evaluateContain(selector);
            case Equal:
                return evaluateEqual(selector);
            default:
                throw new OperationFailed(
                        "Unknown condition type: "
                                + configuration.condition);
        }
    }

    private boolean evaluateContain(ReferenceSource source)
            throws OperationFailed {
        Reference next;
        while ((next = source.next()) != null) {
            if (!next.isPrimitiveReference()) {
                continue;
            }
            PrimitiveReference primitiveReference =
                    (PrimitiveReference) next;
            String value = primitiveReference.getValue();
            if (configuration.value.equals(value)) {
                return true;
            }
        }
        return false;
    }

    private boolean evaluateEqual(ReferenceSource source)
            throws OperationFailed {
        Reference first = source.next();
        if (first == null) {
            return false;
        }
        if (!first.isPrimitiveReference()) {
            return false;
        }
        PrimitiveReference primitiveReference = (PrimitiveReference) first;
        String value = primitiveReference.getValue();
        if (!configuration.value.equals(value)) {
            return false;
        }
        return source.next() == null;
    }

}
