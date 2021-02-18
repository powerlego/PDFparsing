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
 * Class that parses a pdf to extract tables
 * @author Nicholas Curl
 */
public class Parsers {
    /**
     * 1 inch in pts
     */
    private static float INCH_POINT = 72.0f;
    /**
     * The width of a tabloid sheet in pts
     */
    private static float pageX = 792.0f;
    /**
     * Constant to specify left margin
     */
    private static float LEFT = INCH_POINT;
    /**
     * The height of a tabloid sheet in pts
     */
    private static float pageY = 1224.0f;
    /**
     * Constant used to expand the area searched
     */
    private static float MAX_INCREMENT = 20.0f;
    /**
     * Constant to specify the right margin
     */
    private static float RIGHT = pageX - INCH_POINT;
    /**
     * The position that a specific string was found at
     */
    private float stopPosition;
    /**
     * Basic Extraction Algorithm
     */
    private BasicExtractionAlgorithm bea;
    /**
     * Boolean to flag which type of comments present
     */
    private boolean commentFlag;

    /**
     * Class Constructor
     */
    public Parsers() {
        this.bea = new BasicExtractionAlgorithm();
        this.stopPosition = 0.0f;
        this.commentFlag = false;
    }


    /**
     * Extracts the headers from a ruled table
     *
     * @param path     path to pdf file input
     * @param pageNum  page number
     * @param sea      spreadsheet extraction algorithm instance
     * @param table    table that is missing the header
     * @param starting the starting position to search for the header
     *
     * @return table with the added header
     *
     * @throws IOException exception for invalid file
     */
    private Table extractHeader(String path, int pageNum, SpreadsheetExtractionAlgorithm sea, Table table, float starting) throws IOException {
        Table tableNew = new Table(sea);
        Page page = getAreaFromPage(path, pageNum, starting - 50.0f, LEFT, starting, RIGHT);
        Table row = this.bea.extract(page).get(0);
        TextChunk header = (TextChunk) row.getCell(0, 0);
        int col;
        int colCount = table.getColCount();
        //Centers the header in the new table
        if ((colCount % 2) == 0) {
            col = Math.floorDiv(table.getColCount() - 1, 2);
        } else {
            col = Math.floorDiv(table.getColCount(), 2);
        }
        int blankColNum = 0;
        boolean blankCol = false;
        tableNew.add(header, 0, col);
        //add data from previous table
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
                //checks to see if there is a blank column
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
        tableNew.add(TextChunk.EMPTY, tableNew.getRowCount(), 0); //add a blank row for spacing
        return tableNew;
    }

    /**
     * Parses the pdf
     * @param source pdf input
     * @param pageNum pdf page number
     * @param csvWriter CSVWriter instance
     * @param fw FileWriter instance
     * @param pafProcessing PAFProcessing instance
     * @param report IDReport Instance
     * @return PAF instance with necessary data
     * @throws IOException exception for invalid file
     * @throws ParseException exception for invalid date
     */
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

    /**
     * Parses the bottom tables of the pdf
     * @param path path of the pdf input
     * @param pageNum the pdf page number
     * @return List of tables that were parsed
     * @throws IOException exception for invalid file
     */
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

    /**
     * Parses the middle table of the pdf
     * @param path path of the pdf input
     * @param pageNum the pdf page number
     * @return The table extracted from the middle of the pdf
     * @throws IOException exception for invalid file
     */
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

    /**
     * Parses the top table of the pdf
     * @param path path of the input pdf
     * @param pageNum the pdf page number
     * @return Table extracted from the top in a two row system
     * @throws IOException exception for invalid file
     */
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

    /**
     * Helper function to extract comments from the pdf
     * @param currRow the row that the comment string was found
     * @param currCol the column that the comment string was found
     * @param row2col the column position in the new table
     * @param table the old table with data
     * @param tableNew the new table that is being filled
     * @return the column number after processing the comments
     */
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

    /**
     * Helper function that extracts the employee name from the pdf
     * @param path path to the input pdf
     * @param pageNum the pdf page number
     * @return the employee name formatted with first name first
     * @throws IOException exception for invalid file
     */
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

    /**
     * Helper function that gets the area of the page to extract the table from
     * @param path path for the input pdf
     * @param page the pdf page number
     * @param top top position the rectangle in pts
     * @param left left position of the rectangle
     * @param bottom bottom position of the rectangle
     * @param right right position of the rectangle
     * @return Area containing data
     * @throws IOException exception for invalid file
     */
    private static Page getAreaFromPage(String path, int page, float top, float left, float bottom, float right) throws IOException {
        return getPage(path, page).getArea(top, left, bottom, right);
    }

    /**
     * Helper function that gets the specified pdf page
     * @param path path for the input pdf
     * @param pageNumber the pdf page number
     * @return the specified page
     * @throws IOException exception for invalid file
     */
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
