package com.linkedpipes.plugin.transformer.modifydate;

import com.linkedpipes.etl.component.api.service.RdfToPojo;

/**
 * @author Škoda Petr
 */
@RdfToPojo.Type(uri = ModifyDateVocabulary.CONFIG_CLASS)
public class ModifyDateConfiguration {

    @RdfToPojo.Property(uri = ModifyDateVocabulary.HAS_INPUT)
    private String inputPredicate = "http://localhost/temp/ontology/date";

    @RdfToPojo.Property(uri = ModifyDateVocabulary.HAS_VALUE)
    private int modifyDay = 1;

    @RdfToPojo.Property(uri = ModifyDateVocabulary.HAS_OUTPUT)
    private String outputPredicate = "http://localhost/temp/ontology/date";

    public ModifyDateConfiguration() {
    }

    public String getInputPredicate() {
        return inputPredicate;
    }

    public void setInputPredicate(String inputPredicate) {
        this.inputPredicate = inputPredicate;
    }

    public int getModifyDay() {
        return modifyDay;
    }

    public void setModifyDay(int modifyDay) {
        this.modifyDay = modifyDay;
    }

    public String getOutputPredicate() {
        return outputPredicate;
    }

    public void setOutputPredicate(String outputPredicate) {
        this.outputPredicate = outputPredicate;
    }
}
