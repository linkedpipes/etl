package com.linkedpipes.plugin.loader.wikibase.model;

import java.math.BigDecimal;

public class QuantityValue implements WikibaseValue {

    public BigDecimal amount;

    public BigDecimal lowerBound;

    public String unit;

    public BigDecimal upperBound;

}
