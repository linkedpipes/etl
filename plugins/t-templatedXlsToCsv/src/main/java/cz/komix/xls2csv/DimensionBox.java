package cz.komix.xls2csv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DimensionBox {

    private static final Logger LOG
            = LoggerFactory.getLogger(DimensionBox.class);

    private final List<Dimension> box = new ArrayList<>();

    /**
     * Metoda pridava do seznamu dimenzi bez kontroly
     * duplicity CISLA - pro seznamy vsech vyskytu a jejich adresaci
     *
     * @param nova
     */
    public void appendDimenze(Dimension nova) {
        box.add(nova);
    }

    /**
     * Metoda pridava do seznamu dimenzi NEDUPLICTNE
     * dle CISLA - pro hlavni katalog s pojmenovanim
     *
     * @param nova
     */
    public void saveDimenze(Dimension nova) {
        for (Dimension d : this.box) {
            if (d.getOrder() == nova.getOrder()) {
                LOG.debug("Appending SUB " + nova + " to " + d);
                d.getSub().appendDimenze(nova);
                return; // Uz existuje
            }
        }
        this.box.add(nova); // FIXME !!!
    }

    /**
     * Metoda dohledava a vraci serazeny seznam dimenzi ve
     * sloupci a radku dle zadanych souradnic prochazenim
     * celeho hlavniho Boxu dimenzi.
     *
     * @param col
     * @param row
     * @return
     */
    public Dimension getDimenzeAt(int col, int row) {
        for (Dimension d : box) {
            if (d.contains(col, row)) {
                return d;
            }
            List<Dimension> subs = d.getSub().box;
            for (Dimension sub : subs) {
                if (sub.contains(col, row)) {
                    return (sub);
                }
            }
        }
        return null; // TODO : Chyba odkazu?
    }

    /**
     * Metoda dohledava a vraci serazeny seznam dimenzi ve sloupci
     * a radku dle zadanych souradnic prochazenim
     * celeho hlavniho Boxu dimenzi.
     *
     * @param col
     * @param row
     * @return
     */
    public List<Dimension> getDimenze(int col, int row, List<Link> linkBox) {
        final List<Dimension> nalez = new ArrayList<>();
        for (Dimension d : box) {
            // Pokud je dimenze ve sloupci NEBO je dimenze v radku
            if (d.containsColumn(col) || d.containsRow(row)) {
                boolean found = false;
                for (Dimension k : nalez) {
                    if (k.getOrder() == d.getOrder()) {
                        found = true;
                        nalez.set(nalez.indexOf(k), d);
                        LOG.debug("Replacing D-main : " + k + " -> " + d);
                        break;
                    }
                }
                // ale jeste neni ve vysledku hledani, tak ji tam pridej
                if (!found) {
                    LOG.debug("Setting D-main : " + d + "("
                            + d.getSub().box + ")");
                    nalez.add(d);
                }
            }
            List<Dimension> subs = d.getSub().box;
            for (Dimension sub : subs) {
                if (sub.containsColumn(col) || sub.containsRow(row)) {
                    boolean found = false;
                    for (Dimension k : nalez) {
                        if (k.getOrder() == d.getOrder()) {
                            found = true;
                            LOG.debug("Replacing D-sub : " + k + " -> " + sub);
                            nalez.set(nalez.indexOf(k), sub);
                            break;
                        }
                    }
                    if (!found) {
                        nalez.add(sub);
                    }
                }
            }
        }
        for (Link l : linkBox) {
            if (l.containsColumn(col) || l.containsRow(row)) {
                Dimension dim = getDimenzeAt(l.getLinkX(), l.getLinkY());
                LOG.debug("Linked dimenze z " + l + " na " + dim);
                if (dim != null) {
                    boolean found = false;
                    for (Dimension k : nalez) {
                        if (k.getOrder() == dim.getOrder()) {
                            found = true;
                            LOG.debug("Replacing D-link : " + k + " -> " + dim);
                            nalez.set(nalez.indexOf(k), dim);
                            break;
                        }
                    }
                    if (!found) {
                        nalez.add(dim);
                    }
                }
            }
        }
        Collections.sort(nalez, (Dimension left, Dimension right) -> {
            return left.getOrder() - right.getOrder();
        });
        return nalez;
    }

    public List<Dimension> getSortedDimenze() {
        Collections.sort(box, (Dimension left, Dimension right) -> {
            return left.getOrder() - right.getOrder();
        });
        return box;
    }

    /**
     *
     * @return Read only instance.
     */
    public List<Dimension> getBox() {
        return Collections.unmodifiableList(box);
    }

}
