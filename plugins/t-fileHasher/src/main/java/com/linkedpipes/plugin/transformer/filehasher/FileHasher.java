package com.linkedpipes.plugin.transformer.filehasher;

import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.dataunit.sesame.api.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.RDF;

/**
 *
 */
public class FileHasher implements Component.Sequential {

    private static final int BUFFER_SIZE = 4096;

    @Component.OutputPort(id = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.OutputPort(id = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdf;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        final ValueFactory vf = SimpleValueFactory.getInstance();
        final IRI typeHexBinary = vf.createIRI(
                "http://www.w3.org/2001/XMLSchema#hexBinary");
        for (FilesDataUnit.Entry entry : inputFiles) {
            // Compute checksum.
            final String checkSum;
            try {
                checkSum = computeChecksum(entry.toFile());
            } catch (IOException ex) {
                throw exceptionFactory.failure("Can't read file.", ex);
            } catch (NoSuchAlgorithmException ex) {
                throw exceptionFactory.failure("Missing algorithm.", ex);
            }
            // Create RDF output.
            outputRdf.execute((connection) -> {
                final Resource root = vf.createBNode();
                final Resource checkSumNode = vf.createBNode();
                connection.begin();
                // Root object.
                connection.add(vf.createStatement(root,
                        vf.createIRI(FileHasherVocabulary.HAS_FILE_NAME),
                        vf.createLiteral(entry.getFileName())
                ), outputRdf.getGraph());
                connection.add(vf.createStatement(root,
                        vf.createIRI(FileHasherVocabulary.HAS_CHECKSUM),
                        checkSumNode), outputRdf.getGraph());
                // Checksum object.
                connection.add(vf.createStatement(checkSumNode,
                        RDF.TYPE,
                        vf.createIRI(FileHasherVocabulary.HAS_CHECKSUM)
                ), outputRdf.getGraph());
                connection.add(vf.createStatement(checkSumNode,
                        vf.createIRI(FileHasherVocabulary.HAS_ALGORITHM),
                        vf.createIRI(FileHasherVocabulary.SHA1)
                ), outputRdf.getGraph());
                connection.add(vf.createStatement(checkSumNode,
                        vf.createIRI(FileHasherVocabulary.HAS_CHECKSUM_VALUE),
                        vf.createLiteral(checkSum, typeHexBinary)
                ), outputRdf.getGraph());
                connection.commit();
            });
        }
    }

    /**
     * Compute SHA1 checksum for given file. Based on the code example
     * from http://www.sha1-online.com/sha1-java/ .
     *
     * @param file
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    protected String computeChecksum(File file)
            throws NoSuchAlgorithmException, IOException {
        final MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        try (FileInputStream stream = new FileInputStream(file)) {
            final byte[] data = new byte[BUFFER_SIZE];
            int read;
            while ((read = stream.read(data)) != -1) {
                sha1.update(data, 0, read);
            }
            // Convert to string.
            final byte[] hashBytes = sha1.digest();
            final StringBuffer hashString = new StringBuffer();
            for (int i = 0; i < hashBytes.length; i++) {
                hashString.append(Integer.toString(
                        (hashBytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            return hashString.toString();
        }
    }

}
