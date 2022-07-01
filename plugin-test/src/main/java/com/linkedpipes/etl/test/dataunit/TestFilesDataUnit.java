package com.linkedpipes.etl.test.dataunit;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class TestFilesDataUnit
        implements FilesDataUnit, WritableFilesDataUnit {

    private final File directory;

    public TestFilesDataUnit(File directory) {
        this.directory = directory;
    }

    @Override
    public File createFile(String fileName) throws LpException {
        final File file = new File(directory, fileName);
        if (file.exists()) {
            throw new LpException("File already exists!");
        }
        return file;
    }

    @Override
    public File getWriteDirectory() {
        return directory;
    }

    @Override
    public Collection<File> getReadDirectories() {
        return Arrays.asList(directory);
    }

    @Override
    public long size() {
        return listFiles().size();
    }

    @Override
    public Iterator<Entry> iterator() {
        final Iterator<File> files = listFiles().iterator();
        return new Iterator<Entry>() {
            @Override
            public boolean hasNext() {
                return files.hasNext();
            }

            @Override
            public Entry next() {
                final File file = files.next();
                return new Entry() {

                    @Override
                    public File toFile() {
                        return file;
                    }

                    @Override
                    public String getFileName() {
                        return directory.toPath().relativize(
                                file.toPath()).toString();
                    }
                };
            }
        };
    }

    private List<File> listFiles() {
        final List<File> files = new LinkedList<>();
        try {
            Files.walkFileTree(directory.toPath(),
                    new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file,
                                BasicFileAttributes attrs)
                                throws IOException {
                            files.add(file.toFile());
                            return super.visitFile(file, attrs);
                        }
                    });
        } catch (IOException ex) {
            throw new RuntimeException("Can't iterate files.", ex);
        }
        return files;
    }

}
