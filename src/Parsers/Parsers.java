package Parsers;

import Containers.IDReport;
import Containers.PAF;
import org.apache.pdfbox.pdmodel.PDDocument;
import technology.tabula.*;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;
import technology.tabula.writers.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Nicholas Curl
 */
public class Parsers {
    private static float INCH_POINT = 72.0f;
    private static float pageX = 792.0f;
    private static float LEFT = INCH_POINT;
    private static float pageY = 1224.0f;
    private static float MAX_INCREMENT = 20.0f;
    private static float RIGHT = pageX - INCH_POINT;
    private float stopPosition;
    private BasicExtractionAlgorithm bea;
    private boolean commentFlag;


    public Parsers() {
        this.bea = new BasicExtractionAlgorithm();
        this.stopPosition = 0.0f;
        this.commentFlag = false;
    }

    private Table extractHeader(String path, int pageNum, SpreadsheetExtractionAlgorithm sea, Table table, float starting) throws IOException {
        Table tableNew = new Table(sea);
        Page page = getAreaFromPage(path, pageNum, starting - 50.0f, LEFT, starting, RIGHT);
        Table row = this.bea.extract(page).get(0);
        TextChunk header = (TextChunk) row.getCell(0, 0);
        int col;
        int colCount = table.getColCount();
        if ((colCount % 2) == 0) {
            col = Math.floorDiv(table.getColCount() - 1, 2);
        } else {
            col = Math.floorDiv(table.getColCount(), 2);
        }
        int blankColNum = 0;
        boolean blankCol = false;
        tableNew.add(header, 0, col);
        for (int i = 0; i < table.getRowCount(); i++) {
            for (int j = 0; j < table.getColCount(); j++) {
                Cell chunk = (Cell) table.getCell(i, j);
                if (blankCol && j == blankColNum) {
                    continue;
                }
                if (blankCol && j > blankColNum) {
                    tableNew.add(chunk, i + 1, j - 1);
                    continue;
                }
                if (chunk.getText().isBlank() && i == 0) {
                    TextChunk oldHeader = (TextChunk) tableNew.getCell(0, col);
                    TextChunk newHeader = new TextChunk(header.getTextElements());
                    tableNew.add(newHeader, 0, col - 1);
                    oldHeader.getTextElements().clear();
                    blankColNum = j;
                    blankCol = true;
                    continue;
                }
                tableNew.add(chunk, i + 1, j);

            }
        }
        tableNew.add(TextChunk.EMPTY, tableNew.getRowCount(), 0);
        return tableNew;
    }

    public PAF parse(Path source, int pageNum, CSVWriter csvWriter, FileWriter fw, PAFProcessing pafProcessing, IDReport report) throws IOException, ParseException {
        StringBuilder sb = new StringBuilder();
        Table top = parseTop(source.toString(), pageNum);
        Table middle = parseMiddle(source.toString(), pageNum);
        List<Table> bottom = parseBottom(source.toString(), pageNum);
        List<Table> tables = new LinkedList<>();
        tables.add(top);
        tables.add(middle);
        tables.addAll(bottom);
        csvWriter.write(sb, tables);
        fw.write(sb.toString());
        fw.close();
        PAF paf = new PAF(top, middle, bottom);
        pafProcessing.processPAF(paf);
        pafProcessing.transactionIDProcessing(paf, report);
        if (pafProcessing.hasDocs(paf)) {
            paf.setHasDocs(true);
        }
        return paf;
    }

    private List<Table> parseBottom(String path, int pageNum) throws IOException {
        SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
        Page page = getPage(path, pageNum);
        List<Table> tablesExtracted = sea.extract(page);
        List<Table> tables = new LinkedList<>();
        for (Table table : tablesExtracted) {
            Table tableNew = extractHeader(path, pageNum, sea, table, table.y);
            tables.add(tableNew);
        }
        return tables;

    }

    private Table parseMiddle(String path, int pageNum) throws IOException {
        Table tableNew = new Table(this.bea);
        float top = this.stopPosition;
        float bottom = top + MAX_INCREMENT;
        loopbreak:
        while (true) {
            Page page = getAreaFromPage(path, pageNum, top, LEFT, bottom, RIGHT);
            Table table = bea.extract(page).get(0);
            int endRow = table.getRowCount() - 1;
            for (int i = 0; i < table.getColCount(); i++) {
                TextChunk chunk = (TextChunk) table.getCell(endRow, i);
                String text = chunk.getText();
                if (text.equals("Review History")) {
                    this.stopPosition = chunk.y;
                    bottom -= MAX_INCREMENT;
                    break loopbreak;
                }
            }
            bottom += MAX_INCREMENT;
        }
        Page page = getAreaFromPage(path, pageNum, top, LEFT, bottom, RIGHT);
        Table table = bea.extract(page).get(0);
        table.add(TextChunk.EMPTY, table.getRowCount(), 0);
        return table;
    }

    private Table parseTop(String path, int pageNum) throws IOException {
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
                    this.stopPosition = container.y;
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
            top = stopPosition;
            bottom = top + MAX_INCREMENT;
            loopbreak:
            while (true) {
                page = getAreaFromPage(path, pageNum, top, LEFT, bottom, RIGHT);
                table = bea.extract(page).get(0);
                for (int i = 0; i < table.getRowCount(); i++) {
                    for (int j = 0; j < table.getRowCount(); j++) {
                        TextChunk chunk = (TextChunk) table.getCell(i, j);
                        String text = chunk.getText();
                        if (text.contains("Comments")) {
                            break;
                        }
                        if (text.contains("Description") || text.contains("Previous") || text.contains("Approved") || text.contains("Proposed")) {
                            this.stopPosition = chunk.y;
                            bottom -= MAX_INCREMENT;
                            break loopbreak;
                        }
                    }
                }
                bottom += MAX_INCREMENT;
            }
            page = getAreaFromPage(path, pageNum, top, LEFT, bottom, RIGHT);
            table = bea.extract(page).get(0);
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
                    } else if (!(text.isBlank() || text.isEmpty())) {
                        tableNew.add(container, 1, row2col);
                        row2col++;
                    }
                }


            }
        }
        tableNew.add(TextChunk.EMPTY, tableNew.getRowCount(), 0);
        return tableNew;
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
            String[] split = idSplit[0].split(", ");
            formattedName = split[1] + " " + split[0];
        } else {
            String[] split = name.split(", ");
            formattedName = split[1] + " " + split[0];
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
            return oe.extract(pageNumber);
        } finally {
            if (oe != null)
                oe.close();
        }
    }


}
