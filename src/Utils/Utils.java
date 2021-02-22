package Utils;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    /**
     * Find the reports containing the report name at the specified location
     *
     * @param reportName     The report name to search for
     * @param reportLocation The location to search the report in
     *
     * @return List of reports containing the specified name
     */
    public List<File> findReports(String reportName, File reportLocation) {
        List<File> reports = new LinkedList<>();
        File[] list = reportLocation.listFiles();
        if (list != null) {
            for (File fil : list) {
                if (fil.isDirectory()) {
                    List<File> dirReports = findReports(reportName, fil);
                    reports.addAll(dirReports);
                } else if (fil.getName().contains(reportName)) {
                    reports.add(fil);
                }
            }
        }
        return reports;
    }

    public String encodeString(String string) {
        return URLEncoder.encode(string, StandardCharsets.UTF_8);
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
     * Formats string to use in comparisons
     *
     * @param string The string to format
     *
     * @return The formatted string
     */
    public String formatString(String string) {
        if (string.contains("\r")) {
            string = string.replaceAll("\r", " ");
        }
        string = string.strip();
        string = string.toLowerCase();
        return string;
    }

    /**
     * Converts the date string into Date instance
     *
     * @param date The date string
     *
     * @return The converted date string
     *
     * @throws ParseException Exception for invalid date parsing
     */
    public Date stringToDate(String date) throws ParseException {
        return new SimpleDateFormat("MM/dd/yyyy").parse(date);
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

    public List<String[]> transposeTable(List<List<String>> table) {
        List<String[]> tableNew = new LinkedList<>();
        List<String> rowNew1 = new LinkedList<>();
        List<String> rowNew2 = new LinkedList<>();
        for (List<String> row : table) {
            rowNew1.add(row.get(0));
            rowNew2.add(row.get(1));
        }
        String[] row1Array = (String[]) rowNew1.toArray();
        String[] row2Array = (String[]) rowNew2.toArray();
        tableNew.add(row1Array);
        tableNew.add(row2Array);
        return tableNew;
    }
    /*public String writeCSV() throws IOException {
        CSVWriter writer = new CSVWriter(new FileWriter("./"));


    }*/
}
