package com.linkedpipes.commons.entities.executor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.linkedpipes.commons.entities.rest.ListMetadata;
import com.linkedpipes.commons.entities.rest.ListResponse;
import com.linkedpipes.commons.entities.rest.RestException;
import java.util.LinkedList;

/**
 *
 * @author Škoda Petr
 */
public class MessageSelectList extends ListResponse<Map<String, Object>> {

    public MessageSelectList() {
    }

    public MessageSelectList(RestException exception) {
        super(exception);
    }

    public MessageSelectList(ListMetadata metadata, List<Map<String, Object>> data) {
        super(metadata, data);
    }

    public static Map<String, Object> create(String subject) {
        final Map<String, Object> message = new HashMap<>();
        message.put("@id", subject);
        return message;
    }

    public static void addProperty(Map<String, Object> message, String predicate, String value) {
        if (predicate.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
            predicate = "@type";
        }
        Object currentObject = message.get(predicate);
        if (currentObject == null) {
            message.put(predicate, value);
        } else {
            if (currentObject instanceof List) {
                List<Object> list = (List)currentObject;
                list.add(value);
            } else {
                final List<Object> newObject = new LinkedList<>();
                newObject.add(currentObject);
                newObject.add(value);
                message.put(predicate, newObject);
            }
        }
    }

}
