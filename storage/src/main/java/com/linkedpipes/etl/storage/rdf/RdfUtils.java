package com.linkedpipes.etl.storage.rdf;

import com.linkedpipes.etl.storage.StorageException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contains utilities for RDF IO operations.
 */
public final class RdfUtils {

    public static class RdfException extends StorageException {

        RdfException(String message, Object... args) {
            super(message, args);
        }

        public RdfException(Throwable cause) {
            super(cause);
        }
    }

    private RdfUtils() {

    }

    /**
     * Return Null if there is no resource of given type.
     */
    public static Resource find(
            Collection<Statement> statements, IRI type) {
        for (Statement statement : statements) {
            if (RDF.TYPE.equals(statement.getPredicate())) {
                if (type.equals(statement.getObject())) {
                    return statement.getSubject();
                }
            }
        }
        return null;
    }

    public static Collection<Statement> forceContext(
            Collection<Statement> statements, String context) {
        ValueFactory vf = SimpleValueFactory.getInstance();
        return forceContext(statements, vf.createIRI(context));
    }

    /**
     * Return a importJarComponent of given statements with enforced context.
     */
    public static Collection<Statement> forceContext(
            Collection<Statement> statements, Resource context) {
        ValueFactory vf = SimpleValueFactory.getInstance();
        Collection<Statement> result = new ArrayList<>(statements.size());
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

    /**
     * Select resources of given types, ignore graphs.
     */
    private static Collection<Resource> selectTyped(
            Collection<Statement> statements) {
        HashSet<Resource> result = new HashSet<>();
        for (Statement statement : statements) {
            if (statement.getPredicate().equals(RDF.TYPE)) {
                result.add(statement.getSubject());
            }
        }
        return result;
    }

    //
    // IO operation.
    //

    /**
     * Return RDF format required by the client to use in response.
     */
    public static RDFFormat getFormat(
            HttpServletRequest request, RDFFormat defaultValue) {
        // TODO There can ba multiple Accept values
        return Rio.getParserFormatForMIMEType(request.getHeader("Accept"))
                .orElse(defaultValue);
    }

    /**
     * Return RDF type for given MimeType. The JSON MimeType is considered
     * to represent the JSONLD to support backward compatibility, with
     * other LinkedPipes versions.
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
                    String originalName = file.getOriginalFilename();
                    if (originalName == null) {
                        rdfFormat = RdfUtils.getFormat(
                                new File(file.getName()));
                    } else {
                        rdfFormat = RdfUtils.getFormat(
                                new File(originalName));
                    }
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
     */
    public static Collection<Statement> read(File file) throws RdfException {
        return read(file, getFormat(file));
    }

    /**
     * Read and from RDF from {@link File}.
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
     */
    public static Collection<Statement> read(
            InputStream inputStream, RDFFormat format) throws RdfException {
        List<Statement> statements = new ArrayList<>();
        try {
            RDFParser reader = Rio.createParser(format,
                    SimpleValueFactory.getInstance());
            StatementCollector collector
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

    public static void atomicWrite(
            File file, RDFFormat format, Collection<Statement> statements)
            throws RdfException {
        File swap = new File(file + ".swp");
        try {
            write(new FileOutputStream(swap), format, statements);
            Files.move(swap.toPath(), file.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new RdfException(ex);
        }
    }

    /**
     * Write given RDF statements ro file.
     */
    public static void write(
            File file, RDFFormat format, Collection<Statement> statements)
            throws RdfException {
        try {
            write(new FileOutputStream(file), format, statements);
        } catch (IOException ex) {
            throw new RdfException(ex);
        }
    }

    /**
     * Write given RDF statements to given stream.
     */
    public static void write(
            OutputStream outputStream, RDFFormat format,
            Collection<Statement> statements) throws RdfException {
        try {
            RDFWriter writer = Rio.createWriter(format, outputStream);
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
     * TODO Move to package with servlets.
     */
    public static void write(
            HttpServletRequest request,
            HttpServletResponse response, Collection<Statement> data)
            throws StorageException {
        RDFFormat format =
                RdfUtils.getFormat(request, RDFFormat.TRIG);
        response.setHeader("content-type", format.getDefaultMIMEType());
        try (OutputStream stream = response.getOutputStream()) {
            RdfUtils.write(stream, format, data);
        } catch (IOException ex) {
            throw new StorageException(ex);
        }
    }

    public static List<Statement> updateToIriAndGraph(
            Collection<Statement> statements, IRI iri) {
        Map<Value, Value> mapping = createResourceMapping(
                iri.stringValue(), statements);
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        return statements.stream().map(s -> valueFactory.createStatement(
                (Resource) mapping.get(s.getSubject()),
                s.getPredicate(),
                mapping.getOrDefault(s.getObject(), s.getObject()),
                iri
        )).collect(Collectors.toList());
    }

    private static Map<Value, Value> createResourceMapping(
            String baseIri, Collection<Statement> statements) {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        Map<Value, Value> mapping = new HashMap<>();
        for (Statement statement : statements) {
            if (mapping.containsKey(statement.getSubject())) {
                continue;
            }
            String iriAsStr = baseIri + "/" + mapping.size();
            mapping.put(
                    statement.getSubject(), valueFactory.createIRI(iriAsStr));
        }
        return mapping;
    }

}
