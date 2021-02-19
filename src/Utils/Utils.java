package Utils;

import java.io.File;
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

    /**
     * Format an employee name
     *
     * @param name The employee name to format
     *
     * @return The formatted employee name
     */
    public String formatName(String name) {
        String[] split = name.split(", ");
        String formattedName = split[1] + " " + split[0];
        formattedName = formattedName.toLowerCase();
        formattedName = formattedName.replaceAll(" ", "_");
        return formattedName;
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

}
