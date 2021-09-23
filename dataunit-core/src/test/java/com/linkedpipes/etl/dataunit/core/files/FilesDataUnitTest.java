package com.linkedpipes.etl.dataunit.core.files;

import com.linkedpipes.etl.dataunit.core.DataUnitConfiguration;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FilesDataUnitTest {

    private static List<File> DIRECTORIES = new LinkedList<>();

    @AfterAll
    public static void cleanup() {
        for (File directory : DIRECTORIES) {
            FileUtils.deleteQuietly(directory);
        }
    }

    @Test
    public void addAndRead() throws Exception {
        DefaultFilesDataUnit files = new DefaultFilesDataUnit(
                createConfiguration(),
                Collections.EMPTY_LIST);
        //
        Assertions.assertEquals(0, files.size());
        // This does not create the file.
        File a = files.createFile("directory-a/a");
        Assertions.assertEquals(0, files.size());
        // This does create.
        FileUtils.writeStringToFile(a, "");
        Assertions.assertEquals(1, files.size());
        File b = files.createFile("directory-b/b");
        FileUtils.writeStringToFile(b, "");
        Assertions.assertEquals(2, files.size());
        //
        List<String> paths = new ArrayList<>();
        files.forEach((entry) -> paths.add(entry.getFileName()));
        //
        Assertions.assertTrue(
                paths.contains("directory-a" + File.separator + "a"));
        Assertions.assertTrue(
                paths.contains("directory-b" + File.separator + "b"));
    }

    private DataUnitConfiguration createConfiguration() throws IOException {
        return new DataUnitConfiguration(
                null, null, null, getTempDirectory().toURI().toString());
    }

    @Test
    public void merge() throws Exception {
        //
        DefaultFilesDataUnit a = new DefaultFilesDataUnit(
                createConfiguration(), Collections.EMPTY_LIST);
        DefaultFilesDataUnit b = new DefaultFilesDataUnit(
                createConfiguration(), Collections.EMPTY_LIST);
        DefaultFilesDataUnit c = new DefaultFilesDataUnit(
                createConfiguration(),
                Arrays.asList("http://dataunit/a", "http://dataunit/b"));
        //
        FileUtils.writeStringToFile(a.createFile("dir/a"), "");
        FileUtils.writeStringToFile(a.createFile("1"), "");
        Assertions.assertEquals(2, a.size());
        FileUtils.writeStringToFile(b.createFile("dir/b"), "");
        FileUtils.writeStringToFile(b.createFile("1"), "");
        Assertions.assertEquals(2, a.size());
        //
        Map<String, ManageableDataUnit> dataUnits = new HashMap<>();
        dataUnits.put("http://dataunit/a", a);
        dataUnits.put("http://dataunit/b", b);
        dataUnits.put("http://dataunit/c", c);
        c.initialize(dataUnits);
        //
        Assertions.assertEquals(1, a.getReadDirectories().size());
        Assertions.assertEquals(1, b.getReadDirectories().size());
        Assertions.assertEquals(3, c.getReadDirectories().size());
        //
        Assertions.assertEquals(4, c.size());
    }

    @Test
    public void saveAndLoad() throws Exception {
        DefaultFilesDataUnit a = new DefaultFilesDataUnit(
                createConfiguration(), Collections.EMPTY_LIST);
        File file = a.createFile("dir/a");
        FileUtils.writeStringToFile(file, "");
        Assertions.assertEquals(1, a.size());
        //
        File saveDirectory = getTempDirectory();
        saveDirectory.mkdirs();
        a.save(saveDirectory);
        DefaultFilesDataUnit b = new DefaultFilesDataUnit(
                createConfiguration(), Collections.EMPTY_LIST);
        Assertions.assertEquals(0, b.size());
        b.initialize(saveDirectory);
        Assertions.assertEquals(1, b.size());
        Assertions.assertEquals("dir" + File.separator + "a",
                b.iterator().next().getFileName());
        Assertions.assertEquals(file.toPath().normalize(),
                b.iterator().next().toFile().toPath().normalize());
    }

    @Test
    public void createExisting() throws Exception {
        DefaultFilesDataUnit a = new DefaultFilesDataUnit(
                createConfiguration(), Collections.EMPTY_LIST);
        FileUtils.writeStringToFile(a.createFile("dir/a"), "");
        try {
            a.createFile("dir/a");
            Assertions.assertTrue(false);
        } catch (LpException ex) {

        }
    }

    protected File getTempDirectory() throws IOException {
        File file = File.createTempFile("lp-test-du-files", "");
        file.delete();
        this.DIRECTORIES.add(file);
        return file;
    }

}
