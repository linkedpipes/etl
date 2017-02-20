package com.linkedpipes.etl.dataunit.core.files;

import com.linkedpipes.etl.executor.api.v1.dataunit.DataUnit;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_EXEC;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;
import com.linkedpipes.etl.rdf.utils.rdf4j.Rdf4jSource;
import com.linkedpipes.etl.rdf.utils.RdfBuilder;
import com.linkedpipes.etl.rdf.utils.RdfSource;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;
import com.linkedpipes.etl.rdf.utils.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.apache.commons.io.FileUtils.getTempDirectory;

public class FactoryTest {

    @Test
    public void noInformationAboutDataUnit() throws Exception {
        final FilesDataUnitFactory factory = new FilesDataUnitFactory();
        final RdfSource source = Rdf4jSource.createInMemory();
        final DataUnit dataUnit = factory.create(
                "http://dataunit", "http://graph", source);
        Assert.assertNull(dataUnit);
        source.shutdown();
    }

    @Test
    public void initialization() throws Exception {
        final FilesDataUnitFactory factory = new FilesDataUnitFactory();
        final RdfSource source = Rdf4jSource.createInMemory();
        final File file = getTempDirectory();
        initialize(source, file.toURI().toString());
        //
        final DataUnit dataUnit = factory.create(
                "http://dataunit", "http://graph", source);
        Assert.assertNotNull(dataUnit);
        Assert.assertTrue(dataUnit instanceof DefaultFilesDataUnit);
        final DefaultFilesDataUnit files = (DefaultFilesDataUnit) dataUnit;
        Assert.assertEquals("binding", files.getBinding());
        Assert.assertEquals("http://dataunit", files.getIri());
        Assert.assertEquals(file, files.getWriteDirectory());
        //
        source.shutdown();
    }

    protected void initialize(RdfSource source, String file)
            throws RdfUtilsException, IOException {
        final RdfBuilder builder = RdfBuilder.create(source, "http://graph");
        builder.entity("http://dataunit")
                .iri(RDF.TYPE, LP_PIPELINE.FILE_DATA_UNIT)
                .iri(LP_EXEC.HAS_WORKING_DIRECTORY, file)
                .string(LP_PIPELINE.HAS_BINDING, "binding");
        builder.commit();
    }

}
