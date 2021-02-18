package Containers;

import technology.tabula.Table;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author Nicholas Curl
 */
public class PAF {

    private int transactionId;
    private Table top;
    private Table middle;
    private List<Table> bottom;
    private String employeeName;
    private Date effectiveDate;
    private Date finalApprovedDate;
    private String pafType;
    private boolean hasDocs;


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

    public List<Table> getBottom() {
        return bottom;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public void setEffectiveDate(String effectiveDate) throws ParseException {
        this.effectiveDate = new SimpleDateFormat("MM/dd/yyyy").parse(effectiveDate);
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public Date getFinalApprovedDate() {
        return finalApprovedDate;
    }

    public void setFinalApprovedDate(Date finalApprovedDate) {
        this.finalApprovedDate = finalApprovedDate;
    }

    public void setFinalApprovedDate(String finalApprovedDate) throws ParseException {
        this.finalApprovedDate = new SimpleDateFormat("MM/dd/yyyy").parse(finalApprovedDate);
    }

    public Table getMiddle() {
        return middle;
    }

    public String getPafType() {
        return pafType;
    }

    public void setPafType(String pafType) {
        this.pafType = pafType;
    }

    public Table getTop() {
        return top;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public boolean hasDocs() {
        return hasDocs;
    }

    public void setHasDocs(boolean hasDocs) {
        this.hasDocs = hasDocs;
    }
}
