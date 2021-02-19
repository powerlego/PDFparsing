package Containers;

import technology.tabula.Table;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * The data structure for a PAF
 *
 * @author Nicholas Curl
 */
public class PAF {

    /**
     * The transaction ID
     */
    private int transactionId;
    /**
     * The top table
     */
    private Table top;
    /**
     * The middle table
     */
    private Table middle;
    /**
     * List of bottom tables
     */
    private List<Table> bottom;
    /**
     * The employee name listed on the PAF
     */
    private String employeeName;
    /**
     * The effective date listed on the PAF
     */
    private Date effectiveDate;
    /**
     * The final approved date listed on the PAF
     */
    private Date finalApprovedDate;
    /**
     * The PAF type listed on the PAF
     */
    private String pafType;
    /**
     * Flag for signaling that this PAF has supporting documents
     */
    private boolean hasDocs;

    /**
     * The constructor for the PAF
     *
     * @param top    The table parsed at the top of the pdf
     * @param middle The table parsed in the middle of the pdf
     * @param bottom The tables parsed at the bottom of the pdf
     */
    public PAF(Table top, Table middle, List<Table> bottom) {
        this.transactionId = 0;
        this.top = top;
        this.middle = middle;
        this.bottom = bottom;
        this.effectiveDate = null;
        this.finalApprovedDate = null;
        this.employeeName = "";
        this.pafType = "";
        this.hasDocs = false;
    }

    /**
     * Get the list of bottom tables
     *
     * @return The list of bottom tables
     */
    public List<Table> getBottom() {
        return bottom;
    }

    /**
     * Get the effective date
     *
     * @return The effective date
     */
    public Date getEffectiveDate() {
        return effectiveDate;
    }

    /**
     * Sets the PAF effective date
     *
     * @param effectiveDate The effective date as an instance of Date
     */
    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    /**
     * Sets the PAF effective date
     *
     * @param effectiveDate The effective date as a string
     *
     * @throws ParseException Exception for invalid date parsing
     */
    public void setEffectiveDate(String effectiveDate) throws ParseException {
        this.effectiveDate = new SimpleDateFormat("MM/dd/yyyy").parse(effectiveDate);
    }

    /**
     * Get the employee name
     *
     * @return The employee name
     */
    public String getEmployeeName() {
        return employeeName;
    }

    /**
     * Sets the employee name listed on the PAF
     *
     * @param employeeName The employee name to set
     */
    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    /**
     * Get the final approved date
     *
     * @return The final approved date
     */
    public Date getFinalApprovedDate() {
        return finalApprovedDate;
    }

    /**
     * Sets the PAF final approved date
     *
     * @param finalApprovedDate The final approved date as an instance of Date
     */
    public void setFinalApprovedDate(Date finalApprovedDate) {
        this.finalApprovedDate = finalApprovedDate;
    }

    /**
     * Sets the PAF final approved date
     *
     * @param finalApprovedDate The final approved date as a string
     *
     * @throws ParseException Exception for invalid date parsing
     */
    public void setFinalApprovedDate(String finalApprovedDate) throws ParseException {
        this.finalApprovedDate = new SimpleDateFormat("MM/dd/yyyy").parse(finalApprovedDate);
    }

    /**
     * Get the middle table
     *
     * @return The middle table
     */
    public Table getMiddle() {
        return middle;
    }

    /**
     * Get the PAF type
     *
     * @return The PAF type
     */
    public String getPafType() {
        return pafType;
    }

    /**
     * Sets the PAF type listed on the PAF
     *
     * @param pafType The PAF type to set
     */
    public void setPafType(String pafType) {
        this.pafType = pafType;
    }

    /**
     * Get the top table
     *
     * @return The top table
     */
    public Table getTop() {
        return top;
    }

    /**
     * Get the PAF transaction ID
     *
     * @return The PAF transaction ID
     */
    public int getTransactionId() {
        return transactionId;
    }

    /**
     * Sets the PAF transaction ID
     *
     * @param transactionId The PAF transaction ID to set
     */
    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    /**
     * Is the PAF flagged for having supporting documents
     *
     * @return True if PAF has supporting documents false otherwise
     */
    public boolean hasDocs() {
        return hasDocs;
    }

    /**
     * Sets the PAF to have supporting documents
     *
     * @param hasDocs The value to flag
     */
    public void setHasDocs(boolean hasDocs) {
        this.hasDocs = hasDocs;
    }
}
