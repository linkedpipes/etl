package cz.skodape.hdt.selector.path;

import cz.skodape.hdt.core.ArrayReference;
import cz.skodape.hdt.core.ObjectReference;
import cz.skodape.hdt.core.OperationFailed;
import cz.skodape.hdt.core.PropertySource;
import cz.skodape.hdt.core.Reference;
import cz.skodape.hdt.core.ReferenceSource;

class PathStepSelector implements ReferenceSource {

    private final PropertySource propertySource;

    private final PathSelectorConfiguration.Path step;

    private final ReferenceSource inputSource;

    private ReferenceSource nextSource = null;

    public PathStepSelector(
            PropertySource propertySource,
            PathSelectorConfiguration.Path step,
            ReferenceSource inputSource) {
        this.propertySource = propertySource;
        this.step = step;
        this.inputSource = inputSource;
    }

    @Override
    public ReferenceSource split() throws OperationFailed {
        PathStepSelector result = new PathStepSelector(
                propertySource, step, inputSource.split());
        if (nextSource != null) {
            result.nextSource = nextSource.split();
        }
        return result;
    }

    @Override
    public Reference next() throws OperationFailed {
        if (nextSource == null) {
            return prepareNextSource();
        }
        Reference next = nextSource.next();
        if (next == null) {
            return prepareNextSource();
        }
        return next;
    }

    private Reference prepareNextSource() throws OperationFailed {
        Reference next;
        while ((next = inputSource.next()) != null) {
            if (!next.isObjectReference()) {
                continue;
            }
            ObjectReference objectReference = (ObjectReference) next;
            ArrayReference arrayReference;
            if (step.reverse) {
                arrayReference = propertySource.reverseProperty(
                        objectReference, step.predicate);
            } else {
                arrayReference = propertySource.property(
                        objectReference, step.predicate);
            }
            nextSource = propertySource.source(arrayReference);
            Reference result = nextSource.next();
            if (result == null) {
                continue;
            }
            return result;
        }
        return null;
    }

}
