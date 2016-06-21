package cz.komix.xls2csv;

class Dimension {

    private int order;

    private int xMin, xMax, yMin, yMax;

    private String description;

    private String constant;

    private DimensionBox sub;

    Dimension(int order, String description, String constant,
            int xMin, int xMax, int yMin, int yMax) {
        this.order = order;
        this.description = description;
        this.constant = constant;
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        sub = new DimensionBox();
    }

    Dimension(int order, String description, String constant, int x, int y) {
        this(order, description, constant, x, x, y, y);
    }

    @Override
    public String toString() {
        return String.format("D%02d", order) + " (" + description + " # "
                + constant + " @ [" + xMin + "," + yMin + "])";
    }

    public int getOrder() {
        return order;
    }

    public DimensionBox getSub() {
        return sub;
    }

    public String getDescription() {
        return description;
    }

    public boolean contains(int col, int row) {
        return containsColumn(col) && containsRow(row);
    }

    public boolean containsColumn(int col) {
        return xMin <= col && xMax >= col;
    }

    public boolean containsRow(int row) {
        return yMin <= row && yMax >= row;
    }

    public String getConstant() {
        return constant;
    }

    public int getxMin() {
        return xMin;
    }

    public int getyMin() {
        return yMin;
    }

}
