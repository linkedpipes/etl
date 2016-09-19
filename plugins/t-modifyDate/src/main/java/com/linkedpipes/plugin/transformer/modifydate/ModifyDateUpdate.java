package com.linkedpipes.plugin.transformer.modifydate;

import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.openrdf.model.IRI;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.repository.RepositoryResult;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Škoda Petr
 */
public final class ModifyDateUpdate implements Component.Sequential {

    private static final DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Component.InputPort(id = "InputRdf")
    public SingleGraphDataUnit inputRdf;

    @Component.OutputPort(id = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdf;

    @Component.Configuration
    public ModifyDateConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        final ValueFactory valueFactory = SimpleValueFactory.getInstance();
        final IRI xsdDate = valueFactory.createIRI(
                "http://www.w3.org/2001/XMLSchema#date");
        final IRI outputPredicate = valueFactory.createIRI(
                configuration.getOutputPredicate());
        //
        final List<Statement> result = new LinkedList<>();
        inputRdf.execute((connection) -> {
            RepositoryResult<Statement> statements =  connection.getStatements(
                    null,
                    valueFactory.createIRI(configuration.getInputPredicate()),
                    null, inputRdf.getGraph());
            //
            result.clear();
            while (statements.hasNext()) {
                final Statement st = statements.next();
                final String date;
                try {
                    date =modifyDate(st.getObject().stringValue(),
                            configuration.getModifyDay());
                } catch (ParseException ex) {
                    throw exceptionFactory.failure("Invalid date: {} for {}:",
                            st.getObject().stringValue(),
                            st.getSubject().stringValue(),
                            ex);
                }
                //
                result.add(valueFactory.createStatement(st.getSubject(),
                        outputPredicate,
                        valueFactory.createLiteral(date, xsdDate),
                        outputRdf.getGraph()));
            }
        });
    }

    /**
     * Parse given date, modify it, format back to string and return.
     *
     * @param date
     * @param dateShift
     * @return
     * @throws ParseException
     */
    private static String modifyDate(String date, int dateShift)
            throws ParseException{
        final Calendar calendar = new GregorianCalendar();
        calendar.setTime(FORMAT.parse(date));
        calendar.add(Calendar.DATE, dateShift);
        return FORMAT.format(calendar.getTime());
    }

}
