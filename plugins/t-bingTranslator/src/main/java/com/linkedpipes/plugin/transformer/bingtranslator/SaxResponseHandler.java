package com.linkedpipes.plugin.transformer.bingtranslator;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Collect values in the TranslatedText element.
 */
public class SaxResponseHandler extends DefaultHandler {

    /**
     * Name of last open element. Or empty if the last element was closed.
     */
    private String elementName;

    private List<String> values = new ArrayList<>(1000);

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) {
        elementName = qName;
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        elementName = "";
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        if ("TranslatedText".equals(elementName)) {
            values.add(new String(ch, start, length));
        }
    }

    public List<String> getValues() {
        return values;
    }
}
