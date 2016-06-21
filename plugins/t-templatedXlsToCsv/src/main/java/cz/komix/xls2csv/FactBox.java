package cz.komix.xls2csv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FactBox {

    private static final Logger LOG = LoggerFactory.getLogger(FactBox.class);

    private final List<Fact> box = new ArrayList<>();

    private final Xls2Csv xls2csv;

    FactBox(Xls2Csv xls2csv) {
        this.xls2csv = xls2csv;
    }

    void saveFact(int order, String dato, List<Dimension> dimenze) {
        for (Fact f : box) {
            if (f.getOrder() == order) {
                f.getData().add(dato);
                f.saveDimenze(dimenze);
                //f.dimenze.saveDimenze(d);
                return; // Uz existuje
            }
        }
        Fact f = new Fact(order, xls2csv);
        f.saveDimenze(dimenze);
        f.getData().add(dato);
        box.add(f);
    }

    void saveFiltr(int poradi, List<Integer> filtr) {
        for (Fact f : box) {
            if (f.getOrder() == poradi) {
                if (f.getFiltr() == null) {
                    f.setFiltr(filtr);
                } else {
                    throw new RuntimeException(
                            "Chyba - duplicitni prirazeni filtru k dimenzi "
                            + poradi);
                }
            }
        }
        Fact f = new Fact(poradi, xls2csv);
        f.setFiltr(filtr);
        box.add(f);
    }

    /**
     * Prevadi seznam nalezenych dimenzi na text hodnot pro fakt.
     * Pokud chybi pozadovana dimenze pro dany fakt, vyhodi vyjimku!
     *
     * @param poradi
     * @param sheet
     * @param dims
     * @param cellTemplate
     * @return
     */
    String getTextDimenzi(int poradi, HSSFSheet sheet,
            List<Dimension> dims, HSSFCell cellTemplate) {
        Fact fakt = null;
        for (Fact f : this.box) {
            if (f.getOrder() == poradi) {
                fakt = f;
                break;
            }
        }
        if (fakt == null) {
            fakt = new Fact(poradi, xls2csv);
            this.box.add(fakt);
            LOG.debug("Zakladam fakt #" + poradi + " pro dims: " + dims);
            fakt.setFiltr(new ArrayList<>());
            for (Dimension d : dims) {
                fakt.getFiltr().add(d.getOrder());
            }
        }
        if (fakt.getFiltr() == null) {
            throw new RuntimeException("Filtr pro fakt " + poradi
                    + " nebyl naplnen");
        }
        String result = "";
        for (int i : fakt.getFiltr()) {
            boolean found = false;
            for (Dimension e : dims) {
                if (i == e.getOrder()) {
                    found = true;
                    break;
                }
            }
            if (found == false) {
                throw new RuntimeException("Pro fakt " + poradi
                        + " na souradnici R:"
                        + cellTemplate.getRow().getRowNum() + ", S: "
                        + cellTemplate.getColumnIndex()
                        + ", sheet: " + sheet.getSheetName()
                        + " nebyla nalezena pozadovana dimenze " + i);
            }
        }
        for (Dimension d : dims) {
            // Pokud NALEZENA dimenze neni ve filtru, vynecham ji ve vystupu
            boolean found = false;
            for (int i : fakt.getFiltr()) {
                if (d.getOrder() == i) {
                    found = true;
                    break;
                }
            }
            if (found == false) {
                continue;
            }

            String addon;
            if (d.getConstant() == null) {
                addon = getCellFormatedData(sheet, d.getyMin(), d.getxMin());
            } else {
                addon = d.getConstant();
            }

            if ("".equals(result)) {
                result = "\"" + addon + "\"";
            } else {
                result = result + ",\"" + addon + "\"";
            }
        }
        return result;
    }

    public List<Fact> getBox() {
        return Collections.unmodifiableList(box);
    }

    private static String getCellFormatedData(HSSFSheet wbSheet, int rowNum,
            int colIndex) {
        if (wbSheet == null || wbSheet.getRow(rowNum) == null
                || wbSheet.getRow(rowNum).getCell(colIndex) == null) {
            return "";
        }
        HSSFCell cell = wbSheet.getRow(rowNum).getCell(colIndex);
        if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
            return cell.getStringCellValue().trim();
        }
        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            //cell.getCellStyle().getDataFormatString();
            return "" + cell.getNumericCellValue();
        }
        if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
            return cell.getCellFormula() + " / "
                    + cell.getCachedFormulaResultType();
        }
        return "";
    }

}
