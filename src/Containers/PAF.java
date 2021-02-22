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
    private final int transactionId;
    /**
     * The top table
     */
    private final List<List<String>> top;
    /**
     * The middle table
     */
    private final List<List<String>> middle;
    /**
     * List of supporting documents
     */
    private final List<File> supportingDocs;
    /**
     * Does the PAF have supporting documents
     */
    private final boolean hasDocs;


    /**
     * Constructor for PAF without support documents
     *
     * @param transactionId The PAF transaction ID
     * @param top           The top data table
     * @param middle        The middle data table
     */
    public PAF(int transactionId, List<List<String>> top, List<List<String>> middle) {
        this.transactionId = transactionId;
        this.top = top;
        this.middle = middle;
        this.supportingDocs = null;
        this.hasDocs = false;
    }

    /**
     * Constructor for PAF with support documents
     *
     * @param transactionId  The PAF transaction ID
     * @param top            The top data table
     * @param middle         The middle data table
     * @param supportingDocs The list of supporting documents
     */
    public PAF(int transactionId, List<List<String>> top, List<List<String>> middle, List<File> supportingDocs) {
        this.transactionId = transactionId;
        this.top = top;
        this.middle = middle;
        this.supportingDocs = supportingDocs;
        this.hasDocs = true;
    }

    /**
     * Get the middle data table
     *
     * @return The middle data table
     */
    public List<List<String>> getMiddle() {
        return middle;
    }

    /**
     * Get the list of supporting documents
     *
     * @return The list of supporting documents.  Returns empty list if no supporting documents are assigned to PAF.
     */
    public List<File> getSupportingDocs() {
        return supportingDocs;
    }

    /**
     * Get the top data table
     *
     * @return The top data table
     */
    public List<List<String>> getTop() {
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
     * Does the PAF have supporting documents
     *
     * @return True if the PAF has supporting documents, false otherwise.
     */
    public boolean hasDocs() {
        return hasDocs;
    }
}
