package cz.komix.xls2csv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Link {

    private static final Logger LOG = LoggerFactory.getLogger(Link.class);

    private static final String COLUMNS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private final int xMin, xMax, yMin, yMax;

    private int linkX, linkY;

    Link(String reference, int xMin, int xMax, int yMin, int yMax) {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        this.linkX = COLUMNS.indexOf(
                reference.replaceAll("\\%\\%B_\\(([A-Z]),([0-9]+)\\)\\%\\%.*",
                        "$1"));
        this.linkY = Integer.parseInt(
                reference.replaceAll("\\%\\%B_\\(([A-Z]),([0-9]+)\\)\\%\\%.*",
                        "$2")) - 1;
        LOG.debug("Odkaz z X: " + xMin + " - " + xMax + ", Y: "
                + yMin + " - " + yMax);
        LOG.debug("Odkaz na " + reference + " prelozen na X: " + linkX
                + ", Y: " + linkY);
    }

    Link(String reference, int x, int y) {
        this(reference, x, x, y, y);
    }

    @Override
    public String toString() {
        return String.format("L[%02d,%02d]", linkX, linkY);
    }

    public boolean containsColumn(int col) {
        return xMin <= col && xMax >= col;
    }

    public boolean containsRow(int row) {
        return yMin <= row && yMax >= row;
    }

    public int getLinkX() {
        return linkX;
    }

    public void setLinkX(int linkX) {
        this.linkX = linkX;
    }

    public int getLinkY() {
        return linkY;
    }

    public void setLinkY(int linkY) {
        this.linkY = linkY;
    }

}
