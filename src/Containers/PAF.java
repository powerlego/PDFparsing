package Containers;

import java.io.File;
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
    private List<List<String>> top;
    /**
     * The middle table
     */
    private List<List<String>> middle;
    private List<File> supportingDocs;
    private boolean hasDocs;


    public PAF(int transactionId, List<List<String>> top, List<List<String>> middle) {
        this.transactionId = transactionId;
        this.top = top;
        this.middle = middle;
        this.supportingDocs = null;
        this.hasDocs = false;
    }

    public PAF(int transactionId, List<List<String>> top, List<List<String>> middle, List<File> supportingDocs) {
        this.transactionId = transactionId;
        this.top = top;
        this.middle = middle;
        this.supportingDocs = supportingDocs;
        this.hasDocs = true;
    }

    public List<List<String>> getMiddle() {
        return middle;
    }

    public List<File> getSupportingDocs() {
        return supportingDocs;
    }

    public List<List<String>> getTop() {
        return top;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public boolean hasDocs() {
        return hasDocs;
    }
}
