package Containers;

import java.util.Date;
import java.util.List;
import java.util.TreeMap;

/**
 * @author Nicholas Curl
 */
public class IDReport {

    private List<String> employeeNames;
    private List<Integer> transactionIDs;
    private List<Date> effectiveDates;
    private List<Date> finalApprovedDates;
    private TreeMap<String, String> employeeCodes;
    private List<String> pafTypes;
    private int entries;

    public IDReport(List<String> employeeNames, TreeMap<String, String> employeeCodes, List<Integer> transactionIDs, List<Date> effectiveDates, List<Date> finalApprovedDates, List<String> pafTypes, int entries) {
        this.employeeNames = employeeNames;
        this.employeeCodes = employeeCodes;
        this.transactionIDs = transactionIDs;
        this.effectiveDates = effectiveDates;
        this.finalApprovedDates = finalApprovedDates;
        this.pafTypes = pafTypes;
        this.entries = entries;
    }

    public Date getEffectiveDate(int index) {
        return effectiveDates.get(index);
    }

    public List<Date> getEffectiveDates() {
        return effectiveDates;
    }

    public String getEmployeeCode(String employeeName) {
        return employeeCodes.get(employeeName);
    }

    public TreeMap<String, String> getEmployeeCodes() {
        return employeeCodes;
    }

    public String getEmployeeName(int index) {
        return employeeNames.get(index);
    }

    public List<String> getEmployeeNames() {
        return employeeNames;
    }

    public int getEntries() {
        return entries;
    }

    public Date getFinalApprovedDate(int index) {
        return finalApprovedDates.get(index);
    }

    public List<Date> getFinalApprovedDates() {
        return finalApprovedDates;
    }

    public String getPafType(int index) {
        return pafTypes.get(index);
    }

    public List<String> getPafTypes() {
        return pafTypes;
    }

    public int getTransactionID(int index) {
        return transactionIDs.get(index);
    }

    public List<Integer> getTransactionIDs() {
        return transactionIDs;
    }
}
