package com.linkedpipes.plugin.transformer.xslt;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public class UUIDGenerator extends ExtensionFunctionDefinition {

    protected static UUIDGenerator singletonInstance = new UUIDGenerator();

    public static UUIDGenerator getInstance() {
        return singletonInstance;
    }

    private UUIDGenerator() {
    }

    @Override
    public StructuredQName getFunctionQName() {
        return new StructuredQName("uuid", "uuid-functions", "randomUUID");
    }

    @Override
    public SequenceType[] getArgumentTypes() {
        return new SequenceType[0];
    }

    @Override
    public SequenceType getResultType(SequenceType[] sequenceTypes) {
        return SequenceType.SINGLE_STRING;
    }

    @Override
    public ExtensionFunctionCall makeCallExpression() {
        return new ExtensionFunctionCall() {

            @Override
            public Sequence call(XPathContext context, Sequence[] arguments)
                    throws XPathException {
                return new StringValue(java.util.UUID.randomUUID().toString());
            }

        };
    }

}
