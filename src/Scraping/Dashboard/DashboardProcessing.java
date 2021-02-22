package Scraping.Dashboard;

import Containers.Employees;
import Containers.PAF;
import Utils.Utils;
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
 * @author Nicholas Curl
 */
public class DashboardProcessing {
    private final Utils utils = new Utils();

    public DashboardProcessing() {

    }

    public void finalDashboard(WebClient client, Path saveLocation, Employees employees) throws IOException {
        HtmlPage pafDashboard = client.getPage("https://www.paycomonline.net/v4/cl/web.php/paf/dashboard");
        client.waitForBackgroundJavaScript(2000);
        HtmlAnchor anchor = pafDashboard.getAnchorByHref("#final-approved-tab");
        HtmlSelect select = pafDashboard.getElementByName("finaltable_length");
        HtmlOption option = select.getOptionByValue("500");
        option.setSelected(true);
        client.waitForBackgroundJavaScript(2000);
        anchor.setAttribute("aria-selected", "true");
        HtmlTable finaltable = (HtmlTable) pafDashboard.getElementById("finaltable");
        List<String> pafs = new LinkedList<>();
        for (HtmlTableRow row : finaltable.getRows()) {
            DomNodeList<HtmlElement> anchors = row.getElementsByTagName("a");
            if (!(anchors.isEmpty())) {
                HtmlAnchor htmlAnchor = (HtmlAnchor) anchors.get(0);
                String href = htmlAnchor.getHrefAttribute();
                pafs.add(href);
            }
        }
        ProgressBarBuilder pbb = new ProgressBarBuilder().setStyle(ProgressBarStyle.ASCII).setTaskName("Processing PAFs");
        for (String url : ProgressBar.wrap(pafs, pbb)) {
            HtmlPage paf = client.getPage(url);
            processPAFPage(client, paf, saveLocation, employees);
        }
    }

    private void processPAFPage(WebClient client, HtmlPage pafPage, Path saveLocation, Employees employees) throws IOException {

        client.waitForBackgroundJavaScript(1250);
        if (pafPage.getUrl().toString().contains("paf-custemployee.php")) {
            HtmlSpan employeeNameContainer = (HtmlSpan) pafPage.getByXPath("//span[contains(@class,'cardDetailEmployeeFieldText')]").get(0);
            String employeeName = utils.formatName(employeeNameContainer.getTextContent());
            String employeeCode = utils.extractEmployeeCode(pafPage.getUrl().toString());
            HtmlImage employeePhotoContainer = (HtmlImage) pafPage.getByXPath("//div[contains(@class, 'cardPhoto')]//img").get(0);
            boolean added = employees.addEmployee(employeeName, employeeCode);
            Path employeeFolder;
            if (added) {
                employeeFolder = saveLocation.resolve(employeeName);
                if (!(employeeFolder.toFile().exists())) {
                    Files.createDirectories(employeeFolder);
                }
                employees.setEmployeeFileLocation(employeeCode, employeeFolder);
            } else {
                employeeFolder = employees.getEmployeeFileLocation(employeeCode);
            }
            File employeePhoto = employeeFolder.resolve("employee_photo.jpg").toFile();
            if (!employeePhoto.exists()) {
                employeePhotoContainer.saveAs(employeePhoto);
                employees.setEmployeePhoto(employeeCode, employeePhoto);
            }
            HtmlTable table = (HtmlTable) pafPage.getElementById("tblempinfo");
            List<HtmlTableRow> rows = table.getRows();
            List<List<String>> tableTop = new LinkedList<>();
            List<List<String>> tableMiddle = new LinkedList<>();
            for (HtmlTableRow row : rows) {
                List<String> tableTopRow = new LinkedList<>();
                List<String> tableMiddleRow = new LinkedList<>();
                for (int i = 0; i < row.getCells().size(); i++) {
                    HtmlTableCell cell = row.getCell(i);
                    switch (i) {
                        case 0:
                        case 1:
                            tableTopRow.add(cell.getTextContent());
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
                tableTop.add(tableTopRow);
                tableMiddle.add(tableMiddleRow);
            }
            PAF paf;
            List<HtmlAnchor> elements = pafPage.getByXPath("//div[contains(@class, 'row formRowStandard')]//div[contains(@class,'formLine')]//a");
            if (elements.isEmpty()) {
                paf = new PAF(utils.extractTransactionID(pafPage.getUrl().toString()), tableTop, tableMiddle);
            } else {
                List<File> supportingDocs = new LinkedList<>();
                Path supportDocs = employeeFolder.resolve("supporting_docs/");
                Files.createDirectories(supportDocs);
                for (HtmlAnchor downloadAnchor : elements) {
                    UnexpectedPage page = downloadAnchor.click();
                    WebResponse response = page.getWebResponse();
                    String[] headerSplit = response.getResponseHeaderValue("Content-Disposition").split(";");
                    String temp = headerSplit[1].strip();
                    String[] splitTemp = temp.split("\"");
                    String filename = splitTemp[1];
                    File downloadFile = supportDocs.resolve(filename).toFile();
                    InputStream contentAsStream = response.getContentAsStream();
                    FileOutputStream out = new FileOutputStream(downloadFile);
                    IOUtils.copy(contentAsStream, out);
                    out.close();
                    supportingDocs.add(downloadFile);
                }
                paf = new PAF(utils.extractTransactionID(pafPage.getUrl().toString()), tableTop, tableMiddle, supportingDocs);
            }
            employees.addPAF(employeeCode, paf);
        }
    }
}
