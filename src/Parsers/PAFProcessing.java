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
 * @author Nicholas Curl
 */
public class PAFProcessing {

    private Utils utils = new Utils();

    public PAFProcessing() {
    }

    boolean hasDocs(PAF paf) {
        List<Table> bottom = paf.getBottom();
        boolean docs = false;
        if (bottom.size() > 1) {
            Table transactionHistory = bottom.get(1);
            List<RectangularTextContainer> headerRow = transactionHistory.getRows().get(1);
            int fieldChangeCol = 0;
            for (int i = 0; i < headerRow.size(); i++) {
                String text = utils.formatString(headerRow.get(i).getText());
                if (text.equals("field changed")) {
                    fieldChangeCol = i;
                    break;
                }
            }
            for (int i = 1; i < transactionHistory.getRowCount(); i++) {
                String text = utils.formatString(transactionHistory.getCell(i, fieldChangeCol).getText());
                if (text.contains("file uploaded")) {
                    docs = true;
                    break;
                }
            }
        }
        return docs;
    }

    void processPAF(PAF paf) throws ParseException {
        Table top = paf.getTop();
        List<RectangularTextContainer> topRow = top.getRows().get(0);
        for (int i = 0; i < topRow.size(); i++) {
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
        for (int i = 0; i < topRow.size(); i++) {
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
        for (int i = 0; i < reviewTable.getRowCount(); i++) {
            String text = reviewTable.getCell(i, colAction).getText();
            if (text.contains("\r")) {
                text = text.replaceAll("\r", " ");
            }
            if (text.equals("Final Approved") || text.equals("Final Approval")) {
                actionTime = reviewTable.getCell(i, colActionTime).getText();
                break;
            }
        }
        if (actionTime.contains("\r")) {
            actionTime = actionTime.replaceAll("\r", " ");
        }
        actionTime = actionTime.strip();
        String[] split = actionTime.split(" ");
        paf.setFinalApprovedDate(split[0]);
    }

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
            if (reportEmployeeName.equals(pafEmployeeName) && reportEffectiveDate.equals(pafEffectiveDate) && reportFinalDate.equals(pafFinalDate) && reportPAFType.equals(pafType)) {
                paf.setTransactionId(report.getTransactionID(i));
                break;
            }
        }
    }

}
