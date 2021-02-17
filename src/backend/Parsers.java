package backend;

import org.apache.pdfbox.pdmodel.PDDocument;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.Table;
import technology.tabula.TextChunk;
import technology.tabula.detectors.SpreadsheetDetectionAlgorithm;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Nicholas Curl
 */
public class Parsers {
    private static float INCH_POINT = 72.0f;
    private static float pageX = 792.0f;
    private static float LEFT = INCH_POINT;
    private static float RIGHT = pageX - INCH_POINT;
    private static float pageY = 1224.0f;
    private float stopPosition;
    private String employeeName;
    private BasicExtractionAlgorithm bea;
    private boolean commentFlag;
    private static float MAX_INCREMENT = 20.0f;

    public Parsers() {
        this.bea = new BasicExtractionAlgorithm();
        this.stopPosition = 0.0f;
        this.commentFlag = false;
    }

    private int processComments(int currRow, int currCol, int row2col, Table table, Table tableNew) {
        int row = currRow + 1;
        TextChunk chunk = (TextChunk) table.getCell(row, currCol);
        while (!(chunk.getText().contains("Description") || chunk.getText().contains("Previous") || chunk.getText().contains("Approved") || chunk.getText().contains("Proposed") || chunk.getText().isBlank())) {
            TextChunk chunk1 = new TextChunk(chunk.getTextElements());
            tableNew.add(chunk1, row, row2col);
            chunk.getTextElements().clear();
            row++;
            chunk = (TextChunk) table.getCell(row, currCol);
        }
        row2col++;
        return row2col;
    }

    public String processEmployeeName(String path, int pageNum) throws IOException {
        float top = INCH_POINT + 20.0f;
        float bottom = top + MAX_INCREMENT;
        int row;
        int col;
        String formattedName;
        loopbreak:
        while (true) {
            Page page = getAreaFromPage(path, pageNum, top, LEFT, bottom, RIGHT);
            Table table = bea.extract(page).get(0);
            // System.out.println(bottom);
            int endRow = table.getRowCount() - 1;
            for (int i = 0; i < table.getColCount(); i++) {
                TextChunk chunk = (TextChunk) table.getCell(endRow, i);
                String text = chunk.getText();
                if (text.equals("Employee Name:")) {
                    bottom += MAX_INCREMENT;
                    row = endRow;
                    col = i;
                    break loopbreak;
                }
            }
            bottom += MAX_INCREMENT;
        }
        Page page = getAreaFromPage(path, pageNum, top, LEFT, bottom, RIGHT);
        Table table = bea.extract(page).get(0);
        TextChunk chunk = (TextChunk) table.getCell(row + 1, col);
        String name = chunk.getText();
        if (name.contains("(")) {
            String[] idSplit = name.split(" \\(");
            StringBuilder sb = new StringBuilder(idSplit[1]);
            sb.insert(0, "(");
            idSplit[1] = sb.toString();
            String[] split = idSplit[0].split(", ");
            sb = new StringBuilder();
            sb.append(split[1]);
            sb.append(" ");
            sb.append(split[0]);
            sb.append(" ");
            sb.append(idSplit[1]);
            formattedName = sb.toString();
        } else {
            String[] split = name.split(", ");
            StringBuilder sb = new StringBuilder();
            sb.append(split[1]);
            sb.append(" ");
            sb.append(split[0]);
            formattedName = sb.toString();
        }
        formattedName = formattedName.toLowerCase();
        formattedName = formattedName.replaceAll(" ", "_");
        return formattedName;
    }

    private static Page getAreaFromPage(String path, int page, float top, float left, float bottom, float right) throws IOException {
        return getPage(path, page).getArea(top, left, bottom, right);
    }

    private static Page getPage(String path, int pageNumber) throws IOException {
        ObjectExtractor oe = null;
        try {
            PDDocument document = PDDocument
                    .load(new File(path));
            oe = new ObjectExtractor(document);
            Page page = oe.extract(pageNumber);
            return page;
        } finally {
            if (oe != null)
                oe.close();
        }
    }

    public BasicExtractionAlgorithm getBea() {
        return bea;
    }

    public float getStopPosition() {
        return stopPosition;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public Table parseTop(String path, int pageNum) throws IOException {
        Table tableNew = new Table(this.bea);
        float top = INCH_POINT + 20.0f;
        float bottom = top + 150.0f;
        Page page = getAreaFromPage(path, pageNum, top, LEFT, bottom, RIGHT);
        Table table = bea.extract(page).get(0);
        int row1col = tableNew.getColCount();
        int row2col = tableNew.getColCount();
        for (int i = 0; i < table.getRowCount(); i++) {
            for (int j = 0; j < table.getColCount(); j++) {
                TextChunk container = (TextChunk) table.getCell(i, j);
                String text = container.getText();
                if (text.contains(":")) {
                    tableNew.add(container, 0, row1col);
                    row1col++;
                } else if (!(text.isBlank() || text.isEmpty())) {
                    tableNew.add(container, 1, row2col);
                    row2col++;
                }
            }
        }
        top = bottom + 4.0f;
        bottom = top + 100.0f;
        page = getAreaFromPage(path, pageNum, top, LEFT, bottom, RIGHT);
        table = bea.extract(page).get(0);
        outerloop:
        for (int i = 0; i < table.getRowCount(); i++) {
            for (int j = 0; j < table.getColCount(); j++) {
                TextChunk container = (TextChunk) table.getCell(i, j);
                String text = container.getText();
                if (text.contains("Comments") && !(text.contentEquals("Comments:"))) {
                    commentFlag = true;
                    break outerloop;
                } else if (text.contains("Description") || text.contains("Previous") || text.contains("Approved") || text.contains("Proposed")) {
                    this.stopPosition = container.y;
                    break outerloop;
                } else if (text.contains(":")) {
                    tableNew.add(container, 0, row1col);
                    row1col++;
                    if (text.contentEquals("PAF Type :")) {
                        TextChunk chunk1 = (TextChunk) table.getCell(i + 1, j);
                        TextChunk chunk2 = (TextChunk) table.getCell(i + 2, j);
                        if (!(chunk2.getText().contains(":"))) {
                            chunk1.merge(chunk2);
                            chunk2.getTextElements().clear();
                        }
                    }
                    if (text.contentEquals("Comments:")) {
                        row2col = processComments(i, j, row2col, table, tableNew);
                    }
                } else if (!(text.isBlank() || text.isEmpty())) {
                    tableNew.add(container, 1, row2col);
                    row2col++;
                }
            }
        }
        if (commentFlag) {
            top = bottom - 40.0f;
            bottom = top + 150.0f;
            page = getAreaFromPage(path, pageNum, top, LEFT, bottom, RIGHT);
            table = bea.extract(page).get(0);
            loop:
            for (int i = 0; i < table.getRowCount(); i++) {
                for (int j = 0; j < table.getColCount(); j++) {
                    TextChunk container = (TextChunk) table.getCell(i, j);
                    String text = container.getText();
                    if (text.contains(":")) {
                        tableNew.add(container, 0, row1col);
                        row1col++;
                        if (text.contains("Comments:")) {
                            row2col = processComments(i, j, row2col, table, tableNew);
                        }
                    } else if (text.contains("Description") || text.contains("Previous") || text.contains("Approved") || text.contains("Proposed")) {
                        this.stopPosition = container.y;
                        break loop;
                    } else if (!(text.isBlank() || text.isEmpty())) {
                        tableNew.add(container, 1, row2col);
                        row2col++;
                    }
                }


            }
        }
        return tableNew;
    }

    public Table parseMiddle(String path, int pageNum) throws IOException {
        Table tableNew = new Table(this.bea);
        float top = this.stopPosition;
        float bottom = top + MAX_INCREMENT;
        loopbreak:
        while (true) {
            Page page = getAreaFromPage(path, pageNum, top, LEFT, bottom, RIGHT);
            Table table = bea.extract(page).get(0);
            // System.out.println(bottom);
            int endRow = table.getRowCount() - 1;
            for (int i = 0; i < table.getColCount(); i++) {
                TextChunk chunk = (TextChunk) table.getCell(endRow, i);
                String text = chunk.getText();
                if (text.equals("Review History")) {
                    bottom -= MAX_INCREMENT;
                    break loopbreak;
                }
            }
            bottom += MAX_INCREMENT;
        }
        Page page = getAreaFromPage(path, pageNum, top, LEFT, bottom, RIGHT);
        Table table = bea.extract(page).get(0);
        return table;
    }

    public List<Table> parseBottom(String path, int pageNum) throws IOException {
        SpreadsheetDetectionAlgorithm spd = new SpreadsheetDetectionAlgorithm();
        SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
        Page page = getPage(path, pageNum);
        List<Table> tables = sea.extract(page);
        return tables;

    }
}
