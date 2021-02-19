package Containers;

import java.util.Date;
import java.util.List;
import java.util.TreeMap;

/**
 * The data structure for the transaction ID report
 *
 * @author Nicholas Curl
 */
public class IDReport {

    /**
     * List of the employee names in the report
     */
    private List<String> employeeNames;
    /**
     * List of the PAF transaction IDs in the report
     */
    private List<Integer> transactionIDs;
    /**
     * List of the effective dates of the PAF in the report
     */
    private List<Date> effectiveDates;
    /**
     * List of the final approved dates of the PAF in the report
     */
    private List<Date> finalApprovedDates;
    /**
     * The map of employee name as the key and the employee code as the value
     */
    private TreeMap<String, String> employeeCodes;
    /**
     * List of the PAF types in the report
     */
    private List<String> pafTypes;
    /**
     * The number of entries in the report
     */
    private int entries;

    /**
     * The constructor for the report
     *
     * @param employeeNames      List containing employee names
     * @param employeeCodes      Map containing employee codes
     * @param transactionIDs     List containing PAF transaction IDs
     * @param effectiveDates     List containing PAF effective dates
     * @param finalApprovedDates List containing PAF final approved dates
     * @param pafTypes           List containing the PAF types
     * @param entries            The number of entries in the report
     */
    public IDReport(List<String> employeeNames, TreeMap<String, String> employeeCodes, List<Integer> transactionIDs, List<Date> effectiveDates, List<Date> finalApprovedDates, List<String> pafTypes, int entries) {
        this.employeeNames = employeeNames;
        this.employeeCodes = employeeCodes;
        this.transactionIDs = transactionIDs;
        this.effectiveDates = effectiveDates;
        this.finalApprovedDates = finalApprovedDates;
        this.pafTypes = pafTypes;
        this.entries = entries;
    }

    /**
     * Get the effective date at the specified index
     *
     * @param index The index to query
     *
     * @return The effective date at the queried index
     */
    public Date getEffectiveDate(int index) {
        return effectiveDates.get(index);
    }

    /**
     * Get the list of effective dates
     *
     * @return The list of effective dates
     */
    public List<Date> getEffectiveDates() {
        return effectiveDates;
    }

    /**
     * Get the employee code associated with the employee name
     *
     * @param employeeName The employee name
     *
     * @return The employee code mapped to the employee name
     */
    public String getEmployeeCode(String employeeName) {
        return employeeCodes.get(employeeName);
    }

    /**
     * Get the map of the employee codes
     *
     * @return The map of employee codes
     */
    public TreeMap<String, String> getEmployeeCodes() {
        return employeeCodes;
    }

    /**
     * Get the employee name at a specified index
     *
     * @param index The index to query
     *
     * @return The employee at the index queried
     */
    public String getEmployeeName(int index) {
        return employeeNames.get(index);
    }

    /**
     * Get the list of employee names
     *
     * @return The list of employee names
     */
    public List<String> getEmployeeNames() {
        return employeeNames;
    }

    /**
     * Get the number of entries in the report
     *
     * @return The number of Entries
     */
    public int getEntries() {
        return entries;
    }

    /**
     * Get the final approved date at a specified index
     *
     * @param index The index to query
     *
     * @return The final approved date at the queried index
     */
    public Date getFinalApprovedDate(int index) {
        return finalApprovedDates.get(index);
    }

    /**
     * Get the list of final approved dates
     *
     * @return The list of final approved dates
     */
    public List<Date> getFinalApprovedDates() {
        return finalApprovedDates;
    }

    /**
     * Get the PAF type at a specified index
     *
     * @param index The index to query
     *
     * @return The PAF type at the queried index
     */
    public String getPafType(int index) {
        return pafTypes.get(index);
    }

    /**
     * Get the list of PAF types
     *
     * @return The list of PAF types
     */
    public List<String> getPafTypes() {
        return pafTypes;
    }

    /**
     * Get the PAF transaction ID at a specified index
     *
     * @param index The index to query
     *
     * @return The PAF transaction ID at the queried index
     */
    public int getTransactionID(int index) {
        return transactionIDs.get(index);
    }

    /**
     * Get the list of PAF transaction IDs
     *
     * @return The list of PAF transaction IDs
     */
    public List<Integer> getTransactionIDs() {
        return transactionIDs;
    }

    /**
     * Removes a row from the report at the specified index
     *
     * @param index The index to remove the row from the report
     */
    public void removeRow(int index) {
        this.employeeNames.remove(index);
        this.transactionIDs.remove(index);
        this.effectiveDates.remove(index);
        this.finalApprovedDates.remove(index);
        this.pafTypes.remove(index);
        this.entries--;
    }
}
