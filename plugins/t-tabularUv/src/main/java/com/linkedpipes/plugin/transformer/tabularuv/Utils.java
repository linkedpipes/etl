package com.linkedpipes.plugin.transformer.tabularuv;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Utils {

    private Utils() {

    }

    public static String convertStringToIRIPart(String part) {
        try {
            return URLEncoder.encode(part, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("Unsupported encoding", ex);
        }
    }

}
