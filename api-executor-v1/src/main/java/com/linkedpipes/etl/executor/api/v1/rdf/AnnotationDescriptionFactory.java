package com.linkedpipes.etl.executor.api.v1.rdf;

import com.linkedpipes.etl.rdf.utils.pojo.Descriptor;
import com.linkedpipes.etl.rdf.utils.pojo.DescriptorFactory;

class AnnotationDescriptionFactory implements DescriptorFactory {

    public AnnotationDescriptionFactory() {
    }

    @Override
    public Descriptor create(Class<?> type) {
        return new AnnotationDescriptor(type);
    }

}
