package Scraping.Dashboard;

import Containers.Employees;
import Containers.PAF;
import Utils.Utils;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.*;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

/**
 * Processes the PAF dashboard
 *
 * @author Nicholas Curl
 */
public class DashboardProcessing {

    /**
     * Utils instance
     */
    private final Utils utils = new Utils();

    /**
     * Constructor for this class
     */
    public DashboardProcessing() {

    }

    /**
     * Function that processes the PAF dashboard
     *
     * @param client       The web client
     * @param saveLocation The location to save the processed PAFs
     * @param employees    The map of the employees
     *
     * @throws IOException if an IO problem occurs
     */
    public void finalDashboard(WebClient client, Path saveLocation, Employees employees) throws IOException {
        //navigate to the PAF dashboard
        HtmlPage pafDashboard = client.getPage("https://www.paycomonline.net/v4/cl/web.php/paf/dashboard");
        client.waitForBackgroundJavaScript(2000);
        //select the appropriate filter
        pafDashboard = selectFilter(client, pafDashboard);
        client.waitForBackgroundJavaScript(2000);
        //go to the final approved tab
        HtmlAnchor anchor = pafDashboard.getAnchorByHref("#final-approved-tab");
        HtmlSelect select = pafDashboard.getElementByName("finaltable_length");
        HtmlOption option = select.getOptionByValue("500");
        option.setSelected(true);
        client.waitForBackgroundJavaScript(2000);
        anchor.setAttribute("aria-selected", "true");
        HtmlTable finaltable = (HtmlTable) pafDashboard.getElementById("finaltable");
        List<String> pafs = new LinkedList<>();
        //get the PAFs based on filter
        for (HtmlTableRow row : finaltable.getRows()) {
            DomNodeList<HtmlElement> anchors = row.getElementsByTagName("a");
            if (!(anchors.isEmpty())) {
                HtmlAnchor htmlAnchor = (HtmlAnchor) anchors.get(0);
                String href = htmlAnchor.getHrefAttribute();
                pafs.add(href);
            }
        }
        //process the PAFs
        ProgressBarBuilder pbb = new ProgressBarBuilder().setStyle(ProgressBarStyle.ASCII).setTaskName("Processing PAFs");
        for (String url : ProgressBar.wrap(pafs, pbb)) {
            HtmlPage paf = client.getPage(url);
            processPAFPage(client, paf, saveLocation, employees);
        }
    }

    /**
     * Selects the filter needed to grab the PAFs
     *
     * @param client       The Webclient
     * @param pafDashboard The HtmlPage of the PAF dashboard
     *
     * @return The HtmlPage after the filter is selected
     *
     * @throws IOException if an IO problem occurs
     */
    private HtmlPage selectFilter(WebClient client, HtmlPage pafDashboard) throws IOException {
        //checks to see if the PAF filter is present
        List<HtmlListItem> items = navigateFilters(client, pafDashboard);
        if (items.isEmpty()) {
            //copies the filter
            HtmlPage page = copyFilter(client);
            client.waitForBackgroundJavaScript(2000);
            try {
                page.getAnchorByText("PAF Filter");
            } catch (ElementNotFoundException e) {
                System.out.println("Unable to copy Filter");
                System.out.println(e.toString());
                System.exit(-1);
            }
            items = navigateFilters(client, pafDashboard);
        }
        HtmlListItem item = items.get(0);
        //selects the filter
        return item.click();
    }

    /**
     * Process the PAF and saves the necessary data
     *
     * @param client       The Webclient
     * @param pafPage      The HtmlPage of the PAF
     * @param saveLocation The path location to save the parsed PAF
     * @param employees    The map of employees
     *
     * @throws IOException if an IO problem occurs
     */
    private void processPAFPage(WebClient client, HtmlPage pafPage, Path saveLocation, Employees employees) throws IOException {
        client.waitForBackgroundJavaScript(2000);
        String employeeCode = utils.extractEmployeeCode(pafPage.getUrl().toString());
        int transactionID = utils.extractTransactionID(pafPage.getUrl().toString());
        //newer PAF
        if (pafPage.getUrl().toString().contains("paf-custemployee.php")) {
            HtmlSpan employeeNameContainer = (HtmlSpan) pafPage.getByXPath("//span[contains(@class,'cardDetailEmployeeFieldText')]").get(0);
            String employeeName = utils.formatName(employeeNameContainer.getTextContent());
            HtmlImage employeePhotoContainer = (HtmlImage) pafPage.getByXPath("//div[contains(@class, 'cardPhoto')]//img").get(0);
            Path employeeFolder = addEmployee(employees, employeeName, employeeCode, saveLocation);
            File employeePhoto = employeeFolder.resolve("employee_photo.jpg").toFile();
            //saves the employee photo if it exists
            if (!employeePhoto.exists()) {
                employeePhotoContainer.saveAs(employeePhoto);
                employees.setEmployeePhoto(employeeCode, employeePhoto);
            }
            HtmlTable table = (HtmlTable) pafPage.getElementById("tblempinfo");
            List<HtmlTableRow> rows = table.getRows();
            List<List<String>> tableTop = new LinkedList<>();
            List<List<String>> tableMiddle = new LinkedList<>();
            //convert htmlTable into usable variable
            for (HtmlTableRow row : rows) {
                List<String> tableTopRow = new LinkedList<>();
                List<String> tableMiddleRow = new LinkedList<>();
                for (int i = 0; i < row.getCells().size(); i++) {
                    HtmlTableCell cell = row.getCell(i);
                    switch (i) {
                        case 0:
                        case 1:
                            if (!(cell instanceof HtmlTableHeaderCell)) {
                                tableTopRow.add(cell.getTextContent());
                            }
                            break;
                        case 2:
                        case 3:
                        case 4:
                            tableMiddleRow.add(cell.getTextContent());
                            break;
                        default:
                            break;
                    }
                }
                if (!tableTopRow.isEmpty()) {
                    tableTop.add(tableTopRow);
                }
                if (!tableMiddleRow.isEmpty()) {
                    tableMiddle.add(tableMiddleRow);
                }
            }
            List<HtmlAnchor> elements = pafPage.getByXPath("//div[contains(@class, 'row formRowStandard')]//div[contains(@class,'formLine')]//a");
            PAF paf = saveFiles(transactionID, tableTop, tableMiddle, employeeFolder, elements);
            employees.addPAF(employeeCode, paf);
            writePAFCSV(tableTop, tableMiddle, employeeFolder, transactionID);
        }
        //older PAF
        else {
            HtmlTable table = (HtmlTable) pafPage.getElementsByTagName("table").get(1);
            List<List<String>> tableTemp = new LinkedList<>();
            for (HtmlTableRow row : table.getRows()) {
                List<String> tempRow = new LinkedList<>();
                if (row.asText().strip().isBlank()) {
                    tempRow.add("Blank");
                    tableTemp.add(tempRow);
                    continue;
                }
                for (int i = 0; i < row.getCells().size(); i++) {
                    HtmlTableCell cell = row.getCell(i);
                    tempRow.add(cell.asText().strip());
                }
                tableTemp.add(tempRow);
            }
            List<List<String>> tableTop = new LinkedList<>();
            List<List<String>> tableMiddle = new LinkedList<>();
            findTopField("Organization Name", tableTemp, tableTop);
            findTopField("Requested By", tableTemp, tableTop);
            getFinalApprovedBy(tableTemp, tableTop);
            processTable(tableTemp, tableTop, tableMiddle);
            String employeeName = utils.formatName(findEmployeeName(tableTemp).strip());
            Path employeeFolder = addEmployee(employees, employeeName, employeeCode, saveLocation);
            List<HtmlAnchor> files = getSupportingDocs(table, tableTemp);
            PAF paf = saveFiles(transactionID, tableTop, tableMiddle, employeeFolder, files);
            employees.addPAF(employeeCode, paf);
            writePAFCSV(tableTop, tableMiddle, employeeFolder, transactionID);
        }
    }

    /**
     * Navigates the filter selection menu to determine if necessary filter is present
     *
     * @param client       The Webclient
     * @param pafDashboard The HtmlPage of the PAF dashboard
     *
     * @return The elements that match the PAF filter
     *
     * @throws IOException if an IO problem occurs
     */
    private List<HtmlListItem> navigateFilters(WebClient client, HtmlPage pafDashboard) throws IOException {
        HtmlDivision div = (HtmlDivision) pafDashboard.getByXPath("//div[contains(@class, 'largeSelectFilterDropDown employeeTypeFilter')]").get(0);
        String id = div.getAttribute("id");
        HtmlImage selector = (HtmlImage) pafDashboard.getByXPath("//div[contains(@id, '" + id + "')]//div[contains(@class,'view-large-select-input')]//img").get(0);
        selector.click();
        client.waitForBackgroundJavaScript(500);
        return pafDashboard.getByXPath("//div[contains(@id, 'view-large-select-list-" + id + "')]//li[text()[contains(.,'PAF Filter')]]");
    }

    /**
     * Copies the necessary filter
     *
     * @param client The Webclient
     *
     * @return The HtmlPage after copying the filter
     *
     * @throws IOException if an IO problem occurs
     */
    private HtmlPage copyFilter(WebClient client) throws IOException {
        return client.getPage("https://www.paycomonline.net/v4/cl/web.php/filter/advanced/copy/183575");
    }

    /**
     * Adds an employee to the map of employees
     *
     * @param employees    The map of employees
     * @param employeeName The employee name
     * @param employeeCode The employee code
     * @param saveLocation The location to save the parsed PAF
     *
     * @return The folder unique to the employee
     *
     * @throws IOException if an IO problem occurs
     */
    private Path addEmployee(Employees employees, String employeeName, String employeeCode, Path saveLocation) throws IOException {
        //adds the employee to the map of employees
        boolean added = employees.addEmployee(employeeName, employeeCode);
        Path employeeFolder;
        //creates the employee's folder if necessary
        if (added) {
            employeeFolder = saveLocation.resolve(employeeName);
            if (!(employeeFolder.toFile().exists())) {
                Files.createDirectories(employeeFolder);
            }
            employees.setEmployeeFileLocation(employeeCode, employeeFolder);
        } else {
            employeeFolder = employees.getEmployeeFileLocation(employeeCode);
        }
        return employeeFolder;
    }

    /**
     * Saves supporting documents and creates the necessary PAF instance
     *
     * @param transactionID  The PAF transaction ID
     * @param tableTop       The top data table
     * @param tableMiddle    The middle data table
     * @param employeeFolder The folder of the employee
     * @param elements       The supporting document elements
     *
     * @return The PAF Instance created
     *
     * @throws IOException if an IO problem occurs
     */
    private PAF saveFiles(int transactionID, List<List<String>> tableTop, List<List<String>> tableMiddle, Path employeeFolder, List<HtmlAnchor> elements) throws IOException {
        PAF paf;
        //checks to see if there are any supporting documents
        if (elements.isEmpty()) {
            paf = new PAF(transactionID, tableTop, tableMiddle);
        } else {
            List<File> supportingDocs = new LinkedList<>();
            //Creates a supporting documents folder inside the employee's folder
            Path supportDocs = employeeFolder.resolve("supporting_docs/");
            Files.createDirectories(supportDocs);
            //Iterates through all of the elements containing the supporting documents
            for (HtmlAnchor downloadAnchor : elements) {
                UnexpectedPage page = downloadAnchor.click();
                WebResponse response = page.getWebResponse();
                //Grab the file name
                String[] headerSplit = response.getResponseHeaderValue("Content-Disposition").split(";");
                String temp = headerSplit[1].strip();
                String[] splitTemp = temp.split("\"");
                String filename = splitTemp[1];
                //Download and save file
                File downloadFile = supportDocs.resolve(filename).toFile();
                InputStream contentAsStream = response.getContentAsStream();
                FileOutputStream out = new FileOutputStream(downloadFile);
                IOUtils.copy(contentAsStream, out);
                out.close();
                supportingDocs.add(downloadFile);
            }
            paf = new PAF(transactionID, tableTop, tableMiddle, supportingDocs);
        }
        return paf;
    }

    /**
     * Writes the CSV of the PAF
     *
     * @param tableTop       The top data table
     * @param tableMiddle    The middle data table
     * @param employeeFolder The folder of the employee to save the PAF
     * @param transactionID  The PAF transaction ID
     *
     * @throws IOException if an IO problem occurs
     */
    private void writePAFCSV(List<List<String>> tableTop, List<List<String>> tableMiddle, Path employeeFolder, int transactionID) throws IOException {
        //Creates a folder for the PAFs if it does not exist
        Path pafFolder = employeeFolder.resolve("pafs/");
        Files.createDirectories(pafFolder);
        Path pafFile = pafFolder.resolve(transactionID + ".csv");
        utils.writeCSV(pafFile, tableTop, tableMiddle);
    }

    /**
     * Finds the fields found in the top data table
     *
     * @param field       The field to search for
     * @param searchTable The table to search for the field
     * @param destTable   The table to store the found field and its data
     */
    private void findTopField(String field, List<List<String>> searchTable, List<List<String>> destTable) {
        loopbreak:
        for (List<String> row : searchTable) {
            for (String cell : row) {
                if (cell.equalsIgnoreCase(field)) {
                    destTable.add(row);
                    break loopbreak;
                }
            }
        }
    }

    /**
     * Gets and stores the name of the person who final approved the PAF
     *
     * @param searchTable The table to search in
     * @param destTable   The table to store the name
     */
    private void getFinalApprovedBy(List<List<String>> searchTable, List<List<String>> destTable) {
        List<String> approvedBy = new LinkedList<>();
        approvedBy.add("Final Approved By");
        loopbreak:
        for (List<String> row : searchTable) {
            for (int j = 0; j < row.size(); j++) {
                String cell = row.get(j).strip();
                if (cell.equalsIgnoreCase("Final Approval")) {
                    approvedBy.add(row.get(j - 1).strip());
                    break loopbreak;
                }
            }
        }
        destTable.add(approvedBy);
    }

    /**
     * Processes the search table into its individual table components
     *
     * @param searchTable The search table
     * @param tableTop    The top data table
     * @param tableMiddle The middle data table
     */
    private void processTable(List<List<String>> searchTable, List<List<String>> tableTop, List<List<String>> tableMiddle) {
        //Get the bounds of data extraction
        int supportDocRowStart = findSupportingDocRowStart(searchTable);
        int startingRow = findStartingRow(searchTable);
        //Iterate through the cells within the bounds
        for (int i = startingRow; i < supportDocRowStart; i++) {
            List<String> row = searchTable.get(i);
            if (row.get(0).equalsIgnoreCase("Blank")) {
                continue;
            }
            List<String> topRow = new LinkedList<>();
            List<String> middleRow = new LinkedList<>();
            if (row.size() < 5) {
                for (int j = 1; j < row.size(); j++) {
                    String cell = row.get(j).strip();
                    middleRow.add(cell);
                }
            } else {
                if (!row.get(0).isBlank()) {
                    for (int j = 0; j < 2; j++) {
                        String cell = row.get(j);
                        if (i == startingRow) {
                            break;
                        } else if (cell.equalsIgnoreCase("Employee Name")) {
                            break;
                        } else if (cell.equalsIgnoreCase("Hire Date")) {
                            break;
                        }
                        topRow.add(cell);
                    }
                }
                for (int j = 2; j < 5; j++) {
                    String cell = row.get(j);
                    middleRow.add(cell);
                }
            }
            if (!topRow.isEmpty()) {
                tableTop.add(topRow);
            }
            if (!middleRow.isEmpty()) {
                tableMiddle.add(middleRow);
            }
        }
    }

    /**
     * Find the employee name in the search table
     *
     * @param searchTable The table to search for the employee name
     *
     * @return The employee name
     */
    private String findEmployeeName(List<List<String>> searchTable) {
        String name = "";
        for (List<String> row : searchTable) {
            String cell = row.get(0);
            if (cell.equalsIgnoreCase("Employee Name")) {
                name = row.get(1);
                break;
            }
        }
        return name;
    }

    /**
     * Get the elements containing the supporting documents
     *
     * @param table       The HtmlTable to grab the HtmlAnchors
     * @param searchTable The table to search
     *
     * @return The list of HtmlAnchors that contain the supporting documents. Empty list if no supporting documents are
     *         found.
     */
    private List<HtmlAnchor> getSupportingDocs(HtmlTable table, List<List<String>> searchTable) {
        List<HtmlAnchor> files = new LinkedList<>();
        int supportDocRowStart = findSupportingDocRowStart(searchTable) + 1;
        int supportDocRowEnd = findSupportDocEndRow(searchTable);
        for (int i = supportDocRowStart; i < supportDocRowEnd; i++) {
            HtmlTableRow row = table.getRow(i);
            if (row.asText().equalsIgnoreCase("No Document Uploaded")) {
                break;
            }
            DomNodeList<HtmlElement> elements = table.getElementsByTagName("a");
            HtmlAnchor anchor = (HtmlAnchor) elements.get(0);
            files.add(anchor);
        }
        return files;
    }

    /**
     * Find the starting row of the supporting documents
     *
     * @param searchTable The table to search in
     *
     * @return The row that specifies the beginning of the supporting documentation
     */
    private int findSupportingDocRowStart(List<List<String>> searchTable) {
        int rowNum = -1;
        loopbreak:
        for (int i = 0; i < searchTable.size(); i++) {
            List<String> row = searchTable.get(i);
            for (String cell : row) {
                if (cell.equalsIgnoreCase("Supporting Documentation")) {
                    rowNum = i;
                    break loopbreak;
                }
            }
        }
        return rowNum;
    }

    /**
     * Find the starting row of the table to extract from
     *
     * @param searchTable The table to search in
     *
     * @return The row that specifies the beginning of the extraction table
     */
    private int findStartingRow(List<List<String>> searchTable) {
        int rowNum = -1;
        loopbreak:
        for (int i = 0; i < searchTable.size(); i++) {
            List<String> row = searchTable.get(i);
            for (String cell : row) {
                if (cell.equalsIgnoreCase("Description")) {
                    rowNum = i;
                    break loopbreak;
                }
            }
        }
        return rowNum;
    }

    /**
     * Finds the end row for the supporting documentation
     *
     * @param searchTable The table to search in
     *
     * @return The row that specifies the end of the supporting documentation
     */
    private int findSupportDocEndRow(List<List<String>> searchTable) {
        int rowNum = -1;
        loopbreak:
        for (int i = 0; i < searchTable.size(); i++) {
            List<String> row = searchTable.get(i);
            for (String cell : row) {
                if (cell.equalsIgnoreCase("Review History")) {
                    rowNum = i;
                    break loopbreak;
                }
            }
        }
        return rowNum;
    }
}
