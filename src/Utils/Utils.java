package Utils;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

/**
 * Class of utility functions
 *
 * @author Nicholas Curl
 */
public class Utils {

    /**
     * Constructor for utility functions
     */
    public Utils() {

    }

    /**
     * Deletes the specified directory
     *
     * @param directoryToBeDeleted The directory to be deleted
     *
     * @return True if successfully deleted false otherwise
     */
    public boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    public String extractEmployeeCode(String pafURL) {
        String[] urlSplit = pafURL.split("\\?");
        String[] paramSplit = urlSplit[1].split("&");
        String[] employeeCodeSplit = paramSplit[0].split("=");
        return employeeCodeSplit[1];
    }

    public int extractTransactionID(String pafURL) {
        String[] urlSplit = pafURL.split("\\?");
        String[] paramSplit = urlSplit[1].split("&");
        String[] transactionIDSplit = paramSplit[1].split("=");
        return Integer.parseInt(transactionIDSplit[1]);
    }

    /**
     * Format an employee name
     *
     * @param name The employee name to format
     *
     * @return The formatted employee name
     */
    public String formatName(String name) {
        String formattedName;
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

    public void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public void writeCSV(Path filename, List<List<String>> tableTop, List<List<String>> tableMiddle) throws IOException {
        CSVWriter writer = new CSVWriter(new FileWriter(filename.toFile()));
        List<List<String>> tableTopNew = transposeTable(tableTop);
        List<List<String>> combinedTables = new LinkedList<>(tableTopNew);
        combinedTables.add(createEmptyRow(tableTop.get(0).size()));
        combinedTables.addAll(tableMiddle);
        List<String[]> combinedTablesConverted = convertTable(combinedTables);
        writer.writeAll(combinedTablesConverted);
        writer.close();
    }

    public List<List<String>> transposeTable(List<List<String>> table) {
        List<List<String>> tableNew = new LinkedList<>();
        List<String> rowNew1 = new LinkedList<>();
        List<String> rowNew2 = new LinkedList<>();
        for (List<String> row : table) {
            rowNew1.add(row.get(0));
            rowNew2.add(row.get(1));
        }
        tableNew.add(rowNew1);
        tableNew.add(rowNew2);
        return tableNew;
    }

    private List<String> createEmptyRow(int width) {
        List<String> row = new LinkedList<>();
        for (int i = 0; i < width; i++) {
            row.add("");
        }
        return row;
    }

    public List<String[]> convertTable(List<List<String>> table) {
        List<String[]> tableNew = new LinkedList<>();
        for (List<String> row : table) {
            String[] rowArray = row.toArray(new String[0]);
            tableNew.add(rowArray);
        }
        return tableNew;
    }
}
