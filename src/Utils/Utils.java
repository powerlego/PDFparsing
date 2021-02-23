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
     * The OS name
     */
    private static final String OS = System.getProperty("os.name").toLowerCase();
    /**
     * Is the OS windows
     */
    public static boolean IS_WINDOWS = (OS.contains("win"));

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

    /**
     * Clears the screen based on OS
     *
     * @throws IOException          If an IO problem occurs
     * @throws InterruptedException If the current thread is interrupted by another thread while it is waiting, then the
     *                              wait is ended and an InterruptedException is thrown.
     */
    public void clearScreen() throws IOException, InterruptedException {
        if (IS_WINDOWS) {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } else {
            System.out.print("\033[H\033[2J");
            System.out.flush();
        }
    }

    /**
     * Extracts the employee code from url
     *
     * @param pafURL The url to extract the employee code
     *
     * @return The employee code
     */
    public String extractEmployeeCode(String pafURL) {
        String[] urlSplit = pafURL.split("\\?");
        String[] paramSplit = urlSplit[1].split("&");
        String[] employeeCodeSplit = paramSplit[0].split("=");
        return employeeCodeSplit[1];
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

    /**
     * Extracts the PAF transaction ID from url
     *
     * @param pafURL The url to extract the PAF transaction ID
     *
     * @return The PAF transaction ID
     */
    public int extractTransactionID(String pafURL) {
        String[] urlSplit = pafURL.split("\\?");
        String[] paramSplit = urlSplit[1].split("&");
        String[] transactionIDSplit = paramSplit[1].split("=");
        return Integer.parseInt(transactionIDSplit[1]);
    }

    /**
     * Delays the program
     *
     * @param milliseconds The duration to delay in milliseconds (1 second = 1000 milliseconds)
     */
    public void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Writes the CSV of the data tables
     *
     * @param filename    The filename of the CSV
     * @param tableTop    The top data table
     * @param tableMiddle The middle data table
     *
     * @throws IOException If an IO problem occurs
     */
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

    /**
     * Transposes a 2 column table
     *
     * @param table The table to transpose
     *
     * @return The transposed table
     */
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

    /**
     * Creates an empty table row of specified width
     *
     * @param width The width of the empty row
     *
     * @return The empty row of specified width
     */
    private List<String> createEmptyRow(int width) {
        List<String> row = new LinkedList<>();
        for (int i = 0; i < width; i++) {
            row.add("");
        }
        return row;
    }

    /**
     * Converts List&lt;List&lt;String&gt;&gt; Table into List&lt;String[]&gt; Table
     *
     * @param table The table to convert
     *
     * @return The converted table
     */
    public List<String[]> convertTable(List<List<String>> table) {
        List<String[]> tableNew = new LinkedList<>();
        for (List<String> row : table) {
            String[] rowArray = row.toArray(new String[0]);
            tableNew.add(rowArray);
        }
        return tableNew;
    }
}
