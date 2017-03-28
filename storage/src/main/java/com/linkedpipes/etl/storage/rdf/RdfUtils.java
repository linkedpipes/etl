package com.linkedpipes.etl.storage.rdf;

import com.linkedpipes.etl.storage.BaseException;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * Contains utilities for RDF IO operations.
 */
public final class RdfUtils {

    public static class RdfException extends BaseException {

        RdfException(String message, Object... args) {
            super(message, args);
        }

        public RdfException(Throwable cause) {
            super(cause);
        }
    }

    private RdfUtils() {

    }

    //
    //
    //

    /**
     * @param statements
     * @param type
     * @return Null if there is no resource of given type.
     */
    public static Resource find(Collection<Statement> statements,
            IRI type) {
        for (Statement statement : statements) {
            if (RDF.TYPE.equals(statement.getPredicate())) {
                if (type.equals(statement.getObject())) {
                    return statement.getSubject();
                }
            }
        }
        return null;
    }

    /**
     * Return a copy of given statements with enforced context.
     *
     * @param statements
     * @param context
     * @return
     */
    public static Collection<Statement> forceContext(
            Collection<Statement> statements, Resource context) {
        final ValueFactory vf = SimpleValueFactory.getInstance();
        final Collection<Statement> result = new ArrayList<>(statements.size());
        for (Statement statement : statements) {
            result.add(vf.createStatement(
                    statement.getSubject(),
                    statement.getPredicate(),
                    statement.getObject(),
                    context
            ));
        }
        return result;
    }

    //
    // Manipulation with the RDF statements collections.
    //

    /**
     * Ignore graphs.
     *
     * @param statements
     * @return List of resources with type.
     */
    private static Collection<Resource> selectTyped(
            Collection<Statement> statements) {
        final HashSet<Resource> result = new HashSet<>();
        for (Statement statement : statements) {
            if (statement.getPredicate().equals(RDF.TYPE)) {
                result.add(statement.getSubject());
            }
        }
        return result;
    }

    /**
     * Change names of all typed resource so the use given prefix.
     *
     * @param statements
     * @param baseIri Must not end with '/'.
     * @param context Context used for new statements.
     * @return
     */
    public static Collection<Statement> renameResources(
            Collection<Statement> statements, String baseIri,
            Resource context) {
        if (statements == null || statements.isEmpty()) {
            // There is nothing to update.
            return Collections.EMPTY_LIST;
        }
        final ValueFactory vf = SimpleValueFactory.getInstance();
        // Create mapping of resources.
        final Map<Resource, Resource> replaceMap = new HashMap<>();
        for (Resource resource : selectTyped(statements)) {
            replaceMap.put(resource,
                    vf.createIRI(baseIri + "/" + replaceMap.size()));
        }
        // Replace.
        final Collection<Statement> result = new ArrayList<>(statements.size());
        for (Statement statement : statements) {
            // Check for change in resource and object.
            final Resource resource = replaceMap.getOrDefault(
                    statement.getSubject(), statement.getSubject());
            final Value object;
            if (statement.getObject() instanceof Resource) {
                object = replaceMap.getOrDefault(statement.getObject(),
                        (Resource) statement.getObject());
            } else {
                object = statement.getObject();
            }
            result.add(vf.createStatement(
                    resource, statement.getPredicate(),
                    object, context));
        }
        return result;
    }

    //
    // IO operation.
    //

    /**
     * Return RDF format required by the client to use in response.
     *
     * @param request
     * @param defaultValue
     * @return
     */
    public static RDFFormat getFormat(HttpServletRequest request,
            RDFFormat defaultValue) {
        // TODO There can ba multiple Accept values
        return Rio.getParserFormatForMIMEType(request.getHeader("Accept"))
                .orElse(defaultValue);
    }

    /**
     * Return RDF type for given MimeType. The JSON MimeType is considered
     * to represent the JSONLD to support backward compatibility, with
     * other LinkedPipes versions.
     *
     * @param mimeType
     * @return
     */
    public static RDFFormat getFormat(String mimeType)
            throws RdfException {
        // BACKWARD COMPATIBILITY
        if (mimeType == null || mimeType.equals("application/json")) {
            return RDFFormat.JSONLD;
        }
        //
        return Rio.getParserFormatForMIMEType(mimeType).orElseThrow(
                () -> new RdfException("Invalid RDF type for MIME: {}",
                        mimeType));
    }

    /**
     * Return type of RDF format for given type.
     *
     * @param file
     * @return
     */
    public static RDFFormat getFormat(File file) throws RdfException {
        // BACKWARD COMPATIBILITY
        if (file.getName().toLowerCase().endsWith(".json")) {
            return RDFFormat.JSONLD;
        }
        //
        return Rio.getParserFormatForFileName(file.getName()).orElseThrow(
                () -> new RdfException("Invalid RDF type for file: {}",
                        file.getName()));
    }

    /**
     * Read and return RDF from given {@link MultipartFile}.
     *
     * @param file
     * @return
     */
    public static Collection<Statement> read(MultipartFile file)
            throws RdfException {
        if (file == null) {
            return Collections.EMPTY_LIST;
        } else {
            try {
                RDFFormat rdfFormat;
                try {
                    rdfFormat = RdfUtils.getFormat(file.getContentType());
                } catch (Exception ex) {
                    // Use the file name.
                    rdfFormat = RdfUtils.getFormat(
                            new File(file.getOriginalFilename()));
                }
                return RdfUtils.read(file.getInputStream(), rdfFormat);
            } catch (IOException ex) {
                throw new RdfException(ex);
            }
        }
    }

    /**
     * Read and return RDF from given {@link File}. The format is determined
     * from file extension.
     *
     * @param file
     * @return
     */
    public static Collection<Statement> read(File file) throws RdfException {
        return read(file, getFormat(file));
    }

    /**
     * Read and from RDF from {@link File}.
     *
     * @param file
     * @param format
     * @return
     */
    public static Collection<Statement> read(File file, RDFFormat format)
            throws RdfException {
        try (InputStream stream = new FileInputStream(file)) {
            return read(stream, format);
        } catch (IOException ex) {
            throw new RdfException(ex);
        }
    }

    /**
     * Read RDF from stream.
     *
     * @param inputStream Stream is closed after usage.
     * @param format
     * @return
     */
    public static Collection<Statement> read(InputStream inputStream,
            RDFFormat format) throws RdfException {
        final List<Statement> statements = new ArrayList<>(32);
        try {
            final RDFParser reader = Rio.createParser(format,
                    SimpleValueFactory.getInstance());
            final StatementCollector collector
                    = new StatementCollector(statements);
            reader.setRDFHandler(collector);
            reader.parse(inputStream, "http://localhost/base");
        } catch (IOException ex) {
            throw new RdfException(ex);
        } finally {
            try {
                inputStream.close();
            } catch (IOException ex) {
                throw new RdfException("Can't close stream.", ex);
            }
        }
        return statements;
    }

    /**
     * Write given RDF statements ro file.
     *
     * @param file
     * @param format
     * @param statements
     */
    public static void write(File file, RDFFormat format,
            Collection<Statement> statements) throws RdfException {
        try {
            write(new FileOutputStream(file), format, statements);
        } catch (IOException ex) {
            throw new RdfException(ex);
        }
    }

    /**
     * Write given RDF statements to given stream.
     *
     * @param outputStream Stream is closed after usage.
     * @param format
     * @param statements
     */
    public static void write(OutputStream outputStream, RDFFormat format,
            Collection<Statement> statements) throws RdfException {
        try {
            final RDFWriter writer = Rio.createWriter(format, outputStream);
            writer.startRDF();
            for (Statement s : statements) {
                writer.handleStatement(s);
            }
            writer.endRDF();
        } finally {
            try {
                outputStream.close();
            } catch (IOException ex) {
                throw new RdfException("Can't close stream.", ex);
            }
        }
    }

    /**
     * TODO Move to package with servlets
     *
     * @param request
     * @param response
     * @param data
     */
    public static void write(HttpServletRequest request,
            HttpServletResponse response, Collection<Statement> data)
            throws BaseException {
        final RDFFormat format =
                RdfUtils.getFormat(request, RDFFormat.TRIG);
        response.setHeader("content-type", format.getDefaultMIMEType());
        try (OutputStream stream = response.getOutputStream()) {
            RdfUtils.write(stream, format, data);
        } catch (IOException ex) {
            throw new BaseException(ex);
        }
    }

}
