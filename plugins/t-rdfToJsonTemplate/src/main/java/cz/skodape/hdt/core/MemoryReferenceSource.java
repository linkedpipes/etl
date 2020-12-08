package cz.skodape.hdt.core;

import java.util.Collections;
import java.util.List;

/**
 * Simple implementation of reference source build on a list.
 */
public class MemoryReferenceSource<T extends Reference>
        implements ReferenceSource {

    protected int index = 0;

    protected final List<T> references;

    public MemoryReferenceSource(T reference) {
        this.references = Collections.singletonList(reference);
    }

    public MemoryReferenceSource(List<T> references) {
        this.references = references;
    }

    public MemoryReferenceSource(MemoryReferenceSource<T> other) {
        this.index = other.index;
        this.references = other.references;
    }

    @Override
    public ReferenceSource split() {
        return new MemoryReferenceSource<>(this);
    }

    @Override
    public Reference next() {
        if (this.index >= this.references.size()) {
            return null;
        }
        return this.references.get(this.index++);
    }

}

