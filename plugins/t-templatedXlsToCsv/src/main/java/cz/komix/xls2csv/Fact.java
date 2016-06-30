package cz.komix.xls2csv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Fact {

    private static final Logger LOG = LoggerFactory.getLogger(Fact.class);

    private final int order;

    private final List<String> data;

    private List<Integer> filtr;

    private final DimensionBox dimension;

    private final Xls2Csv xls2csv;

    Fact(int order, Xls2Csv xls2csv) {
        this.order = order;
        this.data = new ArrayList<>();
        this.dimension = new DimensionBox();
        this.filtr = null;
        this.xls2csv = xls2csv;
    }

    void saveDimenze(List<Dimension> dimenze) {
        if (filtr == null) {
            throw new RuntimeException("Ukladani dimenzi faktu driv nez filtru");
        }
        if (filtr.size() > dimenze.size()) {
            throw new RuntimeException("Nedostatek dimenzi u faktu");
        }
        if (!this.dimension.getBox().isEmpty()) {
            return;
        }
        for (Dimension d : dimenze) {
            this.dimension.saveDimenze(d);
        }
    }

    /**
     * Create and return file name that corresponds to this fact.
     *
     * @param baseName
     * @return
     */
    public String createFileName(String baseName) {
        // Fxx + baseName + ".csv"
        String finalOutputFilePath;
        if (xls2csv.fileNames.containsKey(this.order)) {
            finalOutputFilePath = xls2csv.fileNames.get(this.order);
            finalOutputFilePath = finalOutputFilePath.replaceAll(" ", "");

        } else {
            finalOutputFilePath = "F" + String.format("%02d", this.order)
                    + "_" + baseName + ".csv";
            finalOutputFilePath = finalOutputFilePath.replaceAll(" ", "");
        }
        return finalOutputFilePath;
    }

    public void saveToFile(File outputFile, String baseName) {
        try (OutputStreamWriter csv = new OutputStreamWriter(
                new FileOutputStream(outputFile),
                Charset.forName("UTF-8").newEncoder())) {
            //
            List<Dimension> dims = dimension.getSortedDimenze();
            csv.append("\"Fakt\"");
            for (Dimension d : dims) {
                if (filtr.contains(d.getOrder())) {
                    csv.append(",\"" + d.getDescription().trim() + "\"");
                }
            }
            csv.append(",\"target_data_cube\",\"source_file\"");
            csv.append("\r\n");
            String cubeName = "MISSING-CUBE-NAME";
            LOG.debug("KOSTKY: " + xls2csv.faktCubeNames + " / " + this.order);
            if (xls2csv.faktCubeNames.containsKey(this.order)) {
                cubeName = xls2csv.faktCubeNames.get(this.order);
                LOG.debug("cubeName : " + cubeName);
            }
            for (String line : data) {
                csv.append(line);
                csv.append(",\"" + cubeName + "\",\"" + baseName + ".xls\"");
                csv.append("\r\n");
            }
            csv.flush();
        } catch (IOException ex) {
            LOG.error("Problem creating output csv file {}: {}",
                    outputFile, ex);
        }
    }

    List<String> getData() {
        return data;
    }

    List<Integer> getFiltr() {
        return filtr;
    }

    void setFiltr(List<Integer> filtr) {
        this.filtr = filtr;
    }

    int getOrder() {
        return order;
    }

}
