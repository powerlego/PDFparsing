package Parsers;

import Containers.IDReport;
import Containers.PAF;
import Utils.Utils;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;
import technology.tabula.TextChunk;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * The class to process the PAF
 * @author Nicholas Curl
 */
public class PAFProcessing {

    /**
     * Utils instance
     */
    private Utils utils = new Utils();

    /**
     * Constructor for processing the PAF
     */
    public PAFProcessing() {
    }

    /**
     * Checks to see if the PAF has supporting documents
     *
     * @param paf The PAF to check
     *
     * @return True if documents are present for this PAF false otherwise
     */
    boolean hasDocs(PAF paf) {
        List<Table> bottom = paf.getBottom();
        boolean docs = false;
        if (bottom.size() > 1) {
            Table transactionHistory = bottom.get(1);
            List<RectangularTextContainer> headerRow = transactionHistory.getRows().get(1);
            int fieldChangeCol = 0;
            for (int i = 0; i < headerRow.size(); i++) { //searches for the field changed column
                String text = utils.formatString(headerRow.get(i).getText());
                if (text.equals("field changed")) {
                    fieldChangeCol = i;
                    break;
                }
            }
            for (int i = 1; i < transactionHistory.getRowCount(); i++) { //searches the field changed column for file uploaded, specifying that documents are present
                String text = utils.formatString(transactionHistory.getCell(i, fieldChangeCol).getText());
                if (text.contains("file uploaded")) {
                    docs = true;
                    break;
                }
            }
        }
        return docs;
    }

    /**
     * Processes the PAF to extract necessary data
     * @param paf The paf to process
     * @throws ParseException Exception for invalid date parsing
     */
    void processPAF(PAF paf) throws ParseException {
        Table top = paf.getTop();
        List<RectangularTextContainer> topRow = top.getRows().get(0);
        for (int i = 0; i < topRow.size(); i++) { //Parses through the top table to grab the necessary data
            TextChunk chunk = (TextChunk) topRow.get(i);
            switch (chunk.getText()) {
                case "Employee Name:":
                    paf.setEmployeeName(utils.formatName(top.getCell(1, i).getText()));
                    break;
                case "Effective Date:":
                    paf.setEffectiveDate(top.getCell(1, i).getText());
                    break;
                case "PAF Type :":
                case "Action Type:":
                    paf.setPafType(top.getCell(1, i).getText());
                    break;
                default:
                    break;
            }
        }
        List<Table> bottom = paf.getBottom();
        Table reviewTable = bottom.get(0);
        topRow = reviewTable.getRows().get(1);
        int colAction = 0;
        int colActionTime = 0;
        for (int i = 0; i < topRow.size(); i++) { //Parses through the first bottom table for the specified columns
            String text = topRow.get(i).getText();
            if (text.contains("\r")) {
                text = text.replaceAll("\r", " ");
            }
            if (text.equals("Action")) {
                colAction = i;
            }
            if (text.equals("Action Time")) {
                colActionTime = i;
            }
        }
        String actionTime = "";
        for (int i = 0; i < reviewTable.getRowCount(); i++) { //finds the date of final approval
            String text = reviewTable.getCell(i, colAction).getText();
            if (text.contains("\r")) {
                text = text.replaceAll("\r", " ");
            }
            if (text.equals("Final Approved") || text.equals("Final Approval")) {
                actionTime = reviewTable.getCell(i, colActionTime).getText();
                break;
            }
        }
        if (actionTime.contains("\r")) {//formats the date string
            actionTime = actionTime.replaceAll("\r", " ");
        }
        actionTime = actionTime.strip();
        String[] split = actionTime.split(" ");
        paf.setFinalApprovedDate(split[0]);
    }

    /**
     * Processes the PAF transaction ID report to assign the correct transaction ID to the specified PAF
     * @param paf The PAF to assign transaction ID
     * @param report The transaction ID report
     */
    void transactionIDProcessing(PAF paf, IDReport report) {
        String pafEmployeeName = paf.getEmployeeName();
        Date pafEffectiveDate = paf.getEffectiveDate();
        Date pafFinalDate = paf.getFinalApprovedDate();
        String pafType = paf.getPafType();
        for (int i = 0; i < report.getEntries(); i++) {
            String reportEmployeeName = report.getEmployeeName(i);
            Date reportEffectiveDate = report.getEffectiveDate(i);
            Date reportFinalDate = report.getFinalApprovedDate(i);
            String reportPAFType = report.getPafType(i);
            if (reportEmployeeName.equals(pafEmployeeName) && reportEffectiveDate.equals(pafEffectiveDate) && reportFinalDate.equals(pafFinalDate) && reportPAFType.equals(pafType)) { //assign transaction ID if all conditions are true
                paf.setTransactionId(report.getTransactionID(i));
                report.removeRow(i); //remove the found row from the report to help speed up processing
                break;
            }
        }
    }

}
