package com.linkedpipes.etl.rdf.utils.pojo;

public interface DescriptorFactory {

    Descriptor create(Class<?> type) throws LoaderException;

}
