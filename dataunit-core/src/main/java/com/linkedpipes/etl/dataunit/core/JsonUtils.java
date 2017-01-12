package com.linkedpipes.etl.dataunit.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.linkedpipes.etl.executor.api.v1.LpException;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class JsonUtils {

    private JsonUtils() {
    }

    public static void save(File file, Collection<String> collection)
            throws LpException {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(file, collection);
        } catch (IOException ex) {
            throw new LpException("Can't save directory list.", ex);
        }
    }

    public static <T> Collection<T> loadCollection(File file, Class<T> type)
            throws LpException {
        final ObjectMapper mapper = new ObjectMapper();
        final TypeFactory typeFactory = mapper.getTypeFactory();
        try {
            return mapper.readValue(file,
                    typeFactory.constructCollectionType(List.class, type));
        } catch (IOException ex) {
            throw new LpException("Can't load directory list.", ex);
        }
    }

}
