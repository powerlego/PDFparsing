package Utils;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Nicholas Curl
 */
public class Utils {

    public Utils() {

    }

    public boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

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

    public String formatName(String name) {
        String[] split = name.split(", ");
        String formattedName = split[1] + " " + split[0];
        formattedName = formattedName.toLowerCase();
        formattedName = formattedName.replaceAll(" ", "_");
        return formattedName;
    }

    public String formatString(String string) {
        if (string.contains("\r")) {
            string = string.replaceAll("\r", " ");
        }
        string = string.strip();
        string = string.toLowerCase();
        return string;
    }

    public Date stringToDate(String date) throws ParseException {
        return new SimpleDateFormat("MM/dd/yyyy").parse(date);
    }

}
