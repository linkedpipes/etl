package cz.komix.xls2csv;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Xls2Csv {

    private static final Logger LOG = LoggerFactory.getLogger(Xls2Csv.class);

    private static final String PREFIX = "\\%\\%";

    private static final String SUFFIX = "\\%\\%";

    private static final boolean ONE_LIST_ONLY = false;

    public List<String> outputFilePathList = new ArrayList();

    private HSSFWorkbook wbTemplate, wbData;

    private HSSFSheet shTemplate;

    private DimensionBox dimBox;

    private final FactBox factBox;

    private List<Link> linkBox;

    private final Map<Integer, List<Integer>> groupBox = new HashMap<>();

    public final Map<Integer, String> fileNames = new HashMap<>();

    public final Map<Integer, String> faktCubeNames = new HashMap<>();

    public final String fileName;

    public Xls2Csv(String fileName) {
        this.factBox = new FactBox(this);
        this.fileName = fileName;
    }

    /**
     * Otevira vstupni soubory
     *
     * @param data
     * @param template
     * @throws IOException
     */
    public void init(File data, File template) throws IOException {
        wbData = new HSSFWorkbook(new FileInputStream(data));
        wbTemplate = new HSSFWorkbook(new FileInputStream(template));
        LOG.debug("Data: {}", data);
        LOG.debug("Template: {}", template);
        LOG.debug("Number of sheets in template: "
                + wbTemplate.getNumberOfSheets());
    }

    /**
     * Parsuje soubor se sablonou a na zaklade nalezenych znacek
     * nastavuje datovou bazi a/nebo provadi vstupne-vystupni ukony.
     */
    public void parse() {
        LOG.info("Starting parsing XLS documents..");
        for (int sheetNumber = 0; sheetNumber < wbTemplate.getNumberOfSheets();
                sheetNumber++) {
            LOG.debug("JDU NA SHEET: " + sheetNumber);
            // Every sheet has its own dimension descriptors
            // (e.g. column/row headers)
            dimBox = new DimensionBox();
            linkBox = new ArrayList<>();
            shTemplate = wbTemplate.getSheetAt(sheetNumber);
            LOG.debug("Number of data rows [" + shTemplate.getSheetName()
                    + "]: " + shTemplate.getPhysicalNumberOfRows()
                    + " (" + shTemplate.getFirstRowNum() + " - "
                    + shTemplate.getLastRowNum() + ")");
            Iterator<Row> rowTemplateIter = shTemplate.rowIterator();
            while (rowTemplateIter.hasNext()) {
                final HSSFRow rowTemplate
                        = shTemplate.getRow(rowTemplateIter.next().getRowNum());
                final Iterator<Cell> cellTemplateIter
                        = rowTemplate.cellIterator();
                while (cellTemplateIter.hasNext()) {
                    HSSFCell cellTemplate = rowTemplate.getCell(
                            cellTemplateIter.next().getColumnIndex());
                    if (cellTemplate.getCellType() == Cell.CELL_TYPE_STRING) {
                        String cellValue = cellTemplate.getStringCellValue();
                        // FAKT:
                        if (cellValue.matches(PREFIX + "F[0-9]+" + SUFFIX)) {
                            parseFakt(sheetNumber, cellTemplate);
                        } // DIMENZE:
                        else if (cellValue.matches(PREFIX
                                + "D[0-9]+(_\\(.+\\))?" + SUFFIX)) {
                            parseDimenze(sheetNumber, cellTemplate);
                        } // PRIRAZENI DIMENZI FAKTU
                        else if (cellValue.matches(PREFIX
                                + "P([0-9]+)_\\((D[0-9]+[,])*D[0-9]+\\)"
                                + SUFFIX)) {
                            parseAsgn(cellTemplate);
                        } // SOUBOR PRO VYSTUP FAKTU
                        else if (cellValue.matches(PREFIX
                                + "S([0-9]+)_\\(([^)]*)\\)" + SUFFIX)) {
                            LOG.debug("S");
                            parseSoubor(cellTemplate);
                        } // SOUBOR PRO PRIRAZENI FAKTU KE KOSTCE
                        // -- %%C_(datova-kostka-01#1,2)%%
                        else if (cellValue.matches(PREFIX
                                + "C_\\(([^)]*)[#]([^)]*)\\)" + SUFFIX)) {
                            LOG.debug("C");
                            parseCubename(cellTemplate);
                        } // GRUPA LISTU SE STEJNOU SABLONOU
                        else if (cellValue.matches(PREFIX
                                + "G([0-9]+)_\\((([0-9]+[,])*[0-9]+)\\)"
                                + SUFFIX)
                                || cellValue.matches(PREFIX
                                        + "G_\\(ALL\\)\" + SUFFIX")) {
                            LOG.debug("G");
                            parseGroup(cellTemplate);
                        } // ODKAZ NA JINOU BUNKU
                        else if (cellValue.matches(PREFIX
                                + "B_\\(([A-Z]),([0-9]+)\\)" + SUFFIX)) {
                            parseOdkaz(sheetNumber, cellTemplate);
                        } else if (cellValue.startsWith("%%")) {
                            LOG.debug("UNMATCHED: " + cellValue);
                        }
                    }
                }
            }
            if (ONE_LIST_ONLY) {
                break; // TODO - projit vsechny
            }
        }
    }

//    public void save() {
//        log.info("About to save output files..");
//        for (Fact f : factBox.getBox()) {
//            String outputFilePath = f.saveToFile(FILEPATH, fileName);
//            outputFilePathList.add(outputFilePath);
//        }
//    }
    String getCellFormatedData(int sheetNumber, int colIndex, int rowNum) {
        if (wbData == null
                || wbData.getSheetAt(sheetNumber) == null
                || wbData.getSheetAt(sheetNumber).getRow(rowNum) == null
                || wbData.getSheetAt(sheetNumber).getRow(rowNum)
                .getCell(colIndex) == null) {
            return "## NULL ##";
        }
        final HSSFCell cell = wbData.getSheetAt(sheetNumber)
                .getRow(rowNum).getCell(colIndex);
        if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
            return cell.getStringCellValue().trim();
        }
        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            //cell.getCellStyle().getDataFormatString();
            return "" + cell.getNumericCellValue();
        }
        if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
            if (cell.getCachedFormulaResultType() == Cell.CELL_TYPE_STRING) {
                return cell.getStringCellValue().trim();
            }
            if (cell.getCachedFormulaResultType() == Cell.CELL_TYPE_NUMERIC) {
                return "" + cell.getNumericCellValue();
            }
            return "## UnKnownCachedType: "
                    + cell.getCachedFormulaResultType() + " ##";
        }
        return "## UnKnownType: " + cell.getCellType() + " ##";
    }

    private void parseFakt(int sheetNumber, HSSFCell cellTemplate) {
        final String cellValue = cellTemplate.getStringCellValue();
        final String factId = cellValue.replaceAll(PREFIX + "F([0-9]+)" + SUFFIX,
                "$1");

        final List<Dimension> dims = dimBox.getDimenze(
                cellTemplate.getColumnIndex(),
                cellTemplate.getRow().getRowNum(), linkBox);
        LOG.debug("FAKT SH: " + sheetNumber
                + ", COL: " + cellTemplate.getColumnIndex() + ", ROW: "
                + cellTemplate.getRow().getRowNum()
                + ": " + factBox.getTextDimenzi(Integer.parseInt(factId),
                        wbData.getSheetAt(sheetNumber), dims, cellTemplate));
        String textDimenzi = "\"" + getCellFormatedData(
                sheetNumber, cellTemplate.getColumnIndex(),
                cellTemplate.getRow().getRowNum())
                + "\"," + factBox.getTextDimenzi(Integer.parseInt(factId),
                        wbData.getSheetAt(sheetNumber), dims, cellTemplate);
        factBox.saveFact(
                Integer.parseInt(factId), textDimenzi, dims);

        for (Integer groupId : groupBox.keySet()) {
            // Neosetreno - je-li v nektere skupine tento sheet jako prvni
            // (zdroj definice), pak:
            if (groupBox.get(groupId).get(0) == sheetNumber) {
                // Zpracuj zbyvajici sheety stejnou definici,
                // jako je aktualni (jiz zpracovana)
                for (int nextSheet = 1;
                        nextSheet < groupBox.get(groupId).size(); nextSheet++) {
                    textDimenzi = "\"" + getCellFormatedData(
                            groupBox.get(groupId).get(nextSheet),
                            cellTemplate.getColumnIndex(),
                            cellTemplate.getRow().getRowNum())
                            + "\","
                            + factBox.getTextDimenzi(Integer.parseInt(factId),
                                    wbData.getSheetAt(
                                            groupBox.get(groupId).get(nextSheet)),
                                    dims, cellTemplate);
                    factBox.saveFact(
                            Integer.parseInt(factId), textDimenzi, dims);
                }
            }
        }
    }

    private void parseDimenze(int sheetNumber, HSSFCell cellTemplate) {
        String cellValue = cellTemplate.getStringCellValue();
        String dimId = cellValue.replaceAll(PREFIX + "D([0-9]+)(_\\(.+\\))?"
                + SUFFIX, "$1");
        String dimName = "D" + dimId;
        String konstanta = null;
        if (cellValue.matches(PREFIX + "D[0-9]+_\\(.+\\)" + SUFFIX)) {
            dimName = cellValue.replaceAll(PREFIX + "D[0-9]+_\\((.+)\\)"
                    + SUFFIX, "$1");
            if (dimName.contains("#")) {
                String[] casti = dimName.split("[#]");
                if (casti.length != 2) {
                    throw new RuntimeException(
                            "Spatny syntaxe v textu dimenzi @ "
                            + cellTemplate);
                }
                if ("".equals(casti[0])) {
                    dimName = "D" + dimId; // DEFAULT
                } else {
                    dimName = casti[0];
                }
                LOG.debug("Nasel jsem konstantu dimenze " + dimName + ": "
                        + casti[1]);
                konstanta = casti[1];
            }
        }
        CellRangeAddress merge = getMergedRegion(cellTemplate, shTemplate);
        if (merge != null) {
            dimBox.saveDimenze(new Dimension(Integer.parseInt(dimId), dimName,
                    konstanta, merge.getFirstColumn(),
                    merge.getLastColumn(), merge.getFirstRow(),
                    merge.getLastRow()));
        } else {
            dimBox.saveDimenze(new Dimension(Integer.parseInt(dimId), dimName,
                    konstanta,
                    cellTemplate.getColumnIndex(),
                    cellTemplate.getRow().getRowNum()));
        }
    }

    private void parseOdkaz(int sheetNumber, HSSFCell cellTemplate) {
        String cellValue = cellTemplate.getStringCellValue();
        CellRangeAddress merge = getMergedRegion(cellTemplate, shTemplate);
        if (merge != null) {
            linkBox.add(new Link(cellValue, merge.getFirstColumn(),
                    merge.getLastColumn(), merge.getFirstRow(),
                    merge.getLastRow()));
        } else {
            linkBox.add(new Link(cellValue, cellTemplate.getColumnIndex(),
                    cellTemplate.getRow().getRowNum()));
        }

    }

    private void parseAsgn(HSSFCell cellTemplate) {
        String cellValue = cellTemplate.getStringCellValue();
        String sFaktId = cellValue.replaceAll(PREFIX
                + "P([0-9]+)_\\(((D[0-9]+[,])*D[0-9]+)\\)" + SUFFIX, "$1");
        String sDimIds = cellValue.replaceAll(PREFIX
                + "P([0-9]+)_\\(((D[0-9]+[,])*D[0-9]+)\\)" + SUFFIX, "$2");
        final List<Integer> dims = new ArrayList<>();
        final String[] sDims = sDimIds.split(",");
        for (String sDim : sDims) {
            dims.add(Integer.parseInt(sDim.substring(1)));
        }
        factBox.saveFiltr(Integer.parseInt(sFaktId), dims);
        LOG.debug("ASGN: Fakt " + sFaktId + " --> " + dims);
    }

    private void parseGroup(HSSFCell cellTemplate) {
        String cellValue = cellTemplate.getStringCellValue();
        Integer groupId = null;
        List<Integer> sheets = new ArrayList<Integer>();
        if (cellValue.matches(PREFIX
                + "G([0-9]+)_\\((([0-9]+[,])*[0-9]+)\\)" + SUFFIX)) {
            groupId = Integer.parseInt(cellValue.replaceAll(PREFIX
                    + "G([0-9]+)_\\((([0-9]+[,])*[0-9]+)\\)" + SUFFIX, "$1"));
            String sSheetIds = cellValue.replaceAll(PREFIX
                    + "G([0-9]+)_\\((([0-9]+[,])*[0-9]+)\\)" + SUFFIX, "$2");
            LOG.debug("sSheetIds: " + sSheetIds);
            LOG.debug("sSheetIds $3: " + cellValue.replaceAll(PREFIX
                    + "G([0-9]+)_\\((([0-9]+[,])*[0-9]+)\\)" + SUFFIX, "$3"));
            String[] sSheets = sSheetIds.split(",");
            for (String sSheet : sSheets) {
                sheets.add(Integer.parseInt(sSheet) - 1);
            }
        } else if (cellValue.matches(PREFIX + "G_\\(ALL\\)" + SUFFIX)) {
            groupId = 0;
            for (int i = 0; i < wbTemplate.getNumberOfSheets(); i++) {
                sheets.add(new Integer(i));
            }
        }
        if (groupId == null || sheets.isEmpty()) {
            throw new RuntimeException("Nekompletni data pro definici grupy");
        }
        if (groupBox.containsKey(groupId)) {
            throw new RuntimeException("Duplicitni cislo Grupy : " + groupId);
        }
        groupBox.put(groupId, sheets);
        LOG.debug("GROUP: gid #" + groupId + " --> " + sheets);
    }

    private void parseSoubor(HSSFCell cellTemplate) {
        String cellValue = cellTemplate.getStringCellValue();
        String sFaktId = cellValue.replaceAll(PREFIX
                + "S([0-9]+)_\\(([^)]*)\\)" + SUFFIX, "$1");
        String sFilename = cellValue.replaceAll(PREFIX
                + "S([0-9]+)_\\(([^)]*)\\)" + SUFFIX, "$2");
        LOG.debug("SOUBOR: Fakt " + sFaktId + " --> " + sFilename);
        fileNames.put(Integer.parseInt(sFaktId), sFilename);
    }

    private void parseCubename(HSSFCell cellTemplate) {
        String cellValue = cellTemplate.getStringCellValue();
        String cubeName = cellValue.replaceAll(PREFIX
                + "C_\\(([^)]*)[#]([^)]*)\\)" + SUFFIX, "$1");
        String sFaktIds = cellValue.replaceAll(PREFIX
                + "C_\\(([^)]*)[#]([^)]*)\\)" + SUFFIX, "$2");
        String asFaktIds[] = sFaktIds.split(",");
        for (int i = 0; i < asFaktIds.length; i++) {
            faktCubeNames.put(Integer.parseInt(asFaktIds[i]), cubeName);
            LOG.debug("CUBENAME: Fakt " + Integer.parseInt(asFaktIds[i])
                    + " --> " + cubeName);
        }
    }

    public FactBox getFactBox() {
        return factBox;
    }

    private static CellRangeAddress getMergedRegion(HSSFCell cell,
            HSSFSheet sheet) {
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            if (sheet.getMergedRegion(i).isInRange(cell.getRowIndex(),
                    cell.getColumnIndex())) {
                return sheet.getMergedRegion(i);
            }
        }
        return null;
    }

}
