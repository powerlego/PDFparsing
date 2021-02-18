package Parsers;

import Containers.IDReport;
import Utils.Utils;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/**
 * @author Nicholas Curl
 */
public class IDReportProcessing {

    private List<List<String>> records;
    private Utils utils = new Utils();

    public IDReportProcessing(File idFile) throws IOException, CsvValidationException {
        this.records = processRecords(idFile);
    }

    private List<List<String>> processRecords(File idPath) throws IOException, CsvValidationException {
        List<List<String>> tempRecords = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader(idPath));) {
            String[] values;
            while ((values = csvReader.readNext()) != null) {
                tempRecords.add(Arrays.asList(values));
            }
        }
        return tempRecords;
    }

    public IDReport processReport() throws ParseException {
        List<String> employeeNames = new LinkedList<>();
        TreeMap<String, String> employeeCodes = new TreeMap<>();
        List<Integer> transactionIDs = new LinkedList<>();
        List<Date> effectiveDates = new LinkedList<>();
        List<Date> finalApprovedDates = new LinkedList<>();
        List<String> pafTypes = new LinkedList<>();
        List<String> headerRow = records.get(0);
        int employeeCol = 0;
        int employeeCodeCol = 0;
        int transactionCol = 0;
        int effectiveCol = 0;
        int finalApprovedCol = 0;
        int pafTypeCol = 0;
        for (int i = 0; i < headerRow.size(); i++) {
            String header = headerRow.get(i);
            if (header.contains("_")) {
                header = header.replaceAll("_", " ");
            }
            header = header.toLowerCase();
            switch (header) {
                case "employee name":
                    employeeCol = i;
                    break;
                case "employee code":
                    employeeCodeCol = i;
                    break;
                case "paf transaction id":
                    transactionCol = i;
                    break;
                case "effective date":
                    effectiveCol = i;
                    break;
                case "final approved date":
                    finalApprovedCol = i;
                    break;
                case "paf type":
                    pafTypeCol = i;
                    break;
                default:
                    break;
            }
        }
        int entries = 0;
        for (int i = 1; i < records.size(); i++) {
            List<String> row = records.get(i);
            employeeNames.add(utils.formatName(row.get(employeeCol).strip()));
            employeeCodes.putIfAbsent(utils.formatName(row.get(employeeCol).strip()), row.get(employeeCodeCol).strip());
            transactionIDs.add(Integer.parseInt(row.get(transactionCol).strip()));
            effectiveDates.add(utils.stringToDate(row.get(effectiveCol).strip()));
            finalApprovedDates.add(utils.stringToDate(row.get(finalApprovedCol).strip()));
            pafTypes.add(row.get(pafTypeCol).strip());
            entries++;
        }
        return new IDReport(employeeNames, employeeCodes, transactionIDs, effectiveDates, finalApprovedDates, pafTypes, entries);
    }

}
